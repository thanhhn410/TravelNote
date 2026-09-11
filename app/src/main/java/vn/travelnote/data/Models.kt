package vn.travelnote.data

data class Trip(
    val id: Long = 0,
    val name: String,
    val destination: String = "",
    val currency: String = "USD",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val closed: Boolean = false
)

data class Expense(
    val id: Long = 0,
    val tripId: Long,
    val category: String,
    val amount: Double,
    val currency: String,
    val rate: Double,
    val amountVnd: Double,
    val note: String = "",
    val date: Long = System.currentTimeMillis()
)

data class TopUp(
    val id: Long = 0,
    val tripId: Long,
    val amount: Double,
    val currency: String,
    val rate: Double,
    val amountVnd: Double,
    val note: String = "",
    val date: Long = System.currentTimeMillis()
)

data class Category(val key: String, val label: String, val emoji: String)

object Categories {
    val ALL = listOf(
        Category("transport", "Di chuyển", "\uD83D\uDE8C"),
        Category("food", "Ăn uống", "\uD83C\uDF5C"),
        Category("stay", "Lưu trú", "\uD83C\uDFE8"),
        Category("ticket", "Vé & vui chơi", "\uD83C\uDFAB"),
        Category("shopping", "Mua sắm", "\uD83D\uDECD"),
        Category("gift", "Quà cáp", "\uD83C\uDF81"),
        Category("health", "Sức khỏe & bảo hiểm", "\uD83D\uDC8A"),
        Category("other", "Khác", "\u2728")
    )

    fun of(key: String): Category = ALL.firstOrNull { it.key == key } ?: ALL.last()
}
