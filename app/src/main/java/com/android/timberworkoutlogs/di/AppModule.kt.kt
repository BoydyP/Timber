package com.android.timberworkoutlogs.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android.timberworkoutlogs.dataStore
import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(db: AppDatabase): WorkoutRepository {
        return WorkoutRepository(db.workoutDao(), db.workoutExerciseDao())
    }

    @Provides
    @Singleton
    fun provideExerciseDefinitionRepository(db: AppDatabase): ExerciseDefinitionRepository {
        return ExerciseDefinitionRepository(db.exerciseDefinitionDao())
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        // This is the same setup logic from your Application class
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository {
        return SettingsRepository(dataStore)
    }
}
