package com.bignerdranch.android.timberworkoutlogs.models


data class Workout(
    val id: Long = 0L,
    val name: String = "New Workout",
    val startTime: Long = System.currentTimeMillis(),
    var durationSeconds: Int = 0,
    val exercises: MutableList<Exercise> = mutableListOf(),
    val notes: String = ""
)
