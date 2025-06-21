package com.bignerdranch.android.timberworkoutlogs.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.timberworkoutlogs.database.converters.ExerciseEquipmentConverter
import com.bignerdranch.android.timberworkoutlogs.database.converters.ExerciseSetListConverter
import com.bignerdranch.android.timberworkoutlogs.database.converters.MuscleGroupListConverter
import com.bignerdranch.android.timberworkoutlogs.database.converters.UUIDConverter
import com.bignerdranch.android.timberworkoutlogs.database.data.DefaultExercises
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseDefinition
import com.bignerdranch.android.timberworkoutlogs.models.Workout
import com.bignerdranch.android.timberworkoutlogs.models.WorkoutExercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ ExerciseDefinition::class,
    Workout::class, WorkoutExercise::class ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    UUIDConverter::class,
    ExerciseSetListConverter::class,
    ExerciseEquipmentConverter::class,
    MuscleGroupListConverter::class
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
                    "timber_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.exerciseDefinitionDao())
                }
            }
        }

        suspend fun populateDatabase(exerciseDefinitionDao: ExerciseDefinitionDao) {
            val predefinedExercises = DefaultExercises.getPredefinedExercises()
            predefinedExercises.forEach {
                exerciseDefinitionDao.addExerciseDefinition(it)
            }
        }
    }
}
