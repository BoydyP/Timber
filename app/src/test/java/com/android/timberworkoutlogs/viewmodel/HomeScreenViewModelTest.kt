package com.android.timberworkoutlogs.viewmodel

import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.services.WorkoutStateHolder
import com.android.timberworkoutlogs.ui.screen.HomeScreenViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeScreenViewModel
    private lateinit var workoutStateHolder: WorkoutStateHolder
    private val isTimerRunningFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        workoutStateHolder = mockk {
            every { isTimerRunning } returns isTimerRunningFlow
        }
        viewModel = HomeScreenViewModel(workoutStateHolder)
    }

    @Test
    fun `isWorkoutInProgress reflects true when timer is running`() = runTest {
        // Given
        isTimerRunningFlow.value = true

        // Then
        assertEquals(true, viewModel.isWorkoutInProgress.value)
    }

    @Test
    fun `isWorkoutInProgress reflects false when timer is not running`() = runTest {
        // Given
        isTimerRunningFlow.value = false

        // Then
        assertEquals(false, viewModel.isWorkoutInProgress.value)
    }

    @Test
    fun `isWorkoutInProgress updates when timer state changes`() = runTest {
        // Initially false
        assertEquals(false, viewModel.isWorkoutInProgress.value)

        // When timer starts
        isTimerRunningFlow.value = true

        // Then state is true
        assertEquals(true, viewModel.isWorkoutInProgress.value)

        // When timer stops
        isTimerRunningFlow.value = false

        // Then state is false again
        assertEquals(false, viewModel.isWorkoutInProgress.value)
    }
}
