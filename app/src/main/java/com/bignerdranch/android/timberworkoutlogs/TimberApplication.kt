package com.bignerdranch.android.timberworkoutlogs

import android.app.Application
import android.util.Log
import com.bignerdranch.android.timberworkoutlogs.database.AppDatabase
import com.bignerdranch.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.bignerdranch.android.timberworkoutlogs.database.WorkoutRepository

private const val TAG = "TimberApplication"
/**
 * Custom Application class to hold singleton instances for the database and repository.
 * This ensures these objects are created only once per application lifecycle.
 */
class TimberApplication : Application() {
    init {
        Log.d(TAG, "Initialising TimberApplication backend")
    }
    val database by lazy { AppDatabase.getDatabase(this) }
    val workoutRepository by lazy {
        WorkoutRepository(database.workoutDao(), database.workoutExerciseDao())
    }
    val exerciseDefinitionRepository by lazy {
        ExerciseDefinitionRepository(database.exerciseDefinitionDao())
    }
}
