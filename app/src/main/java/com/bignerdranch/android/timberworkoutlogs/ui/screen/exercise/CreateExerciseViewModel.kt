package com.bignerdranch.android.timberworkoutlogs.ui.screen.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseDefinition
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseEquipment
import kotlinx.coroutines.launch

class CreateExerciseViewModel(private val repository: ExerciseDefinitionRepository) : ViewModel() {

    /**
     * Creates an ExerciseDefinition and saves it to the database via the repository.
     */
    fun saveExercise(name: String, equipment: ExerciseEquipment) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val newExercise = ExerciseDefinition(name = name, equipment = equipment)
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
