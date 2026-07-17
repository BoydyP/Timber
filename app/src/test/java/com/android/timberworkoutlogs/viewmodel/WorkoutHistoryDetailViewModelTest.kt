package com.android.timberworkoutlogs.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.fixtures.squatExerciseFixture
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.history.WorkoutHistoryDetailUiState
import com.android.timberworkoutlogs.ui.screen.history.WorkoutHistoryDetailViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class WorkoutHistoryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WorkoutHistoryDetailViewModel
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var exerciseDefinitionRepository: ExerciseDefinitionRepository
    private lateinit var settingsRepository: SettingsRepository

    private fun createViewModel(workoutId: Long = -1L) {
        workoutRepository = mockk(relaxed = true)
        exerciseDefinitionRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)

        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
        viewModel = WorkoutHistoryDetailViewModel(
            workoutRepository,
            exerciseDefinitionRepository,
            settingsRepository,
            savedStateHandle
        )
    }

    @Test
    fun `no workoutId yields Error without querying the repository`() = runTest {
        createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is WorkoutHistoryDetailUiState.Error)
    }

    @Test
    fun `workout not found yields Error`() = runTest {
        val workoutId = 5L
        workoutRepository = mockk(relaxed = true)
        exerciseDefinitionRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
        coEvery { workoutRepository.getWorkout(workoutId) } returns null

        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
        viewModel = WorkoutHistoryDetailViewModel(
            workoutRepository,
            exerciseDefinitionRepository,
            settingsRepository,
            savedStateHandle
        )

        val state = viewModel.uiState.value
        assertTrue(state is WorkoutHistoryDetailUiState.Error)
    }

    @Test
    fun `valid workoutId loads workout and its exercises with definitions`() = runTest {
        val workoutId = 1L
        val definition = squatExerciseFixture()
        val exercise = WorkoutExercise(
            workoutId = workoutId,
            definitionId = definition.id,
            unit = WeightUnit.KG,
            sets = listOf(WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true))
        )
        val workout = Workout(id = workoutId, name = "Leg Day")

        workoutRepository = mockk(relaxed = true)
        exerciseDefinitionRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
        coEvery { workoutRepository.getWorkout(workoutId) } returns workout
        coEvery { workoutRepository.getExercisesForWorkout(workoutId) } returns listOf(exercise)
        coEvery { exerciseDefinitionRepository.getExerciseDefinition(definition.id) } returns definition

        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
        viewModel = WorkoutHistoryDetailViewModel(
            workoutRepository,
            exerciseDefinitionRepository,
            settingsRepository,
            savedStateHandle
        )

        val state = viewModel.uiState.value
        assertTrue(state is WorkoutHistoryDetailUiState.Success)
        state as WorkoutHistoryDetailUiState.Success
        assertEquals(workout, state.displayItem.workout)
        assertEquals(1, state.exercises.size)
        assertEquals(definition, state.exercises[0].definition)
        assertEquals(500.0, state.displayItem.totalWeightLiftedInKg, 0.01)
    }
}
