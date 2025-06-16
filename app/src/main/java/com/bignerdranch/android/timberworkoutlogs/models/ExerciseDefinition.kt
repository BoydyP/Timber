package com.bignerdranch.android.timberworkoutlogs.models

import java.util.UUID

/**
 * Represents a single exercise definition in the master list.
 * This is different from an Exercise in a workout log.
 *
 * @param id The unique ID for this exercise definition.
 * @param name The official name of the exercise (e.g., "Barbell Bench Press").
 * // Future properties could include:
 * // val category: String (e.g., "Chest", "Legs")
 * // val equipment: String (e.g., "Barbell", "Dumbbell")
 */
data class ExerciseDefinition(
    val id: UUID = UUID.randomUUID(),
    val name: String
)
