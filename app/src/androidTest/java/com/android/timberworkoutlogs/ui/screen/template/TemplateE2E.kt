package com.android.timberworkoutlogs.ui.screen.template

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.timberworkoutlogs.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TemplateE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun createTemplateFlow_isSuccessful() {
        // Navigate to create template screen
        composeTestRule.onNodeWithText("Templates").performClick()
        composeTestRule.onNodeWithText("Workout Templates").performClick()
        composeTestRule.onNodeWithContentDescription("Create Template").performClick()

        // Create template
        val templateName = "Full Body Workout"
        composeTestRule.onNodeWithText("Template Name").performTextInput(templateName)
        composeTestRule.onNodeWithText("Add Exercise").performClick()
        composeTestRule.onNodeWithText("Select Exercise...").performClick()
        composeTestRule.onNodeWithText("Barbell Bench Press").performClick()
        composeTestRule.onNodeWithText("Save Template").performClick()

        // Verify template created
        composeTestRule.onNodeWithText(templateName).assertIsDisplayed()
    }

    @Test
    fun editTemplateFlow_isSuccessful() {
        // Navigate to create template screen and create a template to be edited
        composeTestRule.onNodeWithText("Templates").performClick()
        composeTestRule.onNodeWithText("Workout Templates").performClick()
        composeTestRule.onNodeWithContentDescription("Create Template").performClick()
        val originalName = "Leg Day"
        composeTestRule.onNodeWithText("Template Name").performTextInput(originalName)
        composeTestRule.onNodeWithText("Save Template").performClick()

        // Navigate to edit the template
        composeTestRule.onNodeWithText(originalName).performClick()

        // Edit the template name
        val updatedName = "Advanced Leg Day"
        composeTestRule.onNodeWithText("Template Name").performTextClearance()
        composeTestRule.onNodeWithText("Template Name").performTextInput(updatedName)
        composeTestRule.onNodeWithText("Save Template").performClick()

        // Verify template updated
        composeTestRule.onNodeWithText(updatedName).assertIsDisplayed()
    }
}
