package com.android.timberworkoutlogs.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.fixtures.squatExerciseFixture
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.exercise.CreateExerciseViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateExerciseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CreateExerciseViewModel
    private lateinit var repository: ExerciseDefinitionRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
    }

    private fun createViewModel(exerciseId: String? = null) {
        savedStateHandle = SavedStateHandle(mapOf("exerciseId" to exerciseId))
        viewModel = CreateExerciseViewModel(repository, savedStateHandle)
    }

    @Test
    fun `init with no exerciseId has correct initial state`() {
        // Given & When
        createViewModel()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals(ExerciseEquipment.BARBELL, state.equipment)
        assertTrue(state.muscleGroups.isEmpty())
        assertEquals(LogType.WEIGHT_AND_REPS, state.logType)
        assertFalse(state.isSaving)
        assertFalse(state.isEditing)
        assertNull(state.exerciseId)
    }

    @Test
    fun `init with exerciseId loads existing exercise`() = runTest {
        // Given
        val existingExercise = squatExerciseFixture()
        coEvery { repository.getExerciseDefinition(existingExercise.id) } returns existingExercise
        
        // When
        createViewModel(existingExercise.id.toString())

        // Then
        coVerify { repository.getExerciseDefinition(existingExercise.id) }
        val state = viewModel.uiState.value
        assertEquals(existingExercise.name, state.name)
        assertEquals(existingExercise.equipment, state.equipment)
        assertEquals(existingExercise.muscleGroups.toSet(), state.muscleGroups)
        assertEquals(existingExercise.logType, state.logType)
        assertTrue(state.isEditing)
        assertEquals(existingExercise.id, state.exerciseId)
    }

    @Test
    fun `onNameChanged updates name in state`() {
        // Given
        createViewModel()
        val newName = "Bench Press"

        // When
        viewModel.onNameChanged(newName)

        // Then
        assertEquals(newName, viewModel.uiState.value.name)
    }

    @Test
    fun `onNameChanged updates uiState flow with new name`() = runTest {
        // Given
        createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals("", awaitItem().name)

            viewModel.onNameChanged("Bench Press")

            assertEquals("Bench Press", awaitItem().name)
        }
    }

    @Test
    fun `onEquipmentChanged updates equipment and preserves logType for non-bodyweight`() {
        // Given
        createViewModel()
        
        // When
        viewModel.onEquipmentChanged(ExerciseEquipment.DUMBBELL)

        // Then
        val state = viewModel.uiState.value
        assertEquals(ExerciseEquipment.DUMBBELL, state.equipment)
        assertEquals(LogType.WEIGHT_AND_REPS, state.logType) // Should remain unchanged
    }

    @Test
    fun `onEquipmentChanged to bodyweight changes logType to reps only`() {
        // Given
        createViewModel()
        
        // When
        viewModel.onEquipmentChanged(ExerciseEquipment.BODYWEIGHT)

        // Then
        val state = viewModel.uiState.value
        assertEquals(ExerciseEquipment.BODYWEIGHT, state.equipment)
        assertEquals(LogType.REPS_ONLY, state.logType)
    }

    @Test
    fun `onEquipmentChanged updates equipment and logType for bodyweight with flow testing`() = runTest {
        // Given
        createViewModel()

        // When & Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(ExerciseEquipment.BARBELL, initialState.equipment)
            assertEquals(LogType.WEIGHT_AND_REPS, initialState.logType)

            viewModel.onEquipmentChanged(ExerciseEquipment.BODYWEIGHT)

            val newState = awaitItem()
            assertEquals(ExerciseEquipment.BODYWEIGHT, newState.equipment)
            assertEquals(LogType.REPS_ONLY, newState.logType)
        }
    }

    @Test
    fun `onMuscleGroupToggled adds muscle group when not present`() {
        // Given
        createViewModel()
        
        // When
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.muscleGroups.contains(MuscleGroup.CHEST))
        assertEquals(1, state.muscleGroups.size)
    }

    @Test
    fun `onMuscleGroupToggled removes muscle group when present`() {
        // Given
        createViewModel()
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST) // Add first
        
        // When
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST) // Remove

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.muscleGroups.contains(MuscleGroup.CHEST))
        assertTrue(state.muscleGroups.isEmpty())
    }

    @Test
    fun `onMuscleGroupToggled adds muscle group with flow testing`() = runTest {
        // Given
        createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertTrue(awaitItem().muscleGroups.isEmpty())

            viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)

            val newState = awaitItem()
            assertEquals(1, newState.muscleGroups.size)
            assertTrue(newState.muscleGroups.contains(MuscleGroup.CHEST))
        }
    }

    @Test
    fun `onMuscleGroupToggled removes existing muscle group with flow testing`() = runTest {
        // Given
        createViewModel()
        // First, add a muscle group
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)

        // When & Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(1, initialState.muscleGroups.size)
            assertTrue(initialState.muscleGroups.contains(MuscleGroup.CHEST))

            // Now, toggle it again to remove it
            viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)

            val newState = awaitItem()
            assertTrue(newState.muscleGroups.isEmpty())
        }
    }

    @Test
    fun `onMuscleGroupToggled handles multiple muscle groups correctly`() {
        // Given
        createViewModel()
        
        // When
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)
        viewModel.onMuscleGroupToggled(MuscleGroup.SHOULDERS)
        viewModel.onMuscleGroupToggled(MuscleGroup.TRICEPS)

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.muscleGroups.size)
        assertTrue(state.muscleGroups.contains(MuscleGroup.CHEST))
        assertTrue(state.muscleGroups.contains(MuscleGroup.SHOULDERS))
        assertTrue(state.muscleGroups.contains(MuscleGroup.TRICEPS))
    }

    @Test
    fun `onLogTypeChanged updates logType in state`() {
        // Given
        createViewModel()
        
        // When
        viewModel.onLogTypeChanged(LogType.DISTANCE_AND_TIME)

        // Then
        assertEquals(LogType.DISTANCE_AND_TIME, viewModel.uiState.value.logType)
    }

    @Test
    fun `saveExercise creates new exercise when not editing`() = runTest {
        // Given
        createViewModel()
        viewModel.onNameChanged("Test Exercise")
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)
        var callbackInvoked = false

        // When
        viewModel.saveExercise { callbackInvoked = true }

        // Then
        coVerify { repository.insert(any()) }
        coVerify(exactly = 0) { repository.update(any()) }
        assertTrue(callbackInvoked)
    }

    @Test
    fun `saveExercise updates existing exercise when editing`() = runTest {
        // Given
        val existingExercise = squatExerciseFixture()
        coEvery { repository.getExerciseDefinition(existingExercise.id) } returns existingExercise
        createViewModel(existingExercise.id.toString())
        
        viewModel.onNameChanged("Updated Exercise")
        var callbackInvoked = false

        // When
        viewModel.saveExercise { callbackInvoked = true }

        // Then
        coVerify { repository.update(any()) }
        coVerify(exactly = 0) { repository.insert(any()) }
        assertTrue(callbackInvoked)
    }

    @Test
    fun `saveExercise calls repository insert for new exercise`() = runTest {
        // Given
        createViewModel()
        val exerciseName = "Squat"
        val muscleGroup = MuscleGroup.LEGS

        viewModel.onNameChanged(exerciseName)
        viewModel.onMuscleGroupToggled(muscleGroup)

        // When
        viewModel.saveExercise { /* onExerciseSaved */ }

        // Then
        coVerify { repository.insert(any()) }
    }

    @Test
    fun `saveExercise does not save when name is blank`() = runTest {
        // Given
        createViewModel()
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST) // Valid muscle group
        // Name remains blank
        var callbackInvoked = false

        // When
        viewModel.saveExercise { callbackInvoked = true }

        // Then
        coVerify(exactly = 0) { repository.insert(any()) }
        coVerify(exactly = 0) { repository.update(any()) }
        assertFalse(callbackInvoked)
    }

    @Test
    fun `saveExercise does not save when no muscle groups selected`() = runTest {
        // Given
        createViewModel()
        viewModel.onNameChanged("Test Exercise") // Valid name
        // No muscle groups added
        var callbackInvoked = false

        // When
        viewModel.saveExercise { callbackInvoked = true }

        // Then
        coVerify(exactly = 0) { repository.insert(any()) }
        coVerify(exactly = 0) { repository.update(any()) }
        assertFalse(callbackInvoked)
    }

    @Test
    fun `saveExercise creates exercise with correct properties`() = runTest {
        // Given
        createViewModel()
        viewModel.onNameChanged("Bench Press")
        viewModel.onEquipmentChanged(ExerciseEquipment.BARBELL)
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)
        viewModel.onMuscleGroupToggled(MuscleGroup.SHOULDERS)
        viewModel.onLogTypeChanged(LogType.WEIGHT_AND_REPS)

        // When
        viewModel.saveExercise { }

        // Then
        coVerify { 
            repository.insert(match<ExerciseDefinition> { exercise ->
                exercise.name == "Bench Press" &&
                exercise.equipment == ExerciseEquipment.BARBELL &&
                exercise.muscleGroups.toSet() == setOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS) &&
                exercise.logType == LogType.WEIGHT_AND_REPS
            })
        }
    }

    @Test
    fun `saveExercise preserves exercise ID when editing`() = runTest {
        // Given
        val existingExercise = squatExerciseFixture()
        coEvery { repository.getExerciseDefinition(existingExercise.id) } returns existingExercise
        createViewModel(existingExercise.id.toString())

        // When
        viewModel.saveExercise { }

        // Then
        coVerify { 
            repository.update(match<ExerciseDefinition> { exercise ->
                exercise.id == existingExercise.id
            })
        }
    }

    @Test
    fun `saveExercise generates new ID when creating new exercise`() = runTest {
        // Given
        createViewModel()
        viewModel.onNameChanged("New Exercise")
        viewModel.onMuscleGroupToggled(MuscleGroup.LEGS)

        // When
        viewModel.saveExercise { }

        // Then
        coVerify { 
            repository.insert(match<ExerciseDefinition> { exercise ->
                true // Should have a generated UUID
            })
        }
    }
}
