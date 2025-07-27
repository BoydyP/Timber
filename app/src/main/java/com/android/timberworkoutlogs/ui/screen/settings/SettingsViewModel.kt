package com.android.timberworkoutlogs.ui.screen.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.models.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val weightUnit: StateFlow<WeightUnit> = settingsRepository.weightUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeightUnit.KG
        )

    val dynamicTheme: StateFlow<Boolean> = settingsRepository.dynamicTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateWeightUnit(unit: WeightUnit) {
        Log.d("SettingsViewModel", "Updating weight unit to $unit")
        viewModelScope.launch {
            settingsRepository.setWeightUnit(unit)
        }
    }

    fun updateDynamicTheme(useDynamicTheme: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicTheme(useDynamicTheme)
        }
    }
}
