package com.pras.slugmenu

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, useMaterialYou: MutableState<Boolean>, menuDb: MenuDatabase) {
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
                    MaterialYouSwitcher(useMaterialYou = useMaterialYou)
                }
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
                    onClick = null // null recommended for accessibility with screenreaders
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

enum class ThemeOption {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun MaterialYouSwitcher(useMaterialYou: MutableState<Boolean>) {
    var checked by remember { mutableStateOf(true) }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    checked = !checked
                    useMaterialYou.value = checked
                    Log.d("TAG", "myoutog")
                },
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,

            onCheckedChange = {
                /*
                useMaterialYou.value = it
                checked = it

                 */
            }
        )
        Text(
            text = "Enable Material You Theming",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}