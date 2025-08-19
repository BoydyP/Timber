package com.android.timberworkoutlogs.ui.screen.workout

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.models.WeightUnit
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class PlateCalculatorDialogTest : TestCase() {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        // Explicitly set the default state before each test to prevent leaks
        runBlocking {
            settingsRepository.setWeightUnit(WeightUnit.KG)
        }
        // Navigate to the workout screen and open the dialog
        composeTestRule.onNodeWithText("Workout").performClick()
        composeTestRule.onNodeWithContentDescription("Plate Calculator").performClick()
    }

    @After
    fun tearDown() {
        // Clear preferences after each test to ensure a clean slate for the next run
        runBlocking {
            settingsRepository.clearPreferences()
        }
    }

    // --- KG Tests (Default Unit) ---

    @Test
    fun whenSimpleWeightIsEntered_KG_correctPlatesAreDisplayed() = run {
        step("Enter a simple target weight in KG") {
            composeTestRule.onNodeWithText("Total Weight").performTextClearance()
            composeTestRule.onNodeWithText("Total Weight").performTextInput("60")
        }

        step("Verify the correct plates are shown") {
            composeTestRule.onAllNodesWithTag("plate_20.0").assertCountEquals(2)
        }
    }

    @Test
    fun whenComplexWeightIsEntered_KG_correctPlatesAreDisplayed() = run {
        step("Enter a complex target weight in KG") {
            composeTestRule.onNodeWithText("Total Weight").performTextClearance()
            composeTestRule.onNodeWithText("Total Weight").performTextInput("142.5")
        }

        step("Verify the correct plates are shown") {
            composeTestRule.onAllNodesWithTag("plate_25.0").assertCountEquals(4)
            composeTestRule.onAllNodesWithTag("plate_10.0").assertCountEquals(2)
            composeTestRule.onAllNodesWithTag("plate_1.25").assertCountEquals(2)
        }
    }

    @Test
    fun whenPlateIsUnavailable_KG_calculatorUsesNextAvailable() = run {
        step("Find the text field for 25kg plates and set its quantity to 0") {
            composeTestRule.onNodeWithTag("plate_quantity_25.0").performTextClearance()
            composeTestRule.onNodeWithTag("plate_quantity_25.0").performTextInput("0")
        }

        step("Enter a target weight that would normally use 25kg plates") {
            composeTestRule.onNodeWithText("Total Weight").performTextClearance()
            composeTestRule.onNodeWithText("Total Weight").performTextInput("90")
        }

        step("Verify the calculator uses the next available plates (20s and 15s)") {
            composeTestRule.onAllNodesWithTag("plate_20.0").assertCountEquals(2)
            composeTestRule.onAllNodesWithTag("plate_15.0").assertCountEquals(2)
        }
    }

    // --- LB Tests (Overridden Unit) ---

    @Test
    fun whenSimpleWeightIsEntered_LB_correctPlatesAreDisplayed() = run {
        step("Switch to LB unit") {
            composeTestRule.onNodeWithTag("unit_switch").performClick()
            Thread.sleep(500) // Wait for recomposition
        }

        step("Enter a simple target weight in LB") {
            composeTestRule.onNodeWithText("Total Weight").performTextClearance()
            composeTestRule.onNodeWithText("Total Weight").performTextInput("135")
        }

        step("Verify the correct plates are shown") {
            composeTestRule.onAllNodesWithTag("plate_45.0").assertCountEquals(2)
        }
    }

    @Test
    fun whenComplexWeightIsEntered_LB_correctPlatesAreDisplayed() = run {
        step("Switch to LB unit") {
            composeTestRule.onNodeWithTag("unit_switch").performClick()
            Thread.sleep(500) // Wait for recomposition
        }

        step("Enter a complex target weight in LB") {
            composeTestRule.onNodeWithText("Total Weight").performTextClearance()
            composeTestRule.onNodeWithText("Total Weight").performTextInput("287.5")
        }

        step("Verify the correct plates are shown") {
            composeTestRule.onAllNodesWithTag("plate_45.0").assertCountEquals(4)
            composeTestRule.onAllNodesWithTag("plate_25.0").assertCountEquals(2)
            composeTestRule.onAllNodesWithTag("plate_5.0").assertCountEquals(2)
            composeTestRule.onAllNodesWithTag("plate_1.25").assertCountEquals(2)
        }
    }

    @Test
    fun whenPlateIsUnavailable_LB_calculatorUsesNextAvailable() = run {
        step("Switch to LB unit") {
            composeTestRule.onNodeWithTag("unit_switch").performClick()
            Thread.sleep(500) // Wait for recomposition
        }

        step("Find the text field for 45lb plates and set its quantity to 0") {
            composeTestRule.onNodeWithTag("plate_quantity_45.0").performTextClearance()
            composeTestRule.onNodeWithTag("plate_quantity_45.0").performTextInput("0")
        }

        step("Enter a target weight that would normally use 45lb plates") {
            composeTestRule.onNodeWithText("Total Weight").performTextClearance()
            composeTestRule.onNodeWithText("Total Weight").performTextInput("135")
        }

        step("Verify the calculator uses the next available plates (35s and 10s)") {
            composeTestRule.onAllNodesWithTag("plate_35.0").assertCountEquals(2)
            composeTestRule.onAllNodesWithTag("plate_10.0").assertCountEquals(2)
        }
    }
}
