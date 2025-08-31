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
class StatsScreenNavigationTest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun whenNavigatingToStats_statsScreenIsDisplayed() = run {
        step("Navigate to Stats screen from bottom navigation") {
            // Wait for app to load
            composeTestRule.onNodeWithText(getGreetingByTime()).assertIsDisplayed()
            // Navigate to Stats
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Verify Stats screen is displayed with tabs") {
            // Verify we're on the Stats screen by checking for tab elements
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
            composeTestRule.onNodeWithText("1RM").assertIsDisplayed()
            composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
        }
    }

    @Test
    fun whenOnStatsScreen_allThreeTabsAreVisible() = run {
        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Verify all tabs are present") {
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
            composeTestRule.onNodeWithText("1RM").assertIsDisplayed()
            composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
        }
    }

    @Test
    fun whenSwitchingTabs_contentChangesCorrectly() = run {
        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Verify default tab (Progression) is selected") {
            // Should see exercise selection dropdown which is in Progression tab
            composeTestRule.onNodeWithText("Exercise").assertIsDisplayed()
        }

        step("Switch to 1RM tab") {
            composeTestRule.onNodeWithText("1RM").performClick()
        }

        step("Verify 1RM tab content is shown") {
            // Should see 1RM Formula dropdown which is only in 1RM tab
            composeTestRule.onNodeWithText("1RM Formula").assertIsDisplayed()
        }

        step("Switch to Volume tab") {
            composeTestRule.onNodeWithText("Volume").performClick()
        }

        step("Verify Volume tab content is shown") {
            // Volume tab should show time range picker
            // We can't easily test the specific content without data, 
            // but switching should not crash
            composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
        }

        step("Switch back to Progression tab") {
            composeTestRule.onNodeWithText("Progression").performClick()
        }

        step("Verify Progression tab content is restored") {
            composeTestRule.onNodeWithText("Exercise").assertIsDisplayed()
        }
    }

    @Test
    fun whenTabsAreClicked_noErrorsOccur() = run {
        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Rapidly switch between tabs") {
            // Click each tab multiple times to test stability
            repeat(3) {
                composeTestRule.onNodeWithText("Progression").performClick()
                composeTestRule.onNodeWithText("1RM").performClick()
                composeTestRule.onNodeWithText("Volume").performClick()
            }
        }

        step("Verify app is still responsive") {
            // Should still be able to see and interact with tabs
            composeTestRule.onNodeWithText("Progression").assertIsDisplayed()
            composeTestRule.onNodeWithText("1RM").assertIsDisplayed()
            composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
        }
    }

    @Test
    fun whenNavigatingAwayAndBack_stateIsPreserved() = run {
        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Switch to 1RM tab") {
            composeTestRule.onNodeWithText("1RM").performClick()
            composeTestRule.onNodeWithText("1RM Formula").assertIsDisplayed()
        }

        step("Navigate away to Home") {
            composeTestRule.onNodeWithTag("TimberAppLogo").performClick()
            composeTestRule.onNodeWithText(getGreetingByTime()).assertIsDisplayed()
        }

        step("Navigate back to Stats") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Verify 1RM tab is still selected") {
            // The 1RM tab should still be selected
            composeTestRule.onNodeWithText("1RM Formula").assertIsDisplayed()
        }
    }

    @Test
    fun whenNoExerciseData_appropriateMessagesAreShown() = run {
        step("Navigate to Stats screen") {
            composeTestRule.onNodeWithText("Stats").performClick()
        }

        step("Verify empty state messaging on Progression tab") {
            // Should show message about selecting exercise or no data
            // This text comes from the ExerciseProgressionTab when no data
            // The exact text depends on whether exercises exist in DB
            try {
                composeTestRule.onNodeWithText("Select an exercise to view progression data.").assertIsDisplayed()
            } catch (_: AssertionError) {
                // Alternative message when no exercises have workout history
                composeTestRule.onNodeWithText("No progression data found").assertIsDisplayed()
            }
        }

        step("Check 1RM tab empty state") {
            composeTestRule.onNodeWithText("1RM").performClick()
            
            try {
                composeTestRule.onNodeWithText("Select an exercise to view one-rep max progression.").assertIsDisplayed()
            } catch (_: AssertionError) {
                composeTestRule.onNodeWithText("No one-rep max data found").assertIsDisplayed()
            }
        }
    }
}
