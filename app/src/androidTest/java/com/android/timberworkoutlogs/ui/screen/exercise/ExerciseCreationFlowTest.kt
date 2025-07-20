package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.timberworkoutlogs.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.performScrollToNode

@HiltAndroidTest
class ExerciseCreationFlowTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createExercise_endToEnd() {
        // Navigate to Templates -> Exercises
        composeTestRule.onNodeWithText("Templates").performClick()

        composeTestRule.onNodeWithText("Manage Exercises").performClick()

        // Click create exercise
        composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()

        // Verify Save is disabled initially
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()

        // Fill in the name of the exercise
        val exerciseName = "Non-existent Press"
        composeTestRule.onNodeWithText("Exercise Name").performTextInput(exerciseName)

        // Verify Save is still disabled
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()

        // Select equipment
        val equipment = "Barbell"
        composeTestRule.onNodeWithText("Barbell").performClick()

        // Verify Save is still disabled
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()

        // Select a primary muscle
        composeTestRule.onNodeWithText("Chest").performClick()

        // Save the exercise - it should be enabled now
        composeTestRule.onNodeWithText("Save Exercise").performClick()

        // Verify that we are back on the list screen and the new exercise is displayed
        // with the correct prefix
        val expectedExerciseName = "$equipment $exerciseName"
        val listMatcher = composeTestRule.onNodeWithTag(TEST_TAG)
        val itemMatcher = hasText(expectedExerciseName)
        listMatcher.performScrollToNode(itemMatcher)
        composeTestRule.onNode(itemMatcher).assertIsDisplayed()
    }
}