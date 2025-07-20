package com.android.timberworkoutlogs.ui.screen.settings

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

    fun updateWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            settingsRepository.setWeightUnit(unit)
        }
    }
}
