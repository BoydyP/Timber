package com.android.timberworkoutlogs.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkoutHistoryDisplayItem(
    val workout: Workout,
    val exerciseCount: Int,
    val totalWeightLifted: Double,
    val totalDistance: Double
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
}
