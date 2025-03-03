package com.bignerdranch.android.timberworkoutlogs


data class Workout(
    val id: Long, // Using Long rather than UUID initially
    val duration: Int, // Duration in minutes
    val exercises: MutableList<Exercise> = mutableListOf()
)
