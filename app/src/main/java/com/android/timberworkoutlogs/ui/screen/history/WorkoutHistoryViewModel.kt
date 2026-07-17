package com.android.timberworkoutlogs.ui.screen.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.models.WorkoutHistoryDisplayItem
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
                WorkoutHistoryDisplayItem.from(workout, exercises, weightUnit)
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