package com.pras.slugmenu.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pras.slugmenu.Favorite
import com.pras.slugmenu.MyApplication
import com.pras.slugmenu.data.repositories.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val error: String = "",
    val showDeleteDialog: Boolean = false,
    val showAddDialog: Boolean = false,
    val addDialogText: String = ""
)

private const val TAG = "FavoritesViewModel"

class FavoritesViewModel(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                menuRepository.fetchFavorites().collect { favorites ->
                    _uiState.update { it.copy(favorites = favorites) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error fetching favorites") }
            }
        }
    }

    fun insertFavorite(favorite: Favorite) {
        viewModelScope.launch {
            try {
                menuRepository.insertFavorite(favorite)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error inserting favorite") }
            }
        }
    }

    fun deleteFavorite(favorite: Favorite) {
        viewModelScope.launch {
            try {
                menuRepository.deleteFavorite(favorite)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error deleting favorite") }
            }
        }
    }

    fun deleteAllFavorites() {
        viewModelScope.launch {
            try {
                menuRepository.deleteAllFavorites()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error deleting favorites") }
            }
        }
    }

    fun toggleAddDialog() {
        _uiState.update { it.copy(showAddDialog = !uiState.value.showAddDialog) }
    }

    fun setAddDialog(state: Boolean) {
        _uiState.update { it.copy(showAddDialog = state) }
    }

    fun toggleDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = !uiState.value.showDeleteDialog) }
    }

    fun setDeleteDialog(state: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = state) }
    }

    fun setAddDialogText(text: String) {
        _uiState.update { it.copy(addDialogText = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = "") }
    }

    // Define ViewModel factory in a companion object
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).menuRepository
                FavoritesViewModel(
                    menuRepository = myRepository
                )
            }
        }
    }
}