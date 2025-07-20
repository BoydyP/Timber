package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val repository: ExerciseDefinitionRepository
) : ViewModel() {

    val allExercises: StateFlow<List<ExerciseDefinition>> = repository.allExerciseDefinitions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteExercise(exercise: ExerciseDefinition) = viewModelScope.launch {
        repository.delete(exercise)
    }
}
