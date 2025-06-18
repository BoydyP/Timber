package com.bignerdranch.android.timberworkoutlogs.database

import android.util.Log
import com.bignerdranch.android.timberworkoutlogs.models.Workout
import com.bignerdranch.android.timberworkoutlogs.models.WorkoutExercise


private const val TAG = "WorkoutRepository"
/**
 * Repository that provides a clean API for data access to the rest of the application.
 * It abstracts the data sources (in this case, only Room) from the app's business logic.
 *
 * @param workoutDao The Data Access Object for workout operations.
 * @param workoutExerciseDao The Data Access Object for workout operations.
 *
 */
class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao)
    {

    /**
     * Inserts a workout into the database via a coroutine.
     * This is a suspend function, so it must be called from a coroutine or another suspend function.
     *
     * @param workout The workout to be saved.
     */
    suspend fun insertWorkout(workout: Workout): Long {
        return workoutDao.insertWorkout(workout)
    }

    suspend fun getAllWorkoutCount(): Int {
        return workoutDao.getWorkoutCount()
    }

    suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun getWorkout(id: Long): Workout? {
        return workoutDao.getWorkout(id)
    }

    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout)
    }

    /**
     * Inserts a list of child WorkoutExercise objects into the database.
     * This function now correctly calls the workoutExerciseDao that was passed into the constructor.
     */
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExercise>) {
        val tmpLog: WorkoutExercise = exercises[0]
        Log.d(TAG, "Writing exercises. Exercise[0]: ${tmpLog.name}, ${tmpLog.unit}")
        workoutExerciseDao.insertWorkoutExercises(exercises)
    }
}
