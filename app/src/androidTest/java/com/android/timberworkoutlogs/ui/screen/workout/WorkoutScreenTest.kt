package com.android.timberworkoutlogs.ui.screen.workout

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
import androidx.test.rule.GrantPermissionRule
import com.android.timberworkoutlogs.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class WorkoutScreenTest : TestCase() {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private fun navigateToWorkoutScreen() {
        composeTestRule.onNodeWithText("Workout").performClick()
    }

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun initialState_isCorrect() = run {
        step("Navigate to workout screen") {
            navigateToWorkoutScreen()
        }

        step("Check initial state") {
            composeTestRule.onNodeWithText("Complete workout").assertIsDisplayed()
            composeTestRule.onNodeWithText("Complete workout").assertIsNotEnabled()

            composeTestRule.onNodeWithText("Select Exercise...").assertIsDisplayed()
            composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()
        }
    }

    @Test
    fun workout_screen_end_to_end() = run {
        val weightUnit = "KG"
        step("Navigate to Settings and set weight unit to KG") {
            composeTestRule.onNodeWithText("Settings").performClick()
            composeTestRule.onNodeWithText(weightUnit).performClick()
            composeTestRule.onNodeWithText(weightUnit).assertIsSelected()
        }

        step("Navigate to workout screen and add an exercise") {
            navigateToWorkoutScreen()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()

            val weight = 123
            val sets = 5

            composeTestRule.onNodeWithText("Barbell Bench Press").performClick()
            composeTestRule.onNodeWithText("Weight ($weightUnit)")
                .performTextInput(weight.toString())
            composeTestRule.onNodeWithText("Reps").performTextInput(sets.toString())
            composeTestRule.onNodeWithTag("checkbox_1").performClick()
        }

        step("Complete the workout and verify history") {
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Complete workout").assertIsEnabled()
            composeTestRule.onNodeWithText("Complete workout").performClick()

            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Are you sure?").assertIsDisplayed()
            composeTestRule.onNodeWithText("Are you sure?").performClick()

            // Wait for database operations to complete and data to propagate
            composeTestRule.waitForIdle()
            Thread.sleep(1000) // Give time for database transaction to complete
            
            composeTestRule.onNodeWithText("History").performClick()
            
            // Wait for History screen to load data from database
            composeTestRule.waitForIdle()
            Thread.sleep(500) // Additional wait for Flow to emit new data
            
            composeTestRule.onNodeWithText("${(123 * 5)} kg").assertIsDisplayed()
        }
    }

    @Test
    fun deleteExercise_restoresInitialState() = run {
        step("Navigate to workout screen and add an exercise") {
            navigateToWorkoutScreen()
            composeTestRule.onNodeWithText("Select Exercise...").assertIsDisplayed()
            composeTestRule.onNodeWithText("Complete workout").assertIsNotEnabled()
        }

        step("Swipe exercise to delete") {
            composeTestRule.onNodeWithText("Select Exercise...").performTouchInput { swipeLeft() }
            composeTestRule.onNodeWithText("Delete").performClick()
        }

        step("Verify initial state is restored") {
            composeTestRule.onNodeWithText("Select Exercise...").assertDoesNotExist()
            composeTestRule.onNodeWithText("Complete workout").assertIsNotEnabled()
            composeTestRule.onNodeWithText("Add Exercise").assertIsEnabled()
        }
    }
}
