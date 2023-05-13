package com.pras.slugmenu

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.internal.StringUtil.padding

@Composable
fun SettingsScreen(navController: NavController, useMaterialYou: MutableState<Boolean>, menuDb: MenuDatabase, preferencesDataStore: PreferencesDatastore) {
    val context = LocalContext.current
    Log.d("TAG","test $useMaterialYou")
    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                TopBar(
                    titleText = "Settings",
                    color = MaterialTheme.colorScheme.primary,
                    navController = navController
                )
            }
        },
        content = { innerPadding ->
            Column(Modifier.padding(innerPadding)) {
                Text("App Theme:")
                ThemeSwitcher()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MaterialYouSwitcher(useMaterialYou = useMaterialYou, preferencesDataStore = preferencesDataStore)
                }
                Divider()
                LayoutSwitcher(preferencesDataStore = preferencesDataStore)
                Divider()
                ClearCache(menuDb = menuDb, context = LocalContext.current)

            }
        }
    )
}



@Composable
fun ThemeSwitcher() {
    val themeOptions = listOf("System Default", "Light", "Dark")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(themeOptions[0]) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(Modifier.selectableGroup()) {
        themeOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = {
                            onOptionSelected(text)
                            Log.d("TAG", "tesr")
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MaterialYouSwitcher(useMaterialYou: MutableState<Boolean>, preferencesDataStore: PreferencesDatastore) {
    var checked by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val materialYouEnabled = preferencesDataStore.getMaterialYouPreference.first()
        checked = materialYouEnabled
    }
    val coroutineScope = rememberCoroutineScope()
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    checked = !checked
                    useMaterialYou.value = checked
                    coroutineScope.launch {
                        preferencesDataStore.setMaterialYouPreference(checked)
                    }
                    Log.d("TAG", "material you toggled")
                },
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,

            onCheckedChange = {
                checked = !checked
                useMaterialYou.value = checked
                coroutineScope.launch {
                    preferencesDataStore.setMaterialYouPreference(checked)
                }
                Log.d("TAG", "material you toggled")
            }
        )
        Text(
            text = "Enable Material You Theming",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
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
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(themeOptions[0]) }
    val coroutineScope = rememberCoroutineScope()
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(Modifier.selectableGroup()) {
        themeOptions.forEach { text ->
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
//                            onOptionSelected(text)
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
//                        onOptionSelected(text)
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
                    }// null recommended for accessibility with screen readers
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ClearCache(menuDb: MenuDatabase, context: Context) {
    ListItem(
        headlineContent = { Text("Clear Menu Cache") },
        supportingContent = { Text("Clears the cache for all location menus.")},
        modifier = Modifier.clickable {
            CoroutineScope(Dispatchers.IO).launch {
                menuDb.menuDao().dropMenus()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Cache cleared.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}