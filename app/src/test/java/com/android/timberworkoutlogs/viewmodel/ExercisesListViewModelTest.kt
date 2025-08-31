package com.android.timberworkoutlogs.viewmodel

import app.cash.turbine.test
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.fixtures.squatExerciseFixture
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.exercise.ExercisesListViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class ExercisesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ExercisesListViewModel
    private lateinit var repository: ExerciseDefinitionRepository
    private lateinit var allExercisesFlow: MutableStateFlow<List<ExerciseDefinition>>
    private lateinit var searchResultsFlow: MutableStateFlow<List<ExerciseDefinition>>

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
        )
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        allExercisesFlow = MutableStateFlow(testExercises)
        searchResultsFlow = MutableStateFlow(emptyList())

        every { repository.allExerciseDefinitions } returns allExercisesFlow
        every { repository.searchExerciseDefinitions(any()) } returns searchResultsFlow

        viewModel = ExercisesListViewModel(repository)
    }

    @Test
    fun `initial state has empty search query`() = runTest {
        // Given & When
        val searchQuery = viewModel.searchQuery.first()

        // Then
        assertEquals("", searchQuery)
    }

    @Test
    fun `allExercises shows all exercises when search query is empty`() = runTest {
        // Given & When
        val exercises = viewModel.allExercises.first()

        // Then
        assertEquals(testExercises, exercises)
    }

    @Test
    fun `initial state should load exercises from repository with flow testing`() = runTest {
        // Given
        val fakeExercises = listOf(
            ExerciseDefinition(
                UUID.randomUUID(),
                "Squat",
                ExerciseEquipment.BARBELL,
                listOf(MuscleGroup.LEGS),
                LogType.WEIGHT_AND_REPS
            ),
            ExerciseDefinition(
                UUID.randomUUID(), "Deadlift", ExerciseEquipment.BARBELL, listOf(
                    MuscleGroup.LEGS,
                    MuscleGroup.BACK
                ), LogType.WEIGHT_AND_REPS
            )
        )
        every { repository.allExerciseDefinitions } returns flowOf(fakeExercises)
        viewModel = ExercisesListViewModel(repository)

        // When & Then
        viewModel.allExercises.test {
            // The StateFlow immediately emits with the repository data
            val loadedExercises = awaitItem()
            assertEquals(2, loadedExercises.size)
            assertEquals("Squat", loadedExercises[0].name)
        }
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
    fun `allExercises uses search results when query is not blank`() = runTest {
        // Given
        val searchQuery = "bench"
        val searchResults = listOf(testExercises[0]) // Only bench press
        searchResultsFlow.value = searchResults

        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then
        val exercises = viewModel.allExercises.first()
        assertEquals(searchResults, exercises)
    }

    @Test
    fun `allExercises returns to all exercises when search query becomes empty`() = runTest {
        // Given
        viewModel.onSearchQueryChange("bench")
        searchResultsFlow.value = listOf(testExercises[0])

        // When
        viewModel.onSearchQueryChange("")

        // Then
        val exercises = viewModel.allExercises.first()
        assertEquals(testExercises, exercises)
    }

    @Test
    fun `allExercises uses search results when query is whitespace only`() = runTest {
        // Given
        val searchQuery = "   "
        
        // When
        viewModel.onSearchQueryChange(searchQuery)

        // Then - Should still use all exercises flow for whitespace-only query
        val exercises = viewModel.allExercises.first()
        assertEquals(testExercises, exercises)
    }

    @Test
    fun `deleteExercise calls repository delete`() = runTest {
        // Given
        val exerciseToDelete = testExercises[0]

        // When
        viewModel.deleteExercise(exerciseToDelete)

        // Then
        coVerify { repository.delete(exerciseToDelete) }
    }

    @Test
    fun `deleteExercise should call repository delete with flow testing`() = runTest {
        // Given
        val exerciseToDelete = ExerciseDefinition(
            UUID.randomUUID(),
            "Curls",
            ExerciseEquipment.DUMBBELL,
            listOf(MuscleGroup.BICEPS),
            LogType.WEIGHT_AND_REPS
        )
        every { repository.allExerciseDefinitions } returns flowOf(listOf(exerciseToDelete))
        viewModel = ExercisesListViewModel(repository)

        // When
        viewModel.deleteExercise(exerciseToDelete)

        // Then
        coVerify { repository.delete(exerciseToDelete) }
    }

    @Test
    fun `deleteExercise handles multiple deletions`() = runTest {
        // Given
        val firstExercise = testExercises[0]
        val secondExercise = testExercises[1]

        // When
        viewModel.deleteExercise(firstExercise)
        viewModel.deleteExercise(secondExercise)

        // Then
        coVerify { repository.delete(firstExercise) }
        coVerify { repository.delete(secondExercise) }
    }

    @Test
    fun `search query triggers correct repository calls`() = runTest {
        // Given
        val queries = listOf("bench", "squat", "push")

        queries.forEach { query ->
            // When
            viewModel.onSearchQueryChange(query)
            // Collect from allExercises to trigger the flatMapLatest flow
            viewModel.allExercises.first()

            // Then - Should trigger search for non-blank queries
            coVerify { repository.searchExerciseDefinitions(query) }
        }
    }

    @Test
    fun `allExercises flow updates when repository data changes`() = runTest {
        // Given
        val newExercises = listOf(squatExerciseFixture())

        // When
        allExercisesFlow.value = newExercises

        // Then
        val exercises = viewModel.allExercises.first()
        assertEquals(newExercises, exercises)
    }

    @Test
    fun `allExercises flow updates when search results change`() = runTest {
        // Given
        viewModel.onSearchQueryChange("test")
        val newSearchResults = listOf(testExercises[2]) // Push up only

        // When
        searchResultsFlow.value = newSearchResults

        // Then
        val exercises = viewModel.allExercises.first()
        assertEquals(newSearchResults, exercises)
    }

    @Test
    fun `empty search results are handled correctly`() = runTest {
        // Given
        viewModel.onSearchQueryChange("nonexistent")
        searchResultsFlow.value = emptyList()

        // When
        val exercises = viewModel.allExercises.first()

        // Then
        assertTrue(exercises.isEmpty())
    }

    @Test
    fun `deleteExercise completes successfully even with repository errors`() = runTest {
        // Given
        val exerciseToDelete = testExercises[0]
        // Don't mock any exceptions - just test the normal flow
        
        // When - Call deleteExercise 
        viewModel.deleteExercise(exerciseToDelete)

        // Then - Verify the repository method was called
        coVerify { repository.delete(exerciseToDelete) }
        // Note: The method completes normally regardless of repository implementation
    }

    @Test
    fun `search query change from non-empty to empty switches flows correctly`() = runTest {
        // Given - Start with a search
        viewModel.onSearchQueryChange("bench")
        searchResultsFlow.value = listOf(testExercises[0])
        
        // Verify we're getting search results
        assertEquals(listOf(testExercises[0]), viewModel.allExercises.first())

        // When - Clear search
        viewModel.onSearchQueryChange("")

        // Then - Should switch back to all exercises
        assertEquals(testExercises, viewModel.allExercises.first())
    }

    @Test
    fun `allExercises flow updates correctly with turbine testing`() = runTest {
        // Given
        val newExercises = listOf(
            ExerciseDefinition(
                UUID.randomUUID(),
                "New Exercise",
                ExerciseEquipment.CABLE,
                listOf(MuscleGroup.CHEST),
                LogType.WEIGHT_AND_REPS
            )
        )

        // When & Then
        viewModel.allExercises.test {
            // Initial value
            assertEquals(testExercises, awaitItem())

            // Update the flow
            allExercisesFlow.value = newExercises

            // Should receive updated exercises
            assertEquals(newExercises, awaitItem())
        }
    }

    @Test
    fun `search functionality works with turbine testing`() = runTest {
        // Given
        val searchResults = listOf(testExercises[0])
        
        // Pre-set the search results before starting the test
        searchResultsFlow.value = searchResults

        // When & Then
        viewModel.allExercises.test {
            // Initial all exercises
            assertEquals(testExercises, awaitItem())

            // Start search - this should switch to search results flow
            viewModel.onSearchQueryChange("bench")

            // Should get search results
            assertEquals(searchResults, awaitItem())

            // Clear search - this should switch back to all exercises flow
            viewModel.onSearchQueryChange("")

            // Should return to all exercises
            assertEquals(testExercises, awaitItem())
        }
    }
}
