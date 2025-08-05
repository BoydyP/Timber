package com.android.timberworkoutlogs.ui.screen.exercise

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.android.timberworkoutlogs.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ExerciseCreationFlowTest : TestCase() {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun createExercise_endToEnd() = run {
        step("Navigate to create exercise screen") {
            composeTestRule.onNodeWithText("Templates").performClick()
            composeTestRule.onNodeWithText("Manage Exercises").performClick()
            composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()
        }

        step("Verify Save is disabled initially") {
            composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
        }

        step("Fill in exercise details") {
            val exerciseName = "Non-existent Press"
            composeTestRule.onNodeWithText("Exercise Name").performTextInput(exerciseName)
            composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()

            val equipment = "Barbell"
            composeTestRule.onNodeWithText(equipment).performClick()
            composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()

            composeTestRule.onNodeWithText("Chest").performClick()
        }

        step("Save the exercise and verify it's in the list") {
            composeTestRule.onNodeWithText("Save Exercise").performClick()

            val expectedExerciseName = "Barbell Non-existent Press"
            val listMatcher = composeTestRule.onNodeWithTag(TEST_TAG)
            val itemMatcher = hasText(expectedExerciseName)
            listMatcher.performScrollToNode(itemMatcher)
            composeTestRule.onNode(itemMatcher).assertIsDisplayed()
        }
    }
}
