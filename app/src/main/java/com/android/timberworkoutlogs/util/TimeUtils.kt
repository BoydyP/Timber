package com.android.timberworkoutlogs.util

import java.util.Calendar

fun getGreetingByTime(): String {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning!"
        in 12 .. 16 -> "Good afternoon!"
        else -> "Good evening!"
    }
}