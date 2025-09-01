package com.android.timberworkoutlogs.util

import com.android.timberworkoutlogs.models.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightUnitConverterTest {

    @Test
    fun `lbsToKg converts correctly`() {
        val result = WeightUnitConverter.lbsToKg(220.0)
        assertEquals(99.79, result, 0.01)
    }

    @Test
    fun `kgToLbs converts correctly`() {
        val result = WeightUnitConverter.kgToLbs(100.0)
        assertEquals(220.46, result, 0.01)
    }

    @Test
    fun `toKg from LB converts correctly`() {
        val result = WeightUnitConverter.toKg(220.0, WeightUnit.LB)
        assertEquals(99.79, result, 0.01)
    }

    @Test
    fun `toKg from KG returns same value`() {
        val result = WeightUnitConverter.toKg(100.0, WeightUnit.KG)
        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `fromKg to LB converts correctly`() {
        val result = WeightUnitConverter.fromKg(100.0, WeightUnit.LB)
        assertEquals(220.46, result, 0.01)
    }

    @Test
    fun `fromKg to KG returns same value`() {
        val result = WeightUnitConverter.fromKg(100.0, WeightUnit.KG)
        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `convert between same units returns same value`() {
        val result = WeightUnitConverter.convert(100.0, WeightUnit.KG, WeightUnit.KG)
        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `convert from KG to LB works correctly`() {
        val result = WeightUnitConverter.convert(100.0, WeightUnit.KG, WeightUnit.LB)
        assertEquals(220.46, result, 0.01)
    }

    @Test
    fun `convert from LB to KG works correctly`() {
        val result = WeightUnitConverter.convert(220.0, WeightUnit.LB, WeightUnit.KG)
        assertEquals(99.79, result, 0.01)
    }
}
