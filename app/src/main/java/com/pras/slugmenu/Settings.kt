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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(navController: NavController, useMaterialYou: MutableState<Boolean>, themeChoice: MutableState<Int>, menuDb: MenuDatabase, preferencesDataStore: PreferencesDatastore) {
    Log.d("TAG","test $useMaterialYou")
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 4.dp) {
                    TopBarClean(
                        titleText = "Settings",
                        navController = navController
                    )
                }
            },
            content = { innerPadding ->
                Column(Modifier.padding(innerPadding)) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "App Theme:",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    )
                    ThemeSwitcher(
                        preferencesDataStore = preferencesDataStore,
                        themeChoice = themeChoice
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MaterialYouSwitcher(
                            useMaterialYou = useMaterialYou,
                            preferencesDataStore = preferencesDataStore
                        )
                    }
                    Divider()
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "App Layout:",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    )
                    LayoutSwitcher(preferencesDataStore = preferencesDataStore)
                    Divider()
                    ClearCache(menuDb = menuDb, context = LocalContext.current)
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
    var checked by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val amoledBlackEnabled = preferencesDataStore.getAmoledPreference.first()
        checked = amoledBlackEnabled
    }
    LaunchedEffect(Unit) {
        val amoledEnabled = preferencesDataStore.getAmoledPreference.first()
        checked = amoledEnabled
    }
    val coroutineScope = rememberCoroutineScope()
    Row(modifier = Modifier.clickable(
            onClick = {
                checked = !checked
                coroutineScope.launch {
                    preferencesDataStore.setAmoledPreference(checked)
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
                    checked = checked,
                    onCheckedChange = {
                        checked = !checked
                        coroutineScope.launch {
                            preferencesDataStore.setAmoledPreference(checked)
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
            /*
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
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
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

             */
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