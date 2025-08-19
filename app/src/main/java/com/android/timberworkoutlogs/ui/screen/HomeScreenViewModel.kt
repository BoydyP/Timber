package com.android.timberworkoutlogs.ui.screen

import androidx.lifecycle.ViewModel
import com.android.timberworkoutlogs.services.WorkoutStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    workoutStateHolder: WorkoutStateHolder
) : ViewModel() {
    val isWorkoutInProgress: StateFlow<Boolean> = workoutStateHolder.isTimerRunning
}
