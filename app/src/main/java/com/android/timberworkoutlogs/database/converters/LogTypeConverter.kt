package com.android.timberworkoutlogs.database.converters

import androidx.room.TypeConverter
import com.android.timberworkoutlogs.models.LogType

class LogTypeConverter {
    @TypeConverter
    fun toLogType(value: String) = enumValueOf<LogType>(value)

    @TypeConverter
    fun fromLogType(value: LogType) = value.name
}
