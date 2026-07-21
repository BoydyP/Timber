package com.android.timberworkoutlogs.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
import com.android.timberworkoutlogs.fixtures.squatExerciseFixture
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.templates.CreateWorkoutTemplateViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateWorkoutTemplateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CreateWorkoutTemplateViewModel
    private lateinit var workoutTemplateRepository: WorkoutTemplateRepository
    private lateinit var exerciseDefinitionRepository: ExerciseDefinitionRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setUp() {
        workoutTemplateRepository = mockk(relaxed = true)
        exerciseDefinitionRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
    }

    private fun createViewModel(templateId: Long = -1L) {
        savedStateHandle = SavedStateHandle(mapOf("templateId" to templateId))

        viewModel = CreateWorkoutTemplateViewModel(
            workoutTemplateRepository,
            exerciseDefinitionRepository,
            settingsRepository,
            savedStateHandle
        )
    }

    @Test
    fun `init with templateId loads template`() = runTest {
        val templateId = 1L
        createViewModel(templateId)

        coVerify { workoutTemplateRepository.getTemplateWithExercises(templateId) }
        assertTrue(viewModel.uiState.value.isEditing)
        assertEquals(templateId, viewModel.uiState.value.templateId)
    }

    @Test
    fun `onNameChanged updates uiState`() {
        createViewModel()
        val newName = "Leg Day"
        viewModel.onNameChanged(newName)
        assertEquals(newName, viewModel.uiState.value.name)
    }

    @Test
    fun `addExercise updates uiState`() {
        createViewModel()
        viewModel.addExercise()
        assertEquals(1, viewModel.uiState.value.templateExercises.size)
    }

    @Test
    fun `removeExercise updates uiState`() {
        createViewModel()
        viewModel.addExercise()
        viewModel.removeExercise(0)
        assertTrue(viewModel.uiState.value.templateExercises.isEmpty())
    }

    @Test
    fun `onAddSet updates uiState`() = runTest {

        val definition: ExerciseDefinition = squatExerciseFixture()
        val definitionId = definition.id
        coEvery { exerciseDefinitionRepository.getExerciseDefinition(definitionId) } returns definition

        createViewModel()
        viewModel.addExercise()
        viewModel.onExerciseSelected(0, definitionId)
        viewModel.onAddSet(0)

        assertEquals(1, viewModel.uiState.value.templateExercises[0].sets.size)
    }

    @Test
    fun `onDeleteSet updates uiState`() = runTest {
        val definition: ExerciseDefinition = squatExerciseFixture()
        val definitionId = definition.id
        coEvery { exerciseDefinitionRepository.getExerciseDefinition(definitionId) } returns definition

        createViewModel()
        viewModel.addExercise()
        viewModel.onExerciseSelected(0, definitionId)
        viewModel.onAddSet(0)
        viewModel.onDeleteSet(0, 0)

        assertTrue(viewModel.uiState.value.templateExercises[0].sets.isEmpty())
    }

    @Test
    fun `onSetChanged updates uiState`() = runTest {
        val definition: ExerciseDefinition = squatExerciseFixture()
        val definitionId = definition.id
        coEvery { exerciseDefinitionRepository.getExerciseDefinition(definitionId) } returns definition

        createViewModel()
        viewModel.addExercise()
        viewModel.onExerciseSelected(0, definitionId)
        viewModel.onAddSet(0)

        val newSet = WeightAndRepsSet(weight = 100.0, reps = 10)
        viewModel.onSetChanged(0, 0, newSet)

        assertEquals(newSet, viewModel.uiState.value.templateExercises[0].sets[0])
    }

    @Test
    fun `saveTemplate new template saves correctly`() = runTest {
        createViewModel()

        viewModel.onNameChanged("New Workout")
        viewModel.saveTemplate { }

        coVerify { workoutTemplateRepository.insertTemplate(any()) }
    }

    @Test
    fun `saveTemplate existing template updates correctly`() = runTest {
        val templateId = 1L
        createViewModel(templateId)

        viewModel.onNameChanged("Updated Workout")
        viewModel.saveTemplate { }

        coVerify { workoutTemplateRepository.updateTemplate(any()) }
    }

    @Test
    fun `saveTemplate existing template replaces exercises atomically, not as separate delete-then-insert calls`() =
        runTest {
            // A delete and a separate insert as two independent repository calls can be
            // interrupted between them (process death, scope cancellation), permanently
            // wiping a template's exercises without ever writing the replacements. Saving
            // must go through a single atomic replace instead.
            val templateId = 1L
            createViewModel(templateId)

            viewModel.onNameChanged("Updated Workout")
            viewModel.addExercise()
            viewModel.saveTemplate { }

            coVerify(exactly = 1) {
                workoutTemplateRepository.replaceTemplateExercises(templateId, any())
            }
        }

    @Test
    fun `deleteTemplate deletes correctly`() = runTest {
        val templateId = 1L
        createViewModel(templateId)

        viewModel.deleteTemplate()

        coVerify { workoutTemplateRepository.deleteTemplate(any()) }
    }

    @Test
    fun `onExerciseSelected applies to the correct slot even if the list mutates while the definition loads`() =
        runTest {
            val definition: ExerciseDefinition = squatExerciseFixture()
            val definitionId = definition.id
            val definitionLoaded = CompletableDeferred<ExerciseDefinition>()
            coEvery { exerciseDefinitionRepository.getExerciseDefinition(definitionId) } coAnswers {
                definitionLoaded.await()
            }

            createViewModel()
            viewModel.addExercise() // slot 0
            viewModel.addExercise() // slot 1
            viewModel.addExercise() // slot 2 - target
            val targetExerciseId = viewModel.uiState.value.templateExercises[2].id

            // Selection started for slot 2, but its DB lookup hasn't resolved yet.
            viewModel.onExerciseSelected(2, definitionId)
            assertTrue(viewModel.uiState.value.templateExercises[2].definitionId != definitionId)

            // Slot 0 is deleted while that lookup is still in flight, shifting the target to index 1.
            viewModel.removeExercise(0)
            assertEquals(2, viewModel.uiState.value.templateExercises.size)

            // The lookup now resolves - it must still land on the exercise the user actually picked.
            definitionLoaded.complete(definition)

            val updated = viewModel.uiState.value.templateExercises
            assertEquals(definitionId, updated.first { it.id == targetExerciseId }.definitionId)
            assertTrue(updated.none { it.id != targetExerciseId && it.definitionId == definitionId })
        }

    @Test
    fun `onExercisesReordered moves an exercise to its new position`() {
        createViewModel()
        viewModel.addExercise()
        viewModel.addExercise()
        viewModel.addExercise()
        val original = viewModel.uiState.value.templateExercises
        val (first, second, third) = original

        viewModel.onExercisesReordered(0, 2)

        val reordered = viewModel.uiState.value.templateExercises
        assertEquals(listOf(second, third, first), reordered)
    }

    @Test
    fun `saveTemplate stamps each exercise's order to match its final list position`() = runTest {
        createViewModel()
        viewModel.onNameChanged("Push Day")
        viewModel.addExercise()
        viewModel.addExercise()
        val definition: ExerciseDefinition = squatExerciseFixture()
        viewModel.onExerciseSelected(0, definition.id)
        viewModel.onExerciseSelected(1, definition.id)

        // Reorder before saving - order should reflect final position, not insertion order.
        viewModel.onExercisesReordered(0, 1)
        viewModel.saveTemplate { }

        coVerify {
            workoutTemplateRepository.upsertTemplateExercises(match { exercises ->
                exercises.size == 2 && exercises.map { it.order } == listOf(0, 1)
            })
        }
    }
}