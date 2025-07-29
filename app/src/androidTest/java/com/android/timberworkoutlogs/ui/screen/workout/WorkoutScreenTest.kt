package com.android.timberworkoutlogs.ui.screen.workout

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.ui.common.sharedSetUp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class WorkoutScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun navigateToWorkoutScreen() {
        composeTestRule.onNodeWithText("Workout").performClick()
    }


    @Before
    fun setUp() {
        sharedSetUp()
    }

    @Test
    fun initialState_isCorrect() {
        navigateToWorkoutScreen()

        composeTestRule.onNodeWithText("Complete workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Complete workout").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Select Exercise...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()
    }

    @Test
    fun workout_screen_end_to_end() {
        // 1. Navigate to Settings - Janky, reset to KG
        composeTestRule.onNodeWithText("Settings").performClick()

        // 2. Find the Switch associated with "Weight Unit" and click it
        composeTestRule.onNodeWithText("KG").performClick()
        composeTestRule.onNodeWithText("KG").assertIsSelected()

        navigateToWorkoutScreen()
        composeTestRule.onNodeWithText("Select Exercise...").performClick()

        val weight = 123
        val sets = 5
        val liftedStr = (weight * sets).toString()

        // After adding an exercise, the "Select Exercise" prompt should appear
        composeTestRule.onNodeWithText("Barbell Bench Press").performClick()
        composeTestRule.onNodeWithText("Weight").performTextInput(weight.toString())
        composeTestRule.onNodeWithText("Reps").performTextInput(sets.toString())
        composeTestRule.onNodeWithTag("checkbox_1").performClick()

        composeTestRule.waitForIdle()
        // The Finish button should now be enabled
        composeTestRule.onNodeWithText("Complete workout").assertIsEnabled()
        composeTestRule.onNodeWithText("Complete workout").performClick()

        composeTestRule.waitForIdle()
        // Seeks confirmation
        composeTestRule.onNodeWithText("Are you sure?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure?").performClick()

        composeTestRule.waitForIdle()
        // Visible in history?
        composeTestRule.onNodeWithText("History").performClick()
        composeTestRule.onNodeWithText("$liftedStr kg").assertIsDisplayed()
        Log.d("TAG", "workout_screen_end_to_end: $liftedStr")
    }

    @Test
    fun deleteExercise_restoresInitialState() {
        navigateToWorkoutScreen()

        // Add an exercise first
        composeTestRule.onNodeWithText("Select Exercise...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Complete workout").assertIsNotEnabled()

        // Now, swipe the exercise card to delete it
        composeTestRule.onNodeWithText("Select Exercise...").performTouchInput { swipeLeft() }
        composeTestRule.onNodeWithText("Delete").performClick()

        // The card should be gone and the initial state restored
        composeTestRule.onNodeWithText("Select Exercise...").assertDoesNotExist()
        composeTestRule.onNodeWithText("Complete workout").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Add Exercise").assertIsEnabled()

    }
}