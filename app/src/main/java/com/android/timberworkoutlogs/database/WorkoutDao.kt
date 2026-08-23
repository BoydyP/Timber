package com.android.timberworkoutlogs.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import androidx.room.Embedded
import androidx.room.Relation
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExercise>
)

@Dao
interface WorkoutDao {

    /**
     * Inserts a workout into the database. If the workout already exists, it's ignored.
     * @param workout The workout to be inserted.
     * @return The row ID of the newly inserted workout.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkout(workout: Workout): Long

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

    /**
     * Deletes every logged workout. The `workout_exercises` rows go with them, via the
     * foreign key's CASCADE, so this clears all workout history while leaving the exercise
     * and template catalog intact.
     */
    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()

    /**
     * Retrieves all workouts with their exercises from a given start date.
     * @param startTimeMillis The start date in milliseconds.
     * @return A Flow emitting a list of Workouts with their associated exercises.
     */
    @Transaction
    @Query("SELECT * FROM workouts WHERE startTime >= :startTimeMillis ORDER BY startTime DESC")
    fun getWorkoutsWithExercisesFrom(startTimeMillis: Long): Flow<List<WorkoutWithExercises>>

    /**
     * Get distinct exercise definitions that have been performed in workouts.
     * @return A Flow emitting a list of exercise definitions with workout history.
     */
    @Transaction
    @Query("""
        SELECT DISTINCT ed.* FROM exercise_definitions ed 
        INNER JOIN workout_exercises we ON ed.id = we.definitionId 
        INNER JOIN workouts w ON we.workoutId = w.id 
        ORDER BY ed.name ASC
    """)
    fun getExerciseDefinitionsWithWorkoutHistory(): Flow<List<ExerciseDefinition>>

    /**
     * Get exercise history data for a specific exercise within a time range.
     * @param definitionId The ID of the exercise definition to get history for.
     * @param fromTime The start time in milliseconds.
     * @return A Flow emitting workout exercises for the specified exercise and time range.
     */
    @Transaction
    @Query("""
        SELECT we.*, w.startTime as workoutStartTime FROM workout_exercises we 
        INNER JOIN workouts w ON we.workoutId = w.id 
        WHERE we.definitionId = :definitionId AND w.startTime >= :fromTime 
        ORDER BY w.startTime ASC
    """)
    fun getExerciseHistoryData(definitionId: UUID, fromTime: Long): Flow<List<WorkoutExerciseWithDate>>

    /**
     * Get the most recently logged WorkoutExercise for a given exercise definition, so a newly
     * added exercise slot can be pre-filled with what was lifted last time.
     * @param definitionId The ID of the exercise definition to look up.
     * @return The WorkoutExercise from the most recent workout that included this exercise, or
     * null if it has never been logged before.
     */
    @Query("""
        SELECT we.* FROM workout_exercises we
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE we.definitionId = :definitionId
        ORDER BY w.startTime DESC
        LIMIT 1
    """)
    suspend fun getMostRecentWorkoutExercise(definitionId: UUID): WorkoutExercise?

    /**
     * Get count of workouts for each exercise (for display in exercise selection).
     * @return A Flow emitting exercise definitions with their workout counts.
     */
    @Transaction
    @Query("""
        SELECT ed.*, COUNT(DISTINCT we.workoutId) as workoutCount 
        FROM exercise_definitions ed 
        INNER JOIN workout_exercises we ON ed.id = we.definitionId 
        INNER JOIN workouts w ON we.workoutId = w.id 
        GROUP BY ed.id, ed.name, ed.equipment, ed.muscleGroups, ed.logType 
        ORDER BY workoutCount DESC, ed.name ASC
    """)
    fun getExerciseDefinitionsWithWorkoutCounts(): Flow<List<ExerciseDefinitionWithCount>>

    /**
     * Get maximum weight lifted for specific exercises (for personal records display).
     * This query finds the highest weight lifted for each of the specified exercises.
     * @param exerciseNames List of exercise names to get max lifts for
     * @return A Flow emitting max lift data for the specified exercises
     */
    @Transaction
    @Query("""
        SELECT 
            ed.name,
            ed.equipment,
            MAX(json_extract(we.sets, '$[0].weight')) as maxWeight,
            we.unit,
            w.startTime as achievedDate
        FROM exercise_definitions ed
        INNER JOIN workout_exercises we ON ed.id = we.definitionId
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE ed.name IN (:exerciseNames) AND ed.equipment = 'BARBELL'
        AND json_extract(we.sets, '$[0].weight') IS NOT NULL
        GROUP BY ed.name, ed.equipment, we.unit
        ORDER BY ed.name ASC
    """)
    fun getPersonalRecordsMaxLifts(exerciseNames: List<String>): Flow<List<MaxLiftData>>
}

/**
 * Data class to represent exercise with workout date information
 */
data class WorkoutExerciseWithDate(
    @Embedded val workoutExercise: WorkoutExercise,
    val workoutStartTime: Long
)

/**
 * Data class for exercise definition with workout counts
 */
data class ExerciseDefinitionWithCount(
    @Embedded val exerciseDefinition: ExerciseDefinition,
    val workoutCount: Int
)

/**
 * Data class for max lift information
 */
data class MaxLiftData(
    val name: String,
    val equipment: String,
    val maxWeight: Double,
    val unit: String,
    val achievedDate: Long
)
