package com.pras.slugmenu.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pras.slugmenu.LocationOrderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

// no associated dataSource, directly pulls from datastore
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private companion object {
        val USE_GRID_UI = booleanPreferencesKey("use_grid_ui")
        val THEME_PREF = intPreferencesKey("theme_pref")
        val USE_MATERIAL_YOU = booleanPreferencesKey("use_material_you")
        val USE_AMOLED_BLACK = booleanPreferencesKey("use_amoled_black")
        val USE_COLLAPSING_TOOLBAR = booleanPreferencesKey("use_collapsing_toolbar")
        val USE_TWO_PANES = booleanPreferencesKey("use_two_panes")
        val ENABLE_BACKGROUND_UPDATES = booleanPreferencesKey("enable_background_updates")
        val SEND_ITEM_NOTIFICATIONS = booleanPreferencesKey("send_item_notifications")
        val LOCATION_ORDER = stringPreferencesKey("location_order")
        const val TAG = "UserPreferencesRepo"
    }

    suspend fun setStringPreference(prefKey: String, prefValue: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(prefKey)] = prefValue
        }
    }

    suspend fun setIntPreference(prefKey: String, prefValue: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(prefKey)] = prefValue
        }
    }

    suspend fun setBoolPreference(prefKey: String, prefValue: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(prefKey)] = prefValue
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
            preferences[USE_COLLAPSING_TOOLBAR] ?: true
        }

    suspend fun setPanePreference(userChoice: Boolean) {
        dataStore.edit {preferences ->
            preferences[USE_TWO_PANES] = userChoice
        }
    }

    val getPanePreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading Top Bar preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[USE_TWO_PANES] ?: true
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

    val getNotificationPreference: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading Notification preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[SEND_ITEM_NOTIFICATIONS] ?: false
        }

    suspend fun setNotificationPreference(userChoice: Boolean) {
        dataStore.edit {preferences ->
            preferences[SEND_ITEM_NOTIFICATIONS] = userChoice
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
            val defaultLocationOrder = listOf(
                LocationOrderItem(navLocation = "ninelewis", locationName = "Nine/Lewis", visible = true),
                LocationOrderItem(navLocation = "cowellstev", locationName = "Cowell/Stevenson", visible = true),
                LocationOrderItem(navLocation = "crownmerrill", locationName = "Crown/Merrill", visible = true),
                LocationOrderItem(navLocation = "porterkresge", locationName = "Porter/Kresge", visible = true),
                LocationOrderItem(navLocation = "carsonoakes", locationName = "Carson/Oakes", visible = true),
                LocationOrderItem(navLocation = "perkcoffee", locationName = "Perk Coffee Bars", visible = true),
                LocationOrderItem(navLocation = "terrafresca", locationName = "Terra Fresca", visible = true),
                LocationOrderItem(navLocation = "portermarket", locationName = "Porter Market", visible = true),
                LocationOrderItem(navLocation = "stevcoffee", locationName = "Stevenson Coffee House", visible = true),
                LocationOrderItem(navLocation = "globalvillage", locationName = "Global Village Cafe", visible = false),
                LocationOrderItem(navLocation = "oakescafe", locationName = "Oakes Cafe", visible = true)
            )
            preferences[LOCATION_ORDER] ?: Json.encodeToString(defaultLocationOrder)
        }

    suspend fun setLocationOrder(userChoice: String) {
        dataStore.edit {preferences ->
            preferences[LOCATION_ORDER] = userChoice
        }
    }

}

