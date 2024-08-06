package com.pras.slugmenu

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pras.slugmenu.data.repositories.PreferencesRepository
import com.pras.slugmenu.ui.elements.CollapsingLargeTopBar
import com.pras.slugmenu.ui.elements.LongPressFloatingActionButton
import com.pras.slugmenu.ui.elements.TopBar
import com.pras.slugmenu.ui.elements.shortToast
import com.pras.slugmenu.ui.viewmodels.FavoritesViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

private const val TAG = "FavoritesMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesMenu(
    navController: NavController,
    preferencesRepository: PreferencesRepository,
    viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory)
) {

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val favoritesList = uiState.value.favorites

    val useCollapsingTopBar = runBlocking { preferencesRepository.getToolbarPreference.first() }

    // make it so that you can't accidentally press items while navigating out of screen
    val clickable = remember { mutableStateOf(true) }
    TouchBlocker(navController = navController, delay = FADETIME.toLong(), clickable = clickable)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })

    val scaffoldModifier = if (useCollapsingTopBar) {
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        Modifier.fillMaxSize()
    }

    // todo refactor top bars to use callbacks so this isn't necessary
    val showDeleteDialogLocal = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = showDeleteDialogLocal.value) {
        if (showDeleteDialogLocal.value) {
            viewModel.setDeleteDialog(true)
        }
    }

    val errorContext = LocalContext.current
    LaunchedEffect(key1 = uiState.value.error) {
        if (uiState.value.error.isNotEmpty()) {
            shortToast(uiState.value.error, errorContext)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = scaffoldModifier,
        // custom insets necessary to render behind nav bar
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            val titleText = "Manage Favorites"
            if (useCollapsingTopBar) {
                CollapsingLargeTopBar(
                    titleText = titleText,
                    navController = navController,
                    scrollBehavior = scrollBehavior,
                    hasTrailingIcon = true,
                    trailingIcon = Icons.Default.Delete,
                    iconDescription = "Delete all favorites",
                    iconPressed = showDeleteDialogLocal,
                    isClickable = clickable,
                    delay = FADETIME.toLong()
                )
            } else {
                Surface(shadowElevation = 4.dp) {
                    TopBar(
                        titleText = "titleText",
                        navController = navController,
                        hasTrailingIcon = true,
                        trailingIcon = Icons.Default.Delete,
                        iconDescription = "Delete all favorites",
                        iconPressed = showDeleteDialogLocal,
                        isClickable = clickable,
                        delay = FADETIME.toLong()
                    )
                }
            }
        },
        content = { paddingValues ->
            if (favoritesList.isEmpty()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Text(
                        "No Favorites",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 40.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        "Long-press menu items to add favorites",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    val sortedFavorites = favoritesList.sortedBy { it.name.lowercase(Locale.getDefault()) }
                    items(sortedFavorites, key = { it.name }) { favorite ->
                        ListItem(
                            headlineContent = {
                                Text(favorite.name)
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        viewModel.deleteFavorite(favorite)
//                                        coroutineScope.launch {
//                                            favoritesDao.deleteFavorite(favorite)
//                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Delete item",
                                    )
                                }
                            },
                            modifier = Modifier.animateItem(
                                tween(durationMillis = 250)
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            LongPressFloatingActionButton(
                onClick = { viewModel.toggleAddDialog() },
                onLongClick = { viewModel.toggleDeleteDialog() },
                modifier = Modifier.systemBarsPadding()
            ) {
                Icon(Icons.Filled.Add,"Add new favorite")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    )

    if (uiState.value.showDeleteDialog) {
        AlertDialog(
            title = { Text(text = "Delete all favorites") },
            text = { Text(text = "Would you like to delete all favorited items?") },
            onDismissRequest = { viewModel.setDeleteDialog(false) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllFavorites()
                        viewModel.setDeleteDialog(false)
                    },
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.setDeleteDialog(false)
                    },
                ) {
                    Text("Dismiss")
                }
            }


        )
    }

    val context = LocalContext.current
    if (uiState.value.showAddDialog) {
        AlertDialog(
            title = { Text(text = "Add new favorite") },
            text = {
                TextField(
                    value = uiState.value.addDialogText,
                    onValueChange = { viewModel.setAddDialogText(it) },
                    label = { Text("Item Name") }
                )
            },
            onDismissRequest = { viewModel.setAddDialog(false) },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedName = uiState.value.addDialogText.replace("\n", "")
                        if (!favoritesList.contains(Favorite(updatedName))) {
                            viewModel.insertFavorite(Favorite(updatedName))
                            viewModel.setAddDialogText("")
                            viewModel.setAddDialog(false)
                        } else {
                            shortToast("Item is already favorited", context)
                        }

                    },
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.setAddDialog(false)
                    },
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}