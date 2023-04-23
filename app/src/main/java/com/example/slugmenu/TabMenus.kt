package com.example.slugmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime

@Composable
fun TabBar(breakfastMenu: MutableList<String>, lunchMenu: MutableList<String>, dinnerMenu: MutableList<String>, lateNightMenu: MutableList<String>) {
    val currentHour: Int = LocalDateTime.now().hour
//    Log.d("TAG","hour: "+currentHour)


    val titles: List<String> = if (breakfastMenu.isEmpty() && lunchMenu.isEmpty() && dinnerMenu.isEmpty() && lateNightMenu.isEmpty()) {
        listOf("Closed")
    } else if (lateNightMenu.isEmpty()) {
        listOf("Breakfast", "Lunch", "Dinner")
    } else {
        listOf("Breakfast", "Lunch", "Dinner", "Late Night")
    }

    val initState: Int = when {
        titles.size <= 1 -> 0
        //Breakfast from 12AM-11PM
        currentHour in 0..11 -> 0
        // Lunch from 12PM-5PM
        currentHour in 12..17 -> 1
        // dinner from 5-8
        currentHour in 17..19 -> 2
        // Late night from 8-11 if available, dinner archive otherwise
        currentHour in 20..23 && (lateNightMenu.isNotEmpty()) -> 3
        currentHour in 20..23 && (lateNightMenu.isEmpty()) -> 2
        // if all else fails (even though it never should), default to breakfast
        else -> 0
    }
//    Log.d("TAG","initstate: "+initState)

    var state by remember { mutableStateOf(initState) }

    val menuItems = remember { mutableStateOf(mutableListOf<String>()) }

    Column {
        TabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        when (state) {
            0 -> {
                PrintMenu(itemList = breakfastMenu)
                // Content for Tab 1
            }
            1 -> {
                PrintMenu(itemList = lunchMenu)
                // Content for Tab 2
            }
            2 -> {
                PrintMenu(itemList = dinnerMenu)
                // Content for Tab 3
            }
            3 -> {
                PrintMenu(itemList = lateNightMenu)
                // Content for Tab 3
            }
        }

    }
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PrintMenu(itemList: MutableList<String>) {
    if (itemList.size > 0) {
        LazyColumn {
            items(itemList.size) { item ->
                val itemval = itemList[item]
                var boldness = FontWeight.Normal
                var divider: Boolean = false
                if (itemval.contains("--")) {
                    boldness = FontWeight.ExtraBold
                    divider = true
                }
                if (divider) {
                    Divider(
                        thickness = 2.dp
                    )
                }
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    headlineText = {
                        Text(
                            itemList[item],
                            fontWeight = boldness,
//                            color = Color.White
                        )
                    }

                )
                if (divider) {
                    Divider(
                        thickness = 2.dp
                    )
                }


                /*
            Text (
                text = itemList[item]+"\n",
                fontWeight = boldness,
                color = Color.White
            )
             */
            }
        }
    } else {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineText = {

            }

        )
    }
}