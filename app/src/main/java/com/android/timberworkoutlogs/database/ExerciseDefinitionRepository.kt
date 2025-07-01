package com.android.timberworkoutlogs.database

import androidx.annotation.WorkerThread
import com.android.timberworkoutlogs.models.ExerciseDefinition
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for managing ExerciseDefinition data operations.
 * It abstracts the data source from the rest of the application.
 */
class ExerciseDefinitionRepository(private val exerciseDefinitionDao: ExerciseDefinitionDao) {

    val allExerciseDefinitions: Flow<List<ExerciseDefinition>> = exerciseDefinitionDao.getExerciseDefinitions()

    suspend fun getExerciseDefinition(id: UUID): ExerciseDefinition {
        return exerciseDefinitionDao.getExerciseDefinition(id)
    }

    @WorkerThread
    suspend fun update(exerciseDefinition: ExerciseDefinition) {
        exerciseDefinitionDao.addExerciseDefinition(exerciseDefinition)
    }
    @WorkerThread
    suspend fun insert(exerciseDefinition: ExerciseDefinition) {
        exerciseDefinitionDao.addExerciseDefinition(exerciseDefinition)
    }
    @WorkerThread
    suspend fun delete(exerciseDefinition: ExerciseDefinition) {
        exerciseDefinitionDao.deleteExerciseDefinition(exerciseDefinition)
    }
}
