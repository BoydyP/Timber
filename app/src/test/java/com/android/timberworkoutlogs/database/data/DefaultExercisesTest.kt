package com.android.timberworkoutlogs.database.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The seeding design relies on default exercise ids being stable and collision-free:
 * seeding is only idempotent if the same exercise resolves to the same primary key every
 * time, and only correct if two different exercises never share one.
 */
class DefaultExercisesTest {

    @Test
    fun `ids are stable across calls`() {
        val first = DefaultExercises.getPredefinedExercises().associateBy { it.id }
        val second = DefaultExercises.getPredefinedExercises().associateBy { it.id }

        assertEquals(first.keys, second.keys)
    }

    @Test
    fun `ids are unique across the catalog`() {
        val exercises = DefaultExercises.getPredefinedExercises()
        val distinctIds = exercises.map { it.id }.distinct()

        assertEquals(
            "Two default exercises share an id, so seeding would silently drop one",
            exercises.size,
            distinctIds.size
        )
    }

    @Test
    fun `name and equipment together identify an exercise uniquely`() {
        val exercises = DefaultExercises.getPredefinedExercises()
        val naturalKeys = exercises.map { it.name to it.equipment }

        assertEquals(
            "Duplicate name + equipment pair in the catalog",
            exercises.size,
            naturalKeys.distinct().size
        )
    }

    @Test
    fun `catalog is not empty`() {
        assertTrue(DefaultExercises.getPredefinedExercises().isNotEmpty())
    }
}
