package com.android.timberworkoutlogs.models

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseSet(
    val weight: Double = 0.0,
    val reps: Int = 0,
    val isDone: Boolean = false,
)
