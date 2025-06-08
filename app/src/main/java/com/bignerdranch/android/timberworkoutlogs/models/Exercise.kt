package com.bignerdranch.android.timberworkoutlogs.models

import java.util.UUID

data class Exercise(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val sets: MutableList<ExerciseSet> = mutableListOf()
)
