package com.android.timberworkoutlogs.ui.screen.workout

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.rules.DatabaseSeedingRule
import com.android.timberworkoutlogs.util.tryClickBeforeScrollClick
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val EXERCISE = "Barbell Bench Press"
private const val TEMPLATE = "Dupes"

@HiltAndroidTest
class TemplateWorkoutDuplicateDeleteTest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val databaseSeedingRule = DatabaseSeedingRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun cardCount() =
        composeTestRule.onAllNodesWithText(EXERCISE).fetchSemanticsNodes().size

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun getDatabase(): AppDatabase
    }

    private fun database(): AppDatabase {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return EntryPointAccessors.fromApplication(context, DbEntryPoint::class.java).getDatabase()
    }

    /**
     * The instrumentation database is not wiped between runs, so workouts left behind by earlier
     * runs would be counted too. Everything is scoped to workouts created after this baseline.
     */
    private var baselineWorkoutId = 0L

    private fun maxWorkoutId(): Long = runBlocking {
        database().workoutDao().getAllWorkouts().first().maxOfOrNull { it.id } ?: 0L
    }

    /** Exercise rows belonging to the workouts this test created. */
    private fun exerciseRowsForWorkoutUnderTest(): Int = runBlocking {
        val db = database()
        db.workoutDao().getAllWorkouts().first()
            .filter { it.id > baselineWorkoutId }
            .sumOf { db.workoutExerciseDao().getExercisesForWorkout(it.id).size }
    }

    /** Human-readable dump of the workouts under test, used in assertion messages. */
    private fun workoutDump(): String = runBlocking {
        val db = database()
        val lines = mutableListOf<String>()
        for (workout in db.workoutDao().getAllWorkouts().first()) {
            val rows = db.workoutExerciseDao().getExercisesForWorkout(workout.id)
            lines += "id=${workout.id} name=${workout.name} rows=${rows.size}" +
                rows.joinToString(prefix = "[", postfix = "]") { "${it.id}/${it.sets.size}" }
        }
        lines.joinToString(" | ") + " (baseline=$baselineWorkoutId)"
    }

    private fun addDuplicateExercise() {
        composeTestRule.onNodeWithText("Add Exercise").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Select Exercise...").performClick()
        composeTestRule.waitForIdle()
        tryClickBeforeScrollClick(composeTestRule, EXERCISE)
        composeTestRule.waitForIdle()
    }

    @Test
    fun deletingOneDuplicateInATemplateStartedWorkout_persists() = run {
        step("Create a template holding the same exercise twice") {
            baselineWorkoutId = maxWorkoutId()
            composeTestRule.onNodeWithText("Templates").performClick()
            composeTestRule.onNodeWithText("Workout Templates").performClick()
            composeTestRule.onNodeWithContentDescription("Create Template").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Template Name").performTextInput(TEMPLATE)
            addDuplicateExercise()
            addDuplicateExercise()
            assertEquals(2, cardCount())
            composeTestRule.onNodeWithText("Save Template").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(800)
        }

        step("Start a workout from that template") {
            composeTestRule.onNodeWithText(TEMPLATE).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithContentDescription("Start Workout").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(1000)
            assertEquals("workout should open with both duplicates", 2, cardCount())
        }

        step("Delete the first duplicate") {
            composeTestRule.onAllNodesWithText(EXERCISE).onFirst()
                .performTouchInput { swipeLeft() }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Delete").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(500)
            assertEquals("deleting one duplicate removed both", 1, cardCount())
        }

        step("The database must not still hold the deleted exercise") {
            assertEquals(
                "deleted exercise row survived in the database: " + workoutDump(),
                1,
                exerciseRowsForWorkoutUnderTest()
            )
        }

        step("Log a set on the survivor and finish the workout") {
            // The template already gave the survivor one set, so fill that in rather than adding
            // another - a second set would make the Weight/Reps matchers ambiguous.
            composeTestRule.onAllNodesWithText("Weight", substring = true).onFirst()
                .performTextInput("100")
            composeTestRule.onAllNodesWithText("Reps").onFirst().performTextInput("5")
            composeTestRule.onNodeWithTag("checkbox_1").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Complete workout").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Are you sure?").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(1500)
        }

        step("The saved workout must hold a single exercise") {
            assertEquals(
                "the deleted duplicate came back after finishing: " + workoutDump(),
                1,
                exerciseRowsForWorkoutUnderTest()
            )
        }
    }
}
