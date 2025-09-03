package com.android.timberworkoutlogs.util

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso


fun tryClickBeforeScrollClick(composeTestRule: ComposeTestRule, elementText: String){
    try {
        composeTestRule.onNodeWithText(elementText)
            .assertIsDisplayed()
            .performClick()
    } catch (_: AssertionError) {
        scrollToAndAssertClickElement(composeTestRule,elementText)
    }
}

fun backPressUntilElementTextVisible(composeTestRule: ComposeTestRule, elementText: String) {
    var elementVisible = false
    while (!elementVisible) {
        try {
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(elementText)
                .assertIsDisplayed()
            elementVisible = true
        } catch (_: AssertionError) {
            Espresso.pressBack()
        }
    }
}
fun scrollToAndAssertElement(composeTestRule: ComposeTestRule, elementText: String) {
    try {
        composeTestRule.onNodeWithText(elementText).assertIsDisplayed()

    } catch (_: Exception) {
        composeTestRule.onNode(hasScrollAction())
            .performScrollToNode(hasText(elementText))
        composeTestRule.onNodeWithText(elementText).assertIsDisplayed()
    }
}

fun scrollToAndAssertClickElement(composeTestRule: ComposeTestRule, elementText: String) {
    try {
        composeTestRule.onNode(hasScrollAction())
            .performScrollToNode(hasText(elementText))
        composeTestRule.onNodeWithText(elementText)
            .assertIsDisplayed()
            .performClick()
    } catch (_: Exception) {
        composeTestRule.onNodeWithText(elementText).assertIsDisplayed().performClick()
    }
}