package com.android.timberworkoutlogs.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "workout_templates")
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String
) {
    @Ignore
    var templateExercises: List<TemplateExercise> = mutableListOf()
}
