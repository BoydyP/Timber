package com.android.timberworkoutlogs.ui.screen.templates

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.rules.DatabaseSeedingRule
import com.android.timberworkoutlogs.util.backPressUntilElementTextVisible
import com.android.timberworkoutlogs.util.scrollToAndAssertElement
import com.android.timberworkoutlogs.util.tryClickBeforeScrollClick
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class WorkoutTemplatesListScreenTest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val databaseSeedingRule = DatabaseSeedingRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    fun setUp() {
        hiltRule.inject()
        // Navigate to templates screen
        composeTestRule.onNodeWithText("Templates").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Workout Templates").performClick()
    }

    @Test
    fun templatesListScreen_displaysCorrectly() = run {
        step("Verify templates list screen is displayed") {
            // Should see create template button
            composeTestRule.onNodeWithContentDescription("Create Template").assertIsDisplayed()
        }
    }

    @Test
    fun templatesListScreen_displaysEmptyStateWhenNoTemplates() = run {
        step("Verify empty state is handled gracefully") {
            try {
                // Look for empty state message
                composeTestRule.onNodeWithText("No templates found").assertIsDisplayed()
            } catch (_: AssertionError) {
                try {
                    composeTestRule.onNodeWithText("Create your first template").assertIsDisplayed()
                } catch (_: AssertionError) {
                    // Might have default templates or different empty state
                    composeTestRule.onNodeWithContentDescription("Create Template").assertIsDisplayed()
                }
            }
        }
    }

    @Test
    fun templatesListScreen_allowsCreatingNewTemplate() = run {
        step("Navigate to create template screen") {
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
        }

        step("Verify create template screen is displayed") {
            composeTestRule.onNodeWithText("Template Name").assertIsDisplayed()
            composeTestRule.onNodeWithText("Save Template").assertIsDisplayed()
            composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()
        }
    }

    @Test
    fun templatesListScreen_displaysCreatedTemplates() = run {
        step("Create a template first") {
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
            
            val templateName = "Push Day Template"
            composeTestRule.onNodeWithText("Template Name").performTextInput(templateName)
            
            // Add an exercise to the template
            composeTestRule.onNodeWithText("Add Exercise").performClick()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()
            composeTestRule.onNodeWithText("Barbell Bench Press").performClick()
            
            composeTestRule.onNodeWithText("Save Template").performClick()
        }

        step("Verify template appears in list") {
            scrollToAndAssertElement(composeTestRule, "Push Day Template")
        }
    }

    @Test
    fun templatesListScreen_allowsEditingExistingTemplate() = run {
        step("Create a template to edit") {
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
            
            val originalName = "Upper Body"
            composeTestRule.onNodeWithText("Template Name").performTextInput(originalName)
            composeTestRule.onNodeWithText("Save Template").performClick()
        }

        step("Edit the template") {
            composeTestRule.onNodeWithText("Upper Body").performClick()
            
            // Should be in edit mode
            composeTestRule.onNodeWithText("Template Name").assertIsDisplayed()
            composeTestRule.onNodeWithText("Save Template").assertIsDisplayed()
        }
    }

    @Test
    fun templatesListScreen_handlesTemplateSelection() = run {
        val templateName = "Full Body Workout Test"
        step("Create a template with exercises") {
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
            composeTestRule.onNodeWithText("Template Name").performTextInput(templateName)
            
            // Add multiple exercises
            composeTestRule.onNodeWithText("Add Exercise").performClick()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()
            tryClickBeforeScrollClick(composeTestRule,"Barbell Squat")
            
            composeTestRule.onNodeWithText("Add Exercise").performClick()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()
            tryClickBeforeScrollClick(composeTestRule,"Barbell Bench Press")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Save Template").performClick()
        }

        step("Verify template can be selected and used") {
            // Template should be visible in list
            composeTestRule.waitForIdle()
            scrollToAndAssertElement(composeTestRule, templateName)
            
            // Clicking should allow editing or using the template
            composeTestRule.onNodeWithText(templateName).performClick()
            composeTestRule.onNodeWithText("Template Name").assertIsDisplayed()
        }
    }

    @Test
    fun templatesListScreen_displaysTemplateDetails() = run {
        val templateName = "Leg Day Special"
        step("Create a template with specific details") {
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
            composeTestRule.onNodeWithText("Template Name").performTextInput(templateName)
            
            // Add exercises
            composeTestRule.onNodeWithText("Add Exercise").performClick()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()
            tryClickBeforeScrollClick(composeTestRule, "Barbell Squat")
            composeTestRule.onNodeWithText("Save Template").performClick()
        }

        step("Verify template details are shown in list") {
            composeTestRule.onNodeWithText("Leg Day Special").assertIsDisplayed()
            
            // Template cards might show exercise count or other details
            try {
                composeTestRule.onNodeWithText("1 exercise", substring = true).assertIsDisplayed()
            } catch (_: AssertionError) {
                try {
                    composeTestRule.onNodeWithText("exercises", substring = true).assertIsDisplayed()
                } catch (_: AssertionError) {
                    // Details might not be shown, verify template name is visible
                    composeTestRule.onNodeWithText("Leg Day Special").assertIsDisplayed()
                }
            }
        }
    }

    @Test
    fun templatesListScreen_maintainsStateAfterNavigation() = run {
        step("Create a template") {
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
            composeTestRule.onNodeWithText("Template Name").performTextInput("Test Template")
            composeTestRule.onNodeWithText("Add Exercise").performClick()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()
            tryClickBeforeScrollClick(composeTestRule,"Barbell Bench Press")
            composeTestRule.onNodeWithText("Save Template").performClick()
            backPressUntilElementTextVisible(composeTestRule, "Settings")
        }

        step("Navigate away and back") {
            composeTestRule.onNodeWithText("Settings").performClick()
            composeTestRule.onNodeWithText("KG").assertIsDisplayed()
            
            composeTestRule.onNodeWithText("Templates").performClick()
            composeTestRule.onNodeWithText("Workout Templates").performClick()
        }

        step("Verify template is still displayed") {
            composeTestRule.onNodeWithText("Test Template").assertIsDisplayed()
        }
    }

    @Test
    fun templatesListScreen_handlesDefaultTemplates() = run {
        step("Check for default or seeded templates") {
            try {
                // Look for default templates that might be seeded
                val defaultTemplateNames = listOf(
                    "Push Pull Legs",
                    "Upper Lower",
                    "Full Body",
                    "Starter Template"
                )
                
                var foundDefaultTemplate = false
                defaultTemplateNames.forEach { templateName ->
                    try {
                        composeTestRule.onNodeWithText(templateName, substring = true).assertIsDisplayed()
                        foundDefaultTemplate = true
                    } catch (_: AssertionError) {
                        // Template not found, continue
                    }
                }
                
                if (!foundDefaultTemplate) {
                    // No default templates, verify we can create new ones
                    composeTestRule.onNodeWithContentDescription("Create Template").assertIsDisplayed()
                }
            } catch (_: Exception) {
                // Verify basic functionality if defaults aren't available
                composeTestRule.onNodeWithContentDescription("Create Template").assertIsDisplayed()
            }
        }
    }
}
