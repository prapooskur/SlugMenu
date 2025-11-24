package com.pras.slugmenu.ui.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pras.slugmenu.Favorite
import com.pras.slugmenu.MyApplication
import com.pras.slugmenu.data.repositories.MenuRepository
import com.pras.slugmenu.data.sources.HoursList
import com.pras.slugmenu.data.sources.MenuSection
import com.pras.slugmenu.ui.elements.exceptionText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DiningUiState(
    val menuLoading: Boolean = false,
    val menus: List<List<MenuSection>> = listOf(listOf()),
    val waitzLoading: Boolean = false,
    val waitz: List<Map<String, List<String>>> = emptyList(),
    val hoursLoading: Boolean = false,
    val hours: List<HoursList> = listOf(HoursList(emptyList(), emptyList())),
    val error: String = ""
)

class MenuViewModel(
    private val menuRepository: MenuRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiningUiState())
    val uiState: StateFlow<DiningUiState> = _uiState.asStateFlow()

    private val TAG = "MenuViewModel"

    fun fetchMenu(locationName: String, locationUrl: String, checkCache: Boolean = true) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(menuLoading=true) }
                val fetchedMenus = menuRepository.fetchMenu(locationName, locationUrl, checkCache)
                Log.d(TAG, "fetched menus: $fetchedMenus")
                _uiState.update { it.copy(menuLoading=false, menus = fetchedMenus) }
            } catch (e: Exception) {
                _uiState.update { it.copy(menuLoading=false, error = exceptionText(e)) }
            }
        }
    }

//    fun fetchWaitz() {
//        viewModelScope.launch {
//            try {
//                _uiState.update { it.copy(waitzLoading=true) }
//                val busynessData = menuRepository.fetchBusyness()
//                Log.d(TAG, "fetched busyness data: $busynessData")
//                _uiState.update {
//                    it.copy(
//                        waitzLoading=false,
//                        waitz = listOf(busynessData.first, busynessData.second)
//                    )
//                }
//                Log.d(TAG, "waitz: "+uiState.value.waitz.toString())
//            } catch (e: Exception) {
//                _uiState.update { it.copy(waitzLoading = false, error = exceptionText(e)) }
//            }
//        }
//    }

    fun fetchHours(locationName: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(hoursLoading=true) }
                val fetchedHours = menuRepository.fetchHours(locationId = locationName, currentDate = LocalDate.now())
                Log.d(TAG, "fetched hours: $fetchedHours")
                _uiState.update { it.copy(hoursLoading=false, hours = listOf(fetchedHours)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(hoursLoading = false, error = exceptionText(e)) }
            }
        }
    }

    fun fetchHoursMulti(locationNames: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(hoursLoading=true) }
                val fetchedHours = locationNames.map { locationName ->
                    menuRepository.fetchHours(
                        locationId = locationName,
                        currentDate = LocalDate.now()
                    )
                }
                Log.d(TAG, "fetched hours: $fetchedHours")

                _uiState.update { it.copy(hoursLoading=false, hours = fetchedHours) }
            } catch (e: Exception) {
                _uiState.update { it.copy(hoursLoading = false, error = exceptionText(e)) }
            }
        }
    }

    fun insertFavorite(item: String) {
        viewModelScope.launch {
            try {
                menuRepository.insertFavorite(
                    Favorite(
                        name = item
                    )
                )
                // this is kind of hacky, not actually an error but whatever
                _uiState.value = _uiState.value.copy(error = "Item favorited")
            } catch (e: SQLiteConstraintException) {
                Log.d(TAG, "favorited a duplicate")
                _uiState.value = _uiState.value.copy(error = "Item already favorited")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = "") }
    }

    // Define ViewModel factory in a companion object
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).menuRepository
                MenuViewModel(
                    menuRepository = myRepository
                )
            }
        }
    }


}