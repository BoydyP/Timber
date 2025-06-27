package com.android.timberworkoutlogs.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.timberworkoutlogs.models.WorkoutExercise

@Dao
interface WorkoutExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExercise>)

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun getExercisesForWorkout(workoutId: Long): List<WorkoutExercise>

}
