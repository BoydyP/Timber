package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateExerciseUiState(
    val name: String = "",
    val equipment: ExerciseEquipment = ExerciseEquipment.BARBELL,
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val logType: LogType = LogType.WEIGHT_AND_REPS,
    val isSaving: Boolean = false
)

class CreateExerciseViewModel(private val repository: ExerciseDefinitionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateExerciseUiState())
    val uiState: StateFlow<CreateExerciseUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onEquipmentChanged(equipment: ExerciseEquipment) {
        _uiState.update {
            it.copy(
                equipment = equipment,
                logType = if (equipment == ExerciseEquipment.BODYWEIGHT) LogType.REPS_ONLY else it.logType
            )
        }
    }

    fun onMuscleGroupsChanged(muscleGroups: List<MuscleGroup>) {
        _uiState.update { it.copy(muscleGroups = muscleGroups) }
    }

    fun onLogTypeChanged(logType: LogType) {
        _uiState.update { it.copy(logType = logType) }
    }

    fun saveExercise() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.name.isNotBlank() && currentState.muscleGroups.isNotEmpty()) {
                _uiState.update { it.copy(isSaving = true) }
                val newExercise = ExerciseDefinition(
                    name = currentState.name,
                    equipment = currentState.equipment,
                    muscleGroups = currentState.muscleGroups,
                    logType = currentState.logType
                )
                repository.insert(newExercise)
            }
        }
    }
}

class CreateExerciseViewModelFactory(private val repository: ExerciseDefinitionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateExerciseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
