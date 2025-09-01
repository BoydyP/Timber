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
import java.util.UUID

@ExperimentalCoroutinesApi
class PersonalRecordsBugTest {

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
    fun `personalRecordsUiState does not show records for exercises with zero weights`() = runTest {
        // Given - Exercise definitions that exist in templates but workouts have zero weights
        val benchPressId = UUID.randomUUID()
        val squatId = UUID.randomUUID()
        
        exerciseCountsFlow.value = listOf(
            ExerciseDefinitionWithCount(
                ExerciseDefinition(
                    id = benchPressId,
                    name = "Bench Press",
                    equipment = ExerciseEquipment.BARBELL,
                    muscleGroups = listOf(MuscleGroup.CHEST),
                    logType = LogType.WEIGHT_AND_REPS
                ),
                workoutCount = 2 // Has workout count but no actual weights
            ),
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

        // Workouts exist but with zero weights (like template-based workouts that weren't filled in)
        val workout1 = Workout(id = 1L, name = "Test 1", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val workout2 = Workout(id = 2L, name = "Test 2", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        
        val benchExerciseWithZeroWeight = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = benchPressId,
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 0.0, reps = 5, isDone = true), // Zero weight
                WeightAndRepsSet(weight = 0.0, reps = 5, isDone = true)  // Zero weight
            )
        )
        
        val squatExerciseWithActualWeight = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 2L,
            definitionId = squatId,
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true) // Actual weight
            )
        )
        
        workoutsFlow.value = listOf(
            WorkoutWithExercises(workout1, listOf(benchExerciseWithZeroWeight)),
            WorkoutWithExercises(workout2, listOf(squatExerciseWithActualWeight))
        )

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then - Only squat should appear, not bench press
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(1, state.lifts.size)
        assertEquals("Barbell Squat", state.lifts[0].exerciseName)
        assertEquals(100.0, state.lifts[0].currentMax, 0.1)
    }

    @Test
    fun `personalRecordsUiState does not show records for exercises with only zero reps`() = runTest {
        // Given - Exercise with zero reps (incomplete sets)
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
                workoutCount = 1
            )
        )

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val deadliftExerciseWithZeroReps = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = deadliftId,
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 120.0, reps = 0, isDone = false), // Zero reps
                WeightAndRepsSet(weight = 100.0, reps = 0, isDone = false)  // Zero reps
            )
        )
        
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(deadliftExerciseWithZeroReps)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then - No records should appear
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertTrue(state.lifts.isEmpty())
    }

    @Test
    fun `personalRecordsUiState shows records only for exercises with valid weight and reps`() = runTest {
        // Given - Mix of valid and invalid sets
        val benchPressId = UUID.randomUUID()
        val squatId = UUID.randomUUID()
        
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
            ),
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
        
        // Bench press with invalid sets only
        val benchExerciseInvalid = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = benchPressId,
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 0.0, reps = 5, isDone = true),   // Invalid: zero weight
                WeightAndRepsSet(weight = 80.0, reps = 0, isDone = false)  // Invalid: zero reps
            )
        )
        
        // Squat with valid sets
        val squatExerciseValid = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = squatId,
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 0.0, reps = 5, isDone = true),    // Invalid: zero weight
                WeightAndRepsSet(weight = 120.0, reps = 5, isDone = true)   // Valid
            )
        )
        
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(benchExerciseInvalid, squatExerciseValid)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then - Only squat should appear
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertEquals(1, state.lifts.size)
        assertEquals("Barbell Squat", state.lifts[0].exerciseName)
        assertEquals(120.0, state.lifts[0].currentMax, 0.1)
    }

    @Test
    fun `personalRecordsUiState returns empty list when no exercises have valid sets`() = runTest {
        // Given - Exercises exist in database but no valid workout data
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
                workoutCount = 3 // Shows up in counts but no valid data
            )
        )

        val workout = Workout(id = 1L, name = "Test", startTime = System.currentTimeMillis(), durationSeconds = 3600)
        val benchExerciseAllInvalid = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1L,
            definitionId = benchPressId,
            unit = WeightUnit.KG,
            sets = mutableListOf(
                WeightAndRepsSet(weight = 0.0, reps = 5, isDone = true),   // Invalid
                WeightAndRepsSet(weight = 80.0, reps = 0, isDone = false), // Invalid
                WeightAndRepsSet(weight = 0.0, reps = 0, isDone = false)   // Invalid
            )
        )
        
        workoutsFlow.value = listOf(WorkoutWithExercises(workout, listOf(benchExerciseAllInvalid)))

        // When
        val state = viewModel.personalRecordsUiState.first()

        // Then - No records should appear
        assertTrue(state is PersonalRecordsUiState.Success)
        state as PersonalRecordsUiState.Success
        assertTrue(state.lifts.isEmpty())
    }
}
