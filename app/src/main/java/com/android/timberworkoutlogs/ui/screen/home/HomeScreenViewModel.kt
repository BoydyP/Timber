package com.android.timberworkoutlogs.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.services.WorkoutStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

private const val LBS_TO_KG_FACTOR = 0.45359237
private const val KG_TO_LBS_FACTOR = 2.20462262

sealed interface WeeklyVolumeUiState {
    object Loading : WeeklyVolumeUiState
    data class Success(val chartData: List<Float>, val weightUnit: WeightUnit) : WeeklyVolumeUiState
    data class Error(val message: String) : WeeklyVolumeUiState
}

sealed interface PersonalRecordsUiState {
    object Loading : PersonalRecordsUiState
    data class Success(val lifts: List<ExerciseLift>) : PersonalRecordsUiState
    data class Error(val message: String) : PersonalRecordsUiState
}

data class ExerciseLift(
    val exerciseName: String,
    val currentMax: Double,
    val unit: WeightUnit,
    val lastPrDate: Long?,
    val oneRepMax: Double
)

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    workoutStateHolder: WorkoutStateHolder,
    private val workoutDao: WorkoutDao,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val isWorkoutInProgress: StateFlow<Boolean> = workoutStateHolder.isTimerRunning

    val personalRecordsUiState: StateFlow<PersonalRecordsUiState> = 
        combine(
            workoutDao.getExerciseDefinitionsWithWorkoutCounts(),
            workoutDao.getWorkoutsWithExercisesFrom(0), // All time data for max lifts
            settingsRepository.weightUnit
        ) { exerciseCounts, allWorkouts, weightUnit ->
            try {
                calculateTopExerciseLifts(exerciseCounts, allWorkouts, weightUnit)
            } catch (e: Exception) {
                PersonalRecordsUiState.Error("Failed to load exercise records: ${e.localizedMessage ?: "Unknown error"}")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PersonalRecordsUiState.Loading
        )

    val weeklyVolumeUiState: StateFlow<WeeklyVolumeUiState> = 
        combine(
            getWeeklyWorkoutsFlow(),
            settingsRepository.weightUnit
        ) { workoutsWithExercises, weightUnit ->
            try {
                val dailyVolumes = DoubleArray(7)

                for (item in workoutsWithExercises) {
                    val workout = item.workout
                    val workoutCal = Calendar.getInstance().apply {
                        timeInMillis = workout.startTime
                        firstDayOfWeek = Calendar.MONDAY
                    }

                    val dayOfWeek = workoutCal.get(Calendar.DAY_OF_WEEK)
                    val dayIndex = when (dayOfWeek) {
                        Calendar.MONDAY -> 0
                        Calendar.TUESDAY -> 1
                        Calendar.WEDNESDAY -> 2
                        Calendar.THURSDAY -> 3
                        Calendar.FRIDAY -> 4
                        Calendar.SATURDAY -> 5
                        Calendar.SUNDAY -> 6
                        else -> -1
                    }

                    if (dayIndex in 0..6) {
                        var workoutTotalVolume = 0.0
                        item.exercises.forEach { exercise ->
                            exercise.sets.forEach { set ->
                                if (set is WeightAndRepsSet) {
                                    // First convert to kg for calculation
                                    val weightInKg = if (exercise.unit == WeightUnit.LB) {
                                        set.weight * LBS_TO_KG_FACTOR
                                    } else {
                                        set.weight
                                    }
                                    if (set.reps > 0 && weightInKg > 0) {
                                        workoutTotalVolume += set.reps * weightInKg
                                    }
                                }
                            }
                        }
                        dailyVolumes[dayIndex] += workoutTotalVolume
                    }
                }

                // Convert final volumes to user's preferred unit
                val chartYValues = dailyVolumes.map { volumeInKg ->
                    val finalVolume = if (weightUnit == WeightUnit.LB) {
                        volumeInKg * KG_TO_LBS_FACTOR
                    } else {
                        volumeInKg
                    }
                    finalVolume.toFloat()
                }

                WeeklyVolumeUiState.Success(chartYValues, weightUnit)
            } catch (e: Exception) {
                WeeklyVolumeUiState.Error("Failed to load weekly volume: ${e.localizedMessage ?: "Unknown error"}")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyVolumeUiState.Loading
        )

    private fun getWeeklyWorkoutsFlow() = workoutDao.getWorkoutsWithExercisesFrom(getWeekStartMillis())

    private fun getWeekStartMillis(): Long {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            setToStartOfDay()
        }.timeInMillis
    }

    private fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun calculateTopExerciseLifts(
        exerciseCounts: List<com.android.timberworkoutlogs.database.ExerciseDefinitionWithCount>,
        allWorkouts: List<com.android.timberworkoutlogs.database.WorkoutWithExercises>,
        weightUnit: WeightUnit
    ): PersonalRecordsUiState {
        if (exerciseCounts.isEmpty() || allWorkouts.isEmpty()) {
            return PersonalRecordsUiState.Success(emptyList())
        }

        // Get top 6 most performed exercises that use weights
        val topExercises = exerciseCounts
            .filter { it.exerciseDefinition.logType.name.contains("WEIGHT") }
            .take(6)

        val exerciseMaxes = mutableMapOf<String, ExerciseMaxData>()

        // Process all workouts to find max lifts for top exercises
        for (workoutWithExercises in allWorkouts) {
            for (exercise in workoutWithExercises.exercises) {
                // Find matching exercise definition
                val exerciseDefinition = topExercises.find { it.exerciseDefinition.id == exercise.definitionId }?.exerciseDefinition
                
                if (exerciseDefinition != null) {
                    val exerciseName = "${exerciseDefinition.equipment.name.lowercase().replaceFirstChar { it.uppercase() }} ${exerciseDefinition.name}"
                    
                    for (set in exercise.sets) {
                        if (set is WeightAndRepsSet && set.weight > 0 && set.reps > 0) {
                            // Convert weight to consistent unit (KG) for comparison
                            val weightInKg = if (exercise.unit == WeightUnit.LB) {
                                set.weight * LBS_TO_KG_FACTOR
                            } else {
                                set.weight
                            }

                            // Calculate estimated 1RM using Brzycki formula
                            val oneRepMax = if (set.reps == 1) {
                                weightInKg
                            } else {
                                weightInKg / (1.0278 - 0.0278 * set.reps)
                            }

                            val currentMax = exerciseMaxes[exerciseName]
                            if (currentMax == null || oneRepMax > currentMax.oneRepMaxKg) {
                                exerciseMaxes[exerciseName] = ExerciseMaxData(
                                    maxWeightKg = weightInKg,
                                    oneRepMaxKg = oneRepMax,
                                    achievedDate = workoutWithExercises.workout.startTime
                                )
                            }
                        }
                    }
                }
            }
        }

        // Convert to ExerciseLift objects
        val lifts = exerciseMaxes.map { (exerciseName, maxData) ->
            val displayWeight = if (weightUnit == WeightUnit.LB) {
                maxData.maxWeightKg * KG_TO_LBS_FACTOR
            } else {
                maxData.maxWeightKg
            }

            val displayOneRepMax = if (weightUnit == WeightUnit.LB) {
                maxData.oneRepMaxKg * KG_TO_LBS_FACTOR
            } else {
                maxData.oneRepMaxKg
            }

            ExerciseLift(
                exerciseName = exerciseName,
                currentMax = displayWeight,
                unit = weightUnit,
                lastPrDate = maxData.achievedDate,
                oneRepMax = displayOneRepMax
            )
        }.sortedByDescending { it.oneRepMax } // Sort by strongest lifts first

        return PersonalRecordsUiState.Success(lifts)
    }

    private data class ExerciseMaxData(
        val maxWeightKg: Double,
        val oneRepMaxKg: Double,
        val achievedDate: Long
    )
}
