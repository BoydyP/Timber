package com.bignerdranch.android.timberworkoutlogs.util
import java.util.Locale

fun capitaliseEnum(name: String): String {
    return name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
