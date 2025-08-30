package com.android.timberworkoutlogs.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.services.WorkoutStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

private const val LBS_TO_KG_FACTOR = 0.45359237
private const val KG_TO_LBS_FACTOR = 2.20462262

sealed interface WeeklyVolumeUiState {
    object Loading : WeeklyVolumeUiState
    data class Success(val chartData: List<Float>, val weightUnit: WeightUnit) : WeeklyVolumeUiState
    data class Error(val message: String) : WeeklyVolumeUiState
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    workoutStateHolder: WorkoutStateHolder,
    private val workoutDao: WorkoutDao,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val isWorkoutInProgress: StateFlow<Boolean> = workoutStateHolder.isTimerRunning

    val weeklyVolumeUiState: StateFlow<WeeklyVolumeUiState> = 
        combine(
            getWeeklyWorkoutsFlow(),
            settingsRepository.weightUnit
        ) { workoutsWithExercises, weightUnit ->
            try {
                val dailyVolumes = DoubleArray(7)

                for (item in workoutsWithExercises) {
                    val workout = item.workout
                    val workoutCal = Calendar.getInstance().apply {
                        timeInMillis = workout.startTime
                        firstDayOfWeek = Calendar.MONDAY
                    }

                    val dayOfWeek = workoutCal.get(Calendar.DAY_OF_WEEK)
                    val dayIndex = when (dayOfWeek) {
                        Calendar.MONDAY -> 0
                        Calendar.TUESDAY -> 1
                        Calendar.WEDNESDAY -> 2
                        Calendar.THURSDAY -> 3
                        Calendar.FRIDAY -> 4
                        Calendar.SATURDAY -> 5
                        Calendar.SUNDAY -> 6
                        else -> -1
                    }

                    if (dayIndex in 0..6) {
                        var workoutTotalVolume = 0.0
                        item.exercises.forEach { exercise ->
                            exercise.sets.forEach { set ->
                                if (set is WeightAndRepsSet) {
                                    // First convert to kg for calculation
                                    val weightInKg = if (exercise.unit == WeightUnit.LB) {
                                        set.weight * LBS_TO_KG_FACTOR
                                    } else {
                                        set.weight
                                    }
                                    if (set.reps > 0 && weightInKg > 0) {
                                        workoutTotalVolume += set.reps * weightInKg
                                    }
                                }
                            }
                        }
                        dailyVolumes[dayIndex] += workoutTotalVolume
                    }
                }

                // Convert final volumes to user's preferred unit
                val chartYValues = dailyVolumes.map { volumeInKg ->
                    val finalVolume = if (weightUnit == WeightUnit.LB) {
                        volumeInKg * KG_TO_LBS_FACTOR
                    } else {
                        volumeInKg
                    }
                    finalVolume.toFloat()
                }

                WeeklyVolumeUiState.Success(chartYValues, weightUnit)
            } catch (e: Exception) {
                WeeklyVolumeUiState.Error("Failed to load weekly volume: ${e.localizedMessage ?: "Unknown error"}")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyVolumeUiState.Loading
        )

    private fun getWeeklyWorkoutsFlow() = workoutDao.getWorkoutsWithExercisesFrom(getWeekStartMillis())

    private fun getWeekStartMillis(): Long {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            setToStartOfDay()
        }.timeInMillis
    }

    private fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
