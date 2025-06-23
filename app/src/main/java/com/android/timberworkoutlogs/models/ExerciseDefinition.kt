package com.android.timberworkoutlogs.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.timberworkoutlogs.util.capitaliseEnum
import java.util.UUID

/**
 * Represents the definition of an exercise in the master list.
 */
@Entity(tableName = "exercise_definitions")
data class ExerciseDefinition(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val equipment: ExerciseEquipment,
    val muscleGroups: List<MuscleGroup>,
    val logType: LogType
) {
    /**
     * A computed property to get the full, formatted name of the exercise.
     * This is what will be displayed in the UI. It is not stored in the database.
     */
    val computedExerciseName: String
        get() = "${capitaliseEnum(equipment.name)} $name"
}
