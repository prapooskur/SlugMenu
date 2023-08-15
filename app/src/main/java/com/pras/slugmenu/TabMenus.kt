package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
//Swipable tabs
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.mutableIntStateOf
import com.pras.slugmenu.ui.elements.pagerTabIndicatorOffset
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.Collections


private const val TAG = "TabMenus"

//Swipable tab bar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipableTabBar(menuArray: List<List<String>>, padding: PaddingValues) {
    val currentHour: Int = LocalDateTime.now().hour
    val currentMinute: Int = LocalDateTime.now().minute
    val currentDay: DayOfWeek = LocalDateTime.now().dayOfWeek
    Log.d(TAG, "day: $currentDay")
//    Log.d(TAG,"hour: "+currentHour)

    val titles: List<String> = if (menuArray.isEmpty() || menuArray.all { it.isEmpty() }) {
        listOf("Closed")
    } else if (menuArray[3].isEmpty()) {
        listOf("Breakfast", "Lunch", "Dinner")
    } else {
        listOf("Breakfast", "Lunch", "Dinner", "Late Night")
    }

    val initState = if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
        // weekend hours
        when {
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
         when {
            titles.size <= 1 -> 0
            //Breakfast from 12AM-11:30AM
            (currentHour in 0..10) || (currentHour == 11 && currentMinute < 30) -> 0
            // Lunch from 11:30AM-5PM
            (currentHour == 11 && currentMinute >= 30) || (currentHour in 12..17) || (menuArray[2].isEmpty()) -> 1
            // dinner from 5PM-8PM
            currentHour in 17..19 -> 2
            // Late night from 8PM-11PM if available, dinner archive otherwise
            currentHour in 20..23 && (menuArray[3].isNotEmpty()) -> 3
            currentHour in 20..23 && (menuArray[3].isEmpty()) -> 2
            // if all else fails (even though it never should), default to breakfast
            else -> 0
        }
    }
//    Log.d(TAG,"initstate: "+initState)

    var state by remember { mutableIntStateOf(initState) }
    val pagerState = rememberPagerState(
        initialPage = initState,
        initialPageOffsetFraction = 0f
    ) {
        // provide pageCount
        titles.size
    }
    val scope = rememberCoroutineScope()

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
                    onClick = {
                        val current = state
                        state = index
                        Log.d(TAG,"$current $state ${current-state}")
                        // make the animated scroll a bit more continuous
                        // without this, it skips to tab 3 when going from 1 -> 4 and 2 when going from 4 -> 1
                        if (current - state >= 3) {
                            scope.launch {
                                pagerState.scrollToPage(2)
                                pagerState.animateScrollToPage(index)
                            }
                            Log.d(TAG,"rtl")
                        } else if (current - state <= -3) {
                            scope.launch {
                                pagerState.scrollToPage(1)
                                pagerState.animateScrollToPage(index)
                            }
                            Log.d(TAG,"ltr")
                        } else {
                            scope.launch { pagerState.animateScrollToPage(index) }
                            Log.d(TAG,"basic")
                        }
                    },
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
            state = pagerState
        ) { state ->
            if (titles[0] != "No menu available" && titles[0] != "Closed") {
                PrintMenu(itemList = menuArray[state])
            } else {
                PrintMenu(itemList = mutableListOf("Not Open Today"))
            }

            // commented out for now, since some holidays trigger this state in the non-custom date menu
            /*
            else {
                ShortToast(text = "No menu available")
            }
             */
        }
    }
}

@Composable
fun PrintMenu(itemList: List<String>) {
    if (itemList.isNotEmpty() && itemList[0] != "Not Open Today") {
        LazyColumn (
            modifier = Modifier
                .fillMaxSize(),
            // makes it so that content isn't stuck behind the nav bar
            contentPadding = WindowInsets.navigationBars.asPaddingValues()
        ) {
            items(itemList.size) { item ->
                val itemval = itemList[item]
                var boldness = FontWeight.Normal
                var divider = false
                if (itemval.contains("--") || itemval.contains("—")) {
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
    } else {
        val unavailableText = itemList.getOrElse(0) { "Not Available Today" }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = unavailableText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                //eyeballed it, this is close enough to halfway between the tab and the FAB
                modifier = Modifier.offset(y = (-40).dp)
            )
        }
    }
}

//Menus with prices - Coffee Bars, Cafes, Markets
@Composable
fun PrintPriceMenu(itemList: List<String>, padding: PaddingValues) {
    if (itemList.isNotEmpty()) {
        LazyColumn(modifier = Modifier.padding(padding), contentPadding = WindowInsets.navigationBars.asPaddingValues()) {
            items(itemList.size) { item ->
                val itemval = itemList[item]

                if (itemval.contains("—")) {
                    if (item != 0) {
                        Divider(
                            thickness = 2.dp
                        )
                    }

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

                //swap double and single to stop them being printed out of order
                // without the equality check, caramel latte was being swapped with cappuccino
                if (item < itemList.size-3 && itemList[item+1].contains("Double") && itemList[item+3].contains("Single") && itemList[item+1].substringBefore(",") == itemList[item+3].substringBefore(",")) {
                    Collections.swap(itemList,item,item+2)
                    Collections.swap(itemList,item+1,item+3)
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
        Column(modifier = Modifier.padding(padding).fillMaxWidth()) {
            TabRow(
                selectedTabIndex = 0
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
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Not Open Today",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    //eyeballed it, this is close enough to halfway between the tab and the FAB
                    modifier = Modifier.offset(y = (-40).dp)
                )
            }
        }
    }
}

//Menus with multiple lists of prices - Oakes
@Composable
fun PrintOakesMenu(itemList: List<String>) {
    if (itemList.isNotEmpty() && itemList[0] != "Not Open Today") {
        LazyColumn (contentPadding = WindowInsets.navigationBars.asPaddingValues()) {
            items(itemList.size) { item ->
                val itemval = itemList[item]

                if (itemval.contains("--") || itemval.contains("—")) {
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


                //swap double and single to stop them being printed out of order
                // without the equality check, caramel latte was being swapped with cappuccino
                if (item < itemList.size-1 && itemList[item+1].contains("Double") && itemList[item+3].contains("Single") && itemList[item+1].substringBefore(",") == itemList[item+3].substringBefore(",")) {
                    Collections.swap(itemList,item,item+2)
                    Collections.swap(itemList,item+1,item+3)
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
        val unavailableText = itemList.getOrElse(0) { "Not Available Today" }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = unavailableText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                //eyeballed it, this is close enough to halfway between the tab and the FAB
                modifier = Modifier.offset(y = (-40).dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PriceTabBar(menuArray: List<List<String>>, padding: PaddingValues) {
    val currentHour: Int = LocalDateTime.now().hour
//    Log.d(TAG,"hour: "+currentHour)


    val titles: List<String> = if ((menuArray[0].isEmpty() || menuArray[0] == listOf("Not Open Today")) && menuArray[1].isEmpty()) {
        listOf("Closed")
    } else {
        listOf("Breakfast", "All Day")
    }

    val initState: Int = when {
        titles.size <= 1 -> 0
        //Breakfast from 12AM-11AM
        currentHour in 0..11 -> 0
        // All day for rest of day
        currentHour in 12..23 -> 1
        // if all else fails (even though it never should), default to breakfast
        else -> 0
    }
//    Log.d(TAG,"initstate: "+initState)

    var state by remember { mutableIntStateOf(initState) }
    val pagerState = rememberPagerState(
        initialPage = initState,
        initialPageOffsetFraction = 0f
    ) {
        // provide pageCount
        titles.size
    }
    val scope = rememberCoroutineScope()

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
                Tab(
                    selected = state == index,
                    onClick = { state = index; scope.launch{pagerState.animateScrollToPage(index)} },
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
            state = pagerState
        ) {state ->
            PrintOakesMenu(itemList = menuArray[state])
        }
    }
}

