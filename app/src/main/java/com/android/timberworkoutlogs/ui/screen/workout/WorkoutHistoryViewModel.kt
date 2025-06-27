package com.android.timberworkoutlogs.ui.screen.workout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.models.WorkoutHistoryDisplayItem
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val TAG = "WorkoutHistoryViewModel"

class WorkoutHistoryViewModel(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    val allWorkoutDisplayItems: StateFlow<List<WorkoutHistoryDisplayItem>> =
        workoutRepository.allWorkouts
            .map { workouts -> // For each list of workouts
                workouts.map { workout -> // For each workout in the list
                    // Fetch exercises and calculate aggregates for this workout
                    val exercises = workoutRepository.getExercisesForWorkout(workout.id)
                    val exerciseCount = exercises.size
                    var totalWeight = 0.0
                    exercises.forEach { exercise ->
                        exercise.sets.forEach { set ->
                            if (set is WeightAndRepsSet) {
                                val weightInKg = if (exercise.unit == WeightUnit.LB) {
                                    set.weight * 0.453592
                                } else {
                                    set.weight
                                }
                                totalWeight += weightInKg * set.reps // Consider total volume: weight * reps
                            }
                        }
                    }
                    WorkoutHistoryDisplayItem(
                        workout = workout,
                        exerciseCount = exerciseCount,
                        totalWeightLifted = totalWeight
                    )
                }
            }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        Log.d(TAG, "WorkoutHistoryViewModel initialized")
    }
}

class WorkoutHistoryViewModelFactory(
    private val workoutRepository: WorkoutRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutHistoryViewModel(workoutRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
