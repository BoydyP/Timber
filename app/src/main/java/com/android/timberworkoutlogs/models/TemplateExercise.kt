package com.android.timberworkoutlogs.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseDefinition::class,
            parentColumns = ["id"],
            childColumns = ["definitionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["templateId"]), Index(value = ["definitionId"])]
)
data class TemplateExercise(
    @field:PrimaryKey val id: UUID = UUID.randomUUID(),
    val templateId: Long,
    val definitionId: UUID,
    val sets: List<ExerciseSet> = listOf()
)
