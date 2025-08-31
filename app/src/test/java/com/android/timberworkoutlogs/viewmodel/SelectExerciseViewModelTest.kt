package com.android.timberworkoutlogs.viewmodel

import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.exercise.SelectExerciseViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class SelectExerciseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SelectExerciseViewModel
    private lateinit var repository: ExerciseDefinitionRepository
    private lateinit var allExercisesFlow: MutableStateFlow<List<ExerciseDefinition>>

    private val testExercises = listOf(
        ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Bench Press",
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = listOf(MuscleGroup.CHEST),
            logType = LogType.WEIGHT_AND_REPS
        ),
        ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Squat",
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = listOf(MuscleGroup.LEGS),
            logType = LogType.WEIGHT_AND_REPS
        ),
        ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Push Up",
            equipment = ExerciseEquipment.BODYWEIGHT,
            muscleGroups = listOf(MuscleGroup.CHEST),
            logType = LogType.REPS_ONLY
        ),
        ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Curl",
            equipment = ExerciseEquipment.DUMBBELL,
            muscleGroups = listOf(MuscleGroup.BICEPS),
            logType = LogType.WEIGHT_AND_REPS
        )
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        allExercisesFlow = MutableStateFlow(testExercises)

        every { repository.allExerciseDefinitions } returns allExercisesFlow

        viewModel = SelectExerciseViewModel(repository)
    }

    @Test
    fun `initial state has empty search query`() = runTest {
        // Given & When
        val searchQuery = viewModel.searchQuery.first()

        // Then
        assertEquals("", searchQuery)
    }

    @Test
    fun `filteredExercises shows all exercises when search query is empty`() = runTest {
        // Given & When
        val exercises = viewModel.filteredExercises.first()

        // Then
        assertEquals(testExercises, exercises)
    }

    @Test
    fun `onSearchQueryChange updates search query`() = runTest {
        // Given
        val newQuery = "bench"

        // When
        viewModel.onSearchQueryChange(newQuery)

        // Then
        assertEquals(newQuery, viewModel.searchQuery.first())
    }

    @Test
    fun `filteredExercises filters by computedExerciseName case insensitive`() = runTest {
        // Given
        val searchQuery = "bench"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(1, filteredExercises.size)
        assertEquals("Bench Press", filteredExercises[0].name)
        assertTrue(filteredExercises[0].computedExerciseName.contains("bench", ignoreCase = true))
    }

    @Test
    fun `filteredExercises filters by equipment name in computedExerciseName`() = runTest {
        // Given
        val searchQuery = "barbell"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(2, filteredExercises.size) // Bench Press and Squat are both barbell exercises
        assertTrue(filteredExercises.all { it.equipment == ExerciseEquipment.BARBELL })
    }

    @Test
    fun `filteredExercises handles uppercase search query`() = runTest {
        // Given
        val searchQuery = "SQUAT"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(1, filteredExercises.size)
        assertEquals("Squat", filteredExercises[0].name)
    }

    @Test
    fun `filteredExercises handles mixed case search query`() = runTest {
        // Given
        val searchQuery = "PuSh"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(1, filteredExercises.size)
        assertEquals("Push Up", filteredExercises[0].name)
    }

    @Test
    fun `filteredExercises returns empty list when no matches found`() = runTest {
        // Given
        val searchQuery = "nonexistent"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertTrue(filteredExercises.isEmpty())
    }

    @Test
    fun `filteredExercises filters by partial exercise name`() = runTest {
        // Given
        val searchQuery = "cur"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(1, filteredExercises.size)
        assertEquals("Curl", filteredExercises[0].name)
    }

    @Test
    fun `filteredExercises returns all when search query is whitespace only`() = runTest {
        // Given
        val searchQuery = "   "

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(testExercises, filteredExercises)
    }

    @Test
    fun `filteredExercises updates when search query changes from specific to empty`() = runTest {
        // Given
        viewModel.onSearchQueryChange("bench")
        
        // Verify filtered state
        assertEquals(1, viewModel.filteredExercises.first().size)

        // When
        viewModel.onSearchQueryChange("")

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(testExercises, filteredExercises)
    }

    @Test
    fun `filteredExercises updates when underlying exercises change`() = runTest {
        // Given
        val newExercise = ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Deadlift",
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = listOf(MuscleGroup.BACK),
            logType = LogType.WEIGHT_AND_REPS
        )
        val updatedExercises = testExercises + newExercise

        // When
        allExercisesFlow.value = updatedExercises

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(updatedExercises, filteredExercises)
    }

    @Test
    fun `filteredExercises maintains filter when exercises list updates`() = runTest {
        // Given
        viewModel.onSearchQueryChange("barbell")
        val newExercise = ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Deadlift",
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = listOf(MuscleGroup.BACK),
            logType = LogType.WEIGHT_AND_REPS
        )
        val updatedExercises = testExercises + newExercise

        // When
        allExercisesFlow.value = updatedExercises

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(3, filteredExercises.size) // Original 2 barbell exercises + new deadlift
        assertTrue(filteredExercises.all { it.equipment == ExerciseEquipment.BARBELL })
    }

    @Test
    fun `filteredExercises correctly matches equipment with capitalization`() = runTest {
        // Given - Search for bodyweight (should match "Bodyweight Push Up")
        val searchQuery = "bodyweight"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(1, filteredExercises.size)
        assertEquals("Push Up", filteredExercises[0].name)
        assertEquals(ExerciseEquipment.BODYWEIGHT, filteredExercises[0].equipment)
    }

    @Test
    fun `filteredExercises handles multiple word search in computedExerciseName`() = runTest {
        // Given - Search should match "Dumbbell Curl"
        val searchQuery = "dumbbell curl"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(1, filteredExercises.size)
        assertEquals("Curl", filteredExercises[0].name)
        assertEquals(ExerciseEquipment.DUMBBELL, filteredExercises[0].equipment)
    }

    @Test
    fun `filteredExercises handles special characters in search query`() = runTest {
        // Given
        val searchQuery = "bench-press"

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val filteredExercises = viewModel.filteredExercises.first()
        // Should not match anything since computedExerciseName doesn't contain hyphens
        assertTrue(filteredExercises.isEmpty())
    }

    @Test
    fun `search query updates are independent of filtered results`() = runTest {
        // Given
        val firstQuery = "bench"
        val secondQuery = "squat"

        // When
        viewModel.onSearchQueryChange(firstQuery)
        assertEquals(firstQuery, viewModel.searchQuery.first())
        
        viewModel.onSearchQueryChange(secondQuery)

        // Then
        assertEquals(secondQuery, viewModel.searchQuery.first())
        val filteredExercises = viewModel.filteredExercises.first()
        assertEquals(1, filteredExercises.size)
        assertEquals("Squat", filteredExercises[0].name)
    }
}
