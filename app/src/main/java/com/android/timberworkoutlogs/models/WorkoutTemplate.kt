package com.android.timberworkoutlogs.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Represents a single logged workout session.
 */
@Entity(tableName = "workout_templates")
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String = "Workout",
    var exercises: List<ExerciseDefinition> = emptyList(),
    val notes: String = ""
) {
    @Ignore
    var workoutExercises: MutableList<WorkoutExercise> = mutableListOf()
}
