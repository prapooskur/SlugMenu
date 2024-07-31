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
import com.pras.slugmenu.data.sources.AllHoursList
import com.pras.slugmenu.data.sources.MenuSection
import com.pras.slugmenu.ui.elements.exceptionText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate

const val fallbackHoursJson = "{\"ninelewis\":{\"daysList\":[\"Monday-Friday\",\"Saturday-Sunday\"],\"hoursList\":[[\"Breakfast: 7–11AM\",\"Continuous Dining: 11–11:30AM\",\"Lunch: 11:30AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\",\"Late Night: 8–11PM\"],[\"Breakfast: 7–10AM\",\"Brunch: 10AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\",\"Late Night: 8–11PM\"]]},\"cowellstev\":{\"daysList\":[\"Monday-Thursday\",\"Friday\",\"Saturday\",\"Sunday\"],\"hoursList\":[[\"Breakfast: 7–11AM\",\"Continuous Dining: 11–11:30AM\",\"Lunch: 11:30AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\",\"Late Night: 8–11PM\"],[\"Breakfast: 7–11AM\",\"Continuous Dining: 11–11:30AM\",\"Lunch: 11:30AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\"],[\"Breakfast: 7–10AM\",\"Brunch: 10AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\"],[\"Breakfast: 7–10AM\",\"Brunch: 10AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\",\"Late Night: 8–11PM\"]]},\"crownmerrill\":{\"daysList\":[\"Monday-Friday\"],\"hoursList\":[[\"Breakfast: 7–11AM\",\"Continuous Dining: 11–11:30AM\",\"Lunch: 11:30AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\"]]},\"porterkresge\":{\"daysList\":[\"Monday-Friday\"],\"hoursList\":[[\"Breakfast: 7–11AM\",\"Continuous Dining: 11–11:30AM\",\"Lunch: 11:30AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–7PM\"]]},\"carsonoakes\":{\"daysList\":[\"Monday-Thursday\",\"Friday\",\"Saturday\",\"Sunday\"],\"hoursList\":[[\"Breakfast: 7–11AM\",\"Continuous Dining: 11–11:30AM\",\"Lunch: 11:30AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\",\"Late Night: 8–11PM\"],[\"Breakfast: 7–11AM\",\"Continuous Dining: 11–11:30AM\",\"Lunch: 11:30AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\"],[\"Breakfast: 7–10AM\",\"Brunch: 10AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\"],[\"Breakfast: 7–10AM\",\"Brunch: 10AM–2PM\",\"Continuous Dining: 2–5PM\",\"Dinner: 5–8PM\",\"Late Night: 8–11PM\"]]},\"globalvillage\":{\"daysList\":[\"Monday: 8AM–8PM\",\"Tuesday: 8AM–8PM\",\"Wednesday: 8AM–8PM\",\"Thursday: 8AM–8PM\",\"Friday: 8AM–5PM\",\"Saturday: Closed\",\"Sunday: Closed\"]},\"perkbe\":{\"daysList\":[\"Monday: 8AM–6PM\",\"Tuesday: 8AM–6PM\",\"Wednesday: 8AM–6PM\",\"Thursday: 8AM–6PM\",\"Friday: 8AM–5PM\",\"Saturday: Closed\",\"Sunday: Closed\"]},\"perkpsb\":{\"daysList\":[\"Monday: 8AM–6PM\",\"Tuesday: 8AM–6PM\",\"Wednesday: 8AM–6PM\",\"Thursday: 8AM–6PM\",\"Friday: 8AM–5PM\",\"Saturday: Closed\",\"Sunday: Closed\"]},\"perkems\":{\"daysList\":[\"Monday: 8AM–5PM\",\"Tuesday: 8AM–5PM\",\"Wednesday: 8AM–5PM\",\"Thursday: 8AM–5PM\",\"Friday: 8AM–5PM\",\"Saturday: Closed\",\"Sunday: Closed\"]},\"terrafresca\":{\"daysList\":[\"Monday: 8AM–3PM\",\"Tuesday: 8AM–3PM\",\"Wednesday: 8AM–3PM\",\"Thursday: 8AM–3PM\",\"Friday: 8AM–3PM\",\"Saturday: Closed\",\"Sunday: Closed\"]},\"portermarket\":{\"daysList\":[\"Monday: 8AM–6PM\",\"Tuesday: 8AM–6PM\",\"Wednesday: 8AM–6PM\",\"Thursday: 8AM–6PM\",\"Friday: 8AM–6PM\",\"Saturday: 10AM–5PM\",\"Sunday: Closed\"]},\"stevcoffee\":{\"daysList\":[\"Monday: 8AM–5PM\",\"Tuesday: 8AM–5PM\",\"Wednesday: 8AM–5PM\",\"Thursday: 8AM–5PM\",\"Friday: 8AM–5PM\",\"Saturday: Closed\",\"Sunday: Closed\"]},\"oakescafe\":{\"daysList\":[\"Monday: 8AM–8PM\",\"Tuesday: 8AM–8PM\",\"Wednesday: 8AM–8PM\",\"Thursday: 8AM–8PM\",\"Friday: 8AM–8PM\",\"Saturday: Closed\",\"Sunday: Closed\"]}}"
data class DiningUiState(
    val menuLoading: Boolean = false,
    val menus: List<List<MenuSection>> = listOf(listOf()),
    val waitzLoading: Boolean = false,
    val waitz: List<Map<String, List<String>>> = emptyList(),
    val hoursLoading: Boolean = false,
    val hours: AllHoursList = Json.decodeFromString(fallbackHoursJson),
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

    fun fetchWaitz() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(waitzLoading=true) }
                val busynessData = menuRepository.fetchBusyness()
                Log.d(TAG, "fetched busyness data: $busynessData")
                _uiState.update {
                    it.copy(
                        waitzLoading=false,
                        waitz = listOf(busynessData.first, busynessData.second)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(waitzLoading = false, error = exceptionText(e)) }
            }
        }
    }

    fun fetchHours() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(hoursLoading=true) }
                val fetchedHours = menuRepository.fetchHours(currentDate = LocalDate.now())
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