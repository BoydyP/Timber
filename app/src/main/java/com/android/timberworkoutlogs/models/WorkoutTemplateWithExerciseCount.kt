package com.android.timberworkoutlogs.models

import androidx.room.Embedded

data class WorkoutTemplateWithExerciseCount(
    @Embedded
    val workoutTemplate: WorkoutTemplate,
    val exerciseCount: Int
)
