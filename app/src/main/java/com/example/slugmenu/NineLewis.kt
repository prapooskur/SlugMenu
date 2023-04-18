package com.example.slugmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun NineLewis(navController: NavController) {
    val tabTitles = listOf("Breakfast", "Lunch", "Dinner", "Late Night")
    var selectedTabIndex by remember { mutableStateOf(0) }
    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            backgroundColor = Color.White,
            contentColor = Color.Black
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        // Content for each tab goes here
        when (selectedTabIndex) {
            0 -> { /* Content for tab 1 */ }
            1 -> { /* Content for tab 2 */ }
            2 -> { /* Content for tab 3 */ }
        }
    }
}
