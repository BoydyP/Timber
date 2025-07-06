package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class CreateExerciseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repository: ExerciseDefinitionRepository = mockk(relaxed = true)

    @Test
    fun saveButton_isDisabled_whenNameIsBlank() {
        val viewModel = CreateExerciseViewModel(repository, SavedStateHandle())

        composeTestRule.setContent {
            TimberWorkoutLogsTheme {
                CreateExerciseScreen(
                    viewModel = viewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Initially, the button should be disabled because no muscle groups are selected
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()

        // Select a muscle group
        composeTestRule.onNodeWithText("Chest").performClick()


        // Still should be disabled because the name is blank
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
    }

    @Test
    fun saveButton_isDisabled_whenNoMuscleGroupsSelected() {
        val viewModel = CreateExerciseViewModel(repository, SavedStateHandle())

        composeTestRule.setContent {
            TimberWorkoutLogsTheme {
                CreateExerciseScreen(
                    viewModel = viewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Enter a name
        composeTestRule.onNodeWithText("Exercise Name").performTextInput("Good Mornings")

        // Button should still be disabled because no muscle groups are selected
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
    }

    @Test
    fun saveButton_isEnabled_whenNameAndMuscleGroupsArePresent() {
        val viewModel = CreateExerciseViewModel(repository, SavedStateHandle())

        composeTestRule.setContent {
            TimberWorkoutLogsTheme {
                CreateExerciseScreen(
                    viewModel = viewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Enter a name
        composeTestRule.onNodeWithText("Exercise Name").performTextInput("Bicep Curls")

        // Select a muscle group
        composeTestRule.onNodeWithText("Biceps").performClick()

        // Now the button should be enabled
        composeTestRule.onNodeWithText("Save Exercise").assertIsEnabled()
    }
}
