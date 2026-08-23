package com.android.timberworkoutlogs.ui.screen.settings

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.timberworkoutlogs.BuildConfig
import com.android.timberworkoutlogs.database.data.DatabaseSeeder
import com.android.timberworkoutlogs.models.WeightUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val selectedUnit by viewModel.weightUnit.collectAsState()
    val useDynamicTheme by viewModel.dynamicTheme.collectAsState()
    val useWeightRepPrediction by viewModel.weightRepPrediction.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            WeightRepPredictionSetting(
                useWeightRepPrediction = useWeightRepPrediction,
                onWeightRepPredictionChanged = { viewModel.updateWeightRepPrediction(it) }
            )
        }

        // Compiled into debug builds only; the constant is false for release.
        if (BuildConfig.DEVELOPER_TOOLS) {
            DeveloperSettings()
        }
    }
}

/**
 * Demo data generation, on demand. This replaces the old behaviour where every fresh
 * install silently received [DatabaseSeeder.DEMO_HISTORY_DAYS] days of invented history —
 * including for real users, who then had no way to remove it.
 */
@Composable
private fun DeveloperSettings(
    viewModel: DeveloperToolsViewModel = hiltViewModel()
) {
    val isBusy by viewModel.isBusy.collectAsState()

    SettingGroup(title = "Developer") {
        Text(
            "Debug builds only. Demo history is generated data, for exercising the " +
                    "stats and history screens.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = viewModel::regenerateDemoHistory,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate ${DatabaseSeeder.DEMO_HISTORY_DAYS} days of demo history")
        }
        OutlinedButton(
            onClick = viewModel::clearWorkoutHistory,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear workout history")
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

@Composable
private fun WeightRepPredictionSetting(
    useWeightRepPrediction: Boolean,
    onWeightRepPredictionChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Weight / Rep Prediction", style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = useWeightRepPrediction,
            onCheckedChange = onWeightRepPredictionChanged
        )
    }
}
