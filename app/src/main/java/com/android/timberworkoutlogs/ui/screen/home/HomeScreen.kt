package com.android.timberworkoutlogs.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter

@Composable
fun HomeScreen(
    navigateToWorkout: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val isWorkoutInProgress by viewModel.isWorkoutInProgress.collectAsState()
    val weeklyVolumeUiState by viewModel.weeklyVolumeUiState.collectAsState()
    val personalRecordsUiState by viewModel.personalRecordsUiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreenContent(
            modifier = Modifier.fillMaxSize(),
            weeklyVolumeUiState = weeklyVolumeUiState,
            personalRecordsUiState = personalRecordsUiState
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
    weeklyVolumeUiState: WeeklyVolumeUiState,
    personalRecordsUiState: PersonalRecordsUiState
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

        PersonalRecordsSection(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            personalRecordsUiState = personalRecordsUiState
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
    ProvideVicoTheme(rememberM3VicoTheme()) {
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
fun PersonalRecordsSection(
    modifier: Modifier = Modifier,
    personalRecordsUiState: PersonalRecordsUiState
) {
    Column(modifier = modifier) {
        Text(
            text = "Personal Records",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (personalRecordsUiState) {
            is PersonalRecordsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is PersonalRecordsUiState.Success -> {
                if (personalRecordsUiState.lifts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Start lifting to see your personal records!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    ExerciseCarousel(lifts = personalRecordsUiState.lifts)
                }
            }

            is PersonalRecordsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.error)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error loading records: ${personalRecordsUiState.message}",
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
private fun ExerciseCarousel(
    lifts: List<ExerciseLift>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { lifts.size })

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            ExerciseLiftCard(
                lift = lifts[page],
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            )
        }

        // Page indicators
        if (lifts.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(lifts.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                    if (index < lifts.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseLiftCard(
    lift: ExerciseLift,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise name
            Text(
                text = lift.exerciseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Max weight with unit
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "%.0f".format(lift.currentMax),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (lift.unit == WeightUnit.KG) "kg" else "lb",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1RM estimate if different from current max
            if (lift.oneRepMax != lift.currentMax) {
                Text(
                    text = "Est. 1RM: %.0f%s".format(
                        lift.oneRepMax,
                        if (lift.unit == WeightUnit.KG) "kg" else "lb"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Last PR date
            lift.lastPrDate?.let { date ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Latest: ${formatDate(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}


private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
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

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PersonalRecordsSection_LoadingPreview() {
    TimberWorkoutLogsTheme {
        PersonalRecordsSection(
            personalRecordsUiState = PersonalRecordsUiState.Loading,
            modifier = Modifier.height(200.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PersonalRecordsSection_EmptyPreview() {
    TimberWorkoutLogsTheme {
        PersonalRecordsSection(
            personalRecordsUiState = PersonalRecordsUiState.Success(emptyList()),
            modifier = Modifier.height(200.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PersonalRecordsSection_SuccessPreview() {
    val sampleLifts = listOf(
        ExerciseLift(
            exerciseName = "Barbell Bench Press",
            currentMax = 135.0,
            unit = WeightUnit.KG,
            lastPrDate = System.currentTimeMillis() - 86400000, // 1 day ago
            oneRepMax = 142.0
        ),
        ExerciseLift(
            exerciseName = "Barbell Squat",
            currentMax = 180.0,
            unit = WeightUnit.KG,
            lastPrDate = System.currentTimeMillis() - 172800000, // 2 days ago
            oneRepMax = 185.0
        ),
        ExerciseLift(
            exerciseName = "Barbell Deadlift",
            currentMax = 220.0,
            unit = WeightUnit.KG,
            lastPrDate = System.currentTimeMillis() - 259200000, // 3 days ago
            oneRepMax = 225.0
        ),
        ExerciseLift(
            exerciseName = "Dumbbell Overhead Press",
            currentMax = 85.0,
            unit = WeightUnit.KG,
            lastPrDate = System.currentTimeMillis() - 345600000, // 4 days ago
            oneRepMax = 88.0
        )
    )
    
    TimberWorkoutLogsTheme {
        PersonalRecordsSection(
            personalRecordsUiState = PersonalRecordsUiState.Success(sampleLifts),
            modifier = Modifier.height(280.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ExerciseLiftCard_Preview() {
    val sampleLift = ExerciseLift(
        exerciseName = "Barbell Bench Press",
        currentMax = 135.0,
        unit = WeightUnit.KG,
        lastPrDate = System.currentTimeMillis() - 86400000,
        oneRepMax = 142.0
    )
    
    TimberWorkoutLogsTheme {
        ExerciseLiftCard(
            lift = sampleLift,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        )
    }
}
