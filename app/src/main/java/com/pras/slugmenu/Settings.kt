package com.pras.slugmenu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertSelector
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException

private const val TAG = "Settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, useMaterialYou: MutableState<Boolean>, themeChoice: MutableState<Int>, menuDb: MenuDatabase, preferencesDataStore: PreferencesDatastore) {
    Log.d(TAG,"test $useMaterialYou")
    val useCollapsingTopBar = remember { mutableStateOf(false) }

    val updateInBackground = remember { mutableStateOf(false) }
    val showSelector = remember { mutableStateOf(false) }

    val appVersion = BuildConfig.VERSION_NAME
    val newVersion = remember { mutableStateOf(appVersion) }
    val updateAvailable = remember { mutableStateOf(false) }


    val context = LocalContext.current

    runBlocking {
        val collapsingTopBarChoice = preferencesDataStore.getToolbarPreference.first()
        useCollapsingTopBar.value = collapsingTopBarChoice
        val backgroundDownloadChoice = preferencesDataStore.getBackgroundUpdatePreference.first()
        updateInBackground.value = backgroundDownloadChoice
    }

    //TODO: Complete collapsing top bar rewrite

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
            // custom insets necessary to render behind nav bar
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                if (useCollapsingTopBar.value) {
                    CollapsingLargeTopBar(titleText = "Settings", navController = navController, scrollBehavior = scrollBehavior, isHome = false)
                } else {
                    TopBar("Settings", navController = navController, isHome = false)
                }
            },
            content = { innerPadding ->
                LazyColumn(Modifier.padding(innerPadding),contentPadding = WindowInsets.navigationBars.asPaddingValues()) {
                    item {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = "App Theme:",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        )
                    }
                    item {
                        ThemeSwitcher(
                            preferencesDataStore = preferencesDataStore,
                            themeChoice = themeChoice
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
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = "App Layout:",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        )
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
                        Divider()
                    }
                    item {
                        BackgroundUpdateSwitcher(updateInBackground = updateInBackground, preferencesDataStore = preferencesDataStore, context = context)
                    }

                    item {
                        BackgroundOneTimeDownload(context)
                    }

                    item {
                        BackgroundSelectorToggle(showSelector = showSelector)
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

                    item {
                        UpdateChecker(context = context, appVersion = appVersion, newVersion = newVersion, updateAvailable = updateAvailable)
                    }
                    /*
                    item {
                        AboutItem(appVersion = appVersion)
                    }
                    */
                }
            }
        )
        BackgroundDownloadSelector(showSelector = showSelector, preferencesDataStore = preferencesDataStore)
    }

    UpdateDialog(updateAvailable = updateAvailable, newVersion = newVersion, context = context)
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
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    },
                    trailingContent = {
                        RadioButton(
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
                            },
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
                Log.d(TAG, "material you toggled")
            },
    )) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Enable Material You Theming",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            },
            trailingContent = {
                Switch(
                    checked = useMaterialYou.value,

                    onCheckedChange = {
                        useMaterialYou.value = !useMaterialYou.value
                        coroutineScope.launch {
                            preferencesDataStore.setMaterialYouPreference(useMaterialYou.value)
                        }
                        Log.d(TAG, "material you toggled")
                    }
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
                    text = "Enable AMOLED Theme",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            },
            trailingContent = {
                Switch(
                    checked = useAmoledBlack.value,
                    onCheckedChange = {
                        useAmoledBlack.value = !useAmoledBlack.value
                        coroutineScope.launch {
                            preferencesDataStore.setAmoledPreference(useAmoledBlack.value)
                        }
                        Log.d(TAG, "amoled toggled")
                    }
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
                            modifier = Modifier.padding(start = 16.dp)
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
                            }
                        )
                    }
                )
            }
        }
    }
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
                    modifier = Modifier.padding(start = 16.dp)
                )
            },
            supportingContent = {
                Text(
                    text = "Currently buggy, use with caution.",
                    modifier = Modifier.padding(start = 16.dp)
                )
            },
            trailingContent = {
                Switch(
                    checked = useLargeTopBar.value,
                    onCheckedChange = {
                        useLargeTopBar.value = !useLargeTopBar.value
                        coroutineScope.launch {
                            preferencesDataStore.setToolbarPreference(useLargeTopBar.value)
                        }

                        Log.d(TAG, "Top Bar toggled")
                    }
                )
            }
        )
    }
}

// currently not yet tested
@Composable
fun BackgroundUpdateSwitcher(updateInBackground: MutableState<Boolean>, preferencesDataStore: PreferencesDatastore, context: Context) {
    val coroutineScope = rememberCoroutineScope()
    val backgroundDownloadScheduler = BackgroundDownloadScheduler
    Row(modifier = Modifier.clickable(
        onClick = {
            updateInBackground.value = !updateInBackground.value
            coroutineScope.launch {
                preferencesDataStore.setBackgroundUpdatePreference(updateInBackground.value)
            }
            Log.d(TAG, "Background Updates toggled")
        },
    )) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Download Menus in Background",
                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(start = 16.dp)
                )
            },
            supportingContent = {
                if (updateInBackground.value) {
                    Text(
                        text = "Untested, use with caution. Downloading at 2AM Pacific Time.",
                    )
                } else {
                    Text(
                        text = "Untested, use with caution.",
    //                    modifier = Modifier.padding(start = 16.dp)
                    )
                }
            },
            trailingContent = {
                Switch(
                    checked = updateInBackground.value,
                    onCheckedChange = {
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

                    }
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
                text = "Download all menus now",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = "Untested, use with caution. Probably works?",
            )
        },
        modifier = Modifier.clickable {
            runSingleDownload(context)
        }
    )
}

@Composable
fun BackgroundSelectorToggle (showSelector: MutableState<Boolean>) {
    ListItem(
        headlineContent = {
            Text(
                text = "Select locations to download",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = "Untested, use with caution. Doesn't work?",
            )
        },
        modifier = Modifier.clickable {
            showSelector.value = !showSelector.value
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundDownloadSelector(showSelector: MutableState<Boolean>, preferencesDataStore: PreferencesDatastore) {

    //TODO: make this non-blocking
    var locationList by remember { mutableStateOf(mutableListOf<LocationListItem>()) }
    runBlocking {
        locationList = (Json.decodeFromString<List<LocationListItem>>(preferencesDataStore.getBackgroundDownloadPreference.first())).toMutableList()
    }
    Log.d(TAG,"location list: $locationList")

    val textStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )


    if (showSelector.value) {
        AlertDialog(onDismissRequest = { showSelector.value = false } ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = AlertDialogDefaults.shape,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "Select locations to download",
                        style = textStyle,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn {
                        items(locationList.size) { location ->
                            ListItem(
                                headlineContent = {
                                    Text(locationList[location].name, modifier = Modifier.padding(horizontal = 16.dp))
                                },
                                trailingContent = {
                                    Checkbox(
                                        checked = remember { mutableStateOf(locationList[location].enabled) }.value,
                                        onCheckedChange = {
                                            locationList[location].enabled = !locationList[location].enabled
                                            Log.d(TAG, "item at ${locationList[location]} swapped")
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .clickable {
                                        locationList[location].enabled = !locationList[location].enabled
                                        Log.d(TAG, "item at ${locationList[location]} swapped")
                                    }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp)) {
                        TextButton(
                            onClick = {
                                showSelector.value = false
                            }
                        ) {
                            Text("Cancel")
                        }

                        TextButton(
                            onClick = {
                                showSelector.value = false
                            }
                        ) {
                            Text("Confirm")
                        }
                    }


                }
            }
        }
    }
}

@Composable
fun ClearCache(menuDb: MenuDatabase, context: Context) {
    ListItem(
        headlineContent = { Text("Clear App Cache") },
        supportingContent = { Text("Clears menu and busyness data for all locations.")},
        modifier = Modifier.clickable {
            CoroutineScope(Dispatchers.IO).launch {
                menuDb.menuDao().dropMenus()
                menuDb.waitzDao().dropWaitz()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Cache cleared.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}

//Not currently necessary, since the update checker already shows the version number
@Composable
fun AboutItem(appVersion: String) {
    ListItem(
        leadingContent = {
            Icon(
                Icons.Outlined.Info,
                contentDescription = "About",
            )
        },
        headlineContent = {
            Text(text = "Slug Menu")
        },
        supportingContent = {
            Text(text = "Version $appVersion")
        },
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
        supportingContent = { Text(text = "Current version is v$appVersion") },
        modifier = Modifier.clickable {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    latestVersion = getLatestVersion()
                } catch (e: UnresolvedAddressException) {
                    exceptionFound = "No Internet connection"
                } catch (e: SocketTimeoutException) {
                    exceptionFound = "Connection timed out"
                } catch (e: UnknownHostException) {
                    exceptionFound = "Failed to resolve URL"
                } catch (e: CertificateException) {
                    exceptionFound = "Website's SSL certificate is invalid"
                } catch (e: SSLHandshakeException) {
                    exceptionFound = "SSL handshake failed"
                } catch (e: Exception) {
                    exceptionFound = "Exception: $e"
                }
                withContext(Dispatchers.Main) {
                    if (exceptionFound.isNotEmpty()) {
                        Toast.makeText(context, exceptionFound, Toast.LENGTH_SHORT).show()
                    } else if (latestVersion != appVersion) {
//                        Toast.makeText(context, "Update available!", Toast.LENGTH_SHORT).show()
                        updateAvailable.value = true
                        newVersion.value = latestVersion
                    } else {
                        Toast.makeText(context, "You are on the latest version.", Toast.LENGTH_SHORT).show()
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
            title = { Text(text = "Update Available") },
            text = { Text(text = "A new version of Slug Menu is available. Please update to v${newVersion.value}.", fontSize = 15.sp) },
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

@Composable
fun GithubItem(context: Context) {
    ListItem(
        leadingContent = {
            // add Github icon here
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh",
            )
        },
        headlineContent = { Text(text = "Github") },
        supportingContent = { Text(text = "Follow the app's development on Github") },
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/prapooskur/SlugMenu")
            startActivity(context, intent, null)
        }
    )
}
