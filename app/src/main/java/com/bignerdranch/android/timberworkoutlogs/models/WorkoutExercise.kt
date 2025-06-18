package com.bignerdranch.android.timberworkoutlogs.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bignerdranch.android.timberworkoutlogs.database.converters.ExerciseSetListConverter
import java.util.UUID

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutExercise(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val workoutId: Long,
    val name: String = "",
    val unit: WeightUnit = WeightUnit.KG,
    @TypeConverters(ExerciseSetListConverter::class)
    val sets: List<ExerciseSet> = listOf()
    // TODO: Consider how to link back to ExerciseDefinition
)
