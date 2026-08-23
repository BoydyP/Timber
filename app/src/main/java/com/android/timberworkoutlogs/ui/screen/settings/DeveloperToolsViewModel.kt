package com.android.timberworkoutlogs.ui.screen.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.DatabaseInitializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the debug-only "Developer" section of the settings screen.
 *
 * Kept separate from [SettingsViewModel] so that the user-facing settings have no
 * dependency on development tooling.
 */
@HiltViewModel
class DeveloperToolsViewModel @Inject constructor(
    private val databaseInitializer: DatabaseInitializer
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)

    /** True while a seed or wipe is running, so the buttons can disable themselves. */
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun regenerateDemoHistory() = launchTask("regenerate demo history") {
        databaseInitializer.regenerateDemoHistory()
    }

    fun clearWorkoutHistory() = launchTask("clear workout history") {
        databaseInitializer.clearWorkoutHistory()
    }

    private fun launchTask(description: String, block: suspend () -> Unit) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to $description", e)
            } finally {
                _isBusy.value = false
            }
        }
    }

    private companion object {
        const val TAG = "DeveloperToolsViewModel"
    }
}
