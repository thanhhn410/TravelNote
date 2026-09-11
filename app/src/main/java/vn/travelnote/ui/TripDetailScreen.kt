package vn.travelnote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.travelnote.data.Categories
import vn.travelnote.data.Expense
import vn.travelnote.data.TopUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTopUp: () -> Unit,
    onEditTrip: () -> Unit,
    onExportExcel: () -> Unit
) {
    val trip = vm.currentTrip ?: return
    var tab by remember { mutableStateOf(0) }
    var menu by remember { mutableStateOf(false) }
    var confirmDeleteTrip by remember { mutableStateOf(false) }
    var pendingExpense by remember { mutableStateOf<Expense?>(null) }
    var pendingTopUp by remember { mutableStateOf<TopUp?>(null) }

    val spent = vm.expenses.sumOf { it.amountVnd }
    val topped = vm.topups.sumOf { it.amountVnd }
    val days = daysBetween(trip.startDate, trip.endDate).coerceAtLeast(1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip.name, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = onExportExcel) {
                        Icon(Icons.Filled.Download, contentDescription = "Xuất Excel")
                    }
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Thêm")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Sửa chuyến đi") },
                            leadingIcon = { Icon(Icons.Filled.Edit, null) },
                            onClick = { menu = false; onEditTrip() }
                        )
                        DropdownMenuItem(
                            text = { Text("Xoá chuyến đi") },
                            leadingIcon = { Icon(Icons.Filled.Delete, null) },
                            onClick = { menu = false; confirmDeleteTrip = true }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (tab == 0) onAddExpense() else onAddTopUp() },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(if (tab == 0) "Ghi chi tiêu" else "Nạp tiền") }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    Text(
                        "Đã chi",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        fmtVnd(spent),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (topped > 0) {
                        Box(Modifier.padding(top = 12.dp)) {
                            ProgressBarThin(
                                fraction = (spent / topped).toFloat(),
                                color = if (spent > topped) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column(Modifier.padding(top = 12.dp)) {
                        StatRow("Tổng nạp", fmtVnd(topped))
                        StatRow(
                            "Còn lại",
                            fmtVnd(topped - spent),
                            emphasis = true,
                            color = if (topped - spent < 0) MaterialTheme.colorScheme.error else null
                        )
                        StatRow("Trung bình / ngày", fmtVnd(spent / days))
                        StatRow("Số ngày", days.toString() + " ngày")
                    }
                }
            }

            item {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Chi tiêu") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Nạp tiền") })
                }
            }

            if (tab == 0) {
                if (vm.expenses.isEmpty()) {
                    item {
                        EmptyState("\uD83D\uDCDD", "Chưa có khoản chi nào", "Nhấn Ghi chi tiêu để thêm khoản đầu tiên")
                    }
                } else {
                    item { CategoryBreakdown(vm.expenses, spent) }

                    val grouped = vm.expenses.groupBy { startOfDay(it.date) }
                        .entries.sortedByDescending { it.key }
                    grouped.forEach { entry ->
                        val day = entry.key
                        val items = entry.value
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    fmtDayLabel(day),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    fmtVnd(items.sumOf { it.amountVnd }),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        item {
                            SectionCard {
                                items.forEachIndexed { i, e ->
                                    ExpenseRow(e) { pendingExpense = e }
                                    if (i < items.size - 1) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                if (vm.topups.isEmpty()) {
                    item {
                        EmptyState("\uD83D\uDCB3", "Chưa nạp khoản nào", "Ghi lại số tiền bạn mang theo cho chuyến này")
                    }
                } else {
                    item {
                        SectionCard {
                            vm.topups.forEachIndexed { i, t ->
                                TopUpRow(t) { pendingTopUp = t }
                                if (i < vm.topups.size - 1) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDeleteTrip) {
        AlertDialog(
            onDismissRequest = { confirmDeleteTrip = false },
            title = { Text("Xoá chuyến đi?") },
            text = { Text("Toàn bộ khoản chi và khoản nạp của chuyến này sẽ bị xoá.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteTrip = false
                    vm.deleteTrip(trip.id)
                    onBack()
                }) { Text("Xoá") }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteTrip = false }) { Text("Huỷ") } }
        )
    }

    pendingExpense?.let { e ->
        AlertDialog(
            onDismissRequest = { pendingExpense = null },
            title = { Text("Xoá khoản chi?") },
            text = { Text(fmtAmount(e.amount, e.currency) + " · " + Categories.of(e.category).label) },
            confirmButton = {
                TextButton(onClick = { vm.deleteExpense(e); pendingExpense = null }) { Text("Xoá") }
            },
            dismissButton = { TextButton(onClick = { pendingExpense = null }) { Text("Huỷ") } }
        )
    }

    pendingTopUp?.let { t ->
        AlertDialog(
            onDismissRequest = { pendingTopUp = null },
            title = { Text("Xoá khoản nạp?") },
            text = { Text(fmtAmount(t.amount, t.currency)) },
            confirmButton = {
                TextButton(onClick = { vm.deleteTopUp(t); pendingTopUp = null }) { Text("Xoá") }
            },
            dismissButton = { TextButton(onClick = { pendingTopUp = null }) { Text("Huỷ") } }
        )
    }
}

@Composable
private fun CategoryBreakdown(expenses: List<Expense>, total: Double) {
    val rows = Categories.ALL.mapNotNull { cat ->
        val sum = expenses.filter { it.category == cat.key }.sumOf { it.amountVnd }
        if (sum > 0) Triple(cat.emoji + "  " + cat.label, sum, if (total > 0) sum / total else 0.0) else null
    }.sortedByDescending { it.second }

    if (rows.isEmpty()) return

    SectionCard {
        Text("Theo danh mục", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Column(Modifier.padding(top = 8.dp)) {
            rows.forEach { (label, sum, ratio) ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        (Math.round(ratio * 100)).toString() + "%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(fmtVnd(sum), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                ProgressBarThin(ratio.toFloat(), MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ExpenseRow(e: Expense, onDelete: () -> Unit) {
    val cat = Categories.of(e.category)
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) { Text(cat.emoji, fontSize = 18.sp) }

        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                if (e.note.isBlank()) cat.label else e.note,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Text(
                if (e.currency == "VND") cat.label
                else cat.label + " · " + fmtAmount(e.amount, e.currency),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(fmtVnd(e.amountVnd), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Xoá",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopUpRow(t: TopUp, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                if (t.note.isBlank()) "Nạp tiền" else t.note,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Text(
                fmtDate(t.date) + (if (t.currency == "VND") "" else " · " + fmtAmount(t.amount, t.currency)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "+ " + fmtVnd(t.amountVnd),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Xoá",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
