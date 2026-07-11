package com.android.timberworkoutlogs.viewmodel

import com.android.timberworkoutlogs.database.ExerciseDefinitionWithCount
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.database.WorkoutWithExercises
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.services.WorkoutStateHolder
import com.android.timberworkoutlogs.ui.screen.home.HomeScreenViewModel
import com.android.timberworkoutlogs.ui.screen.home.PersonalRecordsUiState
import com.android.timberworkoutlogs.ui.screen.home.WeeklyVolumeUiState
import com.android.timberworkoutlogs.ui.screen.stats.utils.OneRepMaxCalculator
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.UUID

@ExperimentalCoroutinesApi
class HomeScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeScreenViewModel
    private lateinit var workoutStateHolder: WorkoutStateHolder
    private lateinit var workoutDao: WorkoutDao
    private lateinit var settingsRepository: SettingsRepository
    private val isTimerRunningFlow = MutableStateFlow(false)
    private val weightUnitFlow = MutableStateFlow(WeightUnit.KG)
    private val workoutsFlow = MutableStateFlow<List<WorkoutWithExercises>>(emptyList())
    private val exerciseCountsFlow = MutableStateFlow<List<ExerciseDefinitionWithCount>>(emptyList())

    @Before
    fun setup() {
        workoutStateHolder = mockk {
            every { isTimerRunning } returns isTimerRunningFlow
        }
        workoutDao = mockk {
            every { getWorkoutsWithExercisesFrom(any()) } returns workoutsFlow
            every { getExerciseDefinitionsWithWorkoutCounts() } returns exerciseCountsFlow
        }
        settingsRepository = mockk {
            every { weightUnit } returns weightUnitFlow
        }
        viewModel = HomeScreenViewModel(workoutStateHolder, workoutDao, settingsRepository)
    }

    @Test
    fun `isWorkoutInProgress reflects true when timer is running`() = runTest {
        // Given
        isTimerRunningFlow.value = true

        // Then
        assertEquals(true, viewModel.isWorkoutInProgress.value)
    }

    @Test
    fun `isWorkoutInProgress reflects false when timer is not running`() = runTest {
        // Given
        isTimerRunningFlow.value = false

        // Then
        assertEquals(false, viewModel.isWorkoutInProgress.value)
    }

    @Test
    fun `isWorkoutInProgress updates when timer state changes`() = runTest {
        // Initially false
        assertEquals(false, viewModel.isWorkoutInProgress.value)

        // When timer starts
        isTimerRunningFlow.value = true

        // Then state is true
        assertEquals(true, viewModel.isWorkoutInProgress.value)

        // When timer stops
        isTimerRunningFlow.value = false

        // Then state is false again
        assertEquals(false, viewModel.isWorkoutInProgress.value)
    }

    @Test
    fun `weeklyVolumeUiState starts with Loading state`() = runTest {
        // Given & When - Initial state
        val initialState = viewModel.weeklyVolumeUiState.value

        // Then
        assertTrue(initialState is WeeklyVolumeUiState.Loading)
    }

    @Test
    fun `weeklyVolumeUiState shows Success with empty data when no workouts`() = runTest {
        // Given
        workoutsFlow.value = emptyList()

        // When
        val state = viewModel.weeklyVolumeUiState.first()

        // Then
        assertTrue(state is WeeklyVolumeUiState.Success)
        state as WeeklyVolumeUiState.Success
        assertEquals(7, state.chartData.size) // 7 days
        assertTrue(state.chartData.all { it == 0.0f }) // All days should be 0
        assertEquals(WeightUnit.KG, state.weightUnit)
    }

    @Test
    fun `weeklyVolumeUiState calculates weekly volume correctly in KG`() = runTest {
        // Given - Monday workout
        val mondayTime = getTimeForDayOfWeek(Calendar.MONDAY)
        val workout = Workout(
            id = 1L,
            name = "Test Workout",
            startTime = mondayTime,
            durationSeconds = 3600
        )
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true), // 500 kg volume
                WeightAndRepsSet(weight = 80.0, reps = 8, isDone = true)   // 640 kg volume
            )
        )
        val workoutWithExercises = WorkoutWithExercises(
            workout = workout,
            exercises = listOf(exercise)
        )
        workoutsFlow.value = listOf(workoutWithExercises)

        // When
        val state = viewModel.weeklyVolumeUiState.first()

        // Then
        assertTrue(state is WeeklyVolumeUiState.Success)
        state as WeeklyVolumeUiState.Success
        assertEquals(7, state.chartData.size)
        assertEquals(1140.0f, state.chartData[0], 0.1f) // Monday - total volume
        assertTrue(state.chartData.drop(1).all { it == 0.0f }) // Other days should be 0
        assertEquals(WeightUnit.KG, state.weightUnit)
    }

    @Test
    fun `weeklyVolumeUiState converts LB exercises to KG for calculation`() = runTest {
        // Given - Tuesday workout with LB weights
        val tuesdayTime = getTimeForDayOfWeek(Calendar.TUESDAY)
        val workout = Workout(
            id = 1L,
            name = "Test Workout",
            startTime = tuesdayTime,
            durationSeconds = 3600
        )
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.LB,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true) // 100 lbs * 5 reps
            )
        )
        val workoutWithExercises = WorkoutWithExercises(
            workout = workout,
            exercises = listOf(exercise)
        )
        workoutsFlow.value = listOf(workoutWithExercises)

        // When
        val state = viewModel.weeklyVolumeUiState.first()

        // Then
        assertTrue(state is WeeklyVolumeUiState.Success)
        state as WeeklyVolumeUiState.Success
        // 100 lbs * 5 reps * 0.45359237 = ~226.8 kg
        assertEquals(226.8f, state.chartData[1], 1.0f) // Tuesday
    }

    @Test
    fun `weeklyVolumeUiState displays volume in LB when weight unit is LB`() = runTest {
        // Given - Set weight unit to LB
        weightUnitFlow.value = WeightUnit.LB
        
        val mondayTime = getTimeForDayOfWeek(Calendar.MONDAY)
        val workout = Workout(
            id = 1L,
            name = "Test Workout",
            startTime = mondayTime,
            durationSeconds = 3600
        )
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true) // 500 kg volume
            )
        )
        val workoutWithExercises = WorkoutWithExercises(
            workout = workout,
            exercises = listOf(exercise)
        )
        workoutsFlow.value = listOf(workoutWithExercises)

        // When
        val state = viewModel.weeklyVolumeUiState.first()

        // Then
        assertTrue(state is WeeklyVolumeUiState.Success)
        state as WeeklyVolumeUiState.Success
        // 500 kg * 2.20462262 = ~1102.3 lbs
        assertEquals(1102.3f, state.chartData[0], 1.0f) // Monday
        assertEquals(WeightUnit.LB, state.weightUnit)
    }

    @Test
    fun `weeklyVolumeUiState aggregates multiple workouts on same day`() = runTest {
        // Given - Two workouts on Wednesday
        val wednesdayTime = getTimeForDayOfWeek(Calendar.WEDNESDAY)
        val workout1 = Workout(id = 1L, name = "Morning", startTime = wednesdayTime, durationSeconds = 3600)
        val workout2 = Workout(id = 2L, name = "Evening", startTime = wednesdayTime + 3600000, durationSeconds = 3600)
        
        val exercise1 = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true)) // 500 kg
        )
        val exercise2 = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 2L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 80.0, reps = 10, isDone = true)) // 800 kg
        )
        
        workoutsFlow.value = listOf(
            WorkoutWithExercises(workout1, listOf(exercise1)),
            WorkoutWithExercises(workout2, listOf(exercise2))
        )

        // When
        val state = viewModel.weeklyVolumeUiState.first()

        // Then
        assertTrue(state is WeeklyVolumeUiState.Success)
        state as WeeklyVolumeUiState.Success
        assertEquals(1300.0f, state.chartData[2], 0.1f) // Wednesday - combined volume
    }

    @Test
    fun `weeklyVolumeUiState ignores sets with zero weight or reps`() = runTest {
        // Given - Workout with invalid sets
        val fridayTime = getTimeForDayOfWeek(Calendar.FRIDAY)
        val workout = Workout(id = 1L, name = "Test", startTime = fridayTime, durationSeconds = 3600)
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true),  // Valid: 500 kg
                WeightAndRepsSet(weight = 0.0, reps = 5, isDone = true),    // Invalid: 0 weight
                WeightAndRepsSet(weight = 80.0, reps = 0, isDone = true),   // Invalid: 0 reps
                WeightAndRepsSet(weight = 60.0, reps = 8, isDone = true)    // Valid: 480 kg
            )
        )
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(exercise)))

        // When
        val state = viewModel.weeklyVolumeUiState.first()

        // Then
        assertTrue(state is WeeklyVolumeUiState.Success)
        state as WeeklyVolumeUiState.Success
        assertEquals(980.0f, state.chartData[4], 0.1f) // Friday - only valid sets
    }

    @Test
    fun `weeklyVolumeUiState handles weekend workouts correctly`() = runTest {
        // Given - Saturday and Sunday workouts
        val saturdayTime = getTimeForDayOfWeek(Calendar.SATURDAY)
        val sundayTime = getTimeForDayOfWeek(Calendar.SUNDAY)
        
        val saturdayWorkout = Workout(id = 1L, name = "Saturday", startTime = saturdayTime, durationSeconds = 3600)
        val sundayWorkout = Workout(id = 2L, name = "Sunday", startTime = sundayTime, durationSeconds = 3600)
        
        val saturdayExercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 120.0, reps = 3, isDone = true)) // 360 kg
        )
        val sundayExercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 2L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 90.0, reps = 4, isDone = true)) // 360 kg
        )
        
        workoutsFlow.value = listOf(
            WorkoutWithExercises(saturdayWorkout, listOf(saturdayExercise)),
            WorkoutWithExercises(sundayWorkout, listOf(sundayExercise))
        )

        // When
        val state = viewModel.weeklyVolumeUiState.first()

        // Then
        assertTrue(state is WeeklyVolumeUiState.Success)
        state as WeeklyVolumeUiState.Success
        assertEquals(360.0f, state.chartData[5], 0.1f) // Saturday
        assertEquals(360.0f, state.chartData[6], 0.1f) // Sunday
    }

    @Test
    fun `weeklyVolumeUiState updates when weight unit changes`() = runTest {
        // Given - Initial workout data
        val mondayTime = getTimeForDayOfWeek(Calendar.MONDAY)
        val workout = Workout(id = 1L, name = "Test", startTime = mondayTime, durationSeconds = 3600)
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = UUID.randomUUID(),
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true)) // 500 kg
        )
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(exercise)))

        // Initial state in KG
        val initialState = viewModel.weeklyVolumeUiState.first()
        assertTrue(initialState is WeeklyVolumeUiState.Success)
        assertEquals(WeightUnit.KG, (initialState as WeeklyVolumeUiState.Success).weightUnit)
        assertEquals(500.0f, initialState.chartData[0], 0.1f)

        // When - Change to LB
        weightUnitFlow.value = WeightUnit.LB

        // Then - Volume should be converted to LB
        val updatedState = viewModel.weeklyVolumeUiState.first()
        assertTrue(updatedState is WeeklyVolumeUiState.Success)
        assertEquals(WeightUnit.LB, (updatedState as WeeklyVolumeUiState.Success).weightUnit)
        assertEquals(1102.3f, updatedState.chartData[0], 1.0f) // 500 kg * 2.20462262
    }


    @Test
    fun `personalRecordsUiState starts with Loading state`() = runTest {
        // Given & When - Initial state
        val initialState = viewModel.personalRecordsUiState.value

        // Then
        assertTrue(initialState is PersonalRecordsUiState.Loading)
    }

    @Test
    fun `personalRecordsUiState shows Success with empty data when no exercises`() = runTest {
        // Given
        exerciseCountsFlow.value = emptyList()
        workoutsFlow.value = emptyList()

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertTrue(state.lifts.isEmpty())
    }

    @Test
    fun `personalRecordsUiState filters only weighted exercises`() = runTest {
        // Given - Mix of weighted and non-weighted exercises
        val benchPressId = UUID.randomUUID()
        val plankId = UUID.randomUUID()
        
        exerciseCountsFlow.value = listOf(
            ExerciseDefinitionWithCount(
                ExerciseDefinition(
                    id = benchPressId,
                    name = "Bench Press",
                    equipment = ExerciseEquipment.BARBELL,
                    muscleGroups = listOf(MuscleGroup.CHEST),
                    logType = LogType.WEIGHT_AND_REPS
                ),
                workoutCount = 5
            ),
            ExerciseDefinitionWithCount(
                ExerciseDefinition(
                    id = plankId,
                    name = "Plank",
                    equipment = ExerciseEquipment.BODYWEIGHT,
                    muscleGroups = listOf(MuscleGroup.ABS),
                    logType = LogType.TIME // Not a weighted exercise
                ),
                workoutCount = 3
            )
        )

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val benchPressExercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = benchPressId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 100.0, reps = 8, isDone = true))
        )
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(benchPressExercise)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(1, state.lifts.size)
        assertEquals("Barbell Bench Press", state.lifts[0].exerciseName)
    }

    @Test
    fun `personalRecordsUiState calculates 1RM correctly using Brzycki formula`() = runTest {
        // Given
        val benchPressId = UUID.randomUUID()
        exerciseCountsFlow.value = listOf(
            ExerciseDefinitionWithCount(
                ExerciseDefinition(
                    id = benchPressId,
                    name = "Bench Press",
                    equipment = ExerciseEquipment.BARBELL,
                    muscleGroups = listOf(MuscleGroup.CHEST),
                    logType = LogType.WEIGHT_AND_REPS
                ),
                workoutCount = 1
            )
        )

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = benchPressId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true)) // 5 reps at 100kg
        )
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(exercise)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(1, state.lifts.size)
        val lift = state.lifts[0]
        assertEquals(100.0, lift.currentMax, 0.1)

        // Brzycki: 1RM = weight × 36 / (37 - reps); pin against the shared calculator.
        assertEquals(OneRepMaxCalculator.brzycki(100.0, 5), lift.oneRepMax, 0.1)
    }

    @Test
    fun `personalRecordsUiState clamps 1RM at very high rep counts instead of going negative`() =
        runTest {
            // Given - reps >= 37 made the inline Brzycki formula divide by zero / go
            // negative. Pin that the shared calculator's clamp (returns weight) wins.
            val pushUpId = UUID.randomUUID()
            exerciseCountsFlow.value = listOf(
                ExerciseDefinitionWithCount(
                    ExerciseDefinition(
                        id = pushUpId,
                        name = "Push Up",
                        equipment = ExerciseEquipment.BARBELL,
                        muscleGroups = listOf(MuscleGroup.CHEST),
                        logType = LogType.WEIGHT_AND_REPS
                    ),
                    workoutCount = 1
                )
            )
            val workout = Workout(
                id = 1L,
                name = "Test",
                startTime = System.currentTimeMillis(),
                durationSeconds = 3600
            )
            val exercise = WorkoutExercise(
                id = UUID.randomUUID(),
                workoutId = 1L,
                definitionId = pushUpId,
                unit = WeightUnit.KG,
                sets = mutableListOf(WeightAndRepsSet(weight = 50.0, reps = 40, isDone = true))
            )
            workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(exercise)))

            val state = viewModel.personalRecordsUiState.first()

            assertTrue(state is PersonalRecordsUiState.Success)
            state as PersonalRecordsUiState.Success
            assertEquals(1, state.lifts.size)
            assertTrue(
                "1RM must not go negative for reps >= 37",
                state.lifts[0].oneRepMax > 0.0
            )
            assertEquals(
                OneRepMaxCalculator.brzycki(50.0, 40),
                state.lifts[0].oneRepMax,
                0.1
            )
        }

    @Test
    fun `personalRecordsUiState handles 1RM sets correctly`() = runTest {
        // Given - 1 rep max set
        val squatId = UUID.randomUUID()
        exerciseCountsFlow.value = listOf(
            ExerciseDefinitionWithCount(
                ExerciseDefinition(
                    id = squatId,
                    name = "Squat",
                    equipment = ExerciseEquipment.BARBELL,
                    muscleGroups = listOf(MuscleGroup.LEGS),
                    logType = LogType.WEIGHT_AND_REPS
                ),
                workoutCount = 1
            )
        )

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = squatId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 150.0, reps = 1, isDone = true)) // 1RM
        )
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(exercise)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(1, state.lifts.size)
        val lift = state.lifts[0]
        assertEquals(150.0, lift.currentMax, 0.1)
        assertEquals(150.0, lift.oneRepMax, 0.1) // 1RM should equal current max
    }

    @Test
    fun `personalRecordsUiState finds maximum weight across multiple workouts`() = runTest {
        // Given - Multiple workouts with increasing weights
        val deadliftId = UUID.randomUUID()
        exerciseCountsFlow.value = listOf(
            ExerciseDefinitionWithCount(
                ExerciseDefinition(
                    id = deadliftId,
                    name = "Deadlift",
                    equipment = ExerciseEquipment.BARBELL,
                    muscleGroups = listOf(MuscleGroup.BACK),
                    logType = LogType.WEIGHT_AND_REPS
                ),
                workoutCount = 3
            )
        )

        val workout1 = Workout(id = 1L, name = "Week 1", startTime = System.currentTimeMillis() - 1209600000, durationSeconds = 3600) // 2 weeks ago
        val workout2 = Workout(id = 2L, name = "Week 2", startTime = System.currentTimeMillis() - 604800000, durationSeconds = 3600) // 1 week ago
        val workout3 = Workout(id = 3L, name = "Today", startTime = System.currentTimeMillis(), durationSeconds = 3600)

        val exercise1 = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = deadliftId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 120.0, reps = 5, isDone = true))
        )
        val exercise2 = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 2L,
            definitionId = deadliftId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 140.0, reps = 3, isDone = true)) // Higher 1RM
        )
        val exercise3 = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 3L,
            definitionId = deadliftId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 130.0, reps = 4, isDone = true))
        )

        workoutsFlow.value = listOf(
            WorkoutWithExercises(workout1, listOf(exercise1)),
            WorkoutWithExercises(workout2, listOf(exercise2)), // This should be the PR
            WorkoutWithExercises(workout3, listOf(exercise3))
        )

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(1, state.lifts.size)
        val lift = state.lifts[0]
        assertEquals(140.0, lift.currentMax, 0.1) // Should be the heaviest weight
        assertEquals(workout2.startTime, lift.lastPrDate) // Should be from workout2
    }

    @Test
    fun `personalRecordsUiState converts weights correctly for display unit`() = runTest {
        // Given - KG workout data, LB display preference
        weightUnitFlow.value = WeightUnit.LB
        
        val pressId = UUID.randomUUID()
        exerciseCountsFlow.value = listOf(
            ExerciseDefinitionWithCount(
                ExerciseDefinition(
                    id = pressId,
                    name = "Overhead Press",
                    equipment = ExerciseEquipment.BARBELL,
                    muscleGroups = listOf(MuscleGroup.SHOULDERS),
                    logType = LogType.WEIGHT_AND_REPS
                ),
                workoutCount = 1
            )
        )

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = pressId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 60.0, reps = 8, isDone = true))
        )
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(exercise)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(1, state.lifts.size)
        val lift = state.lifts[0]
        // 60 kg * 2.20462262 ≈ 132.3 lbs
        assertEquals(132.3, lift.currentMax, 1.0)
        assertEquals(WeightUnit.LB, lift.unit)
    }

    @Test
    fun `personalRecordsUiState sorts lifts by strongest first`() = runTest {
        // Given - Multiple exercises with different strength levels
        val benchId = UUID.randomUUID()
        val curlId = UUID.randomUUID()
        
        exerciseCountsFlow.value = listOf(
            ExerciseDefinitionWithCount(
                ExerciseDefinition(id = benchId, name = "Bench Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.CHEST), logType = LogType.WEIGHT_AND_REPS),
                workoutCount = 5
            ),
            ExerciseDefinitionWithCount(
                ExerciseDefinition(id = curlId, name = "Bicep Curl", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
                workoutCount = 3
            )
        )

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val benchExercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = benchId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 100.0, reps = 1, isDone = true)) // 1RM = 100kg
        )
        val curlExercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = curlId,
            unit = WeightUnit.KG,
            sets = mutableListOf(WeightAndRepsSet(weight = 40.0, reps = 1, isDone = true)) // 1RM = 40kg
        )
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(benchExercise, curlExercise)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(2, state.lifts.size)
        // Should be sorted by 1RM descending
        assertEquals("Barbell Bench Press", state.lifts[0].exerciseName)
        assertEquals(100.0, state.lifts[0].oneRepMax, 0.1)
        assertEquals("Barbell Bicep Curl", state.lifts[1].exerciseName)
        assertEquals(40.0, state.lifts[1].oneRepMax, 0.1)
    }

    @Test
    fun `personalRecordsUiState limits to top 6 exercises`() = runTest {
        // Given - 8 exercises, should only show top 6
        val exerciseIds = (1..8).map { UUID.randomUUID() }
        exerciseCountsFlow.value = exerciseIds.mapIndexed { index, id ->
            ExerciseDefinitionWithCount(
                ExerciseDefinition(
                    id = id,
                    name = "Exercise $index",
                    equipment = ExerciseEquipment.DUMBBELL,
                    muscleGroups = listOf(MuscleGroup.CHEST),
                    logType = LogType.WEIGHT_AND_REPS
                ),
                workoutCount = 10 - index // Higher count for earlier exercises
            )
        }

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val exercises = exerciseIds.map { id ->
            WorkoutExercise(
                id = UUID.randomUUID(),
                workoutId = 1L,
                definitionId = id,
                unit = WeightUnit.KG,
                sets = mutableListOf(WeightAndRepsSet(weight = 50.0, reps = 5, isDone = true))
            )
        }
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, exercises))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(6, state.lifts.size) // Should be limited to 6
    }

    @Test
    fun `personalRecordsUiState ignores invalid sets`() = runTest {
        // Given - Exercise with invalid sets (zero weight or reps)
        val squatId = UUID.randomUUID()
        exerciseCountsFlow.value = listOf(
            ExerciseDefinitionWithCount(
                ExerciseDefinition(id = squatId, name = "Squat", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
                workoutCount = 1
            )
        )

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val exercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = squatId,
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 0.0, reps = 5, isDone = true),    // Invalid: 0 weight
                WeightAndRepsSet(weight = 100.0, reps = 0, isDone = true),  // Invalid: 0 reps
                WeightAndRepsSet(weight = 120.0, reps = 3, isDone = true)   // Valid: should be used
            )
        )
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(exercise)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(1, state.lifts.size)
        val lift = state.lifts[0]
        assertEquals(120.0, lift.currentMax, 0.1) // Should use the valid set only
    }

    private fun getTimeForDayOfWeek(dayOfWeek: Int): Long {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, 10) // Set to 10 AM to avoid edge cases
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
