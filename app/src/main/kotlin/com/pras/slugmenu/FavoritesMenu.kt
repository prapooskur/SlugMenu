package com.pras.slugmenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "FavoritesMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesMenu(navController: NavController, preferencesDataStore: PreferencesDatastore) {

    val useCollapsingTopBar = runBlocking { preferencesDataStore.getToolbarPreference.first() }

    val menuDatabase = MenuDatabase.getInstance(LocalContext.current)
    val favoritesDao = menuDatabase.favoritesDao()

    //todo make non-blocking
    var favoritesList by remember { mutableStateOf(listOf<Favorite>()) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit) {
        coroutineScope.launch {
            favoritesDao.getFavoritesFlow().collect {
                favoritesList = it
            }
        }
    }

    val showDeleteDialog = remember { mutableStateOf(false) }

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
                    iconPressed = showDeleteDialog,
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
                        iconPressed = showDeleteDialog,
                        isClickable = clickable,
                        delay = FADETIME.toLong()
                    )
                }
            }
        },
        content = { paddingValues ->
            Column(modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                for (favorite in favoritesList) {
                    ListItem(
                        headlineContent = {
                            Text(favorite.name)
                        },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        favoritesDao.deleteFavorite(favorite)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete item",
                                )
                            }
                        }
                    )
                }
                if (favoritesList.isEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
                    ) {
                        Text(
                            "No Favorites",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 40.sp
                        )
                        Text(
                            "Long-press dining hall items to add favorites",
                            fontSize = 18.sp
                        )
                    }

                }
            }
        }
    )

    if (showDeleteDialog.value) {
        AlertDialog(
            title = { Text(text = "Delete all favorites") },
            text = {
                Text(text = "Would you like to delete all favorited items?")
            },
            onDismissRequest = { showDeleteDialog.value = false },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            favoritesDao.deleteAllFavorites()
                        }
                        showDeleteDialog.value = false
                    },
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog.value = false
                    },
                ) {
                    Text("Dismiss")
                }
            }


        )
    }
}