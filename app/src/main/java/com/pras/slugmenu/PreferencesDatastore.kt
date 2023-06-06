package com.pras.slugmenu

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferencesDatastore(private val dataStore: DataStore<Preferences>) {

    private companion object {
        val USE_GRID_UI = booleanPreferencesKey("use_grid_ui")
        val THEME_PREF = intPreferencesKey("theme_pref")
        val USE_MATERIAL_YOU = booleanPreferencesKey("use_material_you")
        val USE_AMOLED_BLACK = booleanPreferencesKey("use_amoled_black")
        val USE_COLLAPSING_TOOLBAR = booleanPreferencesKey("use_collapsing_toolbar")
        const val TAG = "UserPreferencesRepo"
    }

    val getListPreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[USE_GRID_UI] ?: true
        }

    suspend fun setListPreference(userChoice: Boolean) {
        dataStore.edit {preferences ->
            preferences[USE_GRID_UI] = userChoice
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
            Log.e(TAG, "Error setting themepref to $themePref.")
        } else {
            dataStore.edit { preferences ->
                preferences[THEME_PREF] = themePref
            }
        }
    }

    val getMaterialYouPreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading material you preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[USE_MATERIAL_YOU] ?: true
        }

    suspend fun setMaterialYouPreference(userChoice: Boolean) {
        dataStore.edit {preferences ->
            preferences[USE_MATERIAL_YOU] = userChoice
        }
    }

    val getAmoledPreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading AMOLED preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[USE_AMOLED_BLACK] ?: false
        }

    suspend fun setAmoledPreference(userChoice: Boolean) {
        dataStore.edit {preferences ->
            preferences[USE_AMOLED_BLACK] = userChoice
        }
    }

    val getToolbarPreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading Top Bar preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[USE_COLLAPSING_TOOLBAR] ?: false
        }

    suspend fun setToolbarPreference(userChoice: Boolean) {
        dataStore.edit {preferences ->
            preferences[USE_COLLAPSING_TOOLBAR] = userChoice
        }
    }
}

