package com.android.timberworkoutlogs.ui.screen.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.models.WorkoutHistoryDisplayItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutExerciseWithDefinition(
    val exercise: WorkoutExercise,
    val definition: ExerciseDefinition
)

sealed interface WorkoutHistoryDetailUiState {
    object Loading : WorkoutHistoryDetailUiState
    data class Success(
        val displayItem: WorkoutHistoryDisplayItem,
        val exercises: List<WorkoutExerciseWithDefinition>
    ) : WorkoutHistoryDetailUiState
    data class Error(val message: String) : WorkoutHistoryDetailUiState
}

@HiltViewModel
class WorkoutHistoryDetailViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: Long = savedStateHandle.get<Long>("workoutId") ?: -1L

    private val _uiState = MutableStateFlow<WorkoutHistoryDetailUiState>(
        WorkoutHistoryDetailUiState.Loading
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (workoutId != -1L) {
            loadWorkout(workoutId)
        } else {
            _uiState.value = WorkoutHistoryDetailUiState.Error("No workout selected")
        }
    }

    private fun loadWorkout(id: Long) {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkout(id)
            if (workout == null) {
                _uiState.value = WorkoutHistoryDetailUiState.Error("Workout not found")
                return@launch
            }

            val exercises = workoutRepository.getExercisesForWorkout(id)
            val exercisesWithDefinitions = exercises.map { exercise ->
                WorkoutExerciseWithDefinition(
                    exercise = exercise,
                    definition = exerciseDefinitionRepository.getExerciseDefinition(exercise.definitionId)
                )
            }

            _uiState.value = WorkoutHistoryDetailUiState.Success(
                displayItem = WorkoutHistoryDisplayItem.from(
                    workout = workout,
                    exercises = exercises,
                    systemWeightUnit = settingsRepository.weightUnit.first()
                ),
                exercises = exercisesWithDefinitions
            )
        }
    }
}
