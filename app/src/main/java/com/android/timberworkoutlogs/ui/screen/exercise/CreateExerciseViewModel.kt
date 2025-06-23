package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import kotlinx.coroutines.launch

class CreateExerciseViewModel(private val repository: ExerciseDefinitionRepository) : ViewModel() {

    fun saveExercise(
        name: String,
        equipment: ExerciseEquipment,
        muscleGroups: List<MuscleGroup>,
        logType: LogType
    ) {
        viewModelScope.launch {
            if (name.isNotBlank() && muscleGroups.isNotEmpty()) {
                val newExercise = ExerciseDefinition(
                    name = name,
                    equipment = equipment,
                    muscleGroups = muscleGroups,
                    logType = logType
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
