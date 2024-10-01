package com.pras.slugmenu.ui.elements

//Swipable tabs
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pras.slugmenu.data.sources.MenuSection
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.math.min


private const val TAG = "TabMenus"

//Swipable tab bar

@Composable
fun SwipableTabBar(
    menuArray: List<List<MenuSection>>,
    onFavorite: (String) -> Unit,
    onFullCollapse: () -> Unit,
    padding: PaddingValues
) {
    val currentHour: Int = LocalDateTime.now().hour
    val currentMinute: Int = LocalDateTime.now().minute
    val currentDay: DayOfWeek = LocalDateTime.now().dayOfWeek
    Log.d(TAG, "day: $currentDay")
//    Log.d(TAG,"hour: "+currentHour)

    Log.d(TAG, "provided array: $menuArray")

    val allTabs = listOf("Breakfast", "Lunch", "Dinner", "Late Night")

    val titles: List<String> = if (menuArray.isEmpty() || menuArray.all { it.isEmpty() }) {
        listOf("Closed")
    } else {
        allTabs.take(menuArray.size)
    }

    when (menuArray.size) {
        1 -> listOf("Closed")
        2 -> listOf("Breakfast", "Lunch")
        3 -> listOf("Breakfast", "Lunch", "Dinner")
        4 -> listOf("Breakfast", "Lunch", "Dinner", "Late Night")
    }

    val initTimeState = if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
        // weekend hours
        when {
            titles.size == 1 -> 0
            //Breakfast from 7AM-10AM (but start showing it from beginning of the day)
            currentHour in 0..<10 -> 0
            // Brunch from 10AM-2PM, then continuous from 2PM-5PM
            currentHour in 10..<17 -> 1
            // dinner from 5PM-8PM
            currentHour in 17..<20 -> 2
            // Late night from 8PM-11PM if available, keep showing dinner otherwise
            currentHour in 20..23 && (menuArray.size > 3 && menuArray[3].isNotEmpty()) -> 3
            currentHour in 20..23 && (menuArray.size <= 3 || menuArray[3].isEmpty()) -> 2
            // if all else fails (even though it never should), default to breakfast
            else -> 0
        }
    } else {
        //normal hours
         when {
            titles.size == 1 -> 0
            //Breakfast from 7AM-11AM, then continuous till 11:30AM (but start showing it from beginning of the day)
            (currentHour in 0..<11) || (currentHour == 11 && currentMinute < 30) -> 0
            // Lunch from 11:30AM-2PM, then continuous till 5PM
            (currentHour in 11..<17) || menuArray.size <= 2 || menuArray[2].isEmpty() -> 1
            // dinner from 5PM-8PM
            currentHour in 17..<20 -> 2
            // Late night from 8PM-11PM if available, keep showing dinner otherwise
            currentHour in 20..23 && (menuArray.size > 3 && menuArray[3].isNotEmpty()) -> 3
            currentHour in 20..23 && (menuArray.size <= 3 || menuArray[3].isEmpty()) -> 2
            // if all else fails (even though it never should), default to breakfast
            else -> 0
        }
    }

    val initState = min(initTimeState, menuArray.size)

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
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
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
                        } else if (current - state <= -3) {
                            scope.launch {
                                pagerState.scrollToPage(1)
                                pagerState.animateScrollToPage(index)
                            }
                        } else {
                            scope.launch { pagerState.animateScrollToPage(index) }
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState
        ) { state ->
            if (titles[0] != "Closed") {
                PrintMenu(itemList = menuArray[state], onFavorite = onFavorite, onFullCollapse = onFullCollapse)
            } else {
                PrintMenu(itemList = listOf(), onFavorite = { /* do nothing */ }, onFullCollapse = { /* do nothing */ })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrintMenu(itemList: List<MenuSection>, onFavorite: (String) -> Unit, onFullCollapse: () -> Unit) {
    Log.d(TAG, "provided list: $itemList")
    if (itemList.isNotEmpty()) {

        val itemVisibility = remember { mutableStateListOf<Boolean>() }.apply {
            repeat(itemList.size) {
                add(true)
            }
        }

        val columnState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        LazyColumn (
            modifier = Modifier
                .fillMaxSize(),
            // makes it so that content isn't stuck behind the nav bar
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            state = columnState
        ) {
            items(itemList.size) { index ->
                val currentSection = itemList[index]
//                var showCategoryItems by remember { mutableStateOf(true) }
                val iconRotation by animateFloatAsState(targetValue = if (!itemVisibility[index]) -90F else 0F, label = "collapse category")

                HorizontalDivider(thickness = 2.dp)
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                itemVisibility[index] = !itemVisibility[index]
                            },
                            onLongClick = {
                                // scroll to top
                                coroutineScope.launch {
                                    columnState.animateScrollToItem(0)
                                }
                                itemVisibility.fill(!itemVisibility[index])
                                onFullCollapse()
                            }
                        ),
                    headlineContent = {
                        Text(
                            currentSection.title,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Collapse category",
                            modifier = Modifier.graphicsLayer(rotationZ = iconRotation)
                        )
                    }
                )
                HorizontalDivider(thickness = 2.dp)

                for (item in currentSection.items) {
                    AnimatedVisibility(
                        visible = itemVisibility[index],
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        onFavorite(item.name)
                                    }
                                ),
                            headlineContent = {
                                Text(
                                    item.name,
                                )
                            },
                            supportingContent = {
                                if (item.price.isNotEmpty()) {
                                    Text(
                                        item.price,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    } else {
        val unavailableText = "Not Open Today"
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