package vn.travelnote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.travelnote.data.Categories
import vn.travelnote.data.Expense
import vn.travelnote.data.TopUp
import vn.travelnote.data.Trip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseSheet(vm: AppViewModel, tripId: Long, tripCurrency: String, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var category by remember { mutableStateOf(Categories.ALL.first().key) }
    var amountText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(tripCurrency) }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var showPicker by remember { mutableStateOf(false) }

    val amount = parseAmount(amountText)
    val rate = vm.rateOf(currency)
    val amountVnd = amount * rate
    val rateMissing = currency != "VND" && rate <= 0.0

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("Ghi chi tiêu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

            Text(
                "Danh mục",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Categories.ALL.forEach { cat ->
                    FilterChip(
                        selected = category == cat.key,
                        onClick = { category = cat.key },
                        label = { Text(cat.emoji + " " + cat.label) }
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AmountField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Số tiền",
                    modifier = Modifier.weight(1f)
                )
                CurrencyButton(code = currency, onClick = { showPicker = true })
            }

            ConversionHint(amount, currency, rate, amountVnd, tripCurrency, vm, rateMissing)

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                DateField("Ngày", date, { date = it }, Modifier.weight(1f))
            }

            Button(
                onClick = {
                    vm.saveExpense(
                        Expense(
                            tripId = tripId,
                            category = category,
                            amount = amount,
                            currency = currency,
                            rate = if (currency == "VND") 1.0 else rate,
                            amountVnd = if (currency == "VND") amount else amountVnd,
                            note = note.trim(),
                            date = date
                        )
                    )
                    onDismiss()
                },
                enabled = amount > 0 && !rateMissing,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) { Text("Lưu khoản chi") }
        }
    }

    if (showPicker) {
        CurrencyPickerDialog(
            current = currency,
            rates = vm.rates,
            onPick = { currency = it },
            onDismiss = { showPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpSheet(vm: AppViewModel, tripId: Long, tripCurrency: String, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("VND") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var showPicker by remember { mutableStateOf(false) }

    val amount = parseAmount(amountText)
    val rate = vm.rateOf(currency)
    val amountVnd = amount * rate
    val rateMissing = currency != "VND" && rate <= 0.0

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("Nạp tiền cho chuyến", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Ghi lại số tiền bạn mang theo hoặc rút thêm giữa chuyến.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AmountField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Số tiền nạp",
                    modifier = Modifier.weight(1f)
                )
                CurrencyButton(code = currency, onClick = { showPicker = true })
            }

            ConversionHint(amount, currency, rate, amountVnd, tripCurrency, vm, rateMissing)

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nguồn tiền / ghi chú") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                DateField("Ngày", date, { date = it }, Modifier.weight(1f))
            }

            Button(
                onClick = {
                    vm.saveTopUp(
                        TopUp(
                            tripId = tripId,
                            amount = amount,
                            currency = currency,
                            rate = if (currency == "VND") 1.0 else rate,
                            amountVnd = if (currency == "VND") amount else amountVnd,
                            note = note.trim(),
                            date = date
                        )
                    )
                    onDismiss()
                },
                enabled = amount > 0 && !rateMissing,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) { Text("Lưu khoản nạp") }
        }
    }

    if (showPicker) {
        CurrencyPickerDialog(
            current = currency,
            rates = vm.rates,
            onPick = { currency = it },
            onDismiss = { showPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSheet(vm: AppViewModel, existing: Trip?, onSaved: (Long, Boolean) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var destination by remember { mutableStateOf(existing?.destination ?: "") }
    var currency by remember { mutableStateOf(existing?.currency ?: "USD") }
    var start by remember { mutableStateOf(existing?.startDate ?: System.currentTimeMillis()) }
    var end by remember { mutableStateOf(existing?.endDate ?: System.currentTimeMillis()) }
    var showPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                if (existing == null) "Chuyến đi mới" else "Sửa chuyến đi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên chuyến đi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Điểm đến") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Tiền tệ chính của chuyến",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Dùng làm mặc định khi ghi chi tiêu",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CurrencyButton(code = currency, onClick = { showPicker = true })
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateField("Bắt đầu", start, { start = it }, Modifier.weight(1f))
                DateField("Kết thúc", end, { end = it }, Modifier.weight(1f))
            }

            Button(
                onClick = {
                    val trip = Trip(
                        id = existing?.id ?: 0L,
                        name = name.trim(),
                        destination = destination.trim(),
                        currency = currency,
                        startDate = start,
                        endDate = if (end < start) start else end
                    )
                    val isNew = existing == null
                    vm.saveTrip(trip) { id -> onSaved(id, isNew) }
                    onDismiss()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) { Text(if (existing == null) "Tạo chuyến đi" else "Lưu thay đổi") }
        }
    }

    if (showPicker) {
        CurrencyPickerDialog(
            current = currency,
            rates = vm.rates,
            onPick = { currency = it },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun ConversionHint(
    amount: Double,
    currency: String,
    rate: Double,
    amountVnd: Double,
    tripCurrency: String,
    vm: AppViewModel,
    rateMissing: Boolean
) {
    Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        when {
            rateMissing -> Text(
                "Chưa có tỷ giá cho " + currency + ". Vào Cài đặt để làm mới tỷ giá.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )

            currency != "VND" -> {
                Text(
                    "\u2248 " + fmtVnd(amountVnd),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "1 " + currency + " = " + fmtRate(rate) + " \u20AB · " + rateLabel(vm, currency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                val tripRate = vm.rateOf(tripCurrency)
                if (tripCurrency != "VND" && tripRate > 0 && amount > 0) {
                    Text(
                        "\u2248 " + fmtAmount(amount / tripRate, tripCurrency),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "1 " + tripCurrency + " = " + fmtRate(tripRate) + " \u20AB · " + rateLabel(vm, tripCurrency),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun rateLabel(vm: AppViewModel, code: String): String =
    if (vm.rates.vcbCodes.contains(code)) "Vietcombank" else "quy đổi chéo qua USD"
