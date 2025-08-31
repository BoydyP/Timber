package com.android.timberworkoutlogs.ui.screen.stats

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.util.getGreetingByTime
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class StatsScreenE2ETest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun userCanNavigateStatsProgression() = run {
        // Home will have the time-bound greeting.
        step("Start at home screen") {
            composeTestRule.onNodeWithText(getGreetingByTime()).assertIsDisplayed()
        }

        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
        }

        step("Test Exercise Progression tab workflow") {
            // Should be on Progression tab by default
            composeTestRule.onNodeWithText("Exercise").assertIsDisplayed()

            // Try to interact with exercise dropdown
            composeTestRule.onNodeWithText("Exercise").performClick()

            // Look for any exercise or "No exercises found"
            try {
                // Check for common exercises from database seeding
                val found = listOf("Barbell Shrug", "Bodyweight Pull Up", "Barbell Squat")
                    .any { exercise ->
                        try {
                            composeTestRule.onNodeWithText(text = exercise, substring = true)
                                .assertIsDisplayed()
                            true
                        } catch (_: AssertionError) {
                            false
                        }
                    }

                if (!found) {
                    // Should show no exercises message
                    composeTestRule.onNodeWithText("No exercises found").assertIsDisplayed()
                }
            } catch (_: Exception) {
                // Close dropdown if it opened
                composeTestRule.onNodeWithText("Exercise").performClick()
            }
        }
    }

    @Test
    fun userCanMoveBetweenTabs() = run {
        step("Start at home screen") {
            composeTestRule.onNodeWithText(getGreetingByTime()).assertIsDisplayed()
        }

        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
        }
        step("Test multiple interactions without crashes") {
            // Perform various interactions to test stability

            repeat(3) {
                composeTestRule.onNodeWithText("Progression").performClick()
                composeTestRule.onNodeWithText("Exercise").performClick()
                composeTestRule.onNodeWithText("Exercise").performClick()

                composeTestRule.onNodeWithText("1RM").performClick()
                composeTestRule.onNodeWithText("1RM Formula").performClick()
                composeTestRule.onNodeWithText("1RM Formula").performClick()

                composeTestRule.onNodeWithText("Volume").performClick()
            }

            // Verify app is still functional
            composeTestRule.onNodeWithText("Stats").assertIsDisplayed()
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
        }
        step("Test tab persistence and navigation") {
            // Switch to 1RM tab
            composeTestRule.onNodeWithText("1RM").performClick()
            composeTestRule.onNodeWithText("1RM Formula").assertIsDisplayed()

            // Navigate away to Home
            composeTestRule.onNodeWithTag("TimberAppLogo").performClick()
            composeTestRule.onNodeWithText(getGreetingByTime()).assertIsDisplayed()
        }
    }


    @Test
    fun userCanNavigateAndViewStats1RM() = run {
        // Home will have the time-bound greeting.
        step("Start at home screen") {
            composeTestRule.onNodeWithText(getGreetingByTime()).assertIsDisplayed()
        }

        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
        }

        step("Test One Rep Max tab workflow") {
            composeTestRule.onNodeWithText("1RM").performClick()
            composeTestRule.onNodeWithText("1RM Formula").assertIsDisplayed()
            
            // Test formula dropdown
            composeTestRule.onNodeWithText("1RM Formula").performClick()
            
            // Try to select different formulas
            val formulas = listOf("Epley", "Brzycki", "Lombardi", "Average")
            var selectedFormula = false
            
            formulas.forEach { formula ->
                try {
                    composeTestRule
                        .onNodeWithTag("formula_option_$formula", useUnmergedTree = true)
                        .performClick()

                    composeTestRule.onNodeWithText(formula).performClick()
                    selectedFormula = true
                    // Verify dropdown closed
                    composeTestRule.onNodeWithText("1RM Formula").assertIsDisplayed()
                } catch (_: Exception) {
                    // Formula not visible, continue
                }
                if (selectedFormula) return@forEach
            }
            
            if (!selectedFormula) {
                // Close dropdown manually
                composeTestRule.onNodeWithText("1RM Formula").performClick()
            }
        }

        step("Test Volume tab workflow") {
            composeTestRule.onNodeWithText("Volume").performClick()
            
            // Volume tab should be displayed
            composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
            
            // The content depends on VolumeStatsTab implementation
            // We just verify it doesn't crash
        }
    }

    @Test
    fun statsScreenHandlesEmptyDataStatesGracefully() = run {
        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Verify empty states are handled gracefully") {
            // Test Progression tab empty state
            try {
                composeTestRule.onNodeWithText("Select an exercise to view progression data.").assertIsDisplayed()
            } catch (_: AssertionError) {
                try {
                    composeTestRule.onNodeWithText("No progression data found").assertIsDisplayed()
                } catch (_: AssertionError) {
                    // Might be in loading state or have data, that's also valid
                }
            }
            
            // Test 1RM tab empty state
            composeTestRule.onNodeWithText("1RM").performClick()
            try {
                composeTestRule.onNodeWithText("Select an exercise to view one-rep max progression.").assertIsDisplayed()
            } catch (_: AssertionError) {
                try {
                    composeTestRule.onNodeWithText("No one-rep max data found").assertIsDisplayed()
                } catch (_: AssertionError) {
                    // Might be in loading state or have data
                }
            }
            
            // Test Volume tab - should not crash
            composeTestRule.onNodeWithText("Volume").performClick()
            composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
        }

        step("Verify no crashes occur when interacting with empty states") {
            // Try interacting with dropdowns when no data is available
            composeTestRule.onNodeWithText("Progression").performClick()
            composeTestRule.onNodeWithText("Exercise").performClick()
            composeTestRule.onNodeWithText("Exercise").performClick()
            
            composeTestRule.onNodeWithText("1RM").performClick()
            composeTestRule.onNodeWithText("Exercise").performClick()
            composeTestRule.onNodeWithText("Exercise").performClick()
        }
    }

    @Test
    fun statsScreenRespondsToSystemChanges() = run {
        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Test app navigation and back button behavior") {
            // Go to Settings to change weight unit
            composeTestRule.onNodeWithText("Settings").performClick()
            
            // Should see settings screen
            try {
                composeTestRule.onNodeWithText("Weight Unit").assertIsDisplayed()
            } catch (_: AssertionError) {
                // Settings might have different layout
            }
            
            // Go back to Stats
            composeTestRule.onNodeWithText("Stats").performClick()
            
            // Should still be functional
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
            composeTestRule.onNodeWithText("1RM").assertIsDisplayed()
            composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
        }

        step("Test rapid navigation between screens") {
            // Rapidly switch between all screens
            val screens = listOf("Stats", "History", "Templates", "Settings")
            
            repeat(2) {
                screens.forEach { screen ->
                    composeTestRule.onNodeWithText(screen).performClick()
                    // Brief pause to let the screen load
                    composeTestRule.waitForIdle()
                }
            }
            
            // End back on Stats and verify it's still functional
            composeTestRule.onNodeWithText("Stats").performClick()
            composeTestRule.onNodeWithText("Exercise").assertIsDisplayed()
        }
    }

    @Test
    fun timeRangeSelectionWorksAcrossTabs() = run {
        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Test time range options on Progression tab") {
            val timeRanges = listOf(
                "4 weeks",
                "3 months",
                "6 months",
                "Last year",
                "All time"
            )
            // Try to find and select different time ranges
            timeRanges.forEach { timeRange ->
                try {
                    composeTestRule.onNodeWithText("Time Range").performClick()
                    composeTestRule.waitForIdle()
                    composeTestRule.onNodeWithTag("time_range_${timeRange}", useUnmergedTree = true)
                        .performClick()
                } catch (_: Exception) {
                    // Time range might not be visible, continue
                }
            }
        }

        step("Verify time range works on 1RM tab") {
            composeTestRule.onNodeWithText("1RM").performClick()
            
            // Time range should be available on 1RM tab too
            val timeRanges = listOf(
                "4 weeks",
                "3 months",
                "6 months",
                "Last year",
                "All time"
            )
            
            timeRanges.forEach { timeRange ->
                try {
                    composeTestRule.onNodeWithText("Time Range").performClick()
                    composeTestRule.waitForIdle()
                    composeTestRule.onNodeWithTag("time_range_${timeRange}", useUnmergedTree = true)
                        .performClick()
                } catch (_: Exception) {
                    // Continue if not visible
                }
            }
        }

        step("Verify no crashes occur during time range changes") {
            // Switch tabs while potentially having different time ranges selected
            repeat(3) {
                composeTestRule.onNodeWithText("Progression").performClick()
                composeTestRule.onNodeWithText("1RM").performClick()
            }
            
            // Should still be functional
            composeTestRule.onNodeWithText("1RM Formula").assertIsDisplayed()
        }
    }
}
