package com.example.slugmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun CowellStev(navController: NavController) {
    var state by remember { mutableStateOf(0) }
    val titles = listOf("Breakfast", "Lunch", "Dinner", "Late Night")

    Column {
        TabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = { Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally), textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        Text(
            color = Color.White,
            text = "Text tab "+titles[state]+" selected"
        )
    }
}

