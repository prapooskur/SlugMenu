package com.pras.slugmenu

import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.pras.slugmenu.ui.elements.CollapsingLargeTopBar
import com.pras.slugmenu.ui.elements.TopBar
import com.pras.slugmenu.ui.elements.shortToast
import com.pras.slugmenu.ui.viewmodels.SettingsViewModel

private const val TAG = "SettingsNew"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsNew(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val context = LocalContext.current

    // get initial values?


    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val themeChoice = uiState.value.themeChoice.collectAsStateWithLifecycle(viewModel.initState.themeChoice)
    val useAmoled = uiState.value.useAmoledBlack.collectAsStateWithLifecycle(viewModel.initState.useAmoledBlack)
    val useMaterialYou = uiState.value.useMaterialYou.collectAsStateWithLifecycle(viewModel.initState.useMaterialYou)
    val useGrid = uiState.value.useGridUI.collectAsStateWithLifecycle(viewModel.initState.useGridUI)
    val useTwoPanes = uiState.value.useTwoPanes.collectAsStateWithLifecycle(viewModel.initState.useTwoPanes)
    val useCollapsingTopBar = uiState.value.useCollapsingTopBar.collectAsStateWithLifecycle(viewModel.initState.useCollapsingTopBar)
    val enableBackgroundUpdates = uiState.value.enableBackgroundUpdates.collectAsStateWithLifecycle(viewModel.initState.enableBackgroundUpdates)
    val sendItemNotifications = uiState.value.sendItemNotifications.collectAsStateWithLifecycle(viewModel.initState.sendItemNotifications)

    val clickable = remember { mutableStateOf(true) }
    TouchBlocker(navController = navController, delay = FADETIME.toLong()+20, clickable = clickable)

    val packageManager = context.packageManager
    val packageName = context.packageName
    val installerPackageName: String?
    Log.d(TAG, "App package name: $packageName")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        installerPackageName = packageManager.getInstallSourceInfo(packageName).installingPackageName
        Log.d(TAG, "Installer package name: $installerPackageName")
    } else {
        // this is deprecated, but the replacement only supports android 11+
        @Suppress("DEPRECATION")
        installerPackageName = packageManager.getInstallerPackageName(packageName)
        Log.d(TAG, "Installer package name: $installerPackageName")
    }
    val installedFromPlayStore = (installerPackageName == "com.android.vending" || installerPackageName == "com.google.android.feedback")

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    val scaffoldModifier = if (useCollapsingTopBar.value) {
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        Modifier
            .fillMaxSize()
    }

    Surface(
        modifier = scaffoldModifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                if (useCollapsingTopBar.value) {
                    CollapsingLargeTopBar(titleText = "Settings", navController = navController, scrollBehavior = scrollBehavior, isHome = false, isClickable = clickable, delay = FADETIME.toLong())
                } else {
                    TopBar("Settings", navController = navController, isHome = false, isClickable = clickable, delay = FADETIME.toLong())
                }
            },
            content = { padding ->
                LazyColumn(
                    Modifier.padding(padding),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                ) {
                    item {
                        SectionText("Theme")
                    }
                    item {
                        ThemeSwitcher(
                            themeChoice = themeChoice,
                            themeOptions = listOf("System Default", "Light", "Dark"),
                            onThemeChange = { selected ->
                                viewModel.setPreference(
                                    "theme_pref",
                                    selected
                                )
                            }
                        )
                    }
                    item {
                        AnimatedVisibility(visible = themeChoice.value == 2 || (themeChoice.value == 0 && isSystemInDarkTheme()),
                            enter = expandVertically(
                                // Expand from the top.
                                expandFrom = Alignment.Top
                            ) + fadeIn(
                                // Fade in with the initial alpha of 0.3f.
                                initialAlpha = 0.3f
                            ),
                            exit = shrinkVertically(
                                shrinkTowards = Alignment.Top
                            ) + fadeOut(
                                targetAlpha = 0.3f
                            )
                        ) {
                            PreferenceSwitch(
                                text = "Use AMOLED Dark Theme",
                                isChecked = useAmoled.value
                            ) {
                                viewModel.setPreference("use_amoled_black", it)
                            }
                        }
                    }

                    item {
                        PreferenceSwitch(
                            text = "Use Material You",
                            isChecked = useMaterialYou.value
                        ) {
                            viewModel.setPreference("use_material_you", it)
                        }
                    }

                    item {
                        HorizontalDivider()
                        SectionText("Layout")
                    }
                    item {
                        GridSwitcher(
                            gridChoice = useGrid,
                            gridOptions = listOf("Grid", "List"),
                            onGridChange = { selected ->
                                viewModel.setPreference(
                                    "use_grid_ui",
                                    selected
                                )
                            }
                        )
                    }
                    item {
                        PreferenceSwitch(
                            text = "Use Two-Pane Layout on Large Screens",
                            isChecked = useTwoPanes.value
                        ) {
                            viewModel.setPreference("use_two_panes", it)
                        }
                    }
                    item {
                        PreferenceSwitch(
                            text = "Use Collapsing Top Bar",
                            isChecked = useCollapsingTopBar.value
                        ) {
                            viewModel.setPreference("use_collapsing_toolbar", it)
                        }
                    }
                    item {
                        ListItem(
                            leadingContent = {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                )
                            },
                            headlineContent = {
                                Text(text = "Organize Menu Items")
                            },
                            modifier = Modifier.clickable { navController.navigate("menuorganizer") }
                        )
                    }
                    item {
                        ListItem(
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.Star,
                                    contentDescription = "Favorites",
                                )
                            },
                            headlineContent = {
                                Text(text = "Manage Favorites")
                            },
                            modifier = Modifier.clickable { navController.navigate("favoritesmenu") }
                        )
                    }
                    item {
                        HorizontalDivider()
                        SectionText(text = "Downloads")
                    }
                    item {
                        PreferenceSwitch(
                            text = "Download Menus in Background",
                            supportingText = if (enableBackgroundUpdates.value) "Menus will be updated overnight." else "",
                            isChecked = enableBackgroundUpdates.value
                        ) {
                            viewModel.setPreference("enable_background_updates", it)
                            if (enableBackgroundUpdates.value) {
                                Log.d(TAG, "Background Updates enabled")
                                viewModel.refreshPeriodicWork(context)
                            } else {
                                Log.d(TAG, "Background Updates disabled")
                                viewModel.cancelDownload(context, "backgroundMenuDownload")
                            }
                        }
                    }
                    item {
                        val notificationPermissionState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
                        PreferenceSwitch(
                            text = "Send Item Notifications",
                            supportingText = if (sendItemNotifications.value) {
                                "Will notify when favorites exist."
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && notificationPermissionState.status.shouldShowRationale) {
                                "Notification permissions not granted"
                            } else "",
                            isChecked = sendItemNotifications.value
                        ) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (notificationPermissionState.status.shouldShowRationale) {
                                    Log.d(TAG,"Setting to false, permission not granted")
                                    viewModel.setPreference("send_item_notifications", false)
                                } else if (notificationPermissionState.status.isGranted) {
                                    viewModel.setPreference("send_item_notifications", !sendItemNotifications.value)
                                } else {
                                    notificationPermissionState.launchPermissionRequest()
                                }
                            } else {
                                viewModel.setPreference("send_item_notifications", !sendItemNotifications.value)
                            }
                        }
                    }
                    item {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = "Download Menus Now",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.runSingleDownload(context)
                            }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("Clear App Cache") },
                            supportingContent = { Text("Clears menu and busyness data for all locations.") },
                            modifier = Modifier.clickable {
                                viewModel.clearMenuCache()
                                shortToast("Cache cleared.", context)
                            }
                        )
                    }
                    item {
                        ListItem(
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = "About",
                                )
                            },
                            headlineContent = {
                                Text(text = "About Slug Menu")
                            },
                            // if the app wasn't installed from the play store, update checker already shows version code
                            supportingContent = {
                                Text(text = "Version ${BuildConfig.VERSION_NAME}")
                            },
                            modifier = Modifier.clickable { navController.navigate("about") }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun SectionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun PreferenceSwitch(text: String, supportingText: String = "", isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.clickable {
            onCheckedChange(!isChecked)
            Log.d(TAG, "toggled $text")
        }
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                if (supportingText.isNotBlank()) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            trailingContent = {
                Switch(
                    checked = isChecked,
                    onCheckedChange = null
                )
            }
        )
    }
}

@Composable
private fun ThemeSwitcher(
    themeChoice: State<Int>,
    themeOptions: List<String>,
    onThemeChange: (Int) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        themeOptions.forEach { text ->
            Row(
                Modifier
                    .selectable(
                        selected = (text == themeOptions[themeChoice.value]),
                        onClick = {
                            onThemeChange(themeOptions.indexOf(text))
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    trailingContent = {
                        RadioButton(
                            selected = (text == themeOptions[themeChoice.value]),
                            onClick = null
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun GridSwitcher(
    gridChoice: State<Boolean>,
    gridOptions: List<String>,
    onGridChange: (Boolean) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        gridOptions.forEach { text ->
            Row(
                Modifier
                    .selectable(
                        selected = (
                            if (gridChoice.value) {
                                text == gridOptions[0]
                            } else {
                                text == gridOptions[1]
                            }
                        ),
                        onClick = {
                            onGridChange(!gridChoice.value)
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    trailingContent = {
                        RadioButton(
                            selected = if (gridChoice.value) {
                                text == gridOptions[0]
                            } else {
                                text == gridOptions[1]
                            },
                            onClick = null
                        )
                    },
                )
            }
        }
    }
}