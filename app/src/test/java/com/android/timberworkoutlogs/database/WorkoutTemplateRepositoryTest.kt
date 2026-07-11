package com.android.timberworkoutlogs.database

import com.android.timberworkoutlogs.fixtures.squatExerciseFixture
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExercises
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WorkoutTemplateRepositoryTest {

    private lateinit var workoutTemplateDao: WorkoutTemplateDao
    private lateinit var workoutDao: WorkoutDao
    private lateinit var workoutExerciseDao: WorkoutExerciseDao
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: WorkoutTemplateRepository

    @Before
    fun setUp() {
        workoutTemplateDao = mockk(relaxed = true)
        workoutDao = mockk(relaxed = true)
        workoutExerciseDao = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        repository = WorkoutTemplateRepository(
            workoutTemplateDao,
            workoutDao,
            workoutExerciseDao,
            settingsRepository,
        )
    }

    @Test
    fun `createWorkoutFromTemplate tags new exercises with the user's current weight unit`() =
        runTest {
            // Given the user's preferred unit is LB (not the WorkoutExercise default of KG).
            every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.LB)

            val templateId = 1L
            val definitionId = squatExerciseFixture().id
            val templateExercise = TemplateExercise(
                templateId = templateId,
                definitionId = definitionId,
                sets = listOf(WeightAndRepsSet(weight = 100.0, reps = 5, isDone = true))
            )
            coEvery { workoutTemplateDao.getTemplateWithExercises(templateId) } returns
                WorkoutTemplateWithExercises(
                    template = WorkoutTemplate(id = templateId, name = "Leg Day"),
                    exercises = listOf(templateExercise)
                )
            coEvery { workoutDao.insertWorkout(any()) } returns 42L

            repository.createWorkoutFromTemplate(templateId)

            // Then the persisted WorkoutExercise must carry the user's actual unit, not the
            // WorkoutExercise default (KG), otherwise a LB-preference user's template-started
            // workout silently mislabels every logged weight as KG.
            coVerify {
                workoutExerciseDao.insertWorkoutExercises(match { exercises ->
                    exercises.size == 1 && exercises.first().unit == WeightUnit.LB
                })
            }
        }
}
