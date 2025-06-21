package com.android.timberworkoutlogs.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.android.timberworkoutlogs.models.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    /**
     * Inserts a workout into the database. If the workout already exists, it's ignored.
     * @param workout The workout to be inserted.
     * @return The row ID of the newly inserted workout.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkout(workout: Workout): Long // <-- Changed to return Long

    /**
     * Retrieves all workouts from the database, ordered by start time in descending order.
     * Using Flow ensures that the UI will automatically update when the data changes.
     * @return A Flow emitting a list of all workouts.
     */
    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    /**
     * Update existing workout with new data.
     */
    @Update
    suspend fun updateWorkout(workout: Workout)

    /**
     * For logging - Show count of all workouts
     */
    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun getWorkoutCount(): Int

    /**
     * Retrieves a single workout by its ID.
     * @param id The ID of the workout to retrieve.
     * @return A Flow emitting the specific workout.
     */

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkoutFlow(id: Long): Flow<Workout>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkout(id: Long): Workout?

    @Delete
    suspend fun deleteWorkout(workout: Workout)


}
