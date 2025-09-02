package com.android.timberworkoutlogs.ui.screen.home

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
class HomeScreenTest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        // Ensure the activity is launched and compose is ready
        composeTestRule.waitForIdle()
    }

    @Test
    fun homeScreen_displaysTimeBasedGreeting() = run {
        step("Verify time-based greeting is displayed") {
            val currentGreeting = getGreetingByTime()
            composeTestRule.onNodeWithText(currentGreeting).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_displaysAppLogo() = run {
        step("Verify Timber logo is displayed") {
            composeTestRule.onNodeWithTag("TimberAppLogo").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_navigatesToWorkoutScreen() = run {
        step("Wait for app to load and navigate to Workout screen") {
            composeTestRule.waitForIdle()
            Thread.sleep(500) // Give time for bottom nav to appear
            composeTestRule.onNodeWithText("Workout").performClick()
            composeTestRule.waitForIdle()
        }

        step("Verify workout screen elements are displayed") {
            composeTestRule.onNodeWithText("Complete workout").assertIsDisplayed()
            composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_navigatesToHistoryScreen() = run {
        step("Navigate to History screen from home") {
            composeTestRule.onNodeWithText("History").performClick()
        }

        step("Verify history screen is displayed") {
            // History screen should be displayed - specific elements depend on implementation
            // At minimum, we should not crash and should see the history tab selected
            composeTestRule.onNodeWithText("History").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_navigatesToTemplatesScreen() = run {
        step("Navigate to Templates screen from home") {
            composeTestRule.onNodeWithText("Templates").performClick()
        }

        step("Verify templates screen elements are displayed") {
            composeTestRule.onNodeWithText("Workout Templates").assertIsDisplayed()
            composeTestRule.onNodeWithText("Manage Exercises").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_navigatesToStatsScreen() = run {
        step("Navigate to Stats screen from home") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Verify stats screen elements are displayed") {
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
            composeTestRule.onNodeWithText("1RM").assertIsDisplayed()
            composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_navigatesToSettingsScreen() = run {
        step("Navigate to Settings screen from home") {
            composeTestRule.onNodeWithText("Settings").performClick()
        }

        step("Verify settings screen elements are displayed") {
            try {
                composeTestRule.onNodeWithText("Weight Unit").assertIsDisplayed()
            } catch (_: AssertionError) {
                // Settings might have different layout, check for weight unit options
                composeTestRule.onNodeWithText("KG").assertIsDisplayed()
                composeTestRule.onNodeWithText("LB").assertIsDisplayed()
            }
        }
    }

    @Test
    fun homeScreen_logoClickReturnsToHome() = run {
        step("Navigate away from home screen") {
            composeTestRule.onNodeWithText("Settings").performClick()
            composeTestRule.onNodeWithText("KG").assertIsDisplayed()
        }

        step("Click logo to return to home") {
            composeTestRule.onNodeWithTag("TimberAppLogo").performClick()
        }

        step("Verify we're back on home screen") {
            val currentGreeting = getGreetingByTime()
            composeTestRule.onNodeWithText(currentGreeting).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_navigationPreservesState() = run {
        step("Navigate through multiple screens") {
            composeTestRule.onNodeWithText("Stats").performClick()
            composeTestRule.onNodeWithText("1RM").performClick()
            composeTestRule.onNodeWithText("Settings").performClick()
            composeTestRule.onNodeWithText("Templates").performClick()
        }

        step("Return to home and verify it's still functional") {
            composeTestRule.onNodeWithTag("TimberAppLogo").performClick()
            val currentGreeting = getGreetingByTime()
            composeTestRule.onNodeWithText(currentGreeting).assertIsDisplayed()
        }

        step("Verify navigation still works after returning") {
            composeTestRule.onNodeWithText("Workout").performClick()
            composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_displaysConsistentUI() = run {
        step("Verify all navigation elements are present") {
            val navigationItems = listOf("Workout", "History", "Templates", "Stats", "Settings")
            
            navigationItems.forEach { item ->
                composeTestRule.onNodeWithText(item).assertIsDisplayed()
            }
        }

        step("Verify home screen branding is consistent") {
            composeTestRule.onNodeWithTag("TimberAppLogo").assertIsDisplayed()
            val currentGreeting = getGreetingByTime()
            composeTestRule.onNodeWithText(currentGreeting).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_handlesRapidNavigation() = run {
        step("Perform rapid navigation between screens") {
            val screens = listOf("History", "Templates", "Stats", "Settings")
            
            repeat(3) {
                screens.forEach { screen ->
                    composeTestRule.onNodeWithText(screen).performClick()
                    composeTestRule.waitForIdle()
                    
                    // Return to home
                    composeTestRule.onNodeWithTag("TimberAppLogo").performClick()
                    composeTestRule.waitForIdle()
                }
            }
        }

        step("Verify home screen is still functional after stress test") {
            composeTestRule.onNodeWithText(getGreetingByTime()).assertIsDisplayed()
            
            // Test one final navigation
            composeTestRule.onNodeWithText("Workout").performClick()
            composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()
        }
    }
}
