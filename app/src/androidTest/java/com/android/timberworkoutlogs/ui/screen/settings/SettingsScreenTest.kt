package com.android.timberworkoutlogs.ui.screen.settings

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.ui.navigation.TimberUi
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SettingsScreenTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun navigateToSettingsScreen() {
        composeTestRule.onNodeWithText("Settings").performClick()
    }

    @Test
    fun changeWeightUnit_isSuccessful() {
        navigateToSettingsScreen()

        // Click LB to change unit
        composeTestRule.onNodeWithText("KG").performClick()

        // Default should be KG
        composeTestRule.onNodeWithText("KG").assertIsSelected()

        // Click LB to change unit
        composeTestRule.onNodeWithText("LB").performClick()

        // Verify LB is now selected
        composeTestRule.onNodeWithText("LB").assertIsSelected()
    }
}