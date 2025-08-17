package com.android.timberworkoutlogs

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.database.ExerciseDefinitionDao
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ExerciseDefinitionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var exerciseDefinitionDao: ExerciseDefinitionDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        exerciseDefinitionDao = database.exerciseDefinitionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // BUG-026 test
    @Test
    fun insertDuplicateExerciseDefinition_doesNotCrash() = runBlocking {
        val exercise = ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Test Exercise",
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = listOf(MuscleGroup.CHEST),
            logType = LogType.WEIGHT_AND_REPS
        )

        // Insert the exercise for the first time
        exerciseDefinitionDao.addExerciseDefinition(exercise)

        // Attempt to insert the same exercise again
        exerciseDefinitionDao.addExerciseDefinition(exercise)

        // Verify that the database contains only one copy of the exercise
        val allExercises = exerciseDefinitionDao.getExerciseDefinitions().first()
        assertEquals(1, allExercises.size)
    }
}
