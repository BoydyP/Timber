package com.android.timberworkoutlogs.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkoutHistoryDisplayItem(
    val workout: Workout,
    val exerciseCount: Int,
    val totalWeightLifted: Double
) {
    val formattedStartTime: String
        get() {
            val sdf = SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            return sdf.format(Date(workout.startTime))
        }
}
