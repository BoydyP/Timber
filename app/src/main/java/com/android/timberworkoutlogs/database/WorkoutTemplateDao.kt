package com.android.timberworkoutlogs.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {
    @Insert
    suspend fun insertTemplate(workoutTemplate: WorkoutTemplate): Long

    @Update
    suspend fun updateTemplate(workoutTemplate: WorkoutTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplateExercises(exercises: List<TemplateExercise>)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteExercisesForTemplate(templateId: Long)

    @Query("SELECT * FROM workout_templates")
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>

    @Transaction
    @Query("SELECT * FROM workout_templates")
    fun getTemplatesWithExercises(): Flow<List<WorkoutTemplateWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :templateId")
    suspend fun getTemplateWithExercises(templateId: Long): WorkoutTemplateWithExercises
}
