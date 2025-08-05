package com.android.timberworkoutlogs.ui.screen.settings

import android.content.Context
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.toStringResource
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SettingsInteractionTest : TestCase() {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val kgLabel = context.getString(WeightUnit.KG.toStringResource())
    private val lbLabel = context.getString(WeightUnit.LB.toStringResource())

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun weightUnitSetting_whenChangedToLB_isReflectedInNewWorkout() = run {
        step("Navigate to Settings and change unit to LB") {
            composeTestRule.onNodeWithText("Settings").performClick()
            composeTestRule.onNodeWithText(kgLabel).assertIsDisplayed()
            composeTestRule.onNodeWithText(lbLabel).assertIsDisplayed()
            composeTestRule.onNodeWithText(lbLabel).performClick()
        }

        step("Navigate to Workout screen and add an exercise") {
            composeTestRule.onNodeWithText("Workout").performClick()
            composeTestRule.onNodeWithText("Select Exercise...").performClick()
            composeTestRule.onNodeWithText("Barbell Bench Press").performClick()
        }

        step("Verify that the UnitSwitch is in the 'on' state (representing LB)") {
            composeTestRule.onNode(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Switch
                )
            )
                .assertIsOn()
        }
    }
}
