package com.android.timberworkoutlogs.util

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso

// A modal (e.g. the workout screen's "Import from Template" bottom sheet) keeps the screen
// behind it in the semantics tree, so a plain onNode(hasScrollAction()) becomes ambiguous once
// both scrollables are present - it throws "expected exactly 1 node but found 2". The modal's
// own scrollable is composed after the backdrop's, so .onLast() reliably picks it; with only one
// scrollable on screen this is equivalent to onNode(hasScrollAction()).
private fun ComposeTestRule.onScrollable() = onAllNodes(hasScrollAction()).onLast()


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
            composeTestRule.waitForIdle()
        }
    }
}
// A freshly-opened modal (e.g. the "Import from Template" bottom sheet) can still be settling
// its entrance animation, or its backing list can still be a frame behind the latest DB write,
// when the very next test instruction already tries to scroll/assert against it. A single
// attempt then races that settling; retrying with a short backoff lets it catch up instead of
// failing on a transient, not-yet-stable frame.
private const val SCROLL_ASSERT_ATTEMPTS = 5
private const val SCROLL_ASSERT_RETRY_DELAY_MS = 300L

private inline fun retryOnAssertionError(action: () -> Unit) {
    var lastError: AssertionError? = null
    repeat(SCROLL_ASSERT_ATTEMPTS) {
        try {
            action()
            return
        } catch (e: AssertionError) {
            lastError = e
            Thread.sleep(SCROLL_ASSERT_RETRY_DELAY_MS)
        }
    }
    throw lastError!!
}

fun scrollToAndAssertElement(composeTestRule: ComposeTestRule, elementText: String) {
    retryOnAssertionError {
        composeTestRule.waitForIdle()
        composeTestRule.onScrollable()
            .performScrollToNode(hasText(elementText))
        composeTestRule.onNodeWithText(elementText).assertIsDisplayed()
    }
}

fun scrollToAndAssertClickElement(composeTestRule: ComposeTestRule, elementText: String) {
    retryOnAssertionError {
        composeTestRule.waitForIdle()
        composeTestRule.onScrollable()
            .performScrollToNode(hasText(elementText))
        composeTestRule.onNodeWithText(elementText)
            .assertIsDisplayed()
            .performClick()
    }
}