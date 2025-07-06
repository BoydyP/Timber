package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateExerciseViewModelTest {

    private lateinit var viewModel: CreateExerciseViewModel
    private val repository: ExerciseDefinitionRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val savedStateHandle = SavedStateHandle()
        viewModel = CreateExerciseViewModel(repository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onNameChanged should update uiState with new name`() = runTest {
        viewModel.uiState.test {
            assertEquals("", awaitItem().name)

            viewModel.onNameChanged("Bench Press")

            assertEquals("Bench Press", awaitItem().name)
        }
    }

    @Test
    fun `onEquipmentChanged should update equipment and logType for bodyweight`() = runTest {
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
    fun `onMuscleGroupToggled should add a new muscle group`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem().muscleGroups.isEmpty())

            viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)

            val newState = awaitItem()
            assertEquals(1, newState.muscleGroups.size)
            assertTrue(newState.muscleGroups.contains(MuscleGroup.CHEST))
        }
    }

    @Test
    fun `onMuscleGroupToggled should remove an existing muscle group`() = runTest {
        // First, add a muscle group
        viewModel.onMuscleGroupToggled(MuscleGroup.CHEST)

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
    fun `saveExercise should call repository insert for new exercise`() = runTest {
        val exerciseName = "Squat"
        val muscleGroup = MuscleGroup.LEGS

        viewModel.onNameChanged(exerciseName)
        viewModel.onMuscleGroupToggled(muscleGroup)

        viewModel.saveExercise { /* onExerciseSaved */ }

        // Advance the dispatcher to allow the coroutine in saveExercise to run
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insert(any())
        }
    }
}
