package com.android.timberworkoutlogs.models

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseSetTest {

    @Test
    fun `isDone reflects each ExerciseSet subtype's own flag`() {
        assertEquals(true, RepsOnlySet(reps = 5, isDone = true).isDone)
        assertEquals(false, RepsOnlySet(reps = 5, isDone = false).isDone)

        assertEquals(true, WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true).isDone)
        assertEquals(false, WeightAndRepsSet(weight = 100.0, reps = 5, isDone = false).isDone)

        assertEquals(true, TimedSet(durationSeconds = 60, isDone = true).isDone)
        assertEquals(false, TimedSet(durationSeconds = 60, isDone = false).isDone)

        assertEquals(true, DistanceAndTimeSet(distance = 5.0, durationSeconds = 60, isDone = true).isDone)
        assertEquals(false, DistanceAndTimeSet(distance = 5.0, durationSeconds = 60, isDone = false).isDone)
    }
}
