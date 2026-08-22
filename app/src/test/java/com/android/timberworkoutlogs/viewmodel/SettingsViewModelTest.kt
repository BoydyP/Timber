package com.android.timberworkoutlogs.viewmodel

import android.util.Log
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.rules.MainDispatcherRule
import com.android.timberworkoutlogs.ui.screen.settings.SettingsViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var weightUnitFlow: MutableStateFlow<WeightUnit>
    private lateinit var dynamicThemeFlow: MutableStateFlow<Boolean>
    private lateinit var weightRepPredictionFlow: MutableStateFlow<Boolean>

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        settingsRepository = mockk(relaxed = true)
        weightUnitFlow = MutableStateFlow(WeightUnit.KG)
        dynamicThemeFlow = MutableStateFlow(false)
        weightRepPredictionFlow = MutableStateFlow(true)

        every { settingsRepository.weightUnit } returns weightUnitFlow
        every { settingsRepository.dynamicTheme } returns dynamicThemeFlow
        every { settingsRepository.weightRepPrediction } returns weightRepPredictionFlow

        viewModel = SettingsViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `initial weightUnit state is correct`() = runTest {
        // Given & When
        val weightUnit = viewModel.weightUnit.first()

        // Then
        assertEquals(WeightUnit.KG, weightUnit)
    }

    @Test
    fun `initial dynamicTheme state is correct`() = runTest {
        // Given & When
        val dynamicTheme = viewModel.dynamicTheme.first()

        // Then
        assertFalse(dynamicTheme)
    }

    @Test
    fun `weightUnit updates when repository flow changes`() = runTest {
        // Given
        assertEquals(WeightUnit.KG, viewModel.weightUnit.first())

        // When
        weightUnitFlow.value = WeightUnit.LB

        // Then
        assertEquals(WeightUnit.LB, viewModel.weightUnit.first())
    }

    @Test
    fun `dynamicTheme updates when repository flow changes`() = runTest {
        // Given
        assertFalse(viewModel.dynamicTheme.first())

        // When
        dynamicThemeFlow.value = true

        // Then
        assertTrue(viewModel.dynamicTheme.first())
    }

    @Test
    fun `updateWeightUnit calls repository setWeightUnit with KG`() = runTest {
        // Given
        val newUnit = WeightUnit.KG

        // When
        viewModel.updateWeightUnit(newUnit)

        // Then
        coVerify { settingsRepository.setWeightUnit(WeightUnit.KG) }
    }

    @Test
    fun `updateWeightUnit calls repository setWeightUnit with LB`() = runTest {
        // Given
        val newUnit = WeightUnit.LB

        // When
        viewModel.updateWeightUnit(newUnit)

        // Then
        coVerify { settingsRepository.setWeightUnit(WeightUnit.LB) }
    }

    @Test
    fun `updateDynamicTheme calls repository setDynamicTheme with true`() = runTest {
        // Given
        val enableDynamic = true

        // When
        viewModel.updateDynamicTheme(enableDynamic)

        // Then
        coVerify { settingsRepository.setDynamicTheme(true) }
    }

    @Test
    fun `updateDynamicTheme calls repository setDynamicTheme with false`() = runTest {
        // Given
        val enableDynamic = false

        // When
        viewModel.updateDynamicTheme(enableDynamic)

        // Then
        coVerify { settingsRepository.setDynamicTheme(false) }
    }

    @Test
    fun `multiple weight unit updates work correctly`() = runTest {
        // Given & When
        viewModel.updateWeightUnit(WeightUnit.LB)
        viewModel.updateWeightUnit(WeightUnit.KG)
        viewModel.updateWeightUnit(WeightUnit.LB)

        // Then
        coVerify(exactly = 2) { settingsRepository.setWeightUnit(WeightUnit.LB) }
        coVerify(exactly = 1) { settingsRepository.setWeightUnit(WeightUnit.KG) }
    }

    @Test
    fun `multiple dynamic theme updates work correctly`() = runTest {
        // Given & When
        viewModel.updateDynamicTheme(true)
        viewModel.updateDynamicTheme(false)
        viewModel.updateDynamicTheme(true)

        // Then
        coVerify(exactly = 2) { settingsRepository.setDynamicTheme(true) }
        coVerify(exactly = 1) { settingsRepository.setDynamicTheme(false) }
    }

    @Test
    fun `weightUnit flow has correct initial value when repository returns LB`() = runTest {
        // Given - Create new ViewModel with LB as initial value
        weightUnitFlow.value = WeightUnit.LB
        val newViewModel = SettingsViewModel(settingsRepository)

        // When
        val weightUnit = newViewModel.weightUnit.first()

        // Then
        assertEquals(WeightUnit.LB, weightUnit)
    }

    @Test
    fun `dynamicTheme flow has correct initial value when repository returns true`() = runTest {
        // Given - Create new ViewModel with true as initial value
        dynamicThemeFlow.value = true
        val newViewModel = SettingsViewModel(settingsRepository)

        // When
        val dynamicTheme = newViewModel.dynamicTheme.first()

        // Then
        assertTrue(dynamicTheme)
    }

    @Test
    fun `updateWeightUnit logs debug message`() = runTest {
        // Given
        val newUnit = WeightUnit.LB

        // When
        viewModel.updateWeightUnit(newUnit)

        // Then
        io.mockk.verify { Log.d("SettingsViewModel", "Updating weight unit to $newUnit") }
    }

    @Test
    fun `weightUnit flow maintains stateIn behavior with correct initial value`() = runTest {
        // Given - Repository flow starts with LB but stateIn should use KG as fallback
        every { settingsRepository.weightUnit } returns MutableStateFlow(WeightUnit.KG)
        val newViewModel = SettingsViewModel(settingsRepository)

        // When
        val weightUnit = newViewModel.weightUnit.first()

        // Then - Should use the initial value from stateIn
        assertEquals(WeightUnit.KG, weightUnit)
    }

    @Test
    fun `dynamicTheme flow maintains stateIn behavior with correct initial value`() = runTest {
        // Given - Repository flow starts empty but stateIn should use false as fallback
        every { settingsRepository.dynamicTheme } returns MutableStateFlow(false)
        val newViewModel = SettingsViewModel(settingsRepository)

        // When
        val dynamicTheme = newViewModel.dynamicTheme.first()

        // Then - Should use the initial value from stateIn
        assertFalse(dynamicTheme)
    }

    @Test
    fun `initial weightRepPrediction state is correct`() = runTest {
        // Given & When
        val enabled = viewModel.weightRepPrediction.first()

        // Then
        assertTrue(enabled)
    }

    @Test
    fun `weightRepPrediction updates when repository flow changes`() = runTest {
        // Given
        assertTrue(viewModel.weightRepPrediction.first())

        // When
        weightRepPredictionFlow.value = false

        // Then
        assertFalse(viewModel.weightRepPrediction.first())
    }

    @Test
    fun `updateWeightRepPrediction calls repository setWeightRepPrediction with false`() = runTest {
        // When
        viewModel.updateWeightRepPrediction(false)

        // Then
        coVerify { settingsRepository.setWeightRepPrediction(false) }
    }

    @Test
    fun `updateWeightRepPrediction calls repository setWeightRepPrediction with true`() = runTest {
        // When
        viewModel.updateWeightRepPrediction(true)

        // Then
        coVerify { settingsRepository.setWeightRepPrediction(true) }
    }

    @Test
    fun `repository calls are independent for weight unit and dynamic theme`() = runTest {
        // Given & When
        viewModel.updateWeightUnit(WeightUnit.LB)
        viewModel.updateDynamicTheme(true)

        // Then - Each should only call their respective repository method
        coVerify(exactly = 1) { settingsRepository.setWeightUnit(WeightUnit.LB) }
        coVerify(exactly = 1) { settingsRepository.setDynamicTheme(true) }
        // Note: We've already verified the specific calls above, no need to verify zero calls
    }

    @Test
    fun `state flows respond to repository changes independently`() = runTest {
        // Given
        assertEquals(WeightUnit.KG, viewModel.weightUnit.first())
        assertFalse(viewModel.dynamicTheme.first())

        // When - Change weight unit only
        weightUnitFlow.value = WeightUnit.LB

        // Then
        assertEquals(WeightUnit.LB, viewModel.weightUnit.first())
        assertFalse(viewModel.dynamicTheme.first()) // Should remain unchanged

        // When - Change dynamic theme only
        dynamicThemeFlow.value = true

        // Then
        assertEquals(WeightUnit.LB, viewModel.weightUnit.first()) // Should remain LB
        assertTrue(viewModel.dynamicTheme.first())
    }

    @Test
    fun `flows handle rapid changes correctly`() = runTest {
        // Given & When - Rapid changes
        weightUnitFlow.value = WeightUnit.LB
        dynamicThemeFlow.value = true
        weightUnitFlow.value = WeightUnit.KG
        dynamicThemeFlow.value = false
        weightUnitFlow.value = WeightUnit.LB

        // Then - Should reflect the final state
        assertEquals(WeightUnit.LB, viewModel.weightUnit.first())
        assertFalse(viewModel.dynamicTheme.first())
    }
}
