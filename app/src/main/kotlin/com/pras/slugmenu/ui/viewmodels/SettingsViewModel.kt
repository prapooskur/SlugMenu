package com.pras.slugmenu.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pras.slugmenu.BackgroundDownloadScheduler
import com.pras.slugmenu.MyApplication
import com.pras.slugmenu.data.repositories.MenuRepository
import com.pras.slugmenu.data.repositories.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val themeChoice: Flow<Int>,
    val useAmoledBlack: Flow<Boolean>,
    val useMaterialYou: Flow<Boolean>,
    val useGridUI: Flow<Boolean>,
    val useTwoPanes: Flow<Boolean>,
    val useCollapsingTopBar: Flow<Boolean>,
    val enableBackgroundUpdates: Flow<Boolean>,
    val sendItemNotifications: Flow<Boolean>,
)

data class InitSettingsUiState(
    val themeChoice: Int,
    val useAmoledBlack: Boolean,
    val useMaterialYou: Boolean,
    val useGridUI: Boolean,
    val useTwoPanes: Boolean,
    val useCollapsingTopBar: Boolean,
    val enableBackgroundUpdates: Boolean,
    val sendItemNotifications: Boolean,
)


private const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val menuRepository: MenuRepository,
) : ViewModel() {

    // this is super hacky but it works
    // todo find better solution?
    val initState: InitSettingsUiState = runBlocking {
        InitSettingsUiState(
            themeChoice = preferencesRepository.getThemePreference.first(),
            useAmoledBlack = preferencesRepository.getAmoledPreference.first(),
            useMaterialYou = preferencesRepository.getMaterialYouPreference.first(),
            useGridUI = preferencesRepository.getListPreference.first(),
            useTwoPanes = preferencesRepository.getPanePreference.first(),
            useCollapsingTopBar = preferencesRepository.getToolbarPreference.first(),
            enableBackgroundUpdates = preferencesRepository.getBackgroundUpdatePreference.first(),
            sendItemNotifications = preferencesRepository.getNotificationPreference.first()
        )
    }

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            themeChoice = preferencesRepository.getThemePreference,
            useAmoledBlack = preferencesRepository.getAmoledPreference,
            useMaterialYou = preferencesRepository.getMaterialYouPreference,
            useGridUI = preferencesRepository.getListPreference,
            useTwoPanes = preferencesRepository.getPanePreference,
            useCollapsingTopBar = preferencesRepository.getToolbarPreference,
            enableBackgroundUpdates = preferencesRepository.getBackgroundUpdatePreference,
            sendItemNotifications = preferencesRepository.getNotificationPreference,
        )
    )

    val uiState = _uiState.asStateFlow()

    fun setPreference(key: String, value: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepository.setStringPreference(key, value)
            }
        }
    }

    fun setPreference(key: String, value: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepository.setIntPreference(key, value)
            }
        }
    }

    fun setPreference(key: String, value: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepository.setBoolPreference(key, value)
            }
        }
    }

    fun clearMenuCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                menuRepository.clearLocalCache()
            }
        }
    }

    private val backgroundDownloadScheduler = BackgroundDownloadScheduler
    fun refreshPeriodicWork(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                backgroundDownloadScheduler.refreshPeriodicWork(context)
            }
        }
    }

    fun cancelDownload(context: Context, tag: String = "backgroundMenuDownload") {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                backgroundDownloadScheduler.cancelDownloadByTag(context, tag)
            }
        }
    }

    fun runSingleDownload(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                backgroundDownloadScheduler.runSingleDownload(context)
            }
        }
    }

    // Define ViewModel factory in a companion object
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val preferencesRepository = (this[APPLICATION_KEY] as MyApplication).preferencesRepository
                val menuRepository = (this[APPLICATION_KEY] as MyApplication).menuRepository
                SettingsViewModel(
                    preferencesRepository = preferencesRepository,
                    menuRepository = menuRepository
                )
            }
        }
    }


}