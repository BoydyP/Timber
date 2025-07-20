package com.android.timberworkoutlogs.ui.screen.workout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.ui.navigation.TimberUi
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

    @Test
    fun initialState_isCorrect() {
        navigateToWorkoutScreen()

        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish").assertIsNotEnabled()

        composeTestRule.onNodeWithContentDescription("Add Exercise").assertIsDisplayed()
    }

    @Test
    fun addExercise_updatesState() {
        navigateToWorkoutScreen()

        composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()

        // After adding an exercise, the "Select Exercise" prompt should appear
        composeTestRule.onNodeWithText("Select Exercise").assertIsDisplayed()

        // The Finish button should now be enabled
        composeTestRule.onNodeWithText("Finish").assertIsEnabled()
    }

    @Test
    fun deleteExercise_restoresInitialState() {
        navigateToWorkoutScreen()

        // Add an exercise first
        composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()
        composeTestRule.onNodeWithText("Select Exercise").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish").assertIsEnabled()

        // Now, swipe the exercise card to delete it
        composeTestRule.onNodeWithText("Select Exercise").performTouchInput { swipeLeft() }

        // The card should be gone and the initial state restored
        composeTestRule.onNodeWithText("Select Exercise").assertDoesNotExist()
        composeTestRule.onNodeWithText("Finish").assertIsNotEnabled()
    }
}