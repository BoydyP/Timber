package com.bignerdranch.android.timberworkoutlogs.models


data class Workout(
    val id: Long = 0L,
    val name: String = "New Workout",
    val startTime: Long = System.currentTimeMillis(),
    var durationSeconds: Int = 0,
    val workoutExercises: MutableList<WorkoutExercise> = mutableListOf(),
    val notes: String = ""
)
