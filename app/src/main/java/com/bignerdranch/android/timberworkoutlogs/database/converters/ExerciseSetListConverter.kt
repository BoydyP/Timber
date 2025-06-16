package com.bignerdranch.android.timberworkoutlogs.database.converters

import androidx.room.TypeConverter
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseSet
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExerciseSetListConverter {

    @TypeConverter
    fun fromExerciseSetList(value: List<ExerciseSet>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toExerciseSetList(value: String): List<ExerciseSet> {
        return Json.decodeFromString(value)
    }
}
