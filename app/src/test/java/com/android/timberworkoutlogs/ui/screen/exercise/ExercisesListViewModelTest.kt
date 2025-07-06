package com.android.timberworkoutlogs.ui.screen.exercise

import app.cash.turbine.test
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesListViewModelTest {

    private lateinit var viewModel: ExercisesListViewModel
    private val repository: ExerciseDefinitionRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load exercises from repository`() = runTest {
        val fakeExercises = listOf(
            ExerciseDefinition(
                UUID.randomUUID(),
                "Squat",
                ExerciseEquipment.BARBELL,
                listOf(MuscleGroup.LEGS),
                LogType.WEIGHT_AND_REPS
            ),
            ExerciseDefinition(
                UUID.randomUUID(), "Deadlift", ExerciseEquipment.BARBELL, listOf(
                    MuscleGroup.LEGS,
                    MuscleGroup.BACK
                ), LogType.WEIGHT_AND_REPS
            )
        )
        every { repository.allExerciseDefinitions } returns flowOf(fakeExercises)

        viewModel = ExercisesListViewModel(repository)

        // The robust pattern: check the initial state, advance the dispatcher, then check the loaded state.
        viewModel.allExercises.test {
            // 1. The first item emitted by a StateFlow is its initial value.
            assertEquals(0, awaitItem().size)

            // 2. Now we let the dispatcher run the coroutines that collect from the repository.
            testDispatcher.scheduler.advanceUntilIdle()

            // 3. The flow has been updated, so we can await the new item.
            val loadedExercises = awaitItem()
            assertEquals(2, loadedExercises.size)
            assertEquals("Squat", loadedExercises[0].name)
        }
    }

    @Test
    fun `deleteExercise should call repository delete`() = runTest {
        val exerciseToDelete = ExerciseDefinition(
            UUID.randomUUID(),
            "Curls",
            ExerciseEquipment.DUMBBELL,
            listOf(MuscleGroup.BICEPS),
            LogType.WEIGHT_AND_REPS
        )
        every { repository.allExerciseDefinitions } returns flowOf(listOf(exerciseToDelete))

        viewModel = ExercisesListViewModel(repository)

        // Let the initial list be collected
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteExercise(exerciseToDelete)

        // Advance the dispatcher to allow the coroutine in deleteExercise to run
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.delete(exerciseToDelete)
        }
    }
}
