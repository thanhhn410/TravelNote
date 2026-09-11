package vn.travelnote.fx

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Ty gia quy doi ra VND cho 1 don vi ngoai te.
 * source = "VCB" khi lay truc tiep tu Vietcombank,
 *          "VCB + cross" khi phai bac cau qua USD cho cac dong VCB khong niem yet.
 */
data class Rates(
    val vndPer: Map<String, Double> = emptyMap(),
    val updatedAt: Long = 0L,
    val source: String = "",
    val vcbCodes: Set<String> = emptySet()
) {
    fun rateOf(code: String): Double = if (code == "VND") 1.0 else (vndPer[code] ?: 0.0)
    fun has(code: String): Boolean = code == "VND" || vndPer.containsKey(code)
    val isEmpty: Boolean get() = vndPer.isEmpty()
}

class RateService(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("rates", Context.MODE_PRIVATE)

    private val vcbUrl = "https://portal.vietcombank.com.vn/UserControls/TVPortal.TyGia/pXML.aspx?b=68"
    private val crossUrl = "https://open.er-api.com/v6/latest/USD"

    fun cached(): Rates {
        val raw = prefs.getString("payload", null) ?: return Rates()
        return try {
            val o = JSONObject(raw)
            val map = HashMap<String, Double>()
            val r = o.getJSONObject("vndPer")
            r.keys().forEach { k -> map[k] = r.getDouble(k) }
            val vcb = HashSet<String>()
            val arr = o.optJSONArray("vcbCodes")
            if (arr != null) for (i in 0 until arr.length()) vcb.add(arr.getString(i))
            Rates(map, o.optLong("updatedAt"), o.optString("source"), vcb)
        } catch (e: Exception) {
            Rates()
        }
    }

    private fun persist(r: Rates) {
        val o = JSONObject()
        val m = JSONObject()
        r.vndPer.forEach { (k, v) -> m.put(k, v) }
        o.put("vndPer", m)
        o.put("updatedAt", r.updatedAt)
        o.put("source", r.source)
        o.put("vcbCodes", org.json.JSONArray(r.vcbCodes.toList()))
        prefs.edit().putString("payload", o.toString()).apply()
    }

    /** Tai ty gia moi. Neu that bai hoan toan thi tra ve ban cache gan nhat. */
    suspend fun refresh(): Rates = withContext(Dispatchers.IO) {
        val vnd = HashMap<String, Double>()
        val vcbCodes = HashSet<String>()

        val vcb = runCatching { fetchVcb() }.getOrDefault(emptyMap())
        vcb.forEach { (code, rate) ->
            if (code in Currencies.CODES) {
                vnd[code] = rate
                vcbCodes.add(code)
            }
        }

        val perUsd = runCatching { fetchCross() }.getOrDefault(emptyMap())
        var usdVnd = vnd["USD"] ?: 0.0
        // Neu VCB khong truy cap duoc, lay USD/VND tu nguon du phong de app van chay
        if (usdVnd <= 0.0) usdVnd = perUsd["VND"] ?: 0.0

        var crossUsed = false
        if (usdVnd > 0.0 && perUsd.isNotEmpty()) {
            Currencies.CODES.forEach { code ->
                if (code != "VND" && !vnd.containsKey(code)) {
                    val p = perUsd[code] ?: 0.0
                    if (p > 0.0) {
                        vnd[code] = usdVnd / p
                        crossUsed = true
                    }
                }
            }
        }

        val source = when {
            vcb.isNotEmpty() && crossUsed -> "Vietcombank + quy đổi chéo qua USD"
            vcb.isNotEmpty() -> "Vietcombank"
            crossUsed -> "Nguồn dự phòng, quy đổi chéo qua USD"
            else -> "Bản lưu gần nhất"
        }

        if (vnd.isEmpty()) return@withContext cached()

        val result = Rates(vnd, System.currentTimeMillis(), source, vcbCodes)
        persist(result)
        result
    }

    /** XML cua VCB: <Exrate CurrencyCode="USD" Buy="..." Transfer="..." Sell="..."/> */
    private fun fetchVcb(): Map<String, Double> {
        val body = httpGet(vcbUrl)
        val out = HashMap<String, Double>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(body))
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name.equals("Exrate", true)) {
                val code = parser.getAttributeValue(null, "CurrencyCode")?.trim()?.uppercase()
                val transfer = num(parser.getAttributeValue(null, "Transfer"))
                val sell = num(parser.getAttributeValue(null, "Sell"))
                val buy = num(parser.getAttributeValue(null, "Buy"))
                // Uu tien ty gia chuyen khoan, thieu thi lay ban ra roi den mua vao
                val rate = when {
                    transfer > 0 -> transfer
                    sell > 0 -> sell
                    else -> buy
                }
                if (!code.isNullOrBlank() && rate > 0) out[code] = rate
            }
            event = parser.next()
        }
        return out
    }

    /** So don vi ngoai te tren 1 USD. */
    private fun fetchCross(): Map<String, Double> {
        val body = httpGet(crossUrl)
        val o = JSONObject(body)
        val rates = o.optJSONObject("rates") ?: return emptyMap()
        val out = HashMap<String, Double>()
        rates.keys().forEach { k -> out[k.uppercase()] = rates.optDouble(k, 0.0) }
        return out
    }

    private fun num(s: String?): Double {
        if (s.isNullOrBlank()) return 0.0
        val cleaned = s.replace(",", "").replace(" ", "").trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun httpGet(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 12000
        conn.readTimeout = 12000
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) TravelNote/1.0")
        conn.setRequestProperty("Accept", "*/*")
        try {
            if (conn.responseCode !in 200..299) throw IllegalStateException("HTTP ${conn.responseCode}")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
