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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.database.ExerciseDefinitionWithCount
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.ui.screen.stats.ExerciseProgressionPoint
import com.android.timberworkoutlogs.ui.screen.stats.TimeRange
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseProgressionTab(
    selectedExercise: ExerciseDefinition?,
    availableExercises: List<ExerciseDefinitionWithCount>,
    selectedTimeRange: TimeRange,
    progressionData: List<ExerciseProgressionPoint>,
    weightUnit: WeightUnit,
    isLoading: Boolean,
    error: String?,
    onExerciseSelected: (ExerciseDefinition) -> Unit,
    onTimeRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Exercise and Time Range Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseSelectionDropdown(
                selectedExercise = selectedExercise,
                availableExercises = availableExercises,
                onExerciseSelected = onExerciseSelected,
                modifier = Modifier.weight(3f)
            )

            TimeRangePicker(
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = onTimeRangeSelected,
                modifier = Modifier.weight(2f)
            )
        }

        // Charts Section
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

            progressionData.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedExercise != null) {
                            "No progression data found for ${selectedExercise.computedExerciseName} in the selected time range."
                        } else {
                            "Select an exercise to view progression data."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            else -> {
                // Show progression charts
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Max Weight Progression Chart
                    ExerciseProgressionChart(
                        title = "Max Weight Progression",
                        data = progressionData,
                        valueExtractor = { it.maxWeight },
                        weightUnit = weightUnit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    // Volume Progression Chart
                    ExerciseProgressionChart(
                        title = "Volume Progression",
                        data = progressionData,
                        valueExtractor = { it.totalVolume },
                        weightUnit = weightUnit,
                        isVolume = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseProgressionChart(
    modifier: Modifier = Modifier,
    title: String,
    data: List<ExerciseProgressionPoint>,
    valueExtractor: (ExerciseProgressionPoint) -> Double,
    weightUnit: WeightUnit,
    isVolume: Boolean = false,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val unitSymbol = if (weightUnit == WeightUnit.KG) "kg" else "lb"
    
    Column(modifier = modifier) {
        Text(
            text = "$title ($unitSymbol)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LaunchedEffect(data) {
            if (data.isNotEmpty()) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = data.mapIndexed { index, _ -> index.toFloat() },
                            y = data.map { valueExtractor(it).toFloat() }
                        )
                    }
                }
            }
        }
        val (dateFormatter, valueFormatter) = rememberProgressionFormatters(weightUnit, isVolume, data)
        ProvideVicoTheme(rememberM3VicoTheme()) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(
                        valueFormatter = valueFormatter,
                        title = if (isVolume) "Volume ($unitSymbol)" else "Weight ($unitSymbol)"
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
}

@Composable
private fun rememberProgressionFormatters(
    weightUnit: WeightUnit, 
    isVolume: Boolean, 
    data: List<ExerciseProgressionPoint>
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
    
    val valueFormatter = remember(weightUnit, isVolume) {
        CartesianValueFormatter { _, axisValue, _ ->
            when {
                isVolume && axisValue >= 1000 -> "%.1fk".format(axisValue / 1000f)
                isVolume -> "%.0f".format(axisValue)
                axisValue >= 1000 -> "%.1fk".format(axisValue / 1000f)
                else -> "%.1f".format(axisValue)
            }
        }
    }
    
    return dateFormatter to valueFormatter
}
