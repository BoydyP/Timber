package com.android.timberworkoutlogs.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A Singleton class that holds the state of the active workout, used to avoid context leak
 * when a workout is in progress.*/
@Singleton
class WorkoutStateHolder @Inject constructor() {
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    fun setTimerRunning(isRunning: Boolean) {
        _isTimerRunning.value = isRunning
    }
}
