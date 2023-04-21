package com.example.slugmenu

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.D
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun TabBar(breakfastMenu: MutableList<String>, lunchMenu: MutableList<String>, dinnerMenu: MutableList<String>, lateNightMenu: MutableList<String>) {
    val currentHour: Int = LocalDateTime.now().hour
//    Log.d("TAG","hour: "+currentHour)


    val initState: Int = when {
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

    val titles: List<String>
    if (lateNightMenu.isEmpty()) {
        titles = listOf("Breakfast", "Lunch", "Dinner")
    } else {
        titles = listOf("Breakfast", "Lunch", "Dinner", "Late Night")
    }

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


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PrintMenu(itemList: MutableList<String>) {
    LazyColumn {
        items(itemList.size) {item ->
            val itemval = itemList[item]
            var boldness = FontWeight.Normal
            if (itemval.contains("--")) {
                boldness = FontWeight.ExtraBold
            }
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                text = {
                    Text(
                        itemList[item],
                        fontWeight = boldness,
                        color = Color.White
                    )
                }

            )


            /*
            Text (
                text = itemList[item]+"\n",
                fontWeight = boldness,
                color = Color.White
            )
             */
        }
    }
}

/*
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DisplayMenu(inputUrl: String, time: Time) {

    var itemList by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        itemList = withContext(Dispatchers.IO) { // run the blocking network call on a background thread
            getWebData(inputUrl, time)
        }
    }


    LazyColumn {
        items(itemList.size) {item ->
            val itemval = itemList[item]
            var boldness = FontWeight.Normal
            if (itemval.contains("--")) {
                boldness = FontWeight.ExtraBold
            }
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                text = {
                    Text(
                        itemList[item],
                        fontWeight = boldness,
                        color = Color.White
                    )
                }

            )


            /*
            Text (
                text = itemList[item]+"\n",
                fontWeight = boldness,
                color = Color.White
            )
             */
        }

    }
}

 */

/*
@Composable
fun showMenuData(inputUrl: String, time: Time) {
    val output: MutableList<String> = getWebData(inputUrl, time)
    DisplayMenu(output)
}
 */

