package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.channels.UnresolvedAddressException
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonDiningMenuRoom(navController: NavController, locationName: String, locationUrl: String, menuDatabase: MenuDatabase) {
    Log.d("TAG", "Opening NonDiningMenu with room!")

    val currentDate = LocalDate.now().toString()
    val menuDao = menuDatabase.menuDao()

    var menuList by remember { mutableStateOf<Array<MutableList<String>>>(arrayOf(mutableListOf())) }
    val dataLoadedState = remember { mutableStateOf(false) }
    var noInternet by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val menu = menuDao.getMenu(locationName)
            if (menu != null && menu.cacheDate == currentDate) {
                menuList = MenuTypeConverters().fromString(menu.menus)
                dataLoadedState.value = true
            } else {
                try {
                    menuList = getSingleMenuAsync(locationUrl)
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

    val showBottomSheet = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarClean(titleText = locationName, navController = navController)
        },
        content = {padding ->
            if (menuList.isNotEmpty()) {
                PrintPriceMenu(itemList = menuList[0], padding)
            } else {
                PrintPriceMenu(itemList = mutableListOf(), padding)
            }
        },
        //floating action button - shows hours bottom sheet
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet.value = !showBottomSheet.value }
            ) {
                Icon(Icons.Outlined.Info,"Info")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    )

    /*
    Column {
        TopBar(titleText = locationName, navController = navController, showBottomSheet = showBottomSheet)
        if (dataLoadedState.value) {
            if (menuList.isNotEmpty()) {
                PrintPriceMenu(itemList = menuList[0])
            } else {
                PrintPriceMenu(itemList = mutableListOf())
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

}


//replaced with NonDiningMenuRoom
/*
@Composable
fun NonDiningMenu(navController: NavController, menu: Array<MutableList<String>>, name: String) {
    Log.d("TAG", "Opening NonDiningMenu!")
//    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"
    Column() {
        TopBar(titleText = name, navController = navController)
        if (menu.isNotEmpty()) {
            PrintPriceMenu(itemList = menu[0])
        } else {
            PrintPriceMenu(itemList = mutableListOf<String>())
        }
    }

}
*/
