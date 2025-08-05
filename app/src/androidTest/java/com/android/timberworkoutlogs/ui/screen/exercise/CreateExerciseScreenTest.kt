package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.timberworkoutlogs.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class CreateExerciseScreenTest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        // Navigate to the screen
        composeTestRule.onNodeWithText("Templates").performClick()
        composeTestRule.onNodeWithText("Manage Exercises").performClick()
        composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()
    }

    @Test
    fun saveButton_isDisabled_whenNameIsBlank() = run {
        step("Check save button is disabled initially") {
            composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
        }

        step("Select a muscle group") {
            composeTestRule.onNodeWithText("Chest").performClick()
        }

        step("Check save button is still disabled") {
            composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
        }
    }

    @Test
    fun saveButton_isDisabled_whenNoMuscleGroupsSelected() = run {
        step("Enter an exercise name") {
            composeTestRule.onNodeWithText("Exercise Name").performTextInput("Good Mornings")
        }

        step("Check save button is disabled") {
            composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
        }
    }

    @Test
    fun saveButton_isEnabled_whenNameAndMuscleGroupsArePresent() = run {
        step("Enter an exercise name") {
            composeTestRule.onNodeWithText("Exercise Name").performTextInput("Bicep Curls")
        }

        step("Select a muscle group") {
            composeTestRule.onNodeWithText("Biceps").performClick()
        }

        step("Check save button is enabled") {
            composeTestRule.onNodeWithText("Save Exercise").assertIsEnabled()
        }
    }
}
