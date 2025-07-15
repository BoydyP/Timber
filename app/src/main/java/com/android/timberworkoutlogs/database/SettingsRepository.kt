package com.android.timberworkoutlogs.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.timberworkoutlogs.models.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
    }

    val weightUnit: Flow<WeightUnit> = dataStore.data
        .map { preferences ->
            val unitName = preferences[PreferencesKeys.WEIGHT_UNIT] ?: WeightUnit.KG.name
            WeightUnit.valueOf(unitName)
        }

    suspend fun setWeightUnit(unit: WeightUnit) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit.name
        }
    }
}
