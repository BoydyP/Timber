package com.bignerdranch.android.timberworkoutlogs.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bignerdranch.android.timberworkoutlogs.models.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    /**
     * Inserts a workout into the database. If the workout already exists, it's ignored.
     * @param workout The workout to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkout(workout: Workout)

    /**
     * Retrieves all workouts from the database, ordered by start time in descending order.
     * Using Flow ensures that the UI will automatically update when the data changes.
     * @return A Flow emitting a list of all workouts.
     */
    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    /**
     * Retrieves a single workout by its ID.
     * @param id The ID of the workout to retrieve.
     * @return A Flow emitting the specific workout.
     */
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkoutById(id: Long): Flow<Workout>

    @Delete
    suspend fun deleteWorkout(workout: Workout)


}
