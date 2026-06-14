package com.android.timberworkoutlogs.viewmodel

import android.app.Application
import android.util.Log
import app.cash.turbine.test
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExercises
import com.android.timberworkoutlogs.services.WorkoutStateHolder
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class WorkoutViewModelTest {

    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var workoutTemplateRepository: WorkoutTemplateRepository
    private lateinit var exerciseDefinitionRepository: ExerciseDefinitionRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var workoutStateHolder: WorkoutStateHolder
    private lateinit var application: Application
    private lateinit var viewModel: WorkoutViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val benchPressDef = ExerciseDefinition(
        id = UUID.randomUUID(),
        name = "Bench Press",
        equipment = ExerciseEquipment.BARBELL,
        muscleGroups = listOf(MuscleGroup.CHEST),
        logType = LogType.WEIGHT_AND_REPS
    )
    private val pushUpDef = ExerciseDefinition(
        id = UUID.randomUUID(),
        name = "Push Up",
        equipment = ExerciseEquipment.BODYWEIGHT,
        muscleGroups = listOf(MuscleGroup.CHEST),
        logType = LogType.REPS_ONLY
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        workoutRepository = mockk(relaxed = true)
        workoutTemplateRepository = mockk(relaxed = true)
        exerciseDefinitionRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        workoutStateHolder = mockk(relaxed = true)
        application = mockk(relaxed = true)

        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
        coEvery { workoutRepository.insertWorkout(any()) } returns 1L
        coEvery { workoutTemplateRepository.getAllTemplatesWithExerciseCount() } returns
            MutableStateFlow(emptyList())

        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun createViewModel(): WorkoutViewModel = WorkoutViewModel(
        workoutRepository,
        workoutTemplateRepository,
        exerciseDefinitionRepository,
        settingsRepository,
        workoutStateHolder,
        application,
    )

    // ---------- session bootstrap ----------

    @Test
    fun `init inserts a workout and adds one placeholder exercise`() = runTest(testDispatcher) {
        advanceUntilIdle()

        coVerify(exactly = 1) { workoutRepository.insertWorkout(any()) }
        assertEquals(1, viewModel.workoutExercises.size)
        assertEquals(1, viewModel.exerciseDefinitions.size)
        assertNull(
            "Placeholder slot should have a null exercise definition",
            viewModel.exerciseDefinitions[0]
        )
        assertTrue(
            "Placeholder slot should have no sets",
            viewModel.workoutExercises[0].sets.isEmpty()
        )
    }

    @Test
    fun `init uses settings weight unit for the placeholder`() = runTest(testDispatcher) {
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.LB)
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(WeightUnit.LB, viewModel.workoutExercises[0].unit)
    }

    // ---------- isWorkoutEmpty ----------

    @Test
    fun `isWorkoutEmpty is true with only placeholder`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.isWorkoutEmpty.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Note: `isWorkoutEmpty` derives via `snapshotFlow { … }` over `mutableStateListOf`.
    // Recomputation requires `Snapshot.sendApplyNotifications()`, which the Compose
    // runtime triggers automatically but a JVM unit test doesn't. State *transitions*
    // for this flow are covered by instrumentation (WorkoutPreservationTest); here we
    // only verify the initial value.

    // ---------- onAddExercise / deleteExercise ----------

    @Test
    fun `onAddExercise grows both lists in lockstep`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onAddExercise()
        advanceUntilIdle()

        assertEquals(2, viewModel.workoutExercises.size)
        assertEquals(2, viewModel.exerciseDefinitions.size)
        assertNull(viewModel.exerciseDefinitions[1])
    }

    @Test
    fun `deleteExercise removes from both lists at the same index`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            viewModel.onAddExercise()
            advanceUntilIdle()
            assertEquals(2, viewModel.workoutExercises.size)

            val toDelete = viewModel.workoutExercises[0]
            viewModel.deleteExercise(toDelete)

            assertEquals(1, viewModel.workoutExercises.size)
            assertEquals(1, viewModel.exerciseDefinitions.size)
            assertFalse(viewModel.workoutExercises.any { it.id == toDelete.id })
        }

    // ---------- onAddSet weight-carry behaviour ----------

    @Test
    fun `onAddSet for WEIGHT_AND_REPS copies the previous set's weight`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            coEvery { exerciseDefinitionRepository.getExerciseDefinition(benchPressDef.id) } returns
                benchPressDef
            viewModel.onExerciseSelected(0, benchPressDef.id)
            advanceUntilIdle()

            val id = viewModel.workoutExercises[0].id
            viewModel.onSetChanged(id, 0, WeightAndRepsSet(weight = 80.0, reps = 8, isDone = true))
            viewModel.onAddSet(id)
            advanceUntilIdle()

            val sets = viewModel.workoutExercises[0].sets
            assertEquals(2, sets.size)
            val newSet = sets[1] as WeightAndRepsSet
            assertEquals(80.0, newSet.weight, 0.0)
            assertEquals(0, newSet.reps)
            assertFalse(newSet.isDone)
        }

    @Test
    fun `onAddSet for REPS_ONLY does not crash and yields default reps_only set`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            coEvery { exerciseDefinitionRepository.getExerciseDefinition(pushUpDef.id) } returns
                pushUpDef
            viewModel.onExerciseSelected(0, pushUpDef.id)
            advanceUntilIdle()

            val id = viewModel.workoutExercises[0].id
            viewModel.onAddSet(id)
            advanceUntilIdle()

            val sets = viewModel.workoutExercises[0].sets
            assertEquals(2, sets.size)
            assertTrue(sets[1] is RepsOnlySet)
        }

    @Test
    fun `onAddSet is a no-op when exerciseId is unknown`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val before = viewModel.workoutExercises[0].sets.size
        viewModel.onAddSet(UUID.randomUUID())
        advanceUntilIdle()
        assertEquals(before, viewModel.workoutExercises[0].sets.size)
    }

    @Test
    fun `onAddSet is a no-op when the slot has no definition selected yet`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            // Placeholder has null definition
            val id = viewModel.workoutExercises[0].id
            val before = viewModel.workoutExercises[0].sets.size
            viewModel.onAddSet(id)
            advanceUntilIdle()
            assertEquals(before, viewModel.workoutExercises[0].sets.size)
        }

    // ---------- onExerciseUnitChange ----------

    @Test
    fun `onExerciseUnitChange updates only the targeted exercise`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            viewModel.onAddExercise()
            advanceUntilIdle()

            val firstId = viewModel.workoutExercises[0].id
            viewModel.onExerciseUnitChange(firstId, WeightUnit.LB)

            assertEquals(WeightUnit.LB, viewModel.workoutExercises[0].unit)
            assertEquals(WeightUnit.KG, viewModel.workoutExercises[1].unit)
        }

    // ---------- onFinishWorkout ----------

    @Test
    fun `onFinishWorkout filters out exercises whose definition was never selected`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            // Slot 0: select bench press definition
            coEvery { exerciseDefinitionRepository.getExerciseDefinition(benchPressDef.id) } returns
                benchPressDef
            viewModel.onExerciseSelected(0, benchPressDef.id)
            advanceUntilIdle()

            // Slot 1: never selected (placeholder definitionId, null in defs list)
            viewModel.onAddExercise()
            advanceUntilIdle()

            coEvery { workoutRepository.getWorkout(1L) } returns Workout(id = 1L)
            var navigated = false

            viewModel.onFinishWorkout { navigated = true }
            advanceUntilIdle()

            // Only the bench-press slot should have been persisted
            coVerify(exactly = 1) {
                workoutRepository.insertWorkoutExercises(match { saved ->
                    saved.size == 1 && saved.first().definitionId == benchPressDef.id
                })
            }
            assertTrue(navigated)
        }

    @Test
    fun `onFinishWorkout writes timer-derived duration into the workout`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            coEvery { exerciseDefinitionRepository.getExerciseDefinition(benchPressDef.id) } returns
                benchPressDef
            viewModel.onExerciseSelected(0, benchPressDef.id)
            advanceUntilIdle()

            val original = Workout(id = 1L, name = "Workout", durationSeconds = 0)
            coEvery { workoutRepository.getWorkout(1L) } returns original

            viewModel.onFinishWorkout { /* nav */ }
            advanceUntilIdle()

            // No bound timer service in the unit-test environment → seconds defaults to 0,
            // but the workout row is still updated. Pin that contract.
            coVerify {
                workoutRepository.updateWorkout(match { it.id == 1L && it.durationSeconds == 0 })
            }
        }

    // ---------- onDiscardWorkout ----------

    @Test
    fun `onDiscardWorkout deletes the workout and navigates back`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            val workoutToDelete = Workout(id = 1L)
            coEvery { workoutRepository.getWorkout(1L) } returns workoutToDelete
            var navigated = false

            viewModel.onDiscardWorkout { navigated = true }
            advanceUntilIdle()

            coVerify { workoutRepository.deleteWorkout(workoutToDelete) }
            assertTrue(navigated)
        }

    @Test
    fun `onDiscardWorkout does nothing destructive when no workoutId is set`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            // Force-clear the workout id by finishing the workout, which runs resetWorkoutSession.
            // resetWorkoutSession sets currentWorkoutId = null.
            coEvery { workoutRepository.getWorkout(any()) } returns Workout(id = 1L)
            viewModel.onFinishWorkout { /* nav */ }
            advanceUntilIdle()

            // Now currentWorkoutId is null; calling discard again should not delete anything.
            viewModel.onDiscardWorkout { /* nav */ }
            advanceUntilIdle()

            // Only the original onFinishWorkout-driven update should have hit the repo.
            coVerify(exactly = 0) { workoutRepository.deleteWorkout(any()) }
        }

    // ---------- importExercisesFromTemplate ----------

    @Test
    fun `importExercisesFromTemplate replaces empty placeholder with template exercises`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            // Two template exercises, both real defs.
            val templateId = 42L
            val templateExercises = listOf(
                TemplateExercise(
                    templateId = templateId,
                    definitionId = benchPressDef.id,
                    sets = listOf(WeightAndRepsSet(weight = 60.0, reps = 5)),
                ),
                TemplateExercise(
                    templateId = templateId,
                    definitionId = pushUpDef.id,
                    sets = listOf(RepsOnlySet(reps = 10)),
                ),
            )
            coEvery { workoutTemplateRepository.getTemplateWithExercises(templateId) } returns
                WorkoutTemplateWithExercises(
                    template = WorkoutTemplate(id = templateId, name = "Push"),
                    exercises = templateExercises,
                )
            coEvery { exerciseDefinitionRepository.getExerciseDefinition(benchPressDef.id) } returns
                benchPressDef
            coEvery { exerciseDefinitionRepository.getExerciseDefinition(pushUpDef.id) } returns
                pushUpDef

            viewModel.importExercisesFromTemplate(templateId)
            advanceUntilIdle()

            assertEquals(2, viewModel.workoutExercises.size)
            assertEquals(2, viewModel.exerciseDefinitions.size)
            assertEquals(benchPressDef.id, viewModel.workoutExercises[0].definitionId)
            assertEquals(pushUpDef.id, viewModel.workoutExercises[1].definitionId)
            assertNotNull(viewModel.exerciseDefinitions[0])
            assertNotNull(viewModel.exerciseDefinitions[1])
        }
}
