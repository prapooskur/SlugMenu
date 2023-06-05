package com.pras.slugmenu

import android.util.Log
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TopBar(titleText: String, navController: NavController, showBottomSheet: MutableState<Boolean> = mutableStateOf(false)) {
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
                    Log.d("TAG",showBottomSheet.value.toString())
                }
            ) {
                Icon(
                    Icons.Outlined.Info, contentDescription = "Hours",tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        backgroundColor = MaterialTheme.colorScheme.primaryContainer
//        elevation = 8.dp
    )
}

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
        backgroundColor = MaterialTheme.colorScheme.primaryContainer
//        elevation = 8.dp
    )
}

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
                    Log.d("TAG",showWaitzDialog.value.toString())
                }
            ) {
                Icon(
                    Icons.Outlined.Info, contentDescription = "Busyness",tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        backgroundColor = MaterialTheme.colorScheme.primaryContainer
//        elevation = 8.dp
    )
}