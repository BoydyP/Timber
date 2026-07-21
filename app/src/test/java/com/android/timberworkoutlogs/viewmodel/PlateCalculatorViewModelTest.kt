package com.android.timberworkoutlogs.viewmodel

import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.workout.components.PlateCalculatorViewModel
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
class PlateCalculatorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PlateCalculatorViewModel
    private lateinit var settingsRepository: SettingsRepository
    private val weightUnitFlow = MutableStateFlow(WeightUnit.LB)

    @Before
    fun setup() {
        settingsRepository = mockk {
            every { weightUnit } returns weightUnitFlow
        }
        viewModel = PlateCalculatorViewModel(settingsRepository)
    }

    @Test
    fun `calculatePlates - simple LB case`() = runTest {
        viewModel.onWeightChange("135")
        val expectedPlates = listOf(45.0)
        assertEquals(expectedPlates, viewModel.uiState.value.platesOnBar)
    }

    @Test
    fun `calculatePlates - simple KG case`() = runTest {
        weightUnitFlow.value = WeightUnit.KG
        viewModel.onUnitChange(WeightUnit.KG) // Trigger the unit change
        viewModel.onWeightChange("60") // 20kg bar + 20kg per side
        val expectedPlates = listOf(20.0)
        assertEquals(expectedPlates, viewModel.uiState.value.platesOnBar)
    }

    @Test
    fun `calculatePlates - complex LB case`() = runTest {
        viewModel.onWeightChange("287.5") // 45lb bar + 121.25 per side
        val expectedPlates = listOf(45.0, 45.0, 25.0, 5.0, 1.25)
        assertEquals(expectedPlates, viewModel.uiState.value.platesOnBar)
    }

    @Test
    fun `calculatePlates - with unavailable plates`() = runTest {
        viewModel.onPlateQuantityChange(45.0, "0") // Make 45lb plates unavailable
        viewModel.onWeightChange("135")
        val expectedPlates = listOf(35.0, 10.0)
        assertEquals(expectedPlates, viewModel.uiState.value.platesOnBar)
    }

    @Test
    fun `onWeightChange clamps target to what an odd plate quantity can actually place`() = runTest {
        // Only 3 plates of 45lb available (an odd count), and every other plate type zeroed out.
        viewModel.uiState.value.availablePlates.keys.forEach { plateWeight ->
            if (plateWeight != 45.0) {
                viewModel.onPlateQuantityChange(plateWeight, "0")
            }
        }
        viewModel.onPlateQuantityChange(45.0, "3")

        // Bar (45) + 1 pairable 45lb plate per side (90) = 135, NOT 45 + 3*45 = 180.
        viewModel.onWeightChange("180")

        assertEquals("135", viewModel.uiState.value.targetWeight)
        assertEquals(listOf(45.0), viewModel.uiState.value.platesOnBar)
    }

    @Test
    fun `calculatePlates - weight less than barbell`() = runTest {
        viewModel.onWeightChange("40")
        assertEquals(emptyList<Double>(), viewModel.uiState.value.platesOnBar)
    }

    @Test
    fun `calculatePlates - weight equal to barbell`() = runTest {
        viewModel.onWeightChange("45")
        assertEquals(emptyList<Double>(), viewModel.uiState.value.platesOnBar)
    }

    @Test
    fun `onUnitChange - resets state correctly`() = runTest {
        // Start with LB
        assertEquals(WeightUnit.LB, viewModel.uiState.value.unit)
        assertEquals("45", viewModel.uiState.value.barbellWeight)

        // Switch to KG
        viewModel.onUnitChange(WeightUnit.KG)

        // Verify state is updated for KG
        assertEquals(WeightUnit.KG, viewModel.uiState.value.unit)
        assertEquals("20", viewModel.uiState.value.barbellWeight)
        val expectedKgPlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
        assertEquals(
            expectedKgPlates.sortedDescending(),
            viewModel.uiState.value.availablePlates.keys.sortedDescending()
        )
    }
}
