package com.android.timberworkoutlogs.util

import com.android.timberworkoutlogs.models.WeightUnit

/**
 * Utility object for converting between weight units (KG and LB).
 * Centralizes conversion factors and provides consistent conversion methods.
 */
object WeightUnitConverter {
    private const val LBS_TO_KG_FACTOR = 0.45359237
    private const val KG_TO_LBS_FACTOR = 2.20462262

    /**
     * Converts weight from pounds to kilograms.
     */
    fun lbsToKg(weightInLbs: Double): Double = roundTo2Decimals(weightInLbs * LBS_TO_KG_FACTOR)

    /**
     * Converts weight from kilograms to pounds.
     */
    fun kgToLbs(weightInKg: Double): Double = roundTo2Decimals(weightInKg * KG_TO_LBS_FACTOR)

    // Raw multiplication by the conversion factors leaves floating-point noise (e.g. 18kg ->
    // 39.683207159999995lb), which then fails to round-trip back to 18kg. Rounding to 2 decimal
    // places - already finer than any plate/dumbbell increment - keeps kg<->lb toggling stable.
    private fun roundTo2Decimals(value: Double): Double = Math.round(value * 100) / 100.0

    /**
     * Converts weight to kilograms based on the source unit.
     * @param weight The weight value to convert
     * @param sourceUnit The unit of the input weight
     * @return Weight in kilograms
     */
    fun toKg(weight: Double, sourceUnit: WeightUnit): Double {
        return when (sourceUnit) {
            WeightUnit.KG -> weight
            WeightUnit.LB -> lbsToKg(weight)
        }
    }

    /**
     * Converts weight from kilograms to the target unit.
     * @param weightInKg The weight value in kilograms
     * @param targetUnit The desired output unit
     * @return Weight in the target unit
     */
    fun fromKg(weightInKg: Double, targetUnit: WeightUnit): Double {
        return when (targetUnit) {
            WeightUnit.KG -> weightInKg
            WeightUnit.LB -> kgToLbs(weightInKg)
        }
    }

    /**
     * Converts weight between any two units.
     * @param weight The weight value to convert
     * @param sourceUnit The unit of the input weight
     * @param targetUnit The desired output unit
     * @return Weight in the target unit
     */
    fun convert(weight: Double, sourceUnit: WeightUnit, targetUnit: WeightUnit): Double {
        // Round here too, not just on the cross-unit paths below - callers pass in weights from
        // sources (e.g. seeded/demo data) that were never rounded to begin with, and this is the
        // single place that guarantees the app's 2-decimal display precision.
        if (sourceUnit == targetUnit) return roundTo2Decimals(weight)
        val weightInKg = toKg(weight, sourceUnit)
        return fromKg(weightInKg, targetUnit)
    }
}
