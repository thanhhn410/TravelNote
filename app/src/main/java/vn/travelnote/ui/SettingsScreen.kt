package vn.travelnote.ui

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.travelnote.fx.Currencies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    Text("Giao diện", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Column(Modifier.padding(top = 8.dp)) {
                        ThemeOption("Theo hệ thống", ThemeMode.SYSTEM, vm)
                        ThemeOption("Luôn sáng", ThemeMode.LIGHT, vm)
                        ThemeOption("Luôn tối", ThemeMode.DARK, vm)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Màu động theo hình nền", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Material You, chỉ có trên Android 12 trở lên",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(checked = vm.dynamicColor, onCheckedChange = { vm.updateDynamicColor(it) })
                        }
                    }
                }
            }

            item {
                SectionCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Tỷ giá", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (vm.rates.updatedAt > 0)
                                    "Cập nhật " + fmtDateTimeShort(vm.rates.updatedAt) + " · " + vm.rates.source
                                else "Chưa có dữ liệu tỷ giá",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (vm.loadingRates) {
                            CircularProgressIndicator(Modifier.padding(start = 12.dp))
                        } else {
                            FilledTonalButton(onClick = { vm.refreshRates() }) { Text("Làm mới") }
                        }
                    }

                    Text(
                        "Nguồn chính là bảng tỷ giá chuyển khoản của Vietcombank. Các đồng tiền Vietcombank không niêm yết được quy đổi chéo qua USD, mang tính tham khảo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            item {
                Text(
                    "Bảng tỷ giá (1 đơn vị = ? VND)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                SectionCard {
                    Currencies.ALL.filter { it.code != "VND" }.forEach { c ->
                        val rate = vm.rates.rateOf(c.code)
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(c.flag + "  " + c.code, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (rate > 0) fmtRate(rate) else "chưa có",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (vm.rates.vcbCodes.contains(c.code))
                                    MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SectionCard {
                    Text("Dữ liệu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Xuất toàn bộ dữ liệu ra file JSON để chuyển sang máy khác, hoặc nhập lại từ bản sao lưu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(onClick = onExportBackup, modifier = Modifier.weight(1f)) {
                            Text("Sao lưu")
                        }
                        OutlinedButton(onClick = onImportBackup, modifier = Modifier.weight(1f)) {
                            Text("Nhập lại")
                        }
                    }
                    Text(
                        "Lưu ý: nhập bản sao lưu sẽ thay thế toàn bộ dữ liệu hiện có trên máy này.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            item {
                SectionCard {
                    Text("Về ứng dụng", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    StatRow("Phiên bản", "1.0")
                    StatRow("Tiền tệ gốc", "VND")
                    StatRow("Số đồng tiền hỗ trợ", (Currencies.ALL.size - 1).toString())
                    Text(
                        "Tỷ giá chỉ mang tính tham khảo, không phải tỷ giá giao dịch thực tế của thẻ hay quầy đổi tiền.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(label: String, mode: ThemeMode, vm: AppViewModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { vm.setTheme(mode) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = vm.themeMode == mode, onClick = { vm.setTheme(mode) })
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 6.dp))
    }
}
