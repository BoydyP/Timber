package com.android.timberworkoutlogs.viewmodel

import android.util.Log
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class WorkoutViewModelTest {

    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var workoutTemplateRepository: WorkoutTemplateRepository
    private lateinit var exerciseDefinitionRepository: ExerciseDefinitionRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: WorkoutViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        workoutRepository = mockk(relaxed = true)
        workoutTemplateRepository = mockk(relaxed = true)
        exerciseDefinitionRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        coEvery { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
        coEvery { workoutRepository.insertWorkout(any()) } returns 1L

        viewModel =
            WorkoutViewModel(
                workoutRepository,
                workoutTemplateRepository,
                exerciseDefinitionRepository,
                settingsRepository
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `onAddSet with WeightAndRepsSet copies weight to new set`() = runTest {
        val exerciseId = UUID.randomUUID()
        val definitionId = UUID.randomUUID()
        val definition = ExerciseDefinition(
            id = definitionId,
            name = "Test Exercise",
            logType = LogType.WEIGHT_AND_REPS,
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = listOf(MuscleGroup.CHEST)
        )

        val initialSet = WeightAndRepsSet(weight = 50.0, reps = 10, isDone = true)
        val exercise = WorkoutExercise(
            id = exerciseId,
            workoutId = 1L,
            definitionId = definitionId,
            sets = listOf(initialSet)
        )
        viewModel.workoutExercises.add(exercise)
        viewModel.exerciseDefinitions.add(definition)
        viewModel.onAddSet(exerciseId)
        val updatedExercise = viewModel.workoutExercises.first { it.id == exerciseId }
        Assert.assertEquals(2, updatedExercise.sets.size)
        val newSet = updatedExercise.sets[1] as WeightAndRepsSet
        Assert.assertEquals(50.0, newSet.weight, 0.0)
        Assert.assertEquals(0, newSet.reps)
        Assert.assertEquals(false, newSet.isDone)
    }
}