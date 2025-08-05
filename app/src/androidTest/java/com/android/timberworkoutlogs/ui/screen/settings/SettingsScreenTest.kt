package com.android.timberworkoutlogs.ui.screen.settings

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.timberworkoutlogs.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SettingsScreenTest : TestCase() {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun changeWeightUnit_isSuccessful() = run {
        step("Navigate to settings screen") {
            composeTestRule.onNodeWithText("Settings").performClick()
        }

        step("Change weight unit to LB") {
            composeTestRule.onNodeWithText("KG").performClick()
            composeTestRule.onNodeWithText("KG").assertIsSelected()

            composeTestRule.onNodeWithText("LB").performClick()
            composeTestRule.onNodeWithText("LB").assertIsSelected()
        }
    }
}
