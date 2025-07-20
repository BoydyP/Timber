package com.android.timberworkoutlogs.database.converters

import androidx.room.TypeConverter
import com.android.timberworkoutlogs.models.WeightUnit

class WeightUnitConverter {
    @TypeConverter
    fun fromWeightUnit(value: WeightUnit?): String? {
        return value?.name
    }

    @TypeConverter
    fun toWeightUnit(value: String?): WeightUnit? {
        return value?.let { WeightUnit.valueOf(it) }
    }
}