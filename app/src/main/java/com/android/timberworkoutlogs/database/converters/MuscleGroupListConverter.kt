package com.android.timberworkoutlogs.database.converters

import androidx.room.TypeConverter
import com.android.timberworkoutlogs.models.MuscleGroup

class MuscleGroupListConverter {

    @TypeConverter
    fun fromMuscleGroupList(muscleGroups: List<MuscleGroup>): String {
        return muscleGroups.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toMuscleGroupList(data: String): List<MuscleGroup> {
        return if (data.isBlank()) {
            emptyList()
        } else {
            data.split(",").map { enumValueOf<MuscleGroup>(it) }
        }
    }
}
