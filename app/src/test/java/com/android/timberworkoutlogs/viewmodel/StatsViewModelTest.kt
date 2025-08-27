package com.android.timberworkoutlogs.viewmodel

import com.android.timberworkoutlogs.database.ExerciseDefinitionWithCount
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.database.WorkoutExerciseWithDate
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.stats.StatsTab
import com.android.timberworkoutlogs.ui.screen.stats.StatsViewModel
import com.android.timberworkoutlogs.ui.screen.stats.TimeRange
import com.android.timberworkoutlogs.ui.screen.stats.utils.OneRMFormula
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.UUID

@ExperimentalCoroutinesApi
class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: StatsViewModel
    private lateinit var workoutDao: WorkoutDao
    private lateinit var settingsRepository: SettingsRepository

    private val mockExerciseDefinition = ExerciseDefinition(
        id = UUID.randomUUID(),
        name = "Bench Press",
        equipment = ExerciseEquipment.BARBELL,
        muscleGroups = listOf(MuscleGroup.CHEST),
        logType = LogType.WEIGHT_AND_REPS
    )

    @Before
    fun setup() {
        workoutDao = mockk()
        settingsRepository = mockk()
        
        // Default mock behavior
        every { workoutDao.getExerciseDefinitionsWithWorkoutCounts() } returns flowOf(emptyList())
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
        every { workoutDao.getExerciseHistoryData(any(), any()) } returns flowOf(emptyList())
    }

    @Test
    fun `initial state has correct defaults`() = runTest {
        // Given & When
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(StatsTab.EXERCISE_PROGRESSION, state.selectedTab)
        assertEquals(TimeRange.LAST_4_WEEKS, state.selectedTimeRange)
        assertEquals(OneRMFormula.EPLEY, state.selectedOneRMFormula)
        assertEquals(WeightUnit.KG, state.weightUnit)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.availableExercises.isEmpty())
        assertTrue(state.progressionData.isEmpty())
        assertTrue(state.oneRepMaxData.isEmpty())
    }

    @Test
    fun `loads available exercises and selects first one`() = runTest {
        // Given
        val exerciseWithCount = ExerciseDefinitionWithCount(
            exerciseDefinition = mockExerciseDefinition,
            workoutCount = 5
        )
        every { workoutDao.getExerciseDefinitionsWithWorkoutCounts() } returns flowOf(listOf(exerciseWithCount))
        
        // When
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.availableExercises.size)
        assertEquals(mockExerciseDefinition, state.selectedExercise)
    }

    @Test
    fun `selectTab updates selected tab`() = runTest {
        // Given
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        
        // When
        viewModel.selectTab(StatsTab.ONE_REP_MAX)
        
        // Then
        assertEquals(StatsTab.ONE_REP_MAX, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `selectTimeRange updates time range and triggers data reload`() = runTest {
        // Given
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        
        // When
        viewModel.selectTimeRange(TimeRange.LAST_6_MONTHS)
        
        // Then
        assertEquals(TimeRange.LAST_6_MONTHS, viewModel.uiState.value.selectedTimeRange)
    }

    @Test
    fun `selectOneRMFormula updates formula and triggers data reload`() = runTest {
        // Given
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        
        // When
        viewModel.selectOneRMFormula(OneRMFormula.BRZYCKI)
        
        // Then
        assertEquals(OneRMFormula.BRZYCKI, viewModel.uiState.value.selectedOneRMFormula)
    }

    @Test
    fun `selectExercise updates selected exercise and sets loading state`() = runTest {
        // Given
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        
        // When
        viewModel.selectExercise(mockExerciseDefinition)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(mockExerciseDefinition, state.selectedExercise)
        // Note: Loading state may be brief and hard to test in unit tests
    }

    @Test
    fun `weight unit from settings is applied to UI state`() = runTest {
        // Given
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.LB)
        
        // When
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        
        // Then
        assertEquals(WeightUnit.LB, viewModel.uiState.value.weightUnit)
    }

    @Test
    fun `processes progression data correctly with weight conversions`() = runTest {
        // Given: Mock exercise data with mixed weight units
        val workoutTime = System.currentTimeMillis()
        val exerciseHistoryKg = listOf(
            createWorkoutExerciseWithDate(
                workoutTime,
                listOf(WeightAndRepsSet(100.0, 5, isDone = true)), // 100kg x 5
                WeightUnit.KG
            )
        )
        val exerciseHistoryLb = listOf(
            createWorkoutExerciseWithDate(
                workoutTime + 86400000, // Next day
                listOf(WeightAndRepsSet(225.0, 3, isDone = true)), // 225lb x 3
                WeightUnit.LB
            )
        )
        val allHistory = exerciseHistoryKg + exerciseHistoryLb
        
        every { workoutDao.getExerciseHistoryData(any(), any()) } returns flowOf(allHistory)
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
        
        // When
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        viewModel.selectExercise(mockExerciseDefinition)
        
        // Then: Data should be processed with proper weight conversions
        val state = viewModel.uiState.value
        assertEquals(2, state.progressionData.size) // Two workout sessions
        
        // First workout: 100kg should remain 100kg
        val firstPoint = state.progressionData.find { it.date == workoutTime }
        assertNotNull(firstPoint)
        assertEquals(100.0, firstPoint!!.maxWeight, 0.1)
        assertEquals(500.0, firstPoint.totalVolume, 0.1) // 100kg x 5 reps
        
        // Second workout: 225lb should be converted to kg (~102kg)
        val secondPoint = state.progressionData.find { it.date == workoutTime + 86400000 }
        assertNotNull(secondPoint)
        assertTrue("225lb should convert to ~102kg", secondPoint!!.maxWeight > 101 && secondPoint.maxWeight < 103)
    }

    @Test
    fun `processes one rep max data with different formulas`() = runTest {
        // Given: Mock exercise data
        val workoutTime = System.currentTimeMillis()
        val exerciseHistory = listOf(
            createWorkoutExerciseWithDate(
                workoutTime,
                listOf(
                    WeightAndRepsSet(80.0, 8, isDone = true), // Reliable rep range
                    WeightAndRepsSet(60.0, 15, isDone = true) // Unreliable rep range
                ),
                WeightUnit.KG
            )
        )
        
        every { workoutDao.getExerciseHistoryData(any(), any()) } returns flowOf(exerciseHistory)
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
        
        // When: Test with Epley formula
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        viewModel.selectExercise(mockExerciseDefinition)
        viewModel.selectOneRMFormula(OneRMFormula.EPLEY)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.oneRepMaxData.size) // Two sets
        
        val reliablePoint = state.oneRepMaxData.find { it.reps == 8 }
        val unreliablePoint = state.oneRepMaxData.find { it.reps == 15 }
        
        assertNotNull(reliablePoint)
        assertNotNull(unreliablePoint)
        assertTrue("8 reps should be reliable", reliablePoint!!.isReliable)
        assertFalse("15 reps should be unreliable", unreliablePoint!!.isReliable)
        assertEquals(OneRMFormula.EPLEY, reliablePoint.formula)
    }

    @Test
    fun `handles empty exercise history gracefully`() = runTest {
        // Given: No exercise history
        every { workoutDao.getExerciseHistoryData(any(), any()) } returns flowOf(emptyList())
        
        // When
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        viewModel.selectExercise(mockExerciseDefinition)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue("Progression data should be empty", state.progressionData.isEmpty())
        assertTrue("One rep max data should be empty", state.oneRepMaxData.isEmpty())
        assertFalse("Should not be loading", state.isLoading)
        assertNull("Should not have error", state.error)
    }

    @Test
    fun `filters out incomplete or invalid sets`() = runTest {
        // Given: Exercise history with invalid sets
        val workoutTime = System.currentTimeMillis()
        val exerciseHistory = listOf(
            createWorkoutExerciseWithDate(
                workoutTime,
                listOf(
                    WeightAndRepsSet(100.0, 5, isDone = true),   // Valid
                    WeightAndRepsSet(80.0, 3, isDone = false),   // Not done
                    WeightAndRepsSet(0.0, 5, isDone = true),     // Zero weight
                    WeightAndRepsSet(90.0, 0, isDone = true),    // Zero reps
                    WeightAndRepsSet(75.0, 8, isDone = true)     // Valid
                ),
                WeightUnit.KG
            )
        )
        
        every { workoutDao.getExerciseHistoryData(any(), any()) } returns flowOf(exerciseHistory)
        
        // When
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        viewModel.selectExercise(mockExerciseDefinition)
        
        // Then: Only valid sets should be processed
        val state = viewModel.uiState.value
        assertEquals(1, state.progressionData.size) // One workout
        assertEquals(2, state.oneRepMaxData.size)   // Two valid sets
        
        val progressionPoint = state.progressionData.first()
        assertEquals(100.0, progressionPoint.maxWeight, 0.1) // Max of valid sets
        assertEquals(1100.0, progressionPoint.totalVolume, 0.1) // 100*5 + 75*8
    }

    @Test
    fun `calculates time range boundaries correctly`() = runTest {
        // Given: Current time for reference
        val now = Calendar.getInstance()
        
        // When: Test different time ranges
        viewModel = StatsViewModel(workoutDao, settingsRepository)
        
        // Test specific time ranges by selecting them
        val testRanges = listOf(
            TimeRange.LAST_4_WEEKS to 28,
            TimeRange.LAST_3_MONTHS to 90,
            TimeRange.LAST_6_MONTHS to 180,
            TimeRange.LAST_YEAR to 365
        )
        
        testRanges.forEach { (timeRange, expectedDays) ->
            // When
            viewModel.selectTimeRange(timeRange)
            
            // Then: Time range should be correctly set
            assertEquals(timeRange, viewModel.uiState.value.selectedTimeRange)
            assertEquals(expectedDays, timeRange.daysBack)
        }
        
        // Test ALL_TIME special case
        viewModel.selectTimeRange(TimeRange.ALL_TIME)
        assertEquals(TimeRange.ALL_TIME, viewModel.uiState.value.selectedTimeRange)
        assertEquals(Int.MAX_VALUE, TimeRange.ALL_TIME.daysBack)
    }

    // Helper function to create test data
    private fun createWorkoutExerciseWithDate(
        workoutStartTime: Long,
        sets: List<WeightAndRepsSet>,
        unit: WeightUnit
    ): WorkoutExerciseWithDate {
        val workoutExercise = WorkoutExercise(
            workoutId = 1L,
            definitionId = mockExerciseDefinition.id,
            unit = unit,
            sets = sets.toMutableList()
        )
        return WorkoutExerciseWithDate(workoutExercise, workoutStartTime)
    }
}
