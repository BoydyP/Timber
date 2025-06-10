package com.bignerdranch.android.timberworkoutlogs.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bignerdranch.android.timberworkoutlogs.models.Workout

@Database(entities = [Workout::class], version = 1)
//@TypeConverters(CrimeTypeConverters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}