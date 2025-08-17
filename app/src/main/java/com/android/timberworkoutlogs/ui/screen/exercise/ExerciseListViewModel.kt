package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val repository: ExerciseDefinitionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val allExercises: StateFlow<List<ExerciseDefinition>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allExerciseDefinitions
            } else {
                repository.searchExerciseDefinitions(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteExercise(exercise: ExerciseDefinition) = viewModelScope.launch {
        repository.delete(exercise)
    }
}
