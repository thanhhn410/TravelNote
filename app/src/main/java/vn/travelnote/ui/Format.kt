package vn.travelnote.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val VI = Locale("vi", "VN")
private val intFmt: NumberFormat = NumberFormat.getNumberInstance(VI)
private val decFmt: NumberFormat = NumberFormat.getNumberInstance(VI).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
}

fun fmtVnd(v: Double): String = intFmt.format(Math.round(v)) + " \u20AB"

fun fmtAmount(v: Double, code: String): String =
    if (code == "VND") fmtVnd(v) else decFmt.format(v) + " " + code

fun fmtRate(v: Double): String = when {
    v >= 100 -> intFmt.format(Math.round(v))
    v <= 0 -> "-"
    else -> decFmt.format(v)
}

fun fmtDate(ts: Long): String = SimpleDateFormat("dd/MM/yyyy", VI).format(Date(ts))

fun fmtDayLabel(ts: Long): String = SimpleDateFormat("EEEE, dd/MM", VI).format(Date(ts))
    .replaceFirstChar { it.uppercase() }

fun fmtDateTimeShort(ts: Long): String = SimpleDateFormat("dd/MM HH:mm", VI).format(Date(ts))

fun startOfDay(ts: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = ts
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun daysBetween(a: Long, b: Long): Int {
    val diff = startOfDay(b) - startOfDay(a)
    return (diff / 86_400_000L).toInt() + 1
}

/**
 * Chap nhan ca kieu Viet Nam (1.234.567 hoac 12,5) lan kieu Anh (1234.56).
 * Khi chi co dau cham: coi la phan nghin neu nhom cuoi du 3 chu so, con lai la thap phan.
 */
fun parseAmount(s: String): Double {
    var t = s.replace(" ", "").replace("\u00A0", "").trim()
    if (t.isEmpty()) return 0.0
    val hasComma = t.contains(',')
    val hasDot = t.contains('.')
    t = when {
        hasComma && hasDot -> t.replace(".", "").replace(",", ".")
        hasComma -> t.replace(",", ".")
        hasDot -> {
            val parts = t.split(".")
            if (parts.size > 2 || (parts.size == 2 && parts[1].length == 3)) t.replace(".", "") else t
        }
        else -> t
    }
    return t.toDoubleOrNull() ?: 0.0
}
