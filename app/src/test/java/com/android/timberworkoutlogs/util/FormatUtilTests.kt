package com.android.timberworkoutlogs.util

import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilTests {

    @Test
    fun testCapitaliseEnum() {
        assertEquals("Barbell", capitaliseEnum("BARBELL"))
        assertEquals("Dumbbell", capitaliseEnum("DUMBBELL"))
        assertEquals("Bodyweight", capitaliseEnum("BODYWEIGHT"))
        assertEquals("Full_body", capitaliseEnum("FULL_BODY"))
    }

    @Test
    fun testSpaceSeparateEnum() {
        assertEquals("FULL BODY", spaceSeparateEnum("FULL_BODY"))
        assertEquals("BARBELL", spaceSeparateEnum("BARBELL"))
    }

    @Test
    fun testEnumCapitalizationAndSpaceSuccess() {
        assertEquals("Barbell", spaceSeparateEnum(capitaliseEnum(ExerciseEquipment.BARBELL.name)))
        assertEquals("Full body", spaceSeparateEnum(capitaliseEnum(MuscleGroup.FULL_BODY.name)))
    }
}
