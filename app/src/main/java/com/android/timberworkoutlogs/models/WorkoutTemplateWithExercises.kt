package com.android.timberworkoutlogs.models

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutTemplateWithExercises(
    @Embedded val template: WorkoutTemplate,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val exercises: List<TemplateExercise>
)
