package com.android.timberworkoutlogs.ui.screen

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
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutInProgressBanner
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import com.android.timberworkoutlogs.util.getGreetingByTime
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    val (daysAxisValueFormatter, volumeAxisValueFormatter) = rememberFormatters()

    Column(modifier = modifier) {
        Text(
            text = "Volume this week (kg)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (weeklyVolumeUiState) {
            is WeeklyVolumeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is WeeklyVolumeUiState.Success -> {
                LaunchedEffect(weeklyVolumeUiState.chartData) {
                    modelProducer.runTransaction {
                        columnSeries { series(weeklyVolumeUiState.chartData) }
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
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberColumnCartesianLayer(),
                            startAxis = VerticalAxis.rememberStart(
                                valueFormatter = volumeAxisValueFormatter,
                                title = "Volume (kg)"
                            ),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                valueFormatter = daysAxisValueFormatter,
                                title = "Day of Week"
                            ),
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier.fillMaxSize()
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
private fun rememberFormatters(): Pair<CartesianValueFormatter, CartesianValueFormatter> {
    val dayFormatter = remember {
        CartesianValueFormatter { _, axisValue, _ ->
            val dayOfWeek = when (axisValue.toString()) {
                "0" -> Calendar.MONDAY
                "1" -> Calendar.TUESDAY
                "2" -> Calendar.WEDNESDAY
                "3" -> Calendar.THURSDAY
                "4" -> Calendar.FRIDAY
                "5" -> Calendar.SATURDAY
                "6" -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
            SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
        }
    }
    val volumeFormatter = remember {
        CartesianValueFormatter { _, axisValue, _ ->
            "%.0f kg".format(axisValue.toFloat())
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
    val sampleData = listOf(1000f, 1500f, 800f, 1200f, 0f, 0f, 0f)
    TimberWorkoutLogsTheme {
        VolumeThisWeekSection(
            weeklyVolumeUiState = WeeklyVolumeUiState.Success(sampleData),
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
            weeklyVolumeUiState = WeeklyVolumeUiState.Success(sampleData),
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
