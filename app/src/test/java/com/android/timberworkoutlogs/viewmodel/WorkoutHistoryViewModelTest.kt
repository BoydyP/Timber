package com.android.timberworkoutlogs.viewmodel

import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.models.WorkoutHistoryDisplayItem
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.history.WorkoutHistoryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class WorkoutHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WorkoutHistoryViewModel
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var workoutsFlow: MutableStateFlow<List<Workout>>
    private lateinit var weightUnitFlow: MutableStateFlow<WeightUnit>

    private val testWorkouts = listOf(
        Workout(
            id = 1L,
            name = "Push Day",
            startTime = System.currentTimeMillis(),
            durationSeconds = 3600,
            notes = "Great workout"
        ),
        Workout(
            id = 2L,
            name = "Pull Day",
            startTime = System.currentTimeMillis() - 86400000,
            durationSeconds = 4200,
            notes = ""
        ),
        Workout(
            id = 3L,
            name = "Cardio",
            startTime = System.currentTimeMillis() - 172800000,
            durationSeconds = 1800,
            notes = "Running session"
        )
    )

    private val exercisesForWorkout1 = listOf(
        WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true),
                WeightAndRepsSet(weight = 80.0, reps = 8, isDone = true)
            )
        ),
        WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.LB,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 225.0, reps = 3, isDone = true) // 225 lbs
            )
        )
    )

    private val exercisesForWorkout2 = listOf(
        WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 2L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 60.0, reps = 10, isDone = true),
                RepsOnlySet(reps = 15, isDone = true) // Should not contribute to weight
            )
        )
    )

    private val exercisesForWorkout3 = listOf(
        WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 3L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(
                DistanceAndTimeSet(distance = 5.0, durationSeconds = 1800, isDone = true)
            )
        )
    )

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        workoutRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        workoutsFlow = MutableStateFlow(testWorkouts)
        weightUnitFlow = MutableStateFlow(WeightUnit.KG)

        every { workoutRepository.allWorkouts } returns workoutsFlow
        every { settingsRepository.weightUnit } returns weightUnitFlow
        
        // Mock getExercisesForWorkout calls - called synchronously in combine block
        coEvery { workoutRepository.getExercisesForWorkout(1L) } returns exercisesForWorkout1
        coEvery { workoutRepository.getExercisesForWorkout(2L) } returns exercisesForWorkout2
        coEvery { workoutRepository.getExercisesForWorkout(3L) } returns exercisesForWorkout3

        viewModel = WorkoutHistoryViewModel(workoutRepository, settingsRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `allWorkoutDisplayItems calculates correct data for mixed weight units`() = runTest {
        // Given & When
        val displayItems = viewModel.allWorkoutDisplayItems.first()

        // Then
        assertEquals(3, displayItems.size)
        
        // Workout 1: Mixed KG and LB
        val item1 = displayItems.find { it.workout.id == 1L }!!
        assertEquals(2, item1.exerciseCount)
        // Total weight in KG: (100*5 + 80*8) + (225*3 * 0.453592) = 1140 + 306.1746 = 1446.1746 kg
        assertEquals(1446.1746, item1.totalWeightLiftedInKg, 0.01)
        assertEquals(0.0, item1.totalDistance, 0.01)
        assertEquals(WeightUnit.KG, item1.systemWeightUnit)

        // Workout 2: Only KG weights (RepsOnlySet should not contribute)
        val item2 = displayItems.find { it.workout.id == 2L }!!
        assertEquals(1, item2.exerciseCount)
        assertEquals(600.0, item2.totalWeightLiftedInKg, 0.01) // 60*10
        assertEquals(0.0, item2.totalDistance, 0.01)

        // Workout 3: Distance exercise
        val item3 = displayItems.find { it.workout.id == 3L }!!
        assertEquals(1, item3.exerciseCount)
        assertEquals(0.0, item3.totalWeightLiftedInKg, 0.01)
        assertEquals(5.0, item3.totalDistance, 0.01)
    }

    @Test
    fun `allWorkoutDisplayItems updates when weight unit changes`() = runTest {
        // Given - Initial state with KG
        val initialItems = viewModel.allWorkoutDisplayItems.first()
        assertEquals(WeightUnit.KG, initialItems[0].systemWeightUnit)

        // When - Change to LB
        weightUnitFlow.value = WeightUnit.LB

        // Then - SystemWeightUnit should update but calculations remain in KG
        val updatedItems = viewModel.allWorkoutDisplayItems.first()
        assertEquals(WeightUnit.LB, updatedItems[0].systemWeightUnit)
        // Weight calculations should remain the same (still converted to KG internally)
        val item1 = updatedItems.find { it.workout.id == 1L }!!
        assertEquals(1446.1746, item1.totalWeightLiftedInKg, 0.01)
    }

    @Test
    fun `allWorkoutDisplayItems updates when workouts change`() = runTest {
        // Given - Add a new workout
        val newWorkout = Workout(
            id = 4L,
            name = "Leg Day",
            startTime = System.currentTimeMillis() - 259200000,
            durationSeconds = 5400,
            notes = "Intense session"
        )
        val exercisesForNewWorkout = listOf(
            WorkoutExercise(
                id = UUID.randomUUID(),
                workoutId = 4L,
                definitionId = UUID.randomUUID(),
                unit = WeightUnit.KG,
                sets = mutableListOf(
                    WeightAndRepsSet(weight = 120.0, reps = 3, isDone = true)
                )
            )
        )
        coEvery { workoutRepository.getExercisesForWorkout(4L) } returns exercisesForNewWorkout

        // When
        workoutsFlow.value = testWorkouts + newWorkout

        // Then
        val displayItems = viewModel.allWorkoutDisplayItems.first()
        assertEquals(4, displayItems.size)
        
        val newItem = displayItems.find { it.workout.id == 4L }!!
        assertEquals("Leg Day", newItem.workout.name)
        assertEquals(1, newItem.exerciseCount)
        assertEquals(360.0, newItem.totalWeightLiftedInKg, 0.01) // 120*3
    }

    @Test
    fun `allWorkoutDisplayItems handles empty workouts list`() = runTest {
        // Given
        workoutsFlow.value = emptyList()

        // When
        val displayItems = viewModel.allWorkoutDisplayItems.first()

        // Then
        assertEquals(0, displayItems.size)
    }

    @Test
    fun `allWorkoutDisplayItems handles workouts with no exercises`() = runTest {
        // Given
        val workoutWithNoExercises = Workout(id = 5L, name = "Empty Workout")
        coEvery { workoutRepository.getExercisesForWorkout(5L) } returns emptyList()
        workoutsFlow.value = listOf(workoutWithNoExercises)

        // When
        val displayItems = viewModel.allWorkoutDisplayItems.first()

        // Then
        assertEquals(1, displayItems.size)
        val item = displayItems[0]
        assertEquals(0, item.exerciseCount)
        assertEquals(0.0, item.totalWeightLiftedInKg, 0.01)
        assertEquals(0.0, item.totalDistance, 0.01)
    }

    @Test
    fun `allWorkoutDisplayItems handles exercises with incomplete sets`() = runTest {
        // Given
        val exercisesWithIncompleteSets = listOf(
            WorkoutExercise(
                id = UUID.randomUUID(),
                workoutId = 1L,
                definitionId = UUID.randomUUID(),
                unit = WeightUnit.KG,
                sets = mutableListOf(
                    WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true),
                    WeightAndRepsSet(weight = 80.0, reps = 8, isDone = false) // Not completed
                )
            )
        )
        coEvery { workoutRepository.getExercisesForWorkout(1L) } returns exercisesWithIncompleteSets
        coEvery { workoutRepository.getExercisesForWorkout(2L) } returns emptyList()
        coEvery { workoutRepository.getExercisesForWorkout(3L) } returns emptyList()

        // When
        val displayItems = viewModel.allWorkoutDisplayItems.first()

        // Then
        val item1 = displayItems.find { it.workout.id == 1L }!!
        // Should include all sets regardless of isDone status
        assertEquals(1140.0, item1.totalWeightLiftedInKg, 0.01) // 100*5 + 80*8
    }

    @Test
    fun `deleteWorkout calls repository deleteWorkout`() = runTest {
        // Given
        val displayItem = WorkoutHistoryDisplayItem(
            workout = testWorkouts[0],
            exerciseCount = 2,
            totalWeightLiftedInKg = 1000.0,
            totalDistance = 0.0,
            systemWeightUnit = WeightUnit.KG
        )

        // When
        viewModel.deleteWorkout(displayItem)

        // Then
        coVerify { workoutRepository.deleteWorkout(testWorkouts[0]) }
    }

    @Test
    fun `deleteWorkout handles multiple deletions`() = runTest {
        // Given
        val displayItem1 = WorkoutHistoryDisplayItem(
            workout = testWorkouts[0],
            exerciseCount = 2,
            totalWeightLiftedInKg = 1000.0,
            totalDistance = 0.0,
            systemWeightUnit = WeightUnit.KG
        )
        val displayItem2 = WorkoutHistoryDisplayItem(
            workout = testWorkouts[1],
            exerciseCount = 1,
            totalWeightLiftedInKg = 500.0,
            totalDistance = 0.0,
            systemWeightUnit = WeightUnit.KG
        )

        // When
        viewModel.deleteWorkout(displayItem1)
        viewModel.deleteWorkout(displayItem2)

        // Then
        coVerify { workoutRepository.deleteWorkout(testWorkouts[0]) }
        coVerify { workoutRepository.deleteWorkout(testWorkouts[1]) }
    }

    @Test
    fun `allWorkoutDisplayItems correctly converts LB to KG`() = runTest {
        // Given - Exercise with only LB weights
        val lbOnlyExercises = listOf(
            WorkoutExercise(
                id = UUID.randomUUID(),
                workoutId = 1L,
                definitionId = UUID.randomUUID(),
                unit = WeightUnit.LB,
                sets = mutableListOf(
                    WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true) // 100 lbs
                )
            )
        )
        coEvery { workoutRepository.getExercisesForWorkout(1L) } returns lbOnlyExercises
        coEvery { workoutRepository.getExercisesForWorkout(2L) } returns emptyList()
        coEvery { workoutRepository.getExercisesForWorkout(3L) } returns emptyList()

        // When
        val displayItems = viewModel.allWorkoutDisplayItems.first()

        // Then
        val item1 = displayItems.find { it.workout.id == 1L }!!
        // 100 lbs * 5 reps * 0.453592 = 226.796 kg
        assertEquals(226.796, item1.totalWeightLiftedInKg, 0.01)
    }

    @Test
    fun `allWorkoutDisplayItems handles mixed set types in single exercise`() = runTest {
        // Given
        val mixedSetsExercise = listOf(
            WorkoutExercise(
                id = UUID.randomUUID(),
                workoutId = 1L,
                definitionId = UUID.randomUUID(),
                unit = WeightUnit.KG,
                sets = mutableListOf(
                    WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true),
                    DistanceAndTimeSet(distance = 2.0, durationSeconds = 600, isDone = true),
                    RepsOnlySet(reps = 20, isDone = true)
                )
            )
        )
        coEvery { workoutRepository.getExercisesForWorkout(1L) } returns mixedSetsExercise
        coEvery { workoutRepository.getExercisesForWorkout(2L) } returns emptyList()
        coEvery { workoutRepository.getExercisesForWorkout(3L) } returns emptyList()

        // When
        val displayItems = viewModel.allWorkoutDisplayItems.first()

        // Then
        val item1 = displayItems.find { it.workout.id == 1L }!!
        assertEquals(500.0, item1.totalWeightLiftedInKg, 0.01) // Only WeightAndRepsSet contributes
        assertEquals(2.0, item1.totalDistance, 0.01) // Only DistanceAndTimeSet contributes
    }

    @Test
    fun `allWorkoutDisplayItems preserves workout properties in display items`() = runTest {
        // Given & When
        val displayItems = viewModel.allWorkoutDisplayItems.first()

        // Then
        displayItems.forEach { item ->
            val originalWorkout = testWorkouts.find { it.id == item.workout.id }!!
            assertEquals(originalWorkout.name, item.workout.name)
            assertEquals(originalWorkout.startTime, item.workout.startTime)
            assertEquals(originalWorkout.durationSeconds, item.workout.durationSeconds)
            assertEquals(originalWorkout.notes, item.workout.notes)
        }
    }
}
