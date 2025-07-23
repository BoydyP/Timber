package com.android.timberworkoutlogs.ui.screen.templates

import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTemplateViewModel @Inject constructor(
    private val workoutTemplateRepository: WorkoutTemplateRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val templateId: Long? = savedStateHandle.get<String>("templateId")?.toLongOrNull()

    var templateName = mutableStateOf("")
    private val _templateExercises = MutableStateFlow<List<TemplateExercise>>(emptyList())
    val templateExercises = _templateExercises.asStateFlow()

    private val _exerciseDefinitions = MutableStateFlow<Map<UUID, ExerciseDefinition>>(emptyMap())
    val exerciseDefinitions = _exerciseDefinitions.asStateFlow()

    init {
        if (templateId != null) {
            loadTemplate(templateId)
        }
    }

    private fun loadTemplate(id: Long) {
        viewModelScope.launch {
            val templateWithExercises = workoutTemplateRepository.getTemplateWithExercises(id)
            templateName.value = templateWithExercises.template.name
            _templateExercises.value = templateWithExercises.exercises
            loadExerciseDefinitions(templateWithExercises.exercises.map { it.definitionId })
        }
    }

    private suspend fun loadExerciseDefinitions(ids: List<UUID>) {
        val definitions =
            ids.associateWith { exerciseDefinitionRepository.getExerciseDefinition(it) }
        _exerciseDefinitions.value = definitions
    }

    fun onExerciseSelected(exerciseIndex: Int, definitionId: UUID) {
        viewModelScope.launch {
            val definition = exerciseDefinitionRepository.getExerciseDefinition(definitionId)
            _exerciseDefinitions.value += (definitionId to definition)
            val updatedExercise =
                _templateExercises.value[exerciseIndex].copy(definitionId = definitionId)
            _templateExercises.value = _templateExercises.value.toMutableList().also {
                it[exerciseIndex] = updatedExercise
            }
        }
    }

    fun addExercise() {
        val newExercise = TemplateExercise(
            templateId = templateId ?: 0L, // Temp ID, will be updated on save
            definitionId = UUID.randomUUID(), // Placeholder
            sets = emptyList()
        )
        _templateExercises.value += newExercise
    }

    fun saveTemplate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (templateId == null) {
                // Create new template
                val newTemplate = WorkoutTemplate(name = templateName.value)
                val newId = workoutTemplateRepository.insertTemplate(newTemplate)
                val exercisesToSave = _templateExercises.value.map { it.copy(templateId = newId) }
                workoutTemplateRepository.upsertTemplateExercises(exercisesToSave)
            } else {
                // Update existing template
                val updatedTemplate = WorkoutTemplate(id = templateId, name = templateName.value)
                workoutTemplateRepository.updateTemplate(updatedTemplate)
                val exercisesToSave =
                    _templateExercises.value.map { it.copy(templateId = templateId) }
                workoutTemplateRepository.deleteExercisesForTemplate(templateId)
                workoutTemplateRepository.upsertTemplateExercises(exercisesToSave)
            }
            onSuccess()
        }
    }
}
