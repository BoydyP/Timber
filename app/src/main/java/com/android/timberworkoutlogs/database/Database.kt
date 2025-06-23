package com.android.timberworkoutlogs.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.timberworkoutlogs.database.converters.ExerciseEquipmentConverter
import com.android.timberworkoutlogs.database.converters.ExerciseSetListConverter
import com.android.timberworkoutlogs.database.converters.LogTypeConverter
import com.android.timberworkoutlogs.database.converters.MuscleGroupListConverter
import com.android.timberworkoutlogs.database.converters.UUIDConverter
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise

@Database(entities = [ ExerciseDefinition::class, Workout::class, WorkoutExercise::class ], version = 1, exportSchema = false)
@TypeConverters(
    UUIDConverter::class,
    ExerciseSetListConverter::class,
    ExerciseEquipmentConverter::class,
    MuscleGroupListConverter::class,
    LogTypeConverter::class
)
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
                    "timber_database.db"
                )
                    .createFromAsset("database/timber_database.db")
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
