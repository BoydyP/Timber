package com.android.timberworkoutlogs.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.timberworkoutlogs.models.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme")
    }

    val weightUnit: Flow<WeightUnit> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.WEIGHT_UNIT]
            ?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() }
            ?: WeightUnit.KG
    }

    val dynamicTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DYNAMIC_THEME] ?: true
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        dataStore.edit { it[PreferencesKeys.WEIGHT_UNIT] = unit.name }
    }

    suspend fun setDynamicTheme(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DYNAMIC_THEME] = enabled }
    }

    suspend fun clearPreferences() {
        dataStore.edit { it.clear() }
    }
}
