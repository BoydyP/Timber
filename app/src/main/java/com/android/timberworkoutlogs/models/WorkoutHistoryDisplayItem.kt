package com.android.timberworkoutlogs.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkoutHistoryDisplayItem(
    val workout: Workout,
    val exerciseCount: Int,
    val totalDistance: Double,
    val totalWeightLiftedInKg: Double,
    val systemWeightUnit: WeightUnit
) {
    val formattedStartTime: String
        get() {
            val sdf = SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            return sdf.format(Date(workout.startTime))
        }
    val durationInHms: String
        get() {
            if (workout.durationSeconds < 0) return "00:00"
            val hours = workout.durationSeconds / 3600
            val minutes = (workout.durationSeconds % 3600) / 60
            val seconds = workout.durationSeconds % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    companion object {
        /**
         * Builds a display item from a workout and its exercises, aggregating total weight
         * (converted to kg regardless of each exercise's logged unit) and total distance.
         */
        fun from(
            workout: Workout,
            exercises: List<WorkoutExercise>,
            systemWeightUnit: WeightUnit
        ): WorkoutHistoryDisplayItem {
            var totalWeightInKg = 0.0
            var totalDistance = 0.0
            exercises.forEach { exercise ->
                exercise.sets.forEach { set ->
                    when (set) {
                        is WeightAndRepsSet -> {
                            val weightInKg = if (exercise.unit == WeightUnit.LB) {
                                set.weight * 0.453592
                            } else {
                                set.weight
                            }
                            totalWeightInKg += weightInKg * set.reps
                        }

                        is DistanceAndTimeSet -> totalDistance += set.distance
                        else -> {}
                    }
                }
            }
            return WorkoutHistoryDisplayItem(
                workout = workout,
                exerciseCount = exercises.size,
                totalWeightLiftedInKg = totalWeightInKg,
                totalDistance = totalDistance,
                systemWeightUnit = systemWeightUnit
            )
        }
    }
}
