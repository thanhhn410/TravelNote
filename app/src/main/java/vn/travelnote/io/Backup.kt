package vn.travelnote.io

import org.json.JSONArray
import org.json.JSONObject
import vn.travelnote.data.Expense
import vn.travelnote.data.Repo
import vn.travelnote.data.TopUp
import vn.travelnote.data.Trip
import java.io.InputStream
import java.io.OutputStream

/** Sao luu / phuc hoi toan bo du lieu duoi dang JSON de chuyen sang may khac. */
object Backup {

    const val FORMAT_VERSION = 1

    fun export(out: OutputStream, repo: Repo) {
        val root = JSONObject()
        root.put("app", "TravelNote")
        root.put("formatVersion", FORMAT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val trips = JSONArray()
        val expenses = JSONArray()
        val topups = JSONArray()

        repo.trips().forEach { t ->
            trips.put(
                JSONObject()
                    .put("id", t.id).put("name", t.name).put("destination", t.destination)
                    .put("currency", t.currency).put("startDate", t.startDate)
                    .put("endDate", t.endDate).put("closed", t.closed)
            )
            repo.expenses(t.id).forEach { e ->
                expenses.put(
                    JSONObject()
                        .put("id", e.id).put("tripId", e.tripId).put("category", e.category)
                        .put("amount", e.amount).put("currency", e.currency).put("rate", e.rate)
                        .put("amountVnd", e.amountVnd).put("note", e.note).put("date", e.date)
                )
            }
            repo.topups(t.id).forEach { p ->
                topups.put(
                    JSONObject()
                        .put("id", p.id).put("tripId", p.tripId).put("amount", p.amount)
                        .put("currency", p.currency).put("rate", p.rate)
                        .put("amountVnd", p.amountVnd).put("note", p.note).put("date", p.date)
                )
            }
        }

        root.put("trips", trips)
        root.put("expenses", expenses)
        root.put("topups", topups)
        out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        out.flush()
    }

    data class ImportResult(val trips: Int, val expenses: Int, val topups: Int)

    /** Ghi de toan bo du lieu hien tai bang noi dung file backup. */
    fun import(input: InputStream, repo: Repo): ImportResult {
        val text = input.bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        require(root.optString("app") == "TravelNote") { "File không phải bản sao lưu TravelNote" }

        val trips = root.optJSONArray("trips") ?: JSONArray()
        val expenses = root.optJSONArray("expenses") ?: JSONArray()
        val topups = root.optJSONArray("topups") ?: JSONArray()

        repo.wipeAll()

        for (i in 0 until trips.length()) {
            val o = trips.getJSONObject(i)
            repo.insertRawTrip(
                Trip(
                    id = o.getLong("id"),
                    name = o.optString("name"),
                    destination = o.optString("destination"),
                    currency = o.optString("currency", "USD"),
                    startDate = o.optLong("startDate"),
                    endDate = o.optLong("endDate"),
                    closed = o.optBoolean("closed")
                )
            )
        }
        for (i in 0 until expenses.length()) {
            val o = expenses.getJSONObject(i)
            repo.insertRawExpense(
                Expense(
                    id = o.getLong("id"),
                    tripId = o.getLong("tripId"),
                    category = o.optString("category", "other"),
                    amount = o.optDouble("amount", 0.0),
                    currency = o.optString("currency", "VND"),
                    rate = o.optDouble("rate", 1.0),
                    amountVnd = o.optDouble("amountVnd", 0.0),
                    note = o.optString("note"),
                    date = o.optLong("date")
                )
            )
        }
        for (i in 0 until topups.length()) {
            val o = topups.getJSONObject(i)
            repo.insertRawTopUp(
                TopUp(
                    id = o.getLong("id"),
                    tripId = o.getLong("tripId"),
                    amount = o.optDouble("amount", 0.0),
                    currency = o.optString("currency", "VND"),
                    rate = o.optDouble("rate", 1.0),
                    amountVnd = o.optDouble("amountVnd", 0.0),
                    note = o.optString("note"),
                    date = o.optLong("date")
                )
            )
        }
        return ImportResult(trips.length(), expenses.length(), topups.length())
    }
}
