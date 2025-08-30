package com.android.timberworkoutlogs.ui.screen.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.SecondaryEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.database.ExerciseDefinitionWithCount
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.ui.screen.stats.OneRepMaxPoint
import com.android.timberworkoutlogs.ui.screen.stats.TimeRange
import com.android.timberworkoutlogs.ui.screen.stats.utils.OneRMFormula
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OneRepMaxTab(
    selectedExercise: ExerciseDefinition?,
    availableExercises: List<ExerciseDefinitionWithCount>,
    selectedTimeRange: TimeRange,
    selectedFormula: OneRMFormula,
    oneRepMaxData: List<OneRepMaxPoint>,
    weightUnit: WeightUnit,
    isLoading: Boolean,
    error: String?,
    onExerciseSelected: (ExerciseDefinition) -> Unit,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onFormulaSelected: (OneRMFormula) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Controls Row 1: Exercise and Time Range
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseSelectionDropdown(
                selectedExercise = selectedExercise,
                availableExercises = availableExercises,
                onExerciseSelected = onExerciseSelected,
                modifier = Modifier.weight(2f)
            )

            TimeRangePicker(
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = onTimeRangeSelected,
                modifier = Modifier.weight(1f)
            )
        }

        // Controls Row 2: Formula Selection
        FormulaSelectionDropdown(
            selectedFormula = selectedFormula,
            onFormulaSelected = onFormulaSelected,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("OneRMFormulaDropdown_$selectedFormula")
        )

        // Chart Section
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.error)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            oneRepMaxData.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedExercise != null) {
                            "No one-rep max data found for ${selectedExercise.computedExerciseName} in the selected time range."
                        } else {
                            "Select an exercise to view one-rep max progression."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            else -> {
                OneRepMaxChart(
                    data = oneRepMaxData,
                    formula = selectedFormula,
                    weightUnit = weightUnit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormulaSelectionDropdown(
    selectedFormula: OneRMFormula,
    onFormulaSelected: (OneRMFormula) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedFormula.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("1RM Formula") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(SecondaryEditable)
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                OneRMFormula.entries.forEach { formula ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = formula.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (formula == selectedFormula) {
                                    FontWeight.Medium
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        },
                        onClick = {
                            onFormulaSelected(formula)
                            expanded = false
                        },
                        modifier = Modifier.testTag("formula_option_${formula.name}")
                    )
                }
            }
        }
    }
}

@Composable
private fun OneRepMaxChart(
    data: List<OneRepMaxPoint>,
    formula: OneRMFormula,
    weightUnit: WeightUnit,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val unitSymbol = if (weightUnit == WeightUnit.KG) "kg" else "lb"
    
    Column(modifier = modifier) {
        Text(
            text = "Estimated 1RM Progression (${formula.displayName}) ($unitSymbol)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Warning for unreliable data
        val unreliableCount = data.count { !it.isReliable }
        if (unreliableCount > 0) {
            Text(
                text = "⚠️ $unreliableCount data points based on >10 reps (less reliable estimates)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LaunchedEffect(data) {
            if (data.isNotEmpty()) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = data.mapIndexed { index, _ -> index.toFloat() },
                            y = data.map { it.estimatedOneRM.toFloat() }
                        )
                    }
                }
            }
        }

        val (dateFormatter, weightFormatter) = rememberOneRMFormatters(weightUnit, data)

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = weightFormatter,
                    title = "Estimated 1RM ($unitSymbol)"
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = dateFormatter,
                    title = "Date"
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

@Composable
private fun rememberOneRMFormatters(
    weightUnit: WeightUnit,
    data: List<OneRepMaxPoint>
): Pair<CartesianValueFormatter, CartesianValueFormatter> {
    val dateFormatter = remember(data) {
        CartesianValueFormatter { _, axisValue, _ ->
            val index = axisValue.toInt()
            if (index in data.indices) {
                val date = Date(data[index].date)
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            } else "---"
        }
    }
    
    val weightFormatter = remember(weightUnit) {
        CartesianValueFormatter { _, axisValue, _ ->
            "%.1f".format(axisValue)
        }
    }
    
    return dateFormatter to weightFormatter
}
