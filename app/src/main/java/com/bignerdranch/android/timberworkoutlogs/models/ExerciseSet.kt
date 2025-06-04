package com.bignerdranch.android.timberworkoutlogs.models

data class ExerciseSet(
    val id: Long = 0L, // Using 0L as a default for new, unsaved sets
    val weight: Double = 0.0,
    val reps: Int = 0,
    val isDone: Boolean = false // Added back as it's in the UI and typical for workout apps
    // TODO: Consider adding a unit for weight (kg/lbs) if it can vary per set or user preference
)