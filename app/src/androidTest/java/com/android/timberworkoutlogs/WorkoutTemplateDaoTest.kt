package com.android.timberworkoutlogs

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.database.ExerciseDefinitionDao
import com.android.timberworkoutlogs.database.WorkoutTemplateDao
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WorkoutTemplate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class WorkoutTemplateDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var workoutTemplateDao: WorkoutTemplateDao
    private lateinit var exerciseDefinitionDao: ExerciseDefinitionDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        workoutTemplateDao = database.workoutTemplateDao()
        exerciseDefinitionDao = database.exerciseDefinitionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private suspend fun newExerciseDefinitionId(): UUID {
        val definition = ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Exercise ${UUID.randomUUID()}",
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = emptyList(),
            logType = LogType.WEIGHT_AND_REPS
        )
        exerciseDefinitionDao.addExerciseDefinition(definition)
        return definition.id
    }

    @Test
    fun replaceTemplateExercises_swapsOldExercisesForNewOnesAsOneUnit() = runBlocking {
        val templateId = workoutTemplateDao.insertTemplate(WorkoutTemplate(name = "Leg Day"))

        val originalExercises = listOf(
            TemplateExercise(
                templateId = templateId,
                definitionId = newExerciseDefinitionId(),
                sets = listOf(WeightAndRepsSet(weight = 100.0, reps = 5))
            ),
            TemplateExercise(
                templateId = templateId,
                definitionId = newExerciseDefinitionId(),
                sets = listOf(WeightAndRepsSet(weight = 60.0, reps = 8))
            )
        )
        workoutTemplateDao.upsertTemplateExercises(originalExercises)
        assertEquals(2, workoutTemplateDao.getTemplateWithExercises(templateId).exercises.size)

        val replacementExercise = TemplateExercise(
            templateId = templateId,
            definitionId = newExerciseDefinitionId(),
            sets = listOf(WeightAndRepsSet(weight = 40.0, reps = 12))
        )
        workoutTemplateDao.replaceTemplateExercises(templateId, listOf(replacementExercise))

        val result = workoutTemplateDao.getTemplateWithExercises(templateId).exercises
        assertEquals(1, result.size)
        assertEquals(replacementExercise.id, result.first().id)
        assertTrue(
            "Old exercises must not survive the replace",
            originalExercises.none { old -> result.any { it.id == old.id } }
        )
    }

    @Test
    fun replaceTemplateExercises_withEmptyList_leavesTemplateWithNoExercises() = runBlocking {
        val templateId = workoutTemplateDao.insertTemplate(WorkoutTemplate(name = "Leg Day"))
        workoutTemplateDao.upsertTemplateExercises(
            listOf(TemplateExercise(templateId = templateId, definitionId = newExerciseDefinitionId()))
        )

        workoutTemplateDao.replaceTemplateExercises(templateId, emptyList())

        assertTrue(workoutTemplateDao.getTemplateWithExercises(templateId).exercises.isEmpty())
    }
}
