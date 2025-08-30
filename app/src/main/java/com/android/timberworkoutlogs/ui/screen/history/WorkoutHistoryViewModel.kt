package com.android.timberworkoutlogs.ui.screen.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.WorkoutHistoryDisplayItem
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WorkoutHistoryViewModel"

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val allWorkoutDisplayItems: StateFlow<List<WorkoutHistoryDisplayItem>> =
        workoutRepository.allWorkouts.combine(settingsRepository.weightUnit) { workouts, weightUnit ->
            workouts.map { workout ->
                val exercises = workoutRepository.getExercisesForWorkout(workout.id)
                val exerciseCount = exercises.size
                var totalWeightInKg = 0.0
                var totalDistance = 0.0
                exercises.forEach { exercise ->
                    exercise.sets.forEach { set ->
                        when (set) {
                            is WeightAndRepsSet -> {
                                val weightInKg = if (exercise.unit == WeightUnit.LB) {
                                    set.weight * 0.453592
                                } else {
                                    set.weight
                                }
                                totalWeightInKg += weightInKg * set.reps
                            }

                            is DistanceAndTimeSet -> {
                                totalDistance += set.distance
                            }

                            else -> {}
                        }
                    }
                }
                WorkoutHistoryDisplayItem(
                    workout = workout,
                    exerciseCount = exerciseCount,
                    totalWeightLiftedInKg = totalWeightInKg,
                    totalDistance = totalDistance,
                    systemWeightUnit = weightUnit
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteWorkout(item: WorkoutHistoryDisplayItem) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(item.workout)
        }
    }

    init {
        Log.d(TAG, "WorkoutHistoryViewModel initialized")
    }
}