package com.android.timberworkoutlogs.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionWithCount
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.database.WorkoutExerciseWithDate
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.ui.screen.stats.utils.OneRepMaxCalculator
import com.android.timberworkoutlogs.ui.screen.stats.utils.OneRMFormula
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar
import javax.inject.Inject

private const val LBS_TO_KG_FACTOR = 0.45359237
private const val KG_TO_LBS_FACTOR = 2.20462262

enum class StatsTab {
    EXERCISE_PROGRESSION,
    ONE_REP_MAX,
    VOLUME_STATS
}

enum class TimeRange(val displayName: String, val daysBack: Int) {
    LAST_4_WEEKS("Last 4 weeks", 28),
    LAST_3_MONTHS("Last 3 months", 90),
    LAST_6_MONTHS("Last 6 months", 180),
    LAST_YEAR("Last year", 365),
    ALL_TIME("All time", Int.MAX_VALUE)
}

data class ExerciseProgressionPoint(
    val date: Long,
    val maxWeight: Double,
    val totalVolume: Double,
    val bestSet: WeightAndRepsSet,
    val workoutDate: Long
)

data class OneRepMaxPoint(
    val date: Long,
    val estimatedOneRM: Double,
    val actualWeight: Double,
    val reps: Int,
    val formula: OneRMFormula,
    val isReliable: Boolean
)

data class StatsUiState(
    val selectedTab: StatsTab = StatsTab.EXERCISE_PROGRESSION,
    val selectedExercise: ExerciseDefinition? = null,
    val selectedTimeRange: TimeRange = TimeRange.LAST_4_WEEKS,
    val selectedOneRMFormula: OneRMFormula = OneRMFormula.EPLEY,
    val availableExercises: List<ExerciseDefinitionWithCount> = emptyList(),
    val progressionData: List<ExerciseProgressionPoint> = emptyList(),
    val oneRepMaxData: List<OneRepMaxPoint> = emptyList(),
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        // Load available exercises and weight unit
        combine(
            workoutDao.getExerciseDefinitionsWithWorkoutCounts(),
            settingsRepository.weightUnit
        ) { exercises, weightUnit ->
            _uiState.value = _uiState.value.copy(
                availableExercises = exercises,
                weightUnit = weightUnit,
                selectedExercise = exercises.firstOrNull()?.exerciseDefinition
            )
            
            // Load data for the first exercise if available
            exercises.firstOrNull()?.exerciseDefinition?.let { exerciseDefinition ->
                loadExerciseData(exerciseDefinition)
            }
        }.launchIn(viewModelScope)
    }

    fun selectTab(tab: StatsTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectExercise(exerciseDefinition: ExerciseDefinition) {
        _uiState.value = _uiState.value.copy(
            selectedExercise = exerciseDefinition,
            isLoading = true,
            error = null
        )
        loadExerciseData(exerciseDefinition)
    }

    fun selectTimeRange(timeRange: TimeRange) {
        _uiState.value = _uiState.value.copy(
            selectedTimeRange = timeRange,
            isLoading = true
        )
        _uiState.value.selectedExercise?.let { exerciseDefinition ->
            loadExerciseData(exerciseDefinition)
        }
    }

    fun selectOneRMFormula(formula: OneRMFormula) {
        _uiState.value = _uiState.value.copy(selectedOneRMFormula = formula)
        // Recalculate one rep max data with new formula
        _uiState.value.selectedExercise?.let { exerciseDefinition ->
            loadExerciseData(exerciseDefinition)
        }
    }

    private fun loadExerciseData(exerciseDefinition: ExerciseDefinition) {
        val fromTime = getTimeRangeStartMillis(_uiState.value.selectedTimeRange)
        
        workoutDao.getExerciseHistoryData(exerciseDefinition.id, fromTime)
            .onEach { exerciseHistory ->
                try {
                    val progressionData = processProgressionData(exerciseHistory)
                    val oneRepMaxData = processOneRepMaxData(exerciseHistory)
                    
                    _uiState.value = _uiState.value.copy(
                        progressionData = progressionData,
                        oneRepMaxData = oneRepMaxData,
                        isLoading = false,
                        error = null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load exercise data: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processProgressionData(exerciseHistory: List<WorkoutExerciseWithDate>): List<ExerciseProgressionPoint> {
        val weightUnit = _uiState.value.weightUnit
        
        return exerciseHistory
            .groupBy { it.workoutStartTime }
            .map { (workoutDate, exercisesInWorkout) ->
                var maxWeight = 0.0
                var totalVolume = 0.0
                var bestSet: WeightAndRepsSet? = null

                exercisesInWorkout.forEach { exerciseWithDate ->
                    exerciseWithDate.workoutExercise.sets.forEach { set ->
                        if (set is WeightAndRepsSet && set.isDone && set.reps > 0 && set.weight > 0) {
                            // Convert weight to kg for calculations
                            val weightInKg = if (exerciseWithDate.workoutExercise.unit == WeightUnit.LB) {
                                set.weight * LBS_TO_KG_FACTOR
                            } else {
                                set.weight
                            }

                            // Track max weight
                            if (weightInKg > maxWeight) {
                                maxWeight = weightInKg
                                bestSet = set
                            }

                            // Add to volume
                            totalVolume += weightInKg * set.reps
                        }
                    }
                }

                // Convert final values to user's preferred unit
                val finalMaxWeight = if (weightUnit == WeightUnit.LB) {
                    maxWeight * KG_TO_LBS_FACTOR
                } else {
                    maxWeight
                }
                
                val finalTotalVolume = if (weightUnit == WeightUnit.LB) {
                    totalVolume * KG_TO_LBS_FACTOR
                } else {
                    totalVolume
                }

                ExerciseProgressionPoint(
                    date = workoutDate,
                    maxWeight = finalMaxWeight,
                    totalVolume = finalTotalVolume,
                    bestSet = bestSet ?: WeightAndRepsSet(),
                    workoutDate = workoutDate
                )
            }
            .filter { it.maxWeight > 0 }
            .sortedBy { it.date }
    }

    private fun processOneRepMaxData(exerciseHistory: List<WorkoutExerciseWithDate>): List<OneRepMaxPoint> {
        val selectedFormula = _uiState.value.selectedOneRMFormula
        val weightUnit = _uiState.value.weightUnit
        
        return exerciseHistory.flatMap { exerciseWithDate ->
            exerciseWithDate.workoutExercise.sets.mapNotNull { set ->
                if (set is WeightAndRepsSet && set.isDone && set.reps > 0 && set.weight > 0) {
                    // Convert to kg for calculation
                    val weightInKg = if (exerciseWithDate.workoutExercise.unit == WeightUnit.LB) {
                        set.weight * LBS_TO_KG_FACTOR
                    } else {
                        set.weight
                    }

                    val oneRMInKg = when (selectedFormula) {
                        OneRMFormula.EPLEY -> OneRepMaxCalculator.epley(weightInKg, set.reps)
                        OneRMFormula.BRZYCKI -> OneRepMaxCalculator.brzycki(weightInKg, set.reps)
                        OneRMFormula.LOMBARDI -> OneRepMaxCalculator.lombardi(weightInKg, set.reps)
                        OneRMFormula.AVERAGE -> {
                            val estimates = OneRepMaxCalculator.calculateAll(weightInKg, set.reps)
                            estimates.average
                        }
                    }

                    // Convert final 1RM to user's preferred unit
                    val finalOneRM = if (weightUnit == WeightUnit.LB) {
                        oneRMInKg * KG_TO_LBS_FACTOR
                    } else {
                        oneRMInKg
                    }

                    val finalActualWeight = if (weightUnit == WeightUnit.LB && exerciseWithDate.workoutExercise.unit == WeightUnit.KG) {
                        set.weight * KG_TO_LBS_FACTOR
                    } else if (weightUnit == WeightUnit.KG && exerciseWithDate.workoutExercise.unit == WeightUnit.LB) {
                        set.weight * LBS_TO_KG_FACTOR
                    } else {
                        set.weight
                    }

                    OneRepMaxPoint(
                        date = exerciseWithDate.workoutStartTime,
                        estimatedOneRM = finalOneRM,
                        actualWeight = finalActualWeight,
                        reps = set.reps,
                        formula = selectedFormula,
                        isReliable = OneRepMaxCalculator.isReliableRepRange(set.reps)
                    )
                } else null
            }
        }.sortedBy { it.date }
    }

    private fun getTimeRangeStartMillis(timeRange: TimeRange): Long {
        if (timeRange == TimeRange.ALL_TIME) return 0L
        
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -timeRange.daysBack)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
