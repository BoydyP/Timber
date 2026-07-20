package com.android.timberworkoutlogs.models

import kotlinx.serialization.Serializable


@Serializable
sealed interface ExerciseSet
@Serializable
data class RepsOnlySet(
    val reps: Int = 0,
    val isDone: Boolean = false
) : ExerciseSet
@Serializable
data class WeightAndRepsSet(
    val weight: Double = 0.0,
    val reps: Int = 0,
    val isDone: Boolean = false
) : ExerciseSet
@Serializable
data class TimedSet(
    val durationSeconds: Int = 0,
    val isDone: Boolean = false
) : ExerciseSet
@Serializable
data class DistanceAndTimeSet(
    val distance: Double = 0.0,
    val durationSeconds: Int = 0,
    val isDone: Boolean = false
) : ExerciseSet

val ExerciseSet.isDone: Boolean
    get() = when (this) {
        is RepsOnlySet -> isDone
        is WeightAndRepsSet -> isDone
        is TimedSet -> isDone
        is DistanceAndTimeSet -> isDone
    }
