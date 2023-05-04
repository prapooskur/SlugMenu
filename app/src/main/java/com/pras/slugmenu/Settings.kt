package com.pras.slugmenu

import android.content.Context
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
//    val sharedPrefs = getDefaultSharedPreferences(context)
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
        content = {innerPadding ->
            Text(text = "test menu please ignore",Modifier.padding(innerPadding))
        }
    )
}