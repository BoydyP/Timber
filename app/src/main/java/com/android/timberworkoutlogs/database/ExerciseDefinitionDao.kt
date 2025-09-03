package com.android.timberworkoutlogs.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.android.timberworkoutlogs.models.ExerciseDefinition
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ExerciseDefinitionDao {

    /**
     * Gets a reactive Flow of all exercise definitions from the table, ordered by name.
     * The UI can collect this flow to automatically update when data changes.
     */
    @Query("SELECT * FROM exercise_definitions ORDER BY name ASC")
    fun getExerciseDefinitions(): Flow<List<ExerciseDefinition>>

    /**
     * Searches for exercise definitions by name.
     */
    @Query("SELECT * FROM exercise_definitions WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExerciseDefinitions(query: String): Flow<List<ExerciseDefinition>>

    /**
     * Gets a single exercise definition by its ID.
     */
    @Query("SELECT * FROM exercise_definitions WHERE id=(:id)")
    suspend fun getExerciseDefinition(id: UUID): ExerciseDefinition

    /**
     * Inserts a new exercise definition into the database.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addExerciseDefinition(exercise: ExerciseDefinition)

    /**
     * Modify an existing exercise definitoon.
     */
    @Update
    suspend fun modifyExerciseDefinition(exercise: ExerciseDefinition)

    /**
     * Deletes an exercise definition from the database.
     */
    @Delete
    suspend fun deleteExerciseDefinition(exercise: ExerciseDefinition)

    /**
     * Gets the count of all exercise definitions synchronously (for testing/debugging).
     */
    @Query("SELECT COUNT(*) FROM exercise_definitions")
    suspend fun getExerciseCount(): Int
}
