package com.android.timberworkoutlogs

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.pressBack
import androidx.test.rule.GrantPermissionRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class WorkoutPreservationTest : TestCase() {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun whenWorkoutIsStarted_pressingBack_preservesWorkoutAndShowsBanner() = run {
        step("Navigate to the WorkoutScreen and add an exercise") {
            composeTestRule.onNodeWithText("Workout").performClick()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()
            composeTestRule.onNodeWithText("Barbell Bench Press").performClick()
            composeTestRule.onNodeWithText("Weight (KG)").performTextInput("100")
            composeTestRule.onNodeWithText("Reps").performTextInput("10")
        }

        step("Hide the keyboard") {
            Espresso.closeSoftKeyboard()
        }

        step("Press the system back button") {
            pressBack()
        }

        step("Verify we are back on the HomeScreen and the banner is visible") {
            composeTestRule.onNodeWithText("Volume this week (kg)").assertIsDisplayed()
            composeTestRule.onNodeWithText("Workout in Progress - Tap to Return")
                .assertIsDisplayed()
        }
    }
}
