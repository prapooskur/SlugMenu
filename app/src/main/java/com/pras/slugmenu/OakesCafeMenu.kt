package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDateTime
//Swipable tabs
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.channels.UnresolvedAddressException
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OakesCafeMenuRoom(navController: NavController, locationName: String, locationUrl: String, menuDatabase: MenuDatabase) {
    Log.d("TAG", "Opening OakesCafeMenu with room!")

    val currentDate = LocalDate.now().toString()
    val menuDao = menuDatabase.menuDao()

    var menuList by remember { mutableStateOf<Array<MutableList<String>>>(arrayOf(mutableListOf(),mutableListOf())) }
    val dataLoadedState = remember { mutableStateOf(false) }
    var noInternet by remember { mutableStateOf(false) }

    val showBottomSheet = remember { mutableStateOf(false) }
    val showWaitzDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val menu = menuDao.getMenu(locationName)
            if (menu != null && menu.cacheDate == currentDate) {
                menuList = MenuTypeConverters().fromString(menu.menus)
                Log.d("TAG","menu list: ${menuList.size}")
                dataLoadedState.value = true
            } else {
                try {
                    menuList = getOakesMenuAsync(locationUrl)
                    Log.d("TAG", "menu list: ${menuList.size}")
                    menuDao.insertMenu(
                        Menu(
                            locationName,
                            MenuTypeConverters().fromList(menuList),
                            currentDate
                        )
                    )
                } catch (e: UnresolvedAddressException) {
                    noInternet = true
                }
                dataLoadedState.value = true
            }
        }
    }
    if (noInternet) {
        ShortToast("No internet connection")
    }

    Column {
        if (dataLoadedState.value) {
            Scaffold(
                topBar = {
                    TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
                },
                content = {padding ->
                    if (menuList.isNotEmpty()) {
                        PriceTabBar(menuList,padding)
                    } else {
                        PriceTabBar(arrayOf(mutableListOf(), mutableListOf()),padding)
                    }
                },
                //floating action button - shows hours bottom sheet on press
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showBottomSheet.value = !showBottomSheet.value }
                    ) {
                        Icon(Icons.Outlined.Info,"Info")
                    }
                },
                floatingActionButtonPosition = FabPosition.End
            )
        }
        else {
            TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator()
            }
        }
    }

    /*
    Column {
        TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
        if (dataLoadedState.value) {
            if (menuList.isNotEmpty()) {
                PriceTabBar(menuList)
            } else {
                PriceTabBar(
                    arrayOf(mutableListOf(), mutableListOf()),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator()
            }
        }
    }

     */

    HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = rememberModalBottomSheetState(), locationName = locationName)
    WaitzDialog(showDialog = showWaitzDialog, locationName = locationName)

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PriceTabBar(menuArray: Array<MutableList<String>>, padding: PaddingValues) {
    val currentHour: Int = LocalDateTime.now().hour
//    Log.d("TAG","hour: "+currentHour)


    val titles: List<String> = if (menuArray[0].isEmpty() && menuArray[1].isEmpty()) {
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
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initState)
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
            PrintOakesMenu(itemList = menuArray[state])
        }
    }
}


//replaced with oakescafemenuroom
/*
@Composable
fun OakesCafeMenu(navController: NavController, menu: Array<MutableList<String>>, name: String) {
    Log.d("TAG", "Opening OakesCafeMenu!")
//    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"
    Column() {
        if (menu.isNotEmpty()) {
            PriceTabBar(menu, navController, name)
        } else {
            PriceTabBar(arrayOf(mutableListOf<String>(), mutableListOf<String>()), navController, name)
        }
    }

}
 */