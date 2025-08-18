package com.android.timberworkoutlogs.ui.screen.workout.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.models.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlateCalculatorUiState(
    val targetWeight: String = "45",
    val barbellWeight: String = "45",
    val unit: WeightUnit = WeightUnit.LB,
    val availablePlates: Map<Double, String> = mapOf(), // Changed to String for input
    val platesOnBar: List<Double> = emptyList()
)

@HiltViewModel
class PlateCalculatorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlateCalculatorUiState())
    val uiState: StateFlow<PlateCalculatorUiState> = _uiState.asStateFlow()

    var weightUnitFromSettings: WeightUnit = WeightUnit.LB
        private set

    private val kgPlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    private val lbPlates = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5, 1.25)
    private val maxPlatesPerType = 8

    init {
        viewModelScope.launch {
            settingsRepository.weightUnit.first().let { unit ->
                weightUnitFromSettings = unit
                val plates = if (unit == WeightUnit.KG) kgPlates else lbPlates
                val barbell = if (unit == WeightUnit.KG) "20" else "45"
                _uiState.update {
                    it.copy(
                        unit = unit,
                        barbellWeight = barbell,
                        targetWeight = barbell,
                        availablePlates = plates.associateWith { maxPlatesPerType.toString() }
                    )
                }
                calculatePlates()
            }
        }
    }

    fun onWeightChange(weightStr: String) {
        val filteredText = weightStr.filter { it.isDigit() || it == '.' }.take(6)
        val weightValue = filteredText.toDoubleOrNull() ?: 0.0

        // Calculate max possible weight and cap the input
        val barbellWeight = _uiState.value.barbellWeight.toDoubleOrNull() ?: 0.0
        val maxPlatesWeight = _uiState.value.availablePlates.entries
            .sumOf { (plateValue, quantityStr) ->
                val quantity = quantityStr.toIntOrNull() ?: 0
                plateValue * quantity
            }
        val maxPossibleWeight = barbellWeight + maxPlatesWeight

        val newTargetWeight = if (weightValue > maxPossibleWeight && maxPossibleWeight > 0) {
            maxPossibleWeight.toString().removeSuffix(".0")
        } else {
            filteredText
        }

        _uiState.update { it.copy(targetWeight = newTargetWeight) }
        calculatePlates()
    }

    fun onBarbellWeightChange(weight: String) {
        _uiState.update { it.copy(barbellWeight = weight.filter { it.isDigit() || it == '.' }) }
        calculatePlates()
    }

    fun onUnitChange(unit: WeightUnit) {
        val plates = if (unit == WeightUnit.KG) kgPlates else lbPlates
        val barbell = if (unit == WeightUnit.KG) "20" else "45"
        _uiState.update {
            it.copy(
                unit = unit,
                barbellWeight = barbell,
                targetWeight = barbell,
                availablePlates = plates.associateWith { maxPlatesPerType.toString() }
            )
        }
        calculatePlates()
    }

    fun onPlateQuantityChange(weight: Double, quantityStr: String) {
        val filteredText = quantityStr.filter { it.isDigit() }.take(2)
        val quantity = filteredText.toIntOrNull()

        if (quantity != null && quantity > maxPlatesPerType) {
            val updatedPlates = _uiState.value.availablePlates.toMutableMap()
            updatedPlates[weight] = maxPlatesPerType.toString()
            _uiState.update { it.copy(availablePlates = updatedPlates) }
        } else {
            val updatedPlates = _uiState.value.availablePlates.toMutableMap()
            updatedPlates[weight] = filteredText
            _uiState.update { it.copy(availablePlates = updatedPlates) }
        }
        calculatePlates()
    }

    private fun calculatePlates() {
        val state = _uiState.value
        val targetWeight = state.targetWeight.toDoubleOrNull() ?: 0.0
        val barbellWeight = state.barbellWeight.toDoubleOrNull() ?: 0.0
        if (targetWeight <= barbellWeight) {
            _uiState.update { it.copy(platesOnBar = emptyList()) }
            return
        }

        var weightPerSide = (targetWeight - barbellWeight) / 2.0
        val platesForSide = mutableListOf<Double>()

        state.availablePlates.keys.sortedDescending().forEach { plateWeight ->
            val totalPlatesOfType = (state.availablePlates[plateWeight] ?: "0").toIntOrNull() ?: 0
            val platesPerSide = totalPlatesOfType / 2
            var platesUsedOnThisSide = 0

            while (weightPerSide >= plateWeight && platesUsedOnThisSide < platesPerSide) {
                platesForSide.add(plateWeight)
                weightPerSide -= plateWeight
                platesUsedOnThisSide++
            }
        }

        _uiState.update { it.copy(platesOnBar = platesForSide) }
    }
}
