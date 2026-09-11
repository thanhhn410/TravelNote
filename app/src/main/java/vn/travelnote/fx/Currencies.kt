package vn.travelnote.fx

data class Ccy(val code: String, val name: String, val flag: String)

object Currencies {
    // 50 dong tien pho bien nhat + VND lam goc
    val ALL = listOf(
        Ccy("VND", "Việt Nam Đồng", "\uD83C\uDDFB\uD83C\uDDF3"),
        Ccy("USD", "Đô la Mỹ", "\uD83C\uDDFA\uD83C\uDDF8"),
        Ccy("EUR", "Euro", "\uD83C\uDDEA\uD83C\uDDFA"),
        Ccy("JPY", "Yên Nhật", "\uD83C\uDDEF\uD83C\uDDF5"),
        Ccy("GBP", "Bảng Anh", "\uD83C\uDDEC\uD83C\uDDE7"),
        Ccy("KRW", "Won Hàn Quốc", "\uD83C\uDDF0\uD83C\uDDF7"),
        Ccy("CNY", "Nhân dân tệ", "\uD83C\uDDE8\uD83C\uDDF3"),
        Ccy("THB", "Baht Thái", "\uD83C\uDDF9\uD83C\uDDED"),
        Ccy("SGD", "Đô la Singapore", "\uD83C\uDDF8\uD83C\uDDEC"),
        Ccy("MYR", "Ringgit Malaysia", "\uD83C\uDDF2\uD83C\uDDFE"),
        Ccy("IDR", "Rupiah Indonesia", "\uD83C\uDDEE\uD83C\uDDE9"),
        Ccy("PHP", "Peso Philippines", "\uD83C\uDDF5\uD83C\uDDED"),
        Ccy("HKD", "Đô la Hồng Kông", "\uD83C\uDDED\uD83C\uDDF0"),
        Ccy("TWD", "Đô la Đài Loan", "\uD83C\uDDF9\uD83C\uDDFC"),
        Ccy("AUD", "Đô la Úc", "\uD83C\uDDE6\uD83C\uDDFA"),
        Ccy("NZD", "Đô la New Zealand", "\uD83C\uDDF3\uD83C\uDDFF"),
        Ccy("CAD", "Đô la Canada", "\uD83C\uDDE8\uD83C\uDDE6"),
        Ccy("CHF", "Franc Thụy Sĩ", "\uD83C\uDDE8\uD83C\uDDED"),
        Ccy("INR", "Rupee Ấn Độ", "\uD83C\uDDEE\uD83C\uDDF3"),
        Ccy("LAK", "Kip Lào", "\uD83C\uDDF1\uD83C\uDDE6"),
        Ccy("KHR", "Riel Campuchia", "\uD83C\uDDF0\uD83C\uDDED"),
        Ccy("MMK", "Kyat Myanmar", "\uD83C\uDDF2\uD83C\uDDF2"),
        Ccy("BND", "Đô la Brunei", "\uD83C\uDDE7\uD83C\uDDF3"),
        Ccy("MOP", "Pataca Macau", "\uD83C\uDDF2\uD83C\uDDF4"),
        Ccy("AED", "Dirham UAE", "\uD83C\uDDE6\uD83C\uDDEA"),
        Ccy("SAR", "Riyal Ả Rập", "\uD83C\uDDF8\uD83C\uDDE6"),
        Ccy("QAR", "Riyal Qatar", "\uD83C\uDDF6\uD83C\uDDE6"),
        Ccy("KWD", "Dinar Kuwait", "\uD83C\uDDF0\uD83C\uDDFC"),
        Ccy("BHD", "Dinar Bahrain", "\uD83C\uDDE7\uD83C\uDDED"),
        Ccy("OMR", "Rial Oman", "\uD83C\uDDF4\uD83C\uDDF2"),
        Ccy("TRY", "Lira Thổ Nhĩ Kỳ", "\uD83C\uDDF9\uD83C\uDDF7"),
        Ccy("RUB", "Rúp Nga", "\uD83C\uDDF7\uD83C\uDDFA"),
        Ccy("SEK", "Krona Thụy Điển", "\uD83C\uDDF8\uD83C\uDDEA"),
        Ccy("NOK", "Krone Na Uy", "\uD83C\uDDF3\uD83C\uDDF4"),
        Ccy("DKK", "Krone Đan Mạch", "\uD83C\uDDE9\uD83C\uDDF0"),
        Ccy("PLN", "Zloty Ba Lan", "\uD83C\uDDF5\uD83C\uDDF1"),
        Ccy("CZK", "Koruna Séc", "\uD83C\uDDE8\uD83C\uDDFF"),
        Ccy("HUF", "Forint Hungary", "\uD83C\uDDED\uD83C\uDDFA"),
        Ccy("RON", "Leu Romania", "\uD83C\uDDF7\uD83C\uDDF4"),
        Ccy("ILS", "Shekel Israel", "\uD83C\uDDEE\uD83C\uDDF1"),
        Ccy("EGP", "Bảng Ai Cập", "\uD83C\uDDEA\uD83C\uDDEC"),
        Ccy("ZAR", "Rand Nam Phi", "\uD83C\uDDFF\uD83C\uDDE6"),
        Ccy("BRL", "Real Brazil", "\uD83C\uDDE7\uD83C\uDDF7"),
        Ccy("MXN", "Peso Mexico", "\uD83C\uDDF2\uD83C\uDDFD"),
        Ccy("ARS", "Peso Argentina", "\uD83C\uDDE6\uD83C\uDDF7"),
        Ccy("CLP", "Peso Chile", "\uD83C\uDDE8\uD83C\uDDF1"),
        Ccy("PKR", "Rupee Pakistan", "\uD83C\uDDF5\uD83C\uDDF0"),
        Ccy("BDT", "Taka Bangladesh", "\uD83C\uDDE7\uD83C\uDDE9"),
        Ccy("LKR", "Rupee Sri Lanka", "\uD83C\uDDF1\uD83C\uDDF0"),
        Ccy("NPR", "Rupee Nepal", "\uD83C\uDDF3\uD83C\uDDF5")
    )

    val CODES = ALL.map { it.code }

    fun of(code: String): Ccy = ALL.firstOrNull { it.code == code } ?: ALL.first()
}
