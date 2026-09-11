package vn.travelnote.io

import vn.travelnote.data.Categories
import vn.travelnote.data.Expense
import vn.travelnote.data.TopUp
import vn.travelnote.data.Trip
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Exporter {

    private val df = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))

    fun exportTrip(out: OutputStream, trip: Trip, expenses: List<Expense>, topups: List<TopUp>) {
        val totalSpent = expenses.sumOf { it.amountVnd }
        val totalTop = topups.sumOf { it.amountVnd }

        val overview = Sheet("Tong quan")
        overview.row(Cell.Head("CHUYẾN ĐI"), Cell.Head(trip.name))
        overview.row(Cell.Text("Điểm đến"), Cell.Text(trip.destination))
        overview.row(Cell.Text("Thời gian"), Cell.Text(df.format(Date(trip.startDate)) + " - " + df.format(Date(trip.endDate))))
        overview.row(Cell.Text("Tiền tệ chính"), Cell.Text(trip.currency))
        overview.blank()
        overview.row(Cell.Head("TỔNG HỢP"), Cell.Head("VND"))
        overview.row(Cell.Text("Tổng nạp"), Cell.Num(totalTop, true))
        overview.row(Cell.Text("Tổng chi"), Cell.Num(totalSpent, true))
        overview.row(Cell.Text("Còn lại"), Cell.Num(totalTop - totalSpent, true))
        overview.row(Cell.Text("Số khoản chi"), Cell.Num(expenses.size.toDouble()))
        overview.blank()
        overview.row(Cell.Head("DANH MỤC"), Cell.Head("Số khoản"), Cell.Head("Tổng (VND)"), Cell.Head("Tỷ trọng %"))
        Categories.ALL.forEach { cat ->
            val items = expenses.filter { it.category == cat.key }
            if (items.isNotEmpty()) {
                val sum = items.sumOf { it.amountVnd }
                val pct = if (totalSpent > 0) sum / totalSpent * 100 else 0.0
                overview.row(
                    Cell.Text(cat.label),
                    Cell.Num(items.size.toDouble()),
                    Cell.Num(sum, true),
                    Cell.Num(Math.round(pct * 10) / 10.0)
                )
            }
        }

        val exp = Sheet("Chi tieu")
        exp.row(
            Cell.Head("Ngày"), Cell.Head("Danh mục"), Cell.Head("Ghi chú"),
            Cell.Head("Số tiền"), Cell.Head("Tiền tệ"), Cell.Head("Tỷ giá (VND)"), Cell.Head("Quy đổi VND")
        )
        expenses.sortedBy { it.date }.forEach { e ->
            exp.row(
                Cell.Text(df.format(Date(e.date))),
                Cell.Text(Categories.of(e.category).label),
                Cell.Text(e.note),
                Cell.Num(e.amount),
                Cell.Text(e.currency),
                Cell.Num(e.rate),
                Cell.Num(e.amountVnd, true)
            )
        }
        exp.row(Cell.Head("TỔNG"), Cell.Empty, Cell.Empty, Cell.Empty, Cell.Empty, Cell.Empty, Cell.Num(totalSpent, true))

        val top = Sheet("Nap tien")
        top.row(
            Cell.Head("Ngày"), Cell.Head("Ghi chú"), Cell.Head("Số tiền"),
            Cell.Head("Tiền tệ"), Cell.Head("Tỷ giá (VND)"), Cell.Head("Quy đổi VND")
        )
        topups.sortedBy { it.date }.forEach { t ->
            top.row(
                Cell.Text(df.format(Date(t.date))),
                Cell.Text(t.note),
                Cell.Num(t.amount),
                Cell.Text(t.currency),
                Cell.Num(t.rate),
                Cell.Num(t.amountVnd, true)
            )
        }
        top.row(Cell.Head("TỔNG"), Cell.Empty, Cell.Empty, Cell.Empty, Cell.Empty, Cell.Num(totalTop, true))

        Xlsx.write(out, listOf(overview, exp, top))
    }

    fun fileName(trip: Trip): String {
        val safe = trip.name.replace(Regex("[^\\p{L}\\p{N} _-]"), "").trim().replace(" ", "_")
        val stamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        return "TravelNote_" + (if (safe.isBlank()) "chuyen_di" else safe) + "_" + stamp + ".xlsx"
    }
}
