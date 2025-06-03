package com.bignerdranch.android.timberworkoutlogs

data class ExerciseSet(
    val id: Long = 0,
    val weight: Double, // TODO: Represent in kg or lbs
    val reps: Int
)
