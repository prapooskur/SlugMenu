package com.example.slugmenu

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDateTime

@Composable
fun OakesCafeMenu(navController: NavController, menu: Array<MutableList<String>>, name: String) {
    Log.d("TAG", "Opening OakesCafeMenu!")
//    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"
    Column() {
        PriceTabBar(menu[0],menu[1],navController,"Oakes Cafe")

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PriceTabBar(breakfastMenu: MutableList<String>, allDayMenu: MutableList<String>, navController: NavController, locationName: String = "default college") {
    val currentHour: Int = LocalDateTime.now().hour
//    Log.d("TAG","hour: "+currentHour)


    val titles: List<String> = if (breakfastMenu.isEmpty() && allDayMenu.isEmpty()) {
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
//    Log.d("TAG","initstate: "+initState)

    var state by remember { mutableStateOf(initState) }
    val pagerState = rememberPagerState()

//    val menuItems = remember { mutableStateOf(mutableListOf<String>()) }

    Surface(
// elevation not necessary
//        shadowElevation = 4.dp
    ) {
        Column() {
            TopBar(titleText = locationName, color = MaterialTheme.colorScheme.primary, navController = navController)
        }
    }

    Column {
        TabRow(
            selectedTabIndex = state
            /*
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }

             */
        ) {
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
                PrintMenu(itemList = allDayMenu)
                // Content for Tab 2
            }
        }

    }
}