package vn.travelnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import vn.travelnote.data.Trip
import vn.travelnote.ui.AppViewModel
import vn.travelnote.ui.ExpenseSheet
import vn.travelnote.ui.SettingsScreen
import vn.travelnote.ui.TopUpSheet
import vn.travelnote.ui.TravelNoteTheme
import vn.travelnote.ui.TripDetailScreen
import vn.travelnote.ui.TripSheet
import vn.travelnote.ui.TripsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

private enum class Route { TRIPS, DETAIL, SETTINGS }

@Composable
fun AppRoot() {
    val vm: AppViewModel = viewModel()

    TravelNoteTheme(mode = vm.themeMode, dynamic = vm.dynamicColor) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AppContent(vm)
        }
    }
}

@Composable
private fun AppContent(vm: AppViewModel) {
    var route by remember { mutableStateOf(Route.TRIPS) }
    var tripSheet by remember { mutableStateOf(false) }
    var editingTrip by remember { mutableStateOf<Trip?>(null) }
    var expenseSheet by remember { mutableStateOf(false) }
    var topUpSheet by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.message) {
        val msg = vm.message
        if (msg != null) {
            snackbar.showSnackbar(msg)
            vm.message = null
        }
    }

    val excelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri -> uri?.let { vm.exportExcel(it) } }

    val backupExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { vm.exportBackup(it) } }

    val backupImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { vm.importBackup(it) } }

    BackHandler(enabled = route != Route.TRIPS) {
        route = Route.TRIPS
        vm.closeTrip()
        vm.reloadTrips()
    }

    Box(Modifier.fillMaxSize()) {
        when (route) {
            Route.TRIPS -> TripsScreen(
                vm = vm,
                onOpenTrip = { id -> vm.openTrip(id); route = Route.DETAIL },
                onNewTrip = { editingTrip = null; tripSheet = true },
                onSettings = { route = Route.SETTINGS }
            )

            Route.DETAIL -> TripDetailScreen(
                vm = vm,
                onBack = { route = Route.TRIPS; vm.closeTrip(); vm.reloadTrips() },
                onAddExpense = { expenseSheet = true },
                onAddTopUp = { topUpSheet = true },
                onEditTrip = { editingTrip = vm.currentTrip; tripSheet = true },
                onExportExcel = { excelLauncher.launch(vm.suggestedExcelName()) }
            )

            Route.SETTINGS -> SettingsScreen(
                vm = vm,
                onBack = { route = Route.TRIPS },
                onExportBackup = { backupExportLauncher.launch("TravelNote_backup.json") },
                onImportBackup = {
                    backupImportLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
            )
        }

        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (tripSheet) {
        TripSheet(
            vm = vm,
            existing = editingTrip,
            onSaved = { id, isNew ->
                if (isNew) {
                    vm.openTrip(id)
                    route = Route.DETAIL
                }
            },
            onDismiss = { tripSheet = false; editingTrip = null }
        )
    }

    val trip = vm.currentTrip
    if (expenseSheet && trip != null) {
        ExpenseSheet(
            vm = vm,
            tripId = trip.id,
            tripCurrency = trip.currency,
            onDismiss = { expenseSheet = false }
        )
    }
    if (topUpSheet && trip != null) {
        TopUpSheet(
            vm = vm,
            tripId = trip.id,
            tripCurrency = trip.currency,
            onDismiss = { topUpSheet = false }
        )
    }
}
