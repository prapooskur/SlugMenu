package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDateTime
//Swipable tabs
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import kotlinx.coroutines.launch
import java.time.DayOfWeek


//Swipable tab bar

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SwipableTabBar(menuArray: Array<MutableList<String>>, navController: NavController, collegeName: String = "default college") {
    val currentHour: Int = LocalDateTime.now().hour
    val currentMinute: Int = LocalDateTime.now().minute
    val currentDay: DayOfWeek = LocalDateTime.now().dayOfWeek
    Log.d("TAG", "day: $currentDay")
//    Log.d("TAG","hour: "+currentHour)


    val titles: List<String> = if (menuArray.isEmpty()) {
        listOf("Null")
    } else if (menuArray[0].isEmpty() && menuArray[1].isEmpty() && menuArray[2].isEmpty() && menuArray[3].isEmpty()) {
        listOf("Closed")
    } else if (menuArray[3].isEmpty()) {
        listOf("Breakfast", "Lunch", "Dinner")
    } else {
        listOf("Breakfast", "Lunch", "Dinner", "Late Night")
    }

    val initState: Int
    if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
        // weekend hours
        initState = when {
            titles.size <= 1 -> 0
            //Breakfast from 12AM-11AM
            (currentHour in 0..11) -> 0
            // Brunch from 10AM-5PM
            currentHour in 10..17 -> 1
            // dinner from 5PM-8PM
            currentHour in 17..19 -> 2
            // Late night from 8PM-11PM if available, dinner archive otherwise
            currentHour in 20..23 && (menuArray[3].isNotEmpty()) -> 3
            currentHour in 20..23 && (menuArray[3].isEmpty()) -> 2
            // if all else fails (even though it never should), default to breakfast
            else -> 0
        }
    } else {
        //normal hours
        initState = when {
            titles.size <= 1 -> 0
            //Breakfast from 12AM-11:30AM
            (currentHour in 0..10) || (currentHour == 11 && currentMinute < 30) -> 0
            // Lunch from 11:30AM-5PM
            (currentHour == 11 && currentMinute >= 30) || (currentHour in 12..17) -> 1
            // dinner from 5PM-8PM
            currentHour in 17..19 -> 2
            // Late night from 8PM-11PM if available, dinner archive otherwise
            currentHour in 20..23 && (menuArray[3].isNotEmpty()) -> 3
            currentHour in 20..23 && (menuArray[3].isEmpty()) -> 2
            // if all else fails (even though it never should), default to breakfast
            else -> 0
        }
    }
//    Log.d("TAG","initstate: "+initState)

    var state by remember { mutableStateOf(initState) }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initState)
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopBar(titleText = collegeName, color = MaterialTheme.colorScheme.primary, navController = navController)
        },
        content = {padding ->
            Column(modifier = Modifier.padding(padding)) {
                TabRow(
                    selectedTabIndex = state,
                    indicator = { tabPositions -> // 3.
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(
                                pagerState,
                                tabPositions
                            )
                        )
                    }
                ) {
                    titles.forEachIndexed { index, title ->
                        // create a tab for each element in titles
                        Tab(
                            selected = state == index,
                            onClick = { state = index; scope.launch{pagerState.scrollToPage(index)} },
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
                HorizontalPager(
                    pageCount = titles.size,
                    state = pagerState
                ) {state ->
                    PrintMenu(itemList = menuArray[state])
                }
            }
        },
        //floating action button, currently does nothing
        /*
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* do something */ }
            ) {
                Icon(Icons.Filled.DateRange,"Calendar")
            }
        },
        floatingActionButtonPosition = FabPosition.End

         */
    )
}

@Composable
fun TopBar(titleText: String, color: Color = MaterialTheme.colorScheme.primary, navController: NavController) {
    TopAppBar(
        title = {
            Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = {navController.navigateUp()}) {
                Icon(
                    Icons.Filled.ArrowBack, contentDescription = "Back",tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        backgroundColor = color
//        elevation = 8.dp
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintMenu(itemList: MutableList<String>) {
    if (itemList.size > 0) {
        LazyColumn {
            items(itemList.size) { item ->
                val itemval = itemList[item]
                var boldness = FontWeight.Normal
                var divider = false
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
                    headlineContent = {
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
            }
        }
    }
}

//Menus with prices - Coffee Bars, Cafes, Markets
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPriceMenu(itemList: MutableList<String>) {
    if (itemList.size > 0) {
        LazyColumn {
            items(itemList.size) { item ->
                val itemval = itemList[item]

                if (itemval.contains("--")) {
                    Divider(
                        thickness = 2.dp
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                itemList[item],
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.End
                            )
                        },
                    )

                    Divider(
                        thickness = 2.dp
                    )
                }

                if (itemval.contains("$") && !itemList[item+1].contains("$") ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                itemList[item+1],
                            )
                        },
                        supportingContent = {
                            Text(
                                itemList[item],
                                fontWeight = FontWeight.Bold,
                            )
                        }

                    )
                }
            }
        }
    } else {
        TabRow(
            selectedTabIndex = 0
            /*
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }

             */
        ) {
            Tab(
                selected = true,
                onClick = { /*nothing*/ },
                text = {
                    Text(
                        text = "Closed",
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
}

//Menus with multiple lists of prices - Oakes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintOakesMenu(itemList: MutableList<String>) {
    if (itemList.size > 0) {
        LazyColumn {
            items(itemList.size) { item ->
                val itemval = itemList[item]

                if (itemval.contains("--")) {
                    Divider(
                        thickness = 2.dp
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                itemList[item],
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.End
                            )
                        },
                    )

                    Divider(
                        thickness = 2.dp
                    )
                }

                if (itemval.contains("$") && !itemList[item+1].contains("$") ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                itemList[item+1],
                            )
                        },
                        supportingContent = {
                            Text(
                                itemList[item],
                                fontWeight = FontWeight.Bold,
                            )
                        }

                    )
                }
            }
        }
    } else {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
            }
        )
    }
}