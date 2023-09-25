package com.pras.slugmenu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.pras.slugmenu.BackgroundDownloadScheduler.runSingleDownload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException

private const val TAG = "Settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, useMaterialYou: MutableState<Boolean>, useAmoledBlack: MutableState<Boolean>, themeChoice: MutableState<Int>, menuDb: MenuDatabase, preferencesDataStore: PreferencesDatastore) {
    Log.d(TAG,"test $useMaterialYou")
    val useCollapsingTopBar = remember { mutableStateOf(true) }
    val updateInBackground = remember { mutableStateOf(false) }

    val appVersion = BuildConfig.VERSION_NAME
    val newVersion = remember { mutableStateOf(appVersion) }
    val updateAvailable = remember { mutableStateOf(false) }


    val context = LocalContext.current

    runBlocking {
        useCollapsingTopBar.value = preferencesDataStore.getToolbarPreference.first()
        updateInBackground.value = preferencesDataStore.getBackgroundUpdatePreference.first()
    }

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


    val clickable = remember { mutableStateOf(true) }
    TouchBlocker(navController = navController, delay = FADETIME.toLong(), clickable = clickable)

    Surface(
        modifier = scaffoldModifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            // custom insets necessary to render behind nav bar
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                if (useCollapsingTopBar.value) {
                    CollapsingLargeTopBar(titleText = "Settings", navController = navController, scrollBehavior = scrollBehavior, isHome = false, isClickable = clickable, delay = FADETIME.toLong())
                } else {
                    TopBar("Settings", navController = navController, isHome = false, isClickable = clickable, delay = FADETIME.toLong())
                }
            },
            content = { innerPadding ->
                LazyColumn(Modifier.padding(innerPadding),contentPadding = WindowInsets.navigationBars.asPaddingValues()) {
                    item {
                        SectionText("Theme")
                    }
                    item {
                        ThemeSwitcher(
                            preferencesDataStore = preferencesDataStore,
                            themeChoice = themeChoice
                        )
                    }
                    item {
                        AmoledSwitcher(
                            useAmoledBlack = useAmoledBlack,
                            preferencesDataStore = preferencesDataStore
                        )
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        item {
                            MaterialYouSwitcher(
                                useMaterialYou = useMaterialYou,
                                preferencesDataStore = preferencesDataStore
                            )
                        }
                    }
                    item {
                        Divider()
                    }
                    item {
                        SectionText("Layout")
                    }
                    item {
                        LayoutSwitcher(preferencesDataStore = preferencesDataStore)
                    }
                    item {
                        TopAppBarSwitcher(
                            preferencesDataStore = preferencesDataStore,
                            useLargeTopBar = useCollapsingTopBar
                        )
                    }
                    item {
                        MenuOrganizerNavigator(navController = navController)
                    }
                    item {
                        Divider()
                    }
                    item {
                        SectionText("Downloads")
                    }
                    item {
                        BackgroundUpdateSwitcher(
                            updateInBackground = updateInBackground,
                            preferencesDataStore = preferencesDataStore,
                            context = context
                        )
                    }
                    item {
                        BackgroundOneTimeDownload(context)
                    }
                    item {
                        ClearCache(
                            menuDb = menuDb,
                            context = context
                        )
                    }
                    item {
                        Divider()
                    }
                    // if the app was installed from the play store, it should be updated there
                    if (!installedFromPlayStore) {
                        item {
                            UpdateChecker(
                                context = context,
                                appVersion = appVersion,
                                newVersion = newVersion,
                                updateAvailable = updateAvailable
                            )
                        }
                    }
                    item {
                        AboutNavigator(
                            navController = navController,
                            installedFromPlayStore = installedFromPlayStore,
                            appVersion = BuildConfig.VERSION_NAME
                        )
                    }
                }
            }
        )
//        BackgroundDownloadSelector(showSelector = showSelector, preferencesDataStore = preferencesDataStore)
    }
    UpdateDialog(updateAvailable = updateAvailable, newVersion = newVersion, context = context)
}

// function to display header text for each section
// could this be replaced with a text style?
@Composable
fun SectionText(text: String) {
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
fun ThemeSwitcher(preferencesDataStore: PreferencesDatastore, themeChoice: MutableState<Int>) {
    val themeOptions = listOf("System Default", "Light", "Dark")
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.selectableGroup()) {
        themeOptions.forEach { text ->
            Row(
                Modifier
                    .selectable(
                        selected = (text == themeOptions[themeChoice.value]),
                        onClick = {
                            coroutineScope.launch {
                                preferencesDataStore.setThemePreference(
                                    when (text) {
                                        themeOptions[0] -> 0
                                        themeOptions[1] -> 1
                                        themeOptions[2] -> 2
                                        else -> 0
                                    }
                                )
                            }
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
fun MaterialYouSwitcher(useMaterialYou: MutableState<Boolean>, preferencesDataStore: PreferencesDatastore) {
    val coroutineScope = rememberCoroutineScope()
    Row(modifier = Modifier.clickable(
            onClick = {
                useMaterialYou.value = !useMaterialYou.value
                coroutineScope.launch {
                    preferencesDataStore.setMaterialYouPreference(useMaterialYou.value)
                }
                Log.d(TAG, "Material You toggled")
            },
    )) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Enable Material You Theming",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            trailingContent = {
                Switch(
                    checked = useMaterialYou.value,
                    onCheckedChange = null
                )
            }
        )
    }
}

@Composable
fun AmoledSwitcher(useAmoledBlack: MutableState<Boolean>,preferencesDataStore: PreferencesDatastore) {
    val coroutineScope = rememberCoroutineScope()
    Row(modifier = Modifier.clickable(
            onClick = {
                useAmoledBlack.value = !useAmoledBlack.value
                coroutineScope.launch {
                    preferencesDataStore.setAmoledPreference(useAmoledBlack.value)
                }
                Log.d(TAG, "amoled toggled")
            },
    )) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Use AMOLED Dark Theme",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            trailingContent = {
                Switch(
                    checked = useAmoledBlack.value,
                    onCheckedChange = null
                )
            }
        )
    }
}

@Composable
fun LayoutSwitcher(preferencesDataStore: PreferencesDatastore) {
    val themeOptions = listOf("Grid", "List")
    val currentChoice = remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val userChoice = withContext(Dispatchers.IO) {
            preferencesDataStore.getListPreference.first()
        }
        currentChoice.value = userChoice
    }
    val coroutineScope = rememberCoroutineScope()
    Column(Modifier.selectableGroup()) {
        themeOptions.forEach { text ->
            Row(
                Modifier
                    .selectable(
                        selected = (
                                if (text == "Grid") {
                                    currentChoice.value
                                } else {
                                    !currentChoice.value
                                }
                                ),
                        onClick = {
                            coroutineScope.launch {
                                if (text == "Grid") {
                                    preferencesDataStore.setListPreference(true)
                                    currentChoice.value = true
                                } else {
                                    preferencesDataStore.setListPreference(false)
                                    currentChoice.value = false
                                }
                            }
                            Log.d(TAG, "layout switched")
                        },
                        role = Role.RadioButton
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
                            selected = (
                                    if (text == "Grid") {
                                        currentChoice.value
                                    } else {
                                        !currentChoice.value
                                    }
                                    ),
                            onClick = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MenuOrganizerNavigator(navController: NavController) {
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

@Composable
fun TopAppBarSwitcher(preferencesDataStore: PreferencesDatastore, useLargeTopBar: MutableState<Boolean>) {
    val coroutineScope = rememberCoroutineScope()
    Row(modifier = Modifier.clickable(
        onClick = {
            useLargeTopBar.value = !useLargeTopBar.value
            coroutineScope.launch {
                preferencesDataStore.setToolbarPreference(useLargeTopBar.value)
            }
            Log.d(TAG, "top bar choice toggled")
        },
    )) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Use Collapsing Top Bar",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            trailingContent = {
                Switch(
                    checked = useLargeTopBar.value,
                    onCheckedChange = null
                )
            }
        )
    }
}

@Composable
fun BackgroundUpdateSwitcher(updateInBackground: MutableState<Boolean>, preferencesDataStore: PreferencesDatastore, context: Context) {
    val coroutineScope = rememberCoroutineScope()
    val backgroundDownloadScheduler = BackgroundDownloadScheduler
    Row(modifier = Modifier.clickable(
        onClick = {
            updateInBackground.value = !updateInBackground.value
            if (updateInBackground.value) {
                Log.d(TAG, "Background Updates enabled")
                backgroundDownloadScheduler.refreshPeriodicWork(context)
            } else {
                Log.d(TAG, "Background Updates disabled")
                backgroundDownloadScheduler.cancelDownloadByTag(context, "backgroundMenuDownload")
            }
            coroutineScope.launch {
                preferencesDataStore.setBackgroundUpdatePreference(updateInBackground.value)
            }
        },
    )) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Download Menus in Background",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            // don't be specific about time - workmanager scheduling may cause downloads to not occur at exactly 2AM
            supportingContent = {
                if (updateInBackground.value) {
                    Text(
                        text = "Menus will be updated overnight."
                    )
                }
            },
            trailingContent = {
                Switch(
                    checked = updateInBackground.value,
                    onCheckedChange = null
                )
            }
        )
    }
}

@Composable
fun BackgroundOneTimeDownload(context: Context) {
    ListItem(
        headlineContent = {
            Text(
                text = "Download Menus Now",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        modifier = Modifier.clickable {
            runSingleDownload(context)
        }
    )
}

@Composable
fun ClearCache(menuDb: MenuDatabase, context: Context) {
    ListItem(
        headlineContent = { Text("Clear App Cache") },
        supportingContent = { Text("Clears menu and busyness data for all locations.") },
        modifier = Modifier.clickable {
            CoroutineScope(Dispatchers.IO).launch {
                menuDb.clearAllTables()
                withContext(Dispatchers.Main) {
                    ShortToast("Cache cleared.", context)
                }
            }
        }
    )
}

@Composable
fun AboutNavigator(navController: NavController, installedFromPlayStore: Boolean = false, appVersion: String = BuildConfig.VERSION_NAME) {
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
            if (installedFromPlayStore) {
                Text(text = "Version $appVersion")
            }
        },
        modifier = Modifier.clickable { navController.navigate("about") }
    )
}

@Composable
fun UpdateChecker(context: Context, appVersion: String, newVersion: MutableState<String>, updateAvailable: MutableState<Boolean>) {
    var latestVersion  by remember { mutableStateOf(appVersion) }
    var exceptionFound by remember { mutableStateOf("") }
    ListItem(
        leadingContent = {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh",
//                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        headlineContent = { Text(text = "Check for updates") },
        supportingContent = { if (appVersion != "selfbuilt") { Text(text = "Current version is v$appVersion") } else {Text(text = "Current version is $appVersion")} },
        modifier = Modifier.clickable {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    latestVersion = getLatestVersion()
                } catch (e: Exception) {
                    exceptionFound = when (e) {
                        is UnresolvedAddressException -> "No Internet connection"
                        is SocketTimeoutException -> "Connection timed out"
                        is UnknownHostException -> "Failed to resolve URL"
                        is CertificateException -> "Website's SSL certificate is invalid"
                        is SSLHandshakeException -> "SSL handshake failed"
                        else -> "Exception: $e"
                    }
                }
                withContext(Dispatchers.Main) {
                    if (exceptionFound.isNotEmpty()) {
                        ShortToast(exceptionFound, context)
                    } else if (appVersion == "selfbuilt") {
                        ShortToast("You are on a self-built version, check for updates manually.", context)
                    } else if (latestVersion != appVersion) {
                        updateAvailable.value = true
                        newVersion.value = latestVersion
                    } else {
                        ShortToast("You are on the latest version.", context)
                    }
                }
            }
        }
    )
}

@Composable
fun UpdateDialog(updateAvailable: MutableState<Boolean>, newVersion: MutableState<String>, context: Context) {
    if (updateAvailable.value) {
        AlertDialog(
            onDismissRequest = { updateAvailable.value = false },
            icon = { Icon(Icons.Default.Refresh, contentDescription = "Update") },
            title = { Text(text = "Update Available", textAlign = TextAlign.Center) },
            text = {
                Text(
                    text = "A new version of Slug Menu is available. Please update to v${newVersion.value}.",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateAvailable.value = false
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://github.com/prapooskur/SlugMenu/releases/tag/${newVersion.value}")
                        startActivity(context, intent, null)
                    },
                    content = {
                        Text(text = "Update")
                    }
                )
            },
            dismissButton = {
                Button(
                    onClick = {
                        updateAvailable.value = false
                    },
                    content = {
                        Text(text = "Cancel")
                    }
                )
            }

        )
    }
}