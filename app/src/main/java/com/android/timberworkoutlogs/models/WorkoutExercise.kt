package com.android.timberworkoutlogs.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.android.timberworkoutlogs.database.converters.ExerciseSetListConverter
import java.util.UUID

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseDefinition::class,
            parentColumns = ["id"],
            childColumns = ["definitionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["definitionId"])
    ]
)
data class WorkoutExercise(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val workoutId: Long,
    val definitionId: UUID,
    val unit: WeightUnit = WeightUnit.KG,
    @TypeConverters(ExerciseSetListConverter::class)
    val sets: List<ExerciseSet> = listOf()
)
