package com.android.timberworkoutlogs.ui.screen.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExerciseCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutTemplatesViewModel @Inject constructor(
    private val workoutTemplateRepository: WorkoutTemplateRepository
) : ViewModel() {

    val templates: StateFlow<List<WorkoutTemplateWithExerciseCount>> =
        workoutTemplateRepository.getAllTemplatesWithExerciseCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun deleteTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            workoutTemplateRepository.deleteTemplate(template)
        }
    }
}
