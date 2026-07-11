package com.android.timberworkoutlogs.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.database.WorkoutTemplateDao
import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
import com.android.timberworkoutlogs.database.dataStore
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
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao {
        return db.workoutDao()
    }

    @Provides
    fun provideWorkoutTemplateDao(db: AppDatabase): WorkoutTemplateDao {
        return db.workoutTemplateDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutTemplateRepository(
        db: AppDatabase,
        settingsRepository: SettingsRepository
    ): WorkoutTemplateRepository {
        return WorkoutTemplateRepository(
            db.workoutTemplateDao(),
            db.workoutDao(),
            db.workoutExerciseDao(),
            settingsRepository,
        )
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
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository {
        return SettingsRepository(dataStore)
    }
}
