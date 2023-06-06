package com.pras.slugmenu

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonNull.content

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, useMaterialYou: MutableState<Boolean>, themeChoice: MutableState<Int>, menuDb: MenuDatabase, preferencesDataStore: PreferencesDatastore) {
    Log.d("TAG","test $useMaterialYou")
    val useCollapsingTopBar = remember { mutableStateOf(false) }
    runBlocking {
        val collapsingTopBarChoice = preferencesDataStore.getToolbarPreference.first()
        useCollapsingTopBar.value = collapsingTopBarChoice
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
            topBar = {
                if (useCollapsingTopBar.value) {
                    CollapsingLargeTopBar(titleText = "Settings", navController = navController, scrollBehavior = scrollBehavior, isHome = false)
                } else {
                    Surface(shadowElevation = 4.dp) {
                        TopBarClean(
                            titleText = "Settings",
                            navController = navController
                        )
                    }
                }
            },
            content = { innerPadding ->
                LazyColumn(Modifier.padding(innerPadding)) {
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
                        ClearCache(
                            menuDb = menuDb,
                            context = LocalContext.current
                        )
                    }
                }
            }
        )
    }
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
                Log.d("TAG", "material you toggled")
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
                        Log.d("TAG", "material you toggled")
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
                Log.d("TAG", "amoled toggled")
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
                        Log.d("TAG", "amoled toggled")
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
                            Log.d("TAG", "layout switched")
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
                                Log.d("TAG", "layout switched")
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
            Log.d("TAG", "top bar choice toggled")
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
                    text = "Currently buggy, use with caution? Does not currently persist.",
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

                        Log.d("TAG", "Top Bar toggled")
                    }
                )
            }
        )
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
