package com.android.timberworkoutlogs.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.services.WorkoutStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar
import javax.inject.Inject

private const val LBS_TO_KG_FACTOR = 0.45359237

sealed interface WeeklyVolumeUiState {
    object Loading : WeeklyVolumeUiState
    data class Success(val chartData: List<Float>) : WeeklyVolumeUiState // Changed to List<Float>
    data class Error(val message: String) : WeeklyVolumeUiState
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    workoutStateHolder: WorkoutStateHolder,
    private val workoutDao: WorkoutDao
) : ViewModel() {
    val isWorkoutInProgress: StateFlow<Boolean> = workoutStateHolder.isTimerRunning

    private val _weeklyVolumeUiState =
        MutableStateFlow<WeeklyVolumeUiState>(WeeklyVolumeUiState.Loading)
    val weeklyVolumeUiState: StateFlow<WeeklyVolumeUiState> = _weeklyVolumeUiState.asStateFlow()

    init {
        loadWeeklyVolume()
    }

    private fun loadWeeklyVolume() {
        val currentWeekCalendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            setToStartOfDay()
        }
        val weekStartMillis = currentWeekCalendar.timeInMillis

        workoutDao.getWorkoutsWithExercisesFrom(weekStartMillis)
            .onEach { workoutsWithExercises ->
                val dailyVolumes = DoubleArray(7)

                for (item in workoutsWithExercises) {
                    val workoutCal = Calendar.getInstance().apply {
                        timeInMillis = item.workout.startTime
                        firstDayOfWeek = Calendar.MONDAY
                    }

                    if (workoutCal.timeInMillis >= weekStartMillis) {
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
                }
                val chartYValues = dailyVolumes.map { it.toFloat() }
                _weeklyVolumeUiState.value = WeeklyVolumeUiState.Success(chartYValues)
            }
            .catch { e ->
                _weeklyVolumeUiState.value =
                    WeeklyVolumeUiState.Error("Failed to load weekly volume: ${e.localizedMessage ?: "Unknown error"}")
            }
            .launchIn(viewModelScope)
    }

    private fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
