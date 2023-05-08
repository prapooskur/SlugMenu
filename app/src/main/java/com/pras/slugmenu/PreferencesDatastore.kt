package com.pras.slugmenu

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(val GridView: Boolean)
class PreferencesDatastore(val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    val USELIST = booleanPreferencesKey("use_list")

    val useListFlow: Flow<Boolean> = context.dataStore.data
        .map {preferences ->
            preferences[USELIST] ?: false
        }

    suspend fun enableListUI(enable: Boolean) {
        context.dataStore.edit {settings ->
            settings[USELIST] = enable
        }
    }




}