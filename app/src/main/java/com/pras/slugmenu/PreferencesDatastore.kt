package com.pras.slugmenu

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val ENABLE_BACKGROUND_UPDATES = booleanPreferencesKey("enable_background_updates")
        val LOCATION_ORDER = stringPreferencesKey("location_order")
        const val TAG = "UserPreferencesRepo"
    }

    // generic setters
    // TODO: update settings to use these?
    suspend fun setBooleanPreference(key: Preferences.Key<Boolean>, userChoice: Boolean) {
        dataStore.edit {preferences ->
            preferences[key] = userChoice
        }
    }

    suspend fun setIntPreference(key: Preferences.Key<Int>, userChoice: Int) {
        dataStore.edit {preferences ->
            preferences[key] = userChoice
        }
    }

    suspend fun setStringPreference(key: Preferences.Key<String>, userChoice: String) {
        dataStore.edit {preferences ->
            preferences[key] = userChoice
        }
    }

    val getListPreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading UI preferences.", it)
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
                Log.e(TAG, "Error reading Theme preference.", it)
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
            Log.e(TAG, "Error setting Theme preference to $themePref.")
        } else {
            dataStore.edit { preferences ->
                preferences[THEME_PREF] = themePref
            }
        }
    }

    val getMaterialYouPreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading Material You preferences.", it)
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

    val getBackgroundUpdatePreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading Background Update preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[ENABLE_BACKGROUND_UPDATES] ?: false
        }

    suspend fun setBackgroundUpdatePreference(userChoice: Boolean) {
        dataStore.edit {preferences ->
            preferences[ENABLE_BACKGROUND_UPDATES] = userChoice
        }
    }

    val getLocationOrder: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading Location Order preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[LOCATION_ORDER] ?: "[{\"navLocation\":\"ninelewis\",\"locationName\":\"Nine/Lewis\",\"visible\":true},{\"navLocation\":\"cowellstev\",\"locationName\":\"Cowell/Stevenson\",\"visible\":true},{\"navLocation\":\"crownmerrill\",\"locationName\":\"Crown/Merrill\",\"visible\":true},{\"navLocation\":\"porterkresge\",\"locationName\":\"Porter/Kresge\",\"visible\":true},{\"navLocation\":\"perkcoffee\",\"locationName\":\"Perks\",\"visible\":true},{\"navLocation\":\"terrafresca\",\"locationName\":\"Terra Fresca\",\"visible\":true},{\"navLocation\":\"portermarket\",\"locationName\":\"Porter Market\",\"visible\":true},{\"navLocation\":\"stevcoffee\",\"locationName\":\"Stevenson Coffee House\",\"visible\":true},{\"navLocation\":\"globalvillage\",\"locationName\":\"Global Village Cafe\",\"visible\":true},{\"navLocation\":\"oakescafe\",\"locationName\":\"Oakes Cafe\",\"visible\":true}]"
        }

    suspend fun setLocationOrder(userChoice: String) {
        dataStore.edit {preferences ->
            preferences[LOCATION_ORDER] = userChoice
        }
    }

}

