package com.pras.slugmenu

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class UserPreferences(val GridView: Boolean)

class PreferencesDatastore(private val dataStore: DataStore<Preferences>) {

    private companion object {
        val USE_LIST_UI = booleanPreferencesKey("use_list_ui")
        val THEME_PREF = intPreferencesKey("theme_pref")
        const val TAG = "UserPreferencesRepo"
    }

    val isGridLayout: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[USE_LIST_UI] ?: false
        }

    suspend fun setListPreference(isGrid: Boolean) {
        dataStore.edit {preferences ->
            preferences[USE_LIST_UI] = isGrid
        }
    }

    val getThemePreference: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[THEME_PREF] ?: 0
        }

    suspend fun setThemePreference(themePref: Int) {
        if (themePref > 2 || themePref < 0) {
            dataStore.edit {preferences ->
                preferences[THEME_PREF] = 0
            }
        } else {
            dataStore.edit { preferences ->
                preferences[THEME_PREF] = themePref
            }
        }
    }
}

