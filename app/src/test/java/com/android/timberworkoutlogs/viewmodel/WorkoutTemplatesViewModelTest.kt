package com.android.timberworkoutlogs.viewmodel

import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExerciseCount
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.templates.WorkoutTemplatesViewModel
import io.mockk.coVerify
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

@ExperimentalCoroutinesApi
class WorkoutTemplatesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WorkoutTemplatesViewModel
    private lateinit var workoutTemplateRepository: WorkoutTemplateRepository
    private lateinit var templatesFlow: MutableStateFlow<List<WorkoutTemplateWithExerciseCount>>

    private val testTemplates = listOf(
        WorkoutTemplateWithExerciseCount(
            workoutTemplate = WorkoutTemplate(
                id = 1L,
                name = "Push Day"
            ),
            exerciseCount = 5
        ),
        WorkoutTemplateWithExerciseCount(
            workoutTemplate = WorkoutTemplate(
                id = 2L,
                name = "Pull Day"
            ),
            exerciseCount = 4
        ),
        WorkoutTemplateWithExerciseCount(
            workoutTemplate = WorkoutTemplate(
                id = 3L,
                name = "Leg Day"
            ),
            exerciseCount = 6
        ),
        WorkoutTemplateWithExerciseCount(
            workoutTemplate = WorkoutTemplate(
                id = 4L,
                name = "Upper Body"
            ),
            exerciseCount = 0
        )
    )

    @Before
    fun setUp() {
        workoutTemplateRepository = mockk(relaxed = true)
        templatesFlow = MutableStateFlow(testTemplates)

        every { workoutTemplateRepository.getAllTemplatesWithExerciseCount() } returns templatesFlow

        viewModel = WorkoutTemplatesViewModel(workoutTemplateRepository)
    }

    @Test
    fun `templates flow exposes repository data correctly`() = runTest {
        // Given & When
        val templates = viewModel.templates.first()

        // Then
        assertEquals(testTemplates, templates)
        assertEquals(4, templates.size)
        
        // Verify specific template data
        val pushDay = templates.find { it.workoutTemplate.name == "Push Day" }!!
        assertEquals(1L, pushDay.workoutTemplate.id)
        assertEquals(5, pushDay.exerciseCount)
        
        val pullDay = templates.find { it.workoutTemplate.name == "Pull Day" }!!
        assertEquals(2L, pullDay.workoutTemplate.id)
        assertEquals(4, pullDay.exerciseCount)
    }

    @Test
    fun `templates flow updates when repository data changes`() = runTest {
        // Given - Initial state
        assertEquals(testTemplates, viewModel.templates.first())

        // When - Repository data changes
        val newTemplate = WorkoutTemplateWithExerciseCount(
            workoutTemplate = WorkoutTemplate(
                id = 5L,
                name = "Full Body"
            ),
            exerciseCount = 8
        )
        val updatedTemplates = testTemplates + newTemplate
        templatesFlow.value = updatedTemplates

        // Then
        val templates = viewModel.templates.first()
        assertEquals(5, templates.size)
        assertEquals(updatedTemplates, templates)
        
        val fullBody = templates.find { it.workoutTemplate.name == "Full Body" }!!
        assertEquals(5L, fullBody.workoutTemplate.id)
        assertEquals(8, fullBody.exerciseCount)
    }

    @Test
    fun `templates flow handles empty list`() = runTest {
        // Given
        templatesFlow.value = emptyList()

        // When
        val templates = viewModel.templates.first()

        // Then
        assertTrue(templates.isEmpty())
        assertEquals(0, templates.size)
    }

    @Test
    fun `templates flow handles single template`() = runTest {
        // Given
        val singleTemplate = listOf(testTemplates[0])
        templatesFlow.value = singleTemplate

        // When
        val templates = viewModel.templates.first()

        // Then
        assertEquals(1, templates.size)
        assertEquals(singleTemplate, templates)
        assertEquals("Push Day", templates[0].workoutTemplate.name)
        assertEquals(5, templates[0].exerciseCount)
    }

    @Test
    fun `deleteTemplate calls repository deleteTemplate`() = runTest {
        // Given
        val templateToDelete = testTemplates[0].workoutTemplate

        // When
        viewModel.deleteTemplate(templateToDelete)

        // Then
        coVerify { workoutTemplateRepository.deleteTemplate(templateToDelete) }
    }

    @Test
    fun `deleteTemplate handles multiple deletions`() = runTest {
        // Given
        val firstTemplate = testTemplates[0].workoutTemplate
        val secondTemplate = testTemplates[1].workoutTemplate
        val thirdTemplate = testTemplates[2].workoutTemplate

        // When
        viewModel.deleteTemplate(firstTemplate)
        viewModel.deleteTemplate(secondTemplate)
        viewModel.deleteTemplate(thirdTemplate)

        // Then
        coVerify { workoutTemplateRepository.deleteTemplate(firstTemplate) }
        coVerify { workoutTemplateRepository.deleteTemplate(secondTemplate) }
        coVerify { workoutTemplateRepository.deleteTemplate(thirdTemplate) }
    }

    @Test
    fun `deleteTemplate with same template multiple times calls repository multiple times`() = runTest {
        // Given
        val templateToDelete = testTemplates[0].workoutTemplate

        // When
        viewModel.deleteTemplate(templateToDelete)
        viewModel.deleteTemplate(templateToDelete)

        // Then
        coVerify(exactly = 2) { workoutTemplateRepository.deleteTemplate(templateToDelete) }
    }

    @Test
    fun `templates flow maintains order from repository`() = runTest {
        // Given - Reorder templates
        val reorderedTemplates = listOf(
            testTemplates[2], // Leg Day
            testTemplates[0], // Push Day
            testTemplates[3], // Upper Body
            testTemplates[1]  // Pull Day
        )
        templatesFlow.value = reorderedTemplates

        // When
        val templates = viewModel.templates.first()

        // Then - Should maintain the new order
        assertEquals(4, templates.size)
        assertEquals("Leg Day", templates[0].workoutTemplate.name)
        assertEquals("Push Day", templates[1].workoutTemplate.name)
        assertEquals("Upper Body", templates[2].workoutTemplate.name)
        assertEquals("Pull Day", templates[3].workoutTemplate.name)
    }

    @Test
    fun `templates flow handles templates with zero exercises`() = runTest {
        // Given - Template with zero exercises
        val templatesWithZeroExercises = listOf(
            WorkoutTemplateWithExerciseCount(
                workoutTemplate = WorkoutTemplate(id = 1L, name = "Empty Template"),
                exerciseCount = 0
            )
        )
        templatesFlow.value = templatesWithZeroExercises

        // When
        val templates = viewModel.templates.first()

        // Then
        assertEquals(1, templates.size)
        assertEquals("Empty Template", templates[0].workoutTemplate.name)
        assertEquals(0, templates[0].exerciseCount)
    }

    @Test
    fun `templates flow handles templates with large exercise counts`() = runTest {
        // Given - Template with large exercise count
        val templateWithManyExercises = listOf(
            WorkoutTemplateWithExerciseCount(
                workoutTemplate = WorkoutTemplate(id = 1L, name = "Marathon Workout"),
                exerciseCount = 100
            )
        )
        templatesFlow.value = templateWithManyExercises

        // When
        val templates = viewModel.templates.first()

        // Then
        assertEquals(1, templates.size)
        assertEquals("Marathon Workout", templates[0].workoutTemplate.name)
        assertEquals(100, templates[0].exerciseCount)
    }

    @Test
    fun `templates flow handles repository data replacement`() = runTest {
        // Given - Initial templates
        assertEquals(testTemplates, viewModel.templates.first())

        // When - Complete replacement of data
        val completelyNewTemplates = listOf(
            WorkoutTemplateWithExerciseCount(
                workoutTemplate = WorkoutTemplate(id = 10L, name = "New Template 1"),
                exerciseCount = 3
            ),
            WorkoutTemplateWithExerciseCount(
                workoutTemplate = WorkoutTemplate(id = 11L, name = "New Template 2"),
                exerciseCount = 7
            )
        )
        templatesFlow.value = completelyNewTemplates

        // Then
        val templates = viewModel.templates.first()
        assertEquals(2, templates.size)
        assertEquals(completelyNewTemplates, templates)
        
        // Verify none of the original templates are present
        assertTrue(templates.none { it.workoutTemplate.name == "Push Day" })
        assertTrue(templates.none { it.workoutTemplate.name == "Pull Day" })
        assertTrue(templates.none { it.workoutTemplate.name == "Leg Day" })
        
        // Verify new templates are present
        assertTrue(templates.any { it.workoutTemplate.name == "New Template 1" })
        assertTrue(templates.any { it.workoutTemplate.name == "New Template 2" })
    }

    @Test
    fun `deleteTemplate works with templates that have different IDs but same names`() = runTest {
        // Given - Templates with same name but different IDs
        val template1 = WorkoutTemplate(id = 1L, name = "Same Name")
        val template2 = WorkoutTemplate(id = 2L, name = "Same Name")

        // When
        viewModel.deleteTemplate(template1)
        viewModel.deleteTemplate(template2)

        // Then - Should call repository for each distinct template object
        coVerify { workoutTemplateRepository.deleteTemplate(template1) }
        coVerify { workoutTemplateRepository.deleteTemplate(template2) }
    }

    @Test
    fun `templates stateIn behavior maintains correct initial value`() = runTest {
        // Given - New ViewModel with empty repository flow
        val emptyFlow = MutableStateFlow<List<WorkoutTemplateWithExerciseCount>>(emptyList())
        every { workoutTemplateRepository.getAllTemplatesWithExerciseCount() } returns emptyFlow
        val newViewModel = WorkoutTemplatesViewModel(workoutTemplateRepository)

        // When
        val templates = newViewModel.templates.first()

        // Then - Should start with empty list as per stateIn initial value
        assertTrue(templates.isEmpty())
    }

    @Test
    fun `templates flow survives rapid data changes`() = runTest {
        // Given & When - Rapid successive changes
        templatesFlow.value = emptyList()
        templatesFlow.value = listOf(testTemplates[0])
        templatesFlow.value = testTemplates.take(2)
        templatesFlow.value = testTemplates
        templatesFlow.value = testTemplates.drop(1)

        // Then - Should reflect the final state
        val templates = viewModel.templates.first()
        assertEquals(3, templates.size)
        assertEquals(testTemplates.drop(1), templates)
    }

    @Test
    fun `templates preserves all properties from WorkoutTemplateWithExerciseCount`() = runTest {
        // Given & When
        val templates = viewModel.templates.first()

        // Then - Verify all properties are preserved
        templates.forEachIndexed { index, template ->
            val original = testTemplates[index]
            assertEquals(original.workoutTemplate.id, template.workoutTemplate.id)
            assertEquals(original.workoutTemplate.name, template.workoutTemplate.name)
            assertEquals(original.exerciseCount, template.exerciseCount)
        }
    }
}
