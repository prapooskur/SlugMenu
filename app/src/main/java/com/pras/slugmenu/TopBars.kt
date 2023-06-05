package com.pras.slugmenu

import android.util.Log
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// A large top bar that collapses into a small one
// Intended for use on main and settings screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingLargeTopBar(titleText: String, navController: NavController, showSettingsIcon: Boolean = false) {
    Surface(shadowElevation = 4.dp) {
        TopAppBar(
            title = {
                Text(
                    text = titleText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 20.sp
                )
            },
            actions = {
                if (showSettingsIcon) {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
//            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(titleText: String, navController: NavController, showBottomSheet: MutableState<Boolean> = mutableStateOf(false)) {
    TopAppBar(
        title = {
            Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = {navController.navigateUp()}) {
                Icon(
                    Icons.Filled.ArrowBack, contentDescription = "Back",tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    showBottomSheet.value = !showBottomSheet.value
                    Log.d("TAG",showBottomSheet.value.toString())
                }
            ) {
                Icon(
                    Icons.Outlined.Info, contentDescription = "Hours",tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
//        backgroundColor = MaterialTheme.colorScheme.primaryContainer
//        elevation = 8.dp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarClean(titleText: String, navController: NavController) {
    TopAppBar(
        title = {
            Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = {navController.navigateUp()}) {
                Icon(
                    Icons.Filled.ArrowBack, contentDescription = "Back",tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
//        backgroundColor = MaterialTheme.colorScheme.primaryContainer
//        elevation = 8.dp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWaitz(titleText: String, navController: NavController, showWaitzDialog: MutableState<Boolean> = mutableStateOf(false)) {
    TopAppBar(
        title = {
            Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = {navController.navigateUp()}) {
                Icon(
                    Icons.Filled.ArrowBack, contentDescription = "Back",tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    showWaitzDialog.value = !showWaitzDialog.value
                    Log.d("TAG",showWaitzDialog.value.toString())
                }
            ) {
                Icon(
                    Icons.Outlined.Info, contentDescription = "Busyness",tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
//        backgroundColor = MaterialTheme.colorScheme.primaryContainer
//        elevation = 8.dp
    )
}