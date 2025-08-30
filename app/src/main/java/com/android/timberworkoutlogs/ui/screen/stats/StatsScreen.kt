package com.android.timberworkoutlogs.ui.screen.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.timberworkoutlogs.ui.screen.stats.components.ExerciseProgressionTab
import com.android.timberworkoutlogs.ui.screen.stats.components.OneRepMaxTab
import com.android.timberworkoutlogs.ui.screen.stats.components.VolumeStatsTab

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Row
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatsTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                StatsTab.EXERCISE_PROGRESSION -> "Progression"
                                StatsTab.ONE_REP_MAX -> "1RM"
                                StatsTab.VOLUME_STATS -> "Volume"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (uiState.selectedTab == tab) {
                                FontWeight.Medium
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                )
            }
        }

        // Tab Content
        when (uiState.selectedTab) {
            StatsTab.EXERCISE_PROGRESSION -> {
                ExerciseProgressionTab(
                    selectedExercise = uiState.selectedExercise,
                    availableExercises = uiState.availableExercises,
                    selectedTimeRange = uiState.selectedTimeRange,
                    progressionData = uiState.progressionData,
                    weightUnit = uiState.weightUnit,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onExerciseSelected = viewModel::selectExercise,
                    onTimeRangeSelected = viewModel::selectTimeRange,
                    modifier = Modifier.weight(1f)
                )
            }

            StatsTab.ONE_REP_MAX -> {
                OneRepMaxTab(
                    selectedExercise = uiState.selectedExercise,
                    availableExercises = uiState.availableExercises,
                    selectedTimeRange = uiState.selectedTimeRange,
                    selectedFormula = uiState.selectedOneRMFormula,
                    oneRepMaxData = uiState.oneRepMaxData,
                    weightUnit = uiState.weightUnit,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onExerciseSelected = viewModel::selectExercise,
                    onTimeRangeSelected = viewModel::selectTimeRange,
                    onFormulaSelected = viewModel::selectOneRMFormula,
                    modifier = Modifier.weight(1f)
                )
            }

            StatsTab.VOLUME_STATS -> {
                VolumeStatsTab(
                    selectedTimeRange = uiState.selectedTimeRange,
                    weightUnit = uiState.weightUnit,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onTimeRangeSelected = viewModel::selectTimeRange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
