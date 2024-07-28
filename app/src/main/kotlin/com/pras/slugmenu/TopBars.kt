package com.pras.slugmenu

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.navigation.NavController
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val TAG = "TopBars"

// todo: collapsing and regular top bars are kind of bloated, clean them up somehow?

// A large top bar that collapses into a small one
// Intended for use on main and settings screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingLargeTopBar(
    titleText: String,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    // if false, show an arrow that navigates up on press
    isHome: Boolean = false,
    isClickable: MutableState<Boolean> = remember { mutableStateOf(false) },
    delay: Long = 0,
    hasTrailingIcon: Boolean = false,
    trailingIcon: ImageVector = Icons.Default.Warning,
    iconDescription: String = "",
    iconPressed: MutableState<Boolean> = mutableStateOf(false)
) {
    val topBarColors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer)

    val topBarElevation = if (scrollBehavior.state.collapsedFraction > 0.99) {
        4.dp
    } else {
        0.dp
    }

    val topBarItemColor = ColorUtils.blendARGB(
        MaterialTheme.colorScheme.onSurface.toArgb(),
        MaterialTheme.colorScheme.onPrimaryContainer.toArgb(),
        scrollBehavior.state.collapsedFraction
    )


    // make the title smaller if it's too long (organize screen title was almost hitting the other edge)
    val expandedFontSize = if (titleText.length > 15) { 30 } else { 33 }
    val collapsedFontSize = 20

    val topBarFontSize = (expandedFontSize-(expandedFontSize-collapsedFontSize)*scrollBehavior.state.collapsedFraction).sp

    val coroutineScope = rememberCoroutineScope()

    Surface(shadowElevation = topBarElevation) {
        LargeTopAppBar(
            title = {
                Text(
                    modifier = Modifier,
                    text = titleText,
                    color = Color(topBarItemColor),
                    fontSize = topBarFontSize
                )
            },
            navigationIcon = {
                if (
                    !isHome &&
                    (LocalDisplayFeatures.current.sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT || !LocalDisplayFeatures.current.twoPanePreference)
                ) {
                    IconButton(
                        onClick = {
                            if (isClickable.value) {
                                isClickable.value = !isClickable.value
                                coroutineScope.launch {
                                    // length of the animation
                                    delay(delay)
                                    isClickable.value = true
                                }
                            }
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(topBarItemColor)
                        )
                    }
                }
            },
            actions = {
                if (isHome &&
                    (LocalDisplayFeatures.current.sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT || !LocalDisplayFeatures.current.twoPanePreference)) {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color(topBarItemColor)
                        )
                    }
                } else if (hasTrailingIcon) {
                    IconButton(onClick = { iconPressed.value = true }) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = iconDescription,
                            tint = Color(topBarItemColor)
                        )
                    }
                }
            },
            scrollBehavior = scrollBehavior,
            colors = topBarColors,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    titleText: String,
    navController: NavController,
    isHome: Boolean = false,
    isClickable: MutableState<Boolean> = remember { mutableStateOf(false) },
    delay: Long = 0,
    hasTrailingIcon: Boolean = false,
    trailingIcon: ImageVector = Icons.Default.Warning,
    iconDescription: String = "",
    iconPressed: MutableState<Boolean> = mutableStateOf(false),
) {

    val coroutineScope = rememberCoroutineScope()
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
                if (!isHome &&
                    (LocalDisplayFeatures.current.sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT || !LocalDisplayFeatures.current.twoPanePreference)) {
                    IconButton(onClick = {
                        if (isClickable.value) {
                            isClickable.value = !isClickable.value
                            coroutineScope.launch {
                                // length of the animation
                                delay(delay)
                                isClickable.value = true
                            }
                        }
                        navController.navigateUp()
                    }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                if (isHome &&
                    (LocalDisplayFeatures.current.sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT || !LocalDisplayFeatures.current.twoPanePreference)) {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else if (hasTrailingIcon) {
                    IconButton(onClick = { iconPressed.value = true }) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = iconDescription,
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
            if (LocalDisplayFeatures.current.sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT || !LocalDisplayFeatures.current.twoPanePreference) {
                IconButton(onClick = {navController.navigateUp()}) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
            if (LocalDisplayFeatures.current.sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT || !LocalDisplayFeatures.current.twoPanePreference) {
                IconButton(onClick = {navController.navigateUp()}) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
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