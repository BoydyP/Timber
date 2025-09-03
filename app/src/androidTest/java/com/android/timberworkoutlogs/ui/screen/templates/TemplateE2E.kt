package com.android.timberworkoutlogs.ui.screen.templates

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.util.scrollToAndAssertElement
import com.android.timberworkoutlogs.util.tryClickBeforeScrollClick
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TemplateE2ETest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

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
    fun createTemplateFlow_isSuccessful() = run {
        val templateName = "A Test Workout Name"
        step("Navigate to create template screen") {
            composeTestRule.onNodeWithText("Templates").performClick()
            composeTestRule.onNodeWithText("Workout Templates").performClick()
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
        }

        step("Create template") {
            composeTestRule.onNodeWithText("Template Name").performTextInput(templateName)
            composeTestRule.onNodeWithText("Add Exercise").performClick()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()
            tryClickBeforeScrollClick(composeTestRule,"Barbell Bench Press")
            composeTestRule.onNodeWithText("Save Template").performClick()
        }

        step("Verify template created") {
            composeTestRule.waitForIdle()
            scrollToAndAssertElement(composeTestRule,templateName)
        }
    }

    @Test
    fun editTemplateFlow_isSuccessful() = run {
        step("Create a template to be edited") {
            composeTestRule.onNodeWithText("Templates").performClick()
            composeTestRule.onNodeWithText("Workout Templates").performClick()
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
            val originalName = "Leg Day"
            composeTestRule.onNodeWithText("Template Name").performTextInput(originalName)
            composeTestRule.onNodeWithText("Save Template").performClick()
        }

        step("Navigate to edit the template") {
            composeTestRule.onNodeWithText("Leg Day").performClick()
        }

        step("Edit the template name and save") {
            val updatedName = "Advanced Leg Day"
            composeTestRule.onNodeWithText("Template Name").performTextClearance()
            composeTestRule.onNodeWithText("Template Name").performTextInput(updatedName)
            composeTestRule.onNodeWithText("Save Template").performClick()
        }

        step("Verify template updated") {
            composeTestRule.onNodeWithText("Advanced Leg Day").assertIsDisplayed()
        }
    }
}
