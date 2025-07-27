package com.android.timberworkoutlogs.ui.screen.templates

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.WorkoutTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateWorkoutTemplateUiState(
    var name: String = "",
    val templateExercises: List<TemplateExercise> = emptyList(),
    val exerciseDefinitions: Map<UUID, ExerciseDefinition> = emptyMap(),
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val templateId: Long? = null
)

@HiltViewModel
class CreateWorkoutTemplateViewModel @Inject constructor(
    private val workoutTemplateRepository: WorkoutTemplateRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWorkoutTemplateUiState())
    val uiState = _uiState.asStateFlow()

    private val templateId: Long? = savedStateHandle.get<String>("templateId")?.toLongOrNull()

    init {
        if (templateId != null) {
            _uiState.update { it.copy(templateId = templateId, isEditing = true) }
            loadTemplate(templateId)
        }
    }

    private fun loadTemplate(id: Long) {
        viewModelScope.launch {
            val templateWithExercises = workoutTemplateRepository.getTemplateWithExercises(id)
            _uiState.update {
                it.copy(
                    name = templateWithExercises.template.name,
                    templateExercises = templateWithExercises.exercises
                )
            }
            loadExerciseDefinitions(templateWithExercises.exercises.map { it.definitionId })
        }
    }

    private suspend fun loadExerciseDefinitions(ids: List<UUID>) {
        val definitions =
            ids.associateWith { exerciseDefinitionRepository.getExerciseDefinition(it) }
        _uiState.update { it.copy(exerciseDefinitions = definitions) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onExerciseSelected(exerciseIndex: Int, definitionId: UUID) {
        viewModelScope.launch {
            val definition = exerciseDefinitionRepository.getExerciseDefinition(definitionId)
            _uiState.update {
                val newDefinitions = it.exerciseDefinitions + (definitionId to definition)
                val updatedExercise =
                    it.templateExercises[exerciseIndex].copy(definitionId = definitionId)
                val newExercises = it.templateExercises.toMutableList().also { list ->
                    list[exerciseIndex] = updatedExercise
                }
                it.copy(
                    exerciseDefinitions = newDefinitions,
                    templateExercises = newExercises
                )
            }
        }
    }

    fun addExercise() {
        val newExercise = TemplateExercise(
            templateId = templateId ?: 0L, // Temp ID, will be updated on save
            definitionId = PLACEHOLDER_DEFINITION_ID,
            sets = emptyList()
        )
        _uiState.update { it.copy(templateExercises = it.templateExercises + newExercise) }
    }

    fun saveTemplate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(isSaving = true) }

            val exercisesWithDefinitions = currentState.templateExercises.filter {
                it.definitionId != PLACEHOLDER_DEFINITION_ID
            }

            if (currentState.templateId == null) {
                // Create new template
                val newTemplate = WorkoutTemplate(name = currentState.name)
                val newId = workoutTemplateRepository.insertTemplate(newTemplate)
                val exercisesToSave = exercisesWithDefinitions.map { it.copy(templateId = newId) }
                workoutTemplateRepository.upsertTemplateExercises(exercisesToSave)
            } else {
                // Update existing template
                val updatedTemplate =
                    WorkoutTemplate(id = currentState.templateId, name = currentState.name)
                workoutTemplateRepository.updateTemplate(updatedTemplate)
                val exercisesToSave =
                    exercisesWithDefinitions.map { it.copy(templateId = currentState.templateId) }
                workoutTemplateRepository.deleteExercisesForTemplate(currentState.templateId)
                workoutTemplateRepository.upsertTemplateExercises(exercisesToSave)
            }
            onSuccess()
        }
    }

    companion object {
        private val PLACEHOLDER_DEFINITION_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}
