package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.timberworkoutlogs.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ExerciseListScreenTest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun whenExercisesExist_theyAreDisplayedInList() = run {
        step("Navigate to exercise list screen") {
            composeTestRule.onNodeWithText("Templates").performClick()
            composeTestRule.onNodeWithText("Manage Exercises").performClick()
        }

        step("Verify default exercises are displayed") {
            // These exercises are seeded by the database callback
            composeTestRule.onNodeWithText("Barbell Bench Press").assertIsDisplayed()
            composeTestRule.onNodeWithText("Dumbbell Bicep Curl").assertIsDisplayed()
        }
    }
}
