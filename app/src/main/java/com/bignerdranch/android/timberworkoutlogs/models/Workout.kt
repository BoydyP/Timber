package com.bignerdranch.android.timberworkoutlogs.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Represents a single logged workout session.
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String = "New Workout",
    val startTime: Long = System.currentTimeMillis(),
    var durationSeconds: Int = 0,
    val notes: String = ""
) {
    @Ignore
    var workoutExercises: MutableList<WorkoutExercise> = mutableListOf()
}
