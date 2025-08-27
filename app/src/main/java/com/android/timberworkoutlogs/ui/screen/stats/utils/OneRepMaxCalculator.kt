package com.android.timberworkoutlogs.ui.screen.stats.utils

import kotlin.math.pow

/**
 * Utility class for calculating estimated one-rep max using various formulas.
 * All formulas work best for rep ranges 1-10, accuracy decreases beyond that.
 */
object OneRepMaxCalculator {
    
    /**
     * Epley formula: 1RM = weight × (1 + reps/30)
     * Most widely used, good accuracy for 1-10 rep range
     */
    fun epley(weight: Double, reps: Int): Double {
        if (reps == 1) return weight
        return weight * (1 + reps / 30.0)
    }
    
    /**
     * Brzycki formula: 1RM = weight × (36/(37-reps))
     * Popular alternative, slightly more conservative than Epley
     */
    fun brzycki(weight: Double, reps: Int): Double {
        if (reps == 1) return weight
        if (reps >= 37) return weight // Avoid division by zero/negative
        return weight * (36.0 / (37.0 - reps))
    }
    
    /**
     * Lombardi formula: 1RM = weight × reps^0.10
     * Based on powerlifting data, tends to be more conservative
     */
    fun lombardi(weight: Double, reps: Int): Double {
        if (reps == 1) return weight
        return weight * reps.toDouble().pow(0.10)
    }
    
    /**
     * Calculate 1RM using all three formulas and return the results
     */
    fun calculateAll(weight: Double, reps: Int): OneRepMaxEstimates {
        return OneRepMaxEstimates(
            epley = epley(weight, reps),
            brzycki = brzycki(weight, reps),
            lombardi = lombardi(weight, reps),
            weight = weight,
            reps = reps
        )
    }
    
    /**
     * Get the default (recommended) 1RM calculation using Epley formula
     */
    fun getDefault(weight: Double, reps: Int): Double = epley(weight, reps)
    
    /**
     * Check if the rep count is in the reliable range for 1RM calculations
     */
    fun isReliableRepRange(reps: Int): Boolean = reps in 1..10
}

data class OneRepMaxEstimates(
    val epley: Double,
    val brzycki: Double,
    val lombardi: Double,
    val weight: Double,
    val reps: Int
) {
    val average: Double get() = (epley + brzycki + lombardi) / 3.0
    val isReliable: Boolean get() = OneRepMaxCalculator.isReliableRepRange(reps)
}

enum class OneRMFormula(val displayName: String) {
    EPLEY("Epley"),
    BRZYCKI("Brzycki"),
    LOMBARDI("Lombardi"),
    AVERAGE("Average")
}
