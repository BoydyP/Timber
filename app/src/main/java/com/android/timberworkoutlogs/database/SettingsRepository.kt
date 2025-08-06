package com.android.timberworkoutlogs.database

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.timberworkoutlogs.models.WeightUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme")
    }

    private val _weightUnit = MutableStateFlow(WeightUnit.KG)
    val weightUnit: StateFlow<WeightUnit> = _weightUnit.asStateFlow()

    private val _dynamicTheme = MutableStateFlow(false)
    val dynamicTheme: StateFlow<Boolean> = _dynamicTheme.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            val initialUnit = dataStore.data.map { preferences ->
                val unitName = preferences[PreferencesKeys.WEIGHT_UNIT] ?: WeightUnit.KG.name
                WeightUnit.valueOf(unitName)
            }.first()
            _weightUnit.value = initialUnit

            val initialDynamicTheme = dataStore.data.map { preferences ->
                preferences[PreferencesKeys.DYNAMIC_THEME] ?: true
            }.first()
            _dynamicTheme.value = initialDynamicTheme
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        _weightUnit.value = unit
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit.name
            Log.d("SettingsRepository", "Weight unit updated to $unit")
        }
    }

    suspend fun setDynamicTheme(enabled: Boolean) {
        _dynamicTheme.value = enabled
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_THEME] = enabled
            Log.d("SettingsRepository", "Dynamic theme updated to $enabled")
        }
    }
}
