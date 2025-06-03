package com.bignerdranch.android.timberworkoutlogs

data class Exercise(
    val id: Long = 0, // Represent as a long as will not be a huge number
    val name: String,
    val sets: MutableList<ExerciseSet> = mutableListOf()
)
