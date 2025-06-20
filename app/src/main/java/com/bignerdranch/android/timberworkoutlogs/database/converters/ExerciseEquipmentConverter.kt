package com.bignerdranch.android.timberworkoutlogs.database.converters

import androidx.room.TypeConverter
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseEquipment

class ExerciseEquipmentConverter {
    @TypeConverter
    fun toExerciseEquipment(value: String) = enumValueOf<ExerciseEquipment>(value)

    @TypeConverter
    fun fromExerciseEquipment(value: ExerciseEquipment) = value.name
}
