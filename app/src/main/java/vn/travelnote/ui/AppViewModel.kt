package vn.travelnote.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.travelnote.data.Expense
import vn.travelnote.data.Repo
import vn.travelnote.data.TopUp
import vn.travelnote.data.Trip
import vn.travelnote.fx.RateService
import vn.travelnote.fx.Rates
import vn.travelnote.io.Backup
import vn.travelnote.io.Exporter

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repo(app)
    private val rateService = RateService(app)
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var trips by mutableStateOf<List<Trip>>(emptyList())
        private set
    var totals by mutableStateOf<Map<Long, DoubleArray>>(emptyMap())
        private set
    var currentTrip by mutableStateOf<Trip?>(null)
        private set
    var expenses by mutableStateOf<List<Expense>>(emptyList())
        private set
    var topups by mutableStateOf<List<TopUp>>(emptyList())
        private set
    var rates by mutableStateOf(Rates())
        private set
    var loadingRates by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)

    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme", "SYSTEM") ?: "SYSTEM") }
            .getOrDefault(ThemeMode.SYSTEM)
    )
        private set

    var dynamicColor by mutableStateOf(prefs.getBoolean("dynamic", false))
        private set

    init {
        rates = rateService.cached()
        reloadTrips()
        refreshRates(silent = true)
    }

    // ---------- Settings ----------

    fun setTheme(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme", mode.name).apply()
    }

    fun setDynamicColor(on: Boolean) {
        dynamicColor = on
        prefs.edit().putBoolean("dynamic", on).apply()
    }

    // ---------- Rates ----------

    fun refreshRates(silent: Boolean = false) {
        if (loadingRates) return
        loadingRates = true
        viewModelScope.launch {
            val fresh = runCatching { rateService.refresh() }.getOrNull()
            loadingRates = false
            if (fresh != null && !fresh.isEmpty) {
                rates = fresh
                if (!silent) message = "Đã cập nhật tỷ giá Vietcombank"
            } else if (!silent) {
                message = "Không tải được tỷ giá, đang dùng bản lưu gần nhất"
            }
        }
    }

    fun rateOf(code: String): Double = rates.rateOf(code)

    // ---------- Trips ----------

    fun reloadTrips() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { repo.trips() to repo.totals() }
            trips = data.first
            totals = data.second
        }
    }

    fun spentOf(tripId: Long): Double = totals[tripId]?.get(0) ?: 0.0

    fun toppedOf(tripId: Long): Double = totals[tripId]?.get(1) ?: 0.0

    fun openTrip(id: Long) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                Triple(repo.trip(id), repo.expenses(id), repo.topups(id))
            }
            currentTrip = data.first
            expenses = data.second
            topups = data.third
        }
    }

    fun closeTrip() {
        currentTrip = null
        expenses = emptyList()
        topups = emptyList()
    }

    fun saveTrip(trip: Trip, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { repo.saveTrip(trip) }
            trips = withContext(Dispatchers.IO) { repo.trips() }
            totals = withContext(Dispatchers.IO) { repo.totals() }
            if (currentTrip?.id == id) currentTrip = withContext(Dispatchers.IO) { repo.trip(id) }
            onDone(id)
        }
    }

    fun deleteTrip(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.deleteTrip(id) }
            if (currentTrip?.id == id) closeTrip()
            trips = withContext(Dispatchers.IO) { repo.trips() }
            totals = withContext(Dispatchers.IO) { repo.totals() }
            message = "Đã xoá chuyến đi"
        }
    }

    // ---------- Expenses & top-ups ----------

    fun saveExpense(e: Expense) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.saveExpense(e) }
            expenses = withContext(Dispatchers.IO) { repo.expenses(e.tripId) }
            totals = withContext(Dispatchers.IO) { repo.totals() }
        }
    }

    fun deleteExpense(e: Expense) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.deleteExpense(e.id) }
            expenses = withContext(Dispatchers.IO) { repo.expenses(e.tripId) }
            totals = withContext(Dispatchers.IO) { repo.totals() }
            message = "Đã xoá khoản chi"
        }
    }

    fun saveTopUp(t: TopUp) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.saveTopUp(t) }
            topups = withContext(Dispatchers.IO) { repo.topups(t.tripId) }
            totals = withContext(Dispatchers.IO) { repo.totals() }
        }
    }

    fun deleteTopUp(t: TopUp) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.deleteTopUp(t.id) }
            topups = withContext(Dispatchers.IO) { repo.topups(t.tripId) }
            totals = withContext(Dispatchers.IO) { repo.totals() }
            message = "Đã xoá khoản nạp"
        }
    }

    // ---------- Files ----------

    fun suggestedExcelName(): String = currentTrip?.let { Exporter.fileName(it) } ?: "TravelNote.xlsx"

    fun exportExcel(uri: Uri) {
        val trip = currentTrip ?: return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        Exporter.exportTrip(out, trip, repo.expenses(trip.id), repo.topups(trip.id))
                    } ?: throw IllegalStateException("Không mở được file")
                }.isSuccess
            }
            message = if (ok) "Đã xuất Excel" else "Xuất Excel thất bại"
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        Backup.export(out, repo)
                    } ?: throw IllegalStateException("Không mở được file")
                }.isSuccess
            }
            message = if (ok) "Đã xuất bản sao lưu" else "Sao lưu thất bại"
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        Backup.import(input, repo)
                    } ?: throw IllegalStateException("Không mở được file")
                }
            }
            result.onSuccess {
                closeTrip()
                trips = withContext(Dispatchers.IO) { repo.trips() }
                totals = withContext(Dispatchers.IO) { repo.totals() }
                message = "Đã nhập ${it.trips} chuyến đi, ${it.expenses} khoản chi"
            }.onFailure {
                message = "Nhập dữ liệu thất bại: ${it.message}"
            }
        }
    }
}
