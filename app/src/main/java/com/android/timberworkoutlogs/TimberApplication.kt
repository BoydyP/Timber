package com.android.timberworkoutlogs

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import dagger.hilt.android.HiltAndroidApp


private const val TAG = "TimberApplication"
private const val SETTINGS_PREFERENCES = "settings_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_PREFERENCES
)

/**
 * Custom Application class to hold singleton instances for the database and repository.
 * This ensures these objects are created only once per application lifecycle.
 */
@HiltAndroidApp
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
    val settingsRepository by lazy {
        SettingsRepository(dataStore)
    }
}
