package com.android.timberworkoutlogs.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.timberworkoutlogs.models.WorkoutExercise
import java.util.UUID

@Dao
interface WorkoutExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExercise>)

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun getExercisesForWorkout(workoutId: Long): List<WorkoutExercise>

    /**
     * Deletes a single exercise row by its own id. Deleting by workoutId + definitionId would
     * take out every duplicate of that exercise in the workout, so the row's unique id is the
     * only safe key when the same exercise appears more than once.
     */
    @Query("DELETE FROM workout_exercises WHERE id = :exerciseId")
    suspend fun deleteWorkoutExercise(exerciseId: UUID)

}
