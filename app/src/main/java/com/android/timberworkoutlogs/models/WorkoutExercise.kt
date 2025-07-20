package com.android.timberworkoutlogs.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
    @field:PrimaryKey val id: UUID = UUID.randomUUID(),
    val workoutId: Long,
    val definitionId: UUID,
    val unit: WeightUnit = WeightUnit.KG,
    val sets: List<ExerciseSet> = listOf()
)
