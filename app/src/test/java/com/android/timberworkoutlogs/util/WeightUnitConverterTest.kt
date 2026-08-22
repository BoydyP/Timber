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

    @Test
    fun `converting kg to lb and back returns the exact original value`() {
        // Regression test: raw multiplication by the conversion factors previously left
        // floating-point noise, so 18kg -> lb -> kg landed on 17.999999999999996 instead of 18.0.
        val original = 18.0
        val roundTripped = WeightUnitConverter.convert(
            WeightUnitConverter.convert(original, WeightUnit.KG, WeightUnit.LB),
            WeightUnit.LB,
            WeightUnit.KG
        )
        assertEquals(original, roundTripped, 0.0)
    }

    @Test
    fun `convert between same units rounds a dirty value to 2 decimals`() {
        // Regression test: values that were never precision-clamped to begin with (e.g. the
        // seeded demo data's Random.nextDouble weights) must still come out clean here, since
        // this is the only conversion step a same-unit carry-forward passes through.
        val result = WeightUnitConverter.convert(52.562496183, WeightUnit.KG, WeightUnit.KG)
        assertEquals(52.56, result, 0.0)
    }

    @Test
    fun `converting lb to kg and back stays within a clean rounding tolerance`() {
        // Unlike kg -> lb -> kg, lb -> kg -> lb isn't always bit-exact: the two units' 2-decimal
        // rounding grids don't perfectly align in this direction. That's an acceptable, tiny
        // (at most 0.01 lb) rounding difference - not the floating-point noise this fix targets.
        val original = 135.0
        val roundTripped = WeightUnitConverter.convert(
            WeightUnitConverter.convert(original, WeightUnit.LB, WeightUnit.KG),
            WeightUnit.KG,
            WeightUnit.LB
        )
        assertEquals(original, roundTripped, 0.01)
    }
}
