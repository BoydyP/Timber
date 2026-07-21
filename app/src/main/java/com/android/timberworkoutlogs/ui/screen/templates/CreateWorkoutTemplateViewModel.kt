package com.android.timberworkoutlogs.ui.screen.templates

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
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
    val templateId: Long? = null,
    val weightUnit: WeightUnit = WeightUnit.KG
)

@HiltViewModel
class CreateWorkoutTemplateViewModel @Inject constructor(
    private val workoutTemplateRepository: WorkoutTemplateRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWorkoutTemplateUiState())
    val uiState = _uiState.asStateFlow()

    private val templateId: Long = savedStateHandle.get<Long>("templateId") ?: -1L

    init {
        if (templateId != -1L) {
            _uiState.update { it.copy(templateId = templateId, isEditing = true) }
            loadTemplate(templateId)
        }

        viewModelScope.launch {
            settingsRepository.weightUnit.collect { unit ->
                _uiState.update { it.copy(weightUnit = unit) }
            }
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
        // Resolve the target slot by its stable id, captured before the suspend point below,
        // since the list can be mutated (deleted/reordered) while the definition is loading.
        val targetExerciseId = _uiState.value.templateExercises.getOrNull(exerciseIndex)?.id
            ?: return
        viewModelScope.launch {
            val definition = exerciseDefinitionRepository.getExerciseDefinition(definitionId)
            _uiState.update {
                val currentIndex = it.templateExercises.indexOfFirst { exercise ->
                    exercise.id == targetExerciseId
                }
                if (currentIndex == -1) return@update it

                val newDefinitions = it.exerciseDefinitions + (definitionId to definition)
                val updatedExercise =
                    it.templateExercises[currentIndex].copy(definitionId = definitionId)
                val newExercises = it.templateExercises.toMutableList().also { list ->
                    list[currentIndex] = updatedExercise
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
            templateId = if (templateId != -1L) templateId else 0L,
            definitionId = PLACEHOLDER_DEFINITION_ID
        )
        _uiState.update { it.copy(templateExercises = it.templateExercises + newExercise) }
    }

    fun removeExercise(exerciseIndex: Int) {
        _uiState.update {
            val exercises = it.templateExercises.toMutableList()
            exercises.removeAt(exerciseIndex)
            it.copy(templateExercises = exercises)
        }
    }

    fun onExercisesReordered(fromIndex: Int, toIndex: Int) {
        _uiState.update {
            val exercises = it.templateExercises.toMutableList()
            exercises.add(toIndex, exercises.removeAt(fromIndex))
            it.copy(templateExercises = exercises)
        }
    }

    fun onAddSet(exerciseIndex: Int) {
        _uiState.update {
            val exercise = it.templateExercises[exerciseIndex]
            val definition = it.exerciseDefinitions[exercise.definitionId]
            var newSet: ExerciseSet = when (definition?.logType) {
                LogType.WEIGHT_AND_REPS -> WeightAndRepsSet()
                LogType.REPS_ONLY -> RepsOnlySet()
                LogType.TIME -> TimedSet()
                LogType.DISTANCE_AND_TIME -> DistanceAndTimeSet()
                null -> WeightAndRepsSet()
            }

            if (definition?.logType == LogType.WEIGHT_AND_REPS) {
                val lastSet = it.templateExercises[exerciseIndex].sets.lastOrNull()
                if (lastSet is WeightAndRepsSet) {
                    newSet = lastSet.copy(reps = 0, isDone = false)
                }
            }

            val newExercises = it.templateExercises.toMutableList()
            val newSets = exercise.sets.toMutableList()
            newSets.add(newSet)
            newExercises[exerciseIndex] = exercise.copy(sets = newSets)
            it.copy(templateExercises = newExercises)
        }
    }

    fun onDeleteSet(exerciseIndex: Int, setIndex: Int) {
        _uiState.update {
            val newExercises = it.templateExercises.toMutableList()
            val exercise = newExercises[exerciseIndex]
            val newSets = exercise.sets.toMutableList()
            newSets.removeAt(setIndex)
            newExercises[exerciseIndex] = exercise.copy(sets = newSets)
            it.copy(templateExercises = newExercises)
        }
    }

    fun onSetChanged(exerciseIndex: Int, setIndex: Int, newSet: ExerciseSet) {
        _uiState.update {
            val newExercises = it.templateExercises.toMutableList()
            val exercise = newExercises[exerciseIndex]
            val newSets = exercise.sets.toMutableList()
            newSets[setIndex] = newSet
            newExercises[exerciseIndex] = exercise.copy(sets = newSets)
            it.copy(templateExercises = newExercises)
        }
    }

    fun saveTemplate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(isSaving = true) }

            // Stamp each exercise's current list position as its persisted order, so
            // drag-to-reorder (which only mutates in-memory list order) survives a reload.
            val exercisesWithDefinitions = currentState.templateExercises
                .filter { it.definitionId != PLACEHOLDER_DEFINITION_ID }
                .mapIndexed { index, exercise -> exercise.copy(order = index) }

            if (templateId == -1L) {
                // Create new template
                val newTemplate = WorkoutTemplate(name = currentState.name)
                val newId = workoutTemplateRepository.insertTemplate(newTemplate)
                val exercisesToSave = exercisesWithDefinitions.map { it.copy(templateId = newId) }
                workoutTemplateRepository.upsertTemplateExercises(exercisesToSave)
            } else {
                // Update existing template
                val updatedTemplate =
                    WorkoutTemplate(id = templateId, name = currentState.name)
                workoutTemplateRepository.updateTemplate(updatedTemplate)
                val exercisesToSave =
                    exercisesWithDefinitions.map { it.copy(templateId = templateId) }
                workoutTemplateRepository.deleteExercisesForTemplate(templateId)
                workoutTemplateRepository.upsertTemplateExercises(exercisesToSave)
            }
            onSuccess()
        }
    }

    fun deleteTemplate() {
        viewModelScope.launch {
            if (templateId != -1L) {
                val template =
                    WorkoutTemplate(id = templateId, name = _uiState.value.name)
                workoutTemplateRepository.deleteTemplate(template)
            }
        }
    }

    fun startWorkout(onSuccess: (workoutId: Long) -> Unit) {
        viewModelScope.launch {
            if (templateId != -1L) {
                val newWorkoutId = workoutTemplateRepository.createWorkoutFromTemplate(templateId)
                onSuccess(newWorkoutId)
            }
        }
    }

    companion object {
        private val PLACEHOLDER_DEFINITION_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}
