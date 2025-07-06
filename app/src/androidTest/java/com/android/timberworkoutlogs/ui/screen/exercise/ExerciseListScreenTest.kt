package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class ExerciseListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    private val repository: ExerciseDefinitionRepository = mockk()

    @Test
    fun whenNoExercisesExist_showsEmptyMessage() {
        every { repository.allExerciseDefinitions } returns flowOf(emptyList())

        val viewModel = ExercisesListViewModel(repository)

        composeTestRule.setContent {
            TimberWorkoutLogsTheme {
                ExerciseListScreen(
                    viewModel = viewModel,
                    onNavigateToCreateExercise = {},
                    onNavigateToEditExercise = {},
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No exercises defined yet. Tap the '+' to add one.")
            .assertIsDisplayed()
    }

    @Test
    fun whenExercisesExist_theyAreDisplayedInList() {
        val fakeExercises = listOf(
            ExerciseDefinition(
                UUID.randomUUID(),
                "Squat",
                ExerciseEquipment.BARBELL,
                listOf(MuscleGroup.LEGS),
                LogType.WEIGHT_AND_REPS
            ),
            ExerciseDefinition(
                UUID.randomUUID(),
                "Pull Ups",
                ExerciseEquipment.BODYWEIGHT,
                listOf(MuscleGroup.BACK),
                LogType.REPS_ONLY
            )
        )
        every { repository.allExerciseDefinitions } returns flowOf(fakeExercises)

        val viewModel = ExercisesListViewModel(repository)

        composeTestRule.setContent {
            TimberWorkoutLogsTheme {
                ExerciseListScreen(
                    viewModel = viewModel,
                    onNavigateToCreateExercise = {},
                    onNavigateToEditExercise = {},
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Barbell Squat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bodyweight Pull Ups").assertIsDisplayed()
    }
}
