package com.android.timberworkoutlogs.ui.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking

const val TEST_SETTINGS = "test_settings_preferences"
private lateinit var dataStore: DataStore<Preferences>

val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(
    name = TEST_SETTINGS
)

fun sharedSetUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    dataStore = context.testDataStore
    runBlocking {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
