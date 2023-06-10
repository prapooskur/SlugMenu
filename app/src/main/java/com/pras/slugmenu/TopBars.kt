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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


private const val TAG = "TopBars"

// A large top bar that collapses into a small one
// Intended for use on main and settings screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingLargeTopBar(titleText: String, navController: NavController, scrollBehavior: TopAppBarScrollBehavior, isHome: Boolean = false) {
    val topBarColors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer)

    val topBarElevation = if (scrollBehavior.state.collapsedFraction > 0.99) {
        4.dp
    } else {
        0.dp
    }

    val topBarItemColor = if (scrollBehavior.state.collapsedFraction < 0.5) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val topBarFontSize = (30-10*scrollBehavior.state.collapsedFraction).sp

    Surface(shadowElevation = topBarElevation) {
        LargeTopAppBar(
            title = {
                Text(
                    modifier = Modifier,
                    text = titleText,
                    color = topBarItemColor,
                    fontSize = topBarFontSize
                )
            },
            navigationIcon = {
                if (!isHome) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = topBarItemColor
                        )
                    }
                }
            },
            actions = {
                if (isHome) {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = topBarItemColor
                        )
                    }
                }
            },
            scrollBehavior = scrollBehavior,
            colors = topBarColors
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(titleText: String, navController: NavController, isHome: Boolean = false) {
    Surface(shadowElevation = 4.dp) {
        TopAppBar(
            title = {
                Text(
                    text = titleText,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                if (!isHome) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                if (isHome) {
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
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarBottomSheet(titleText: String, navController: NavController, showBottomSheet: MutableState<Boolean> = mutableStateOf(false)) {
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
                    Log.d(TAG,showBottomSheet.value.toString())
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
                    Log.d(TAG,showWaitzDialog.value.toString())
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