package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateExerciseUiState(
    val name: String = "",
    val equipment: ExerciseEquipment = ExerciseEquipment.BARBELL,
    val muscleGroups: Set<MuscleGroup> = emptySet(),
    val logType: LogType = LogType.WEIGHT_AND_REPS,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val exerciseId: UUID? = null
)

@HiltViewModel
class CreateExerciseViewModel @Inject constructor(
    private val repository: ExerciseDefinitionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateExerciseUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val exerciseId: String? = savedStateHandle["exerciseId"]
        if (exerciseId != null) {
            loadExercise(UUID.fromString(exerciseId))
        }
    }

    private fun loadExercise(id: UUID) {
        viewModelScope.launch {
            val exercise = repository.getExerciseDefinition(id)
            _uiState.update {
                it.copy(
                    name = exercise.name,
                    equipment = exercise.equipment,
                    muscleGroups = exercise.muscleGroups.toSet(),
                    logType = exercise.logType,
                    isEditing = true,
                    exerciseId = exercise.id
                )
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onEquipmentChanged(equipment: ExerciseEquipment) {
        _uiState.update {
            val newLogType =
                if (equipment == ExerciseEquipment.BODYWEIGHT) LogType.REPS_ONLY else it.logType
            it.copy(equipment = equipment, logType = newLogType)
        }
    }

    fun onMuscleGroupToggled(muscleGroup: MuscleGroup) {
        _uiState.update { currentState ->
            val newMuscleGroups = currentState.muscleGroups.toMutableSet()
            if (newMuscleGroups.contains(muscleGroup)) {
                newMuscleGroups.remove(muscleGroup)
            } else {
                newMuscleGroups.add(muscleGroup)
            }
            currentState.copy(muscleGroups = newMuscleGroups)
        }
    }

    fun onLogTypeChanged(logType: LogType) {
        _uiState.update { it.copy(logType = logType) }
    }

    fun saveExercise(onExerciseSaved: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.name.isNotBlank() && currentState.muscleGroups.isNotEmpty()) {
                _uiState.update { it.copy(isSaving = true) }
                val exercise = ExerciseDefinition(
                    id = currentState.exerciseId ?: UUID.randomUUID(),
                    name = currentState.name,
                    equipment = currentState.equipment,
                    muscleGroups = currentState.muscleGroups.toList(),
                    logType = currentState.logType
                )
                if (currentState.isEditing) {
                    repository.update(exercise)
                } else {
                    repository.insert(exercise)
                }
                onExerciseSaved()
            }
        }
    }
}
