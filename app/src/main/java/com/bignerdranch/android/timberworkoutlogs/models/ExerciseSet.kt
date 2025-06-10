package com.bignerdranch.android.timberworkoutlogs.models

data class ExerciseSet(
    val id: Long = 0L,
    val weight: Double = 0.0,
    val reps: Int = 0,
    val isDone: Boolean = false,
    val unit: WeightUnit = WeightUnit.KG
)