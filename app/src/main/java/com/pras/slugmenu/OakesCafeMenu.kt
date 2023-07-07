package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pras.slugmenu.ui.elements.pagerTabIndicatorOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertificateException
import java.time.LocalDate
import java.time.LocalDateTime
import javax.net.ssl.SSLHandshakeException

private const val TAG = "OakesCafeMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OakesCafeMenuRoom(navController: NavController, locationName: String, locationUrl: String, menuDatabase: MenuDatabase) {
    Log.d(TAG, "Opening OakesCafeMenu with room!")

    val currentDate = LocalDate.now().toString()
    val menuDao = menuDatabase.menuDao()

    var menuList by remember { mutableStateOf<List<List<String>>>(listOf(listOf(),listOf())) }
    val dataLoadedState = remember { mutableStateOf(false) }
    var exceptionFound by remember { mutableStateOf("No Exception") }

    val showBottomSheet = remember { mutableStateOf(false) }
    val showWaitzDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val menu = menuDao.getMenu(locationName)
            if (menu != null && menu.cacheDate == currentDate) {
                menuList = MenuTypeConverters().fromString(menu.menus)
                Log.d(TAG, "menu list: $menuList")
                dataLoadedState.value = true
            } else {
                try {
                    menuList = getOakesMenuAsync(locationUrl)
                    Log.d(TAG, "menu list: ${menuList.size}")
                    menuDao.insertMenu(
                        Menu(
                            locationName,
                            MenuTypeConverters().fromList(menuList),
                            currentDate
                        )
                    )
                } catch (e: UnresolvedAddressException) {
                    exceptionFound = "No Internet connection"
                } catch (e: SocketTimeoutException) {
                    exceptionFound = "Connection timed out"
                } catch (e: UnknownHostException) {
                    exceptionFound = "Failed to resolve URL"
                } catch (e: CertificateException) {
                    exceptionFound = "Website's SSL certificate is invalid"
                } catch (e: SSLHandshakeException) {
                    exceptionFound = "SSL handshake failed"
                } catch (e: Exception) {
                    exceptionFound = "Exception: $e"
                }
                dataLoadedState.value = true
            }
        }
    }
    if (exceptionFound != "No Exception") {
        ShortToast(exceptionFound, LocalContext.current)
    }

    Column {
        if (dataLoadedState.value) {
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),

                topBar = {
                    TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
                },
                content = {padding ->
                    Log.d("padding",padding.toString())
                    if (menuList.isNotEmpty()) {
                        PriceTabBar(menuList,padding)
                    } else {
                        PriceTabBar(listOf(listOf("Not Open Today"), listOf()),padding)
                    }
                },
                //floating action button - shows hours bottom sheet on press
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showBottomSheet.value = !showBottomSheet.value },
                        modifier = Modifier.systemBarsPadding()
                    ) {
                        Icon(painterResource(R.drawable.schedule),"Hours")
                    }
                },
                floatingActionButtonPosition = FabPosition.End
            )
            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            Column(modifier = Modifier.fillMaxHeight()) {
                HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = bottomSheetState, locationName = locationName)
            }
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

    WaitzDialog(showDialog = showWaitzDialog, locationName = locationName, menuDatabase = menuDatabase)



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

    var state by remember { mutableStateOf(initState) }
    val pagerState = rememberPagerState(initState)
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
            pageCount = titles.size,
            state = pagerState
        ) {state ->
            PrintOakesMenu(itemList = menuArray[state])
        }
    }
}