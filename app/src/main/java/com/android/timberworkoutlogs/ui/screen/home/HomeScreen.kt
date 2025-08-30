package com.android.timberworkoutlogs.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutInProgressBanner
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import com.android.timberworkoutlogs.util.getGreetingByTime
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter

@Composable
fun HomeScreen(
    navigateToWorkout: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val isWorkoutInProgress by viewModel.isWorkoutInProgress.collectAsState()
    val weeklyVolumeUiState by viewModel.weeklyVolumeUiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreenContent(
            modifier = Modifier.fillMaxSize(),
            weeklyVolumeUiState = weeklyVolumeUiState
        )

        if (isWorkoutInProgress) {
            WorkoutInProgressBanner(
                modifier = Modifier.align(Alignment.TopCenter),
                navigateToWorkout = navigateToWorkout
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    weeklyVolumeUiState: WeeklyVolumeUiState
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = getGreetingByTime(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp),
        )

        PlaceholderContent(
            label = "Best Lifts (e.g., Bench Press)",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        VolumeThisWeekSection(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            weeklyVolumeUiState = weeklyVolumeUiState
        )
    }
}

@Composable
fun VolumeThisWeekSection(
    modifier: Modifier = Modifier,
    weeklyVolumeUiState: WeeklyVolumeUiState
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    Column(modifier = modifier) {
        // Dynamic title based on weight unit
        val titleText = when (weeklyVolumeUiState) {
            is WeeklyVolumeUiState.Success -> {
                val unitSymbol = if (weeklyVolumeUiState.weightUnit == WeightUnit.KG) "kg" else "lb"
                "Volume this week ($unitSymbol)"
            }
            else -> "Volume this week"
        }
        
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (weeklyVolumeUiState) {
            is WeeklyVolumeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is WeeklyVolumeUiState.Success -> {
                val (daysAxisValueFormatter, volumeAxisValueFormatter) = rememberFormatters(weeklyVolumeUiState.weightUnit)
                
                LaunchedEffect(weeklyVolumeUiState.chartData) {
                    modelProducer.runTransaction {
                        // Add both column and line series for a combo effect
                        columnSeries { series(weeklyVolumeUiState.chartData) }
                        lineSeries { series(weeklyVolumeUiState.chartData) }
                    }
                }
                if (weeklyVolumeUiState.chartData.isEmpty() || weeklyVolumeUiState.chartData.all { it == 0f }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No workout volume recorded for this week.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    JazzyVolumeChart(
                        modelProducer = modelProducer,
                        daysAxisValueFormatter = daysAxisValueFormatter,
                        volumeAxisValueFormatter = volumeAxisValueFormatter,
                        weightUnit = weeklyVolumeUiState.weightUnit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }

            is WeeklyVolumeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.error)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error loading volume: ${weeklyVolumeUiState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JazzyVolumeChart(
    modelProducer: CartesianChartModelProducer,
    daysAxisValueFormatter: CartesianValueFormatter,
    volumeAxisValueFormatter: CartesianValueFormatter,
    weightUnit: WeightUnit,
    modifier: Modifier = Modifier
) {
    val unitSymbol = if (weightUnit == WeightUnit.KG) "kg" else "lb"
    
    CartesianChartHost(
        chart = rememberCartesianChart(
            // Column layer for the bars
            rememberColumnCartesianLayer(),
            // Line layer for the trend line overlay - creates a combo chart effect
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = volumeAxisValueFormatter,
                title = "Volume ($unitSymbol)"
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = daysAxisValueFormatter,
                title = "Days of Week"
            )
        ),
        modelProducer = modelProducer,
        modifier = modifier
    )
}

@Composable
private fun rememberFormatters(weightUnit: WeightUnit): Pair<CartesianValueFormatter, CartesianValueFormatter> {
    val dayFormatter = remember {
        CartesianValueFormatter { _, axisValue, _ ->
            val dayNames = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val index = axisValue.toInt()
            if (index in 0..6) dayNames[index] else "---"
        }
    }
    val volumeFormatter = remember(weightUnit) {
        CartesianValueFormatter { _, axisValue, _ ->
            when {
                axisValue < 1000 -> "%.0f".format(axisValue)
                axisValue < 10000 -> "%.1fk".format(axisValue / 1000f)
                else -> "%.0fk".format(axisValue / 1000f)
            }
        }
    }
    return dayFormatter to volumeFormatter
}

@Composable
fun PlaceholderContent(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun HomeScreenPreview() {
    TimberWorkoutLogsTheme {
        HomeScreen(navigateToWorkout = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun VolumeThisWeekSection_LoadingPreview() {
    TimberWorkoutLogsTheme {
        VolumeThisWeekSection(
            weeklyVolumeUiState = WeeklyVolumeUiState.Loading,
            modifier = Modifier.height(200.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun VolumeThisWeekSection_SuccessPreview() {
    val sampleData = listOf(1000f, 1500f, 800f, 1200f, 2500f, 1800f, 900f)
    TimberWorkoutLogsTheme {
        VolumeThisWeekSection(
            weeklyVolumeUiState = WeeklyVolumeUiState.Success(sampleData, WeightUnit.KG),
            modifier = Modifier.height(200.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun VolumeThisWeekSection_EmptyPreview() {
    val sampleData = List(7) { 0f }
    TimberWorkoutLogsTheme {
        VolumeThisWeekSection(
            weeklyVolumeUiState = WeeklyVolumeUiState.Success(sampleData, WeightUnit.LB),
            modifier = Modifier.height(200.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun VolumeThisWeekSection_ErrorPreview() {
    TimberWorkoutLogsTheme {
        VolumeThisWeekSection(
            weeklyVolumeUiState = WeeklyVolumeUiState.Error("Sample error message"),
            modifier = Modifier.height(200.dp)
        )
    }
}
