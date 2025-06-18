package com.bignerdranch.android.timberworkoutlogs.models

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseSet(
    val weight: Int = 0,
    val reps: Int = 0,
    val isDone: Boolean = false,
    val unit: WeightUnit = WeightUnit.KG // If you add this back, WeightUnit also needs to be @Serializable
)
