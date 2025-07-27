package com.android.timberworkoutlogs.ui.screen.settings

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.WeightUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val selectedUnit by viewModel.weightUnit.collectAsState()
    val useDynamicTheme by viewModel.dynamicTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        SettingGroup(title = "General") {
            WeightUnitSetting(
                selectedUnit = selectedUnit,
                onUnitSelected = {
                    viewModel.updateWeightUnit(it)
                    Log.d("SettingsScreen", "Weight unit updated to $it")
                }
            )
            DynamicThemeSetting(
                useDynamicTheme = useDynamicTheme,
                onDynamicThemeChanged = { viewModel.updateDynamicTheme(it) }
            )
        }
    }
}

@Composable
private fun SettingGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightUnitSetting(
    selectedUnit: WeightUnit,
    onUnitSelected: (WeightUnit) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Default Weight Unit", style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedUnit == WeightUnit.KG,
                onClick = { onUnitSelected(WeightUnit.KG) },
                label = { Text("KG") }
            )
            FilterChip(
                selected = selectedUnit == WeightUnit.LB,
                onClick = { onUnitSelected(WeightUnit.LB) },
                label = { Text("LB") }
            )
        }
    }
}

@Composable
private fun DynamicThemeSetting(
    useDynamicTheme: Boolean,
    onDynamicThemeChanged: (Boolean) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Use Dynamic Theme", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = useDynamicTheme,
                onCheckedChange = onDynamicThemeChanged
            )
        }
    }
}
