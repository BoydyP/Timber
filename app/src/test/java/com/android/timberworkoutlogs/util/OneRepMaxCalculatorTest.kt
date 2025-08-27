package com.android.timberworkoutlogs.util

import com.android.timberworkoutlogs.ui.screen.stats.utils.OneRepMaxCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OneRepMaxCalculatorTest {

    private val tolerance = 0.01 // Allow small floating point differences

    @Test
    fun `epley formula calculates correctly for known values`() {
        // Given known benchmarks
        val testCases = listOf(
            Triple(100.0, 1, 100.0), // 1RM should equal the weight
            Triple(90.0, 5, 105.0),  // 90kg x 5 reps ≈ 105kg 1RM
            Triple(80.0, 10, 106.67), // 80kg x 10 reps ≈ 106.67kg 1RM
        )

        testCases.forEach { (weight, reps, expected) ->
            // When
            val result = OneRepMaxCalculator.epley(weight, reps)
            
            // Then
            assertEquals("Epley formula for ${weight}kg x $reps reps", 
                expected, result, tolerance)
        }
    }

    @Test
    fun `brzycki formula calculates correctly for known values`() {
        // Given
        val testCases = listOf(
            Triple(100.0, 1, 100.0),   // 1RM equals weight
            Triple(90.0, 5, 101.25),   // 90 * (36/(37-5)) = 90 * (36/32) = 101.25
            Triple(80.0, 10, 106.67),  // 80 * (36/(37-10)) = 80 * (36/27) = 106.67
        )

        testCases.forEach { (weight, reps, expected) ->
            // When
            val result = OneRepMaxCalculator.brzycki(weight, reps)
            
            // Then
            assertEquals("Brzycki formula for ${weight}kg x $reps reps", 
                expected, result, tolerance)
        }
    }

    @Test
    fun `lombardi formula calculates correctly for known values`() {
        // Given
        val testCases = listOf(
            Triple(100.0, 1, 100.0),   // 1RM equals weight
            Triple(90.0, 5, 105.72),   // 90 * 5^0.10 = 90 * 1.174619 ≈ 105.72
            Triple(80.0, 10, 100.71),  // 80 * 10^0.10 = 80 * 1.258925 ≈ 100.71
        )

        testCases.forEach { (weight, reps, expected) ->
            // When
            val result = OneRepMaxCalculator.lombardi(weight, reps)
            
            // Then
            assertEquals("Lombardi formula for ${weight}kg x $reps reps", 
                expected, result, tolerance)
        }
    }

    @Test
    fun `brzycki handles edge case when reps approach limit`() {
        // Given: High rep count near the formula limit
        val weight = 50.0
        val reps = 36 // Just below the 37 limit
        
        // When
        val result = OneRepMaxCalculator.brzycki(weight, reps)
        
        // Then: Should not crash and should return a reasonable value
        assertTrue("Brzycki should handle edge case", result > weight)
        assertTrue("Result should be finite", result.isFinite())
    }

    @Test
    fun `brzycki handles reps at or above limit safely`() {
        // Given: Reps at the mathematical limit
        val weight = 50.0
        
        // When & Then: Should not crash
        val result37 = OneRepMaxCalculator.brzycki(weight, 37)
        assertEquals("Reps at limit should return original weight", weight, result37, tolerance)
        
        val result40 = OneRepMaxCalculator.brzycki(weight, 40)
        assertEquals("Reps above limit should return original weight", weight, result40, tolerance)
    }

    @Test
    fun `reliability check correctly identifies reliable rep ranges`() {
        // Given: Various rep counts
        val reliableReps = listOf(1, 2, 3, 5, 8, 10)
        val unreliableReps = listOf(0, 11, 12, 15, 20, 30)
        
        // When & Then: Reliable ranges
        reliableReps.forEach { reps ->
            assertTrue("$reps reps should be reliable", 
                OneRepMaxCalculator.isReliableRepRange(reps))
        }
        
        // When & Then: Unreliable ranges
        unreliableReps.forEach { reps ->
            assertFalse("$reps reps should be unreliable", 
                OneRepMaxCalculator.isReliableRepRange(reps))
        }
    }

    @Test
    fun `calculateAll returns consistent results across formulas`() {
        // Given
        val weight = 80.0
        val reps = 8
        
        // When
        val estimates = OneRepMaxCalculator.calculateAll(weight, reps)
        
        // Then: All estimates should be reasonable and consistent
        assertTrue("Epley estimate should be positive", estimates.epley > 0)
        assertTrue("Brzycki estimate should be positive", estimates.brzycki > 0)
        assertTrue("Lombardi estimate should be positive", estimates.lombardi > 0)
        
        // All should be greater than original weight
        assertTrue("Epley should exceed original weight", estimates.epley > weight)
        assertTrue("Brzycki should exceed original weight", estimates.brzycki > weight)
        assertTrue("Lombardi should exceed original weight", estimates.lombardi > weight)
        
        // Average should be calculated correctly
        val expectedAverage = (estimates.epley + estimates.brzycki + estimates.lombardi) / 3.0
        assertEquals("Average calculation", expectedAverage, estimates.average, tolerance)
        
        // Reliability should match the input
        assertEquals("Reliability check", 
            OneRepMaxCalculator.isReliableRepRange(reps), estimates.isReliable)
    }

    @Test
    fun `getDefault uses epley formula`() {
        // Given
        val weight = 100.0
        val reps = 5
        
        // When
        val defaultResult = OneRepMaxCalculator.getDefault(weight, reps)
        val epleyrResult = OneRepMaxCalculator.epley(weight, reps)
        
        // Then
        assertEquals("Default should use Epley formula", 
            epleyrResult, defaultResult, tolerance)
    }

    @Test
    fun `formulas handle zero and negative inputs safely`() {
        // Given: Edge case inputs
        val testInputs = listOf(
            Pair(0.0, 5),    // Zero weight
            Pair(-10.0, 5),  // Negative weight
            Pair(100.0, 0),  // Zero reps
            Pair(100.0, -1)  // Negative reps
        )
        
        testInputs.forEach { (weight, reps) ->
            // When & Then: Should not crash (may return non-finite values for edge cases)
            try {
                val epley = OneRepMaxCalculator.epley(weight, reps)
                val brzycki = OneRepMaxCalculator.brzycki(weight, reps)
                val lombardi = OneRepMaxCalculator.lombardi(weight, reps)
                
                // Just verify the functions don't throw exceptions
                // Results may be NaN or infinite for edge cases, which is acceptable
                assertTrue("Functions should complete without throwing for $weight kg x $reps reps", true)
            } catch (e: Exception) {
                org.junit.Assert.fail("Functions should not throw exceptions for $weight kg x $reps reps: ${e.message}")
            }
        }
    }

    @Test
    fun `one rep max estimates data class behaves correctly`() {
        // Given
        val estimates = OneRepMaxCalculator.calculateAll(80.0, 6)
        
        // When & Then
        assertEquals("Weight should be stored", 80.0, estimates.weight, tolerance)
        assertEquals("Reps should be stored", 6, estimates.reps)
        assertTrue("Should be reliable for 6 reps", estimates.isReliable)
        
        // Average calculation
        val expectedAverage = (estimates.epley + estimates.brzycki + estimates.lombardi) / 3.0
        assertEquals("Average calculation", expectedAverage, estimates.average, tolerance)
    }
}
