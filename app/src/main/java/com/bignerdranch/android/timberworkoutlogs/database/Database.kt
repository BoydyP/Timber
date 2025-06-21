package com.bignerdranch.android.timberworkoutlogs.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.timberworkoutlogs.database.converters.ExerciseEquipmentConverter
import com.bignerdranch.android.timberworkoutlogs.database.converters.ExerciseSetListConverter
import com.bignerdranch.android.timberworkoutlogs.database.converters.MuscleGroupListConverter
import com.bignerdranch.android.timberworkoutlogs.database.converters.UUIDConverter
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseDefinition
import com.bignerdranch.android.timberworkoutlogs.models.Workout
import com.bignerdranch.android.timberworkoutlogs.models.WorkoutExercise

@Database(entities = [ ExerciseDefinition::class, Workout::class, WorkoutExercise::class ], version = 6, exportSchema = false)
@TypeConverters(UUIDConverter::class, ExerciseSetListConverter::class, ExerciseEquipmentConverter::class, MuscleGroupListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDefinitionDao(): ExerciseDefinitionDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timber_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
