package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate


@Composable
fun DiningMenuRoom(navController: NavController, locationName: String, locationUrl: String, menuDatabase: MenuDatabase) {
    Log.d("TAG", "Hello, World from room!")
//    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"

    val currentDate = LocalDate.now().toString()
    val menuDao = menuDatabase.menuDao()

    // Define a state to hold the retrieved Menu object
    var menuList by remember { mutableStateOf<Array<MutableList<String>>>(arrayOf(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())) }

    // Define a state to hold a flag indicating whether the data has been loaded from the cache
    val dataLoadedState = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val menu = menuDao.getMenu(locationName)
            if (menu != null && menu.cacheDate == currentDate) {
                menuList = MenuTypeConverters().fromString(menu.menus)
                Log.d("TAG","menu list: ${menuList.size}")
                dataLoadedState.value = true
            } else {
                menuList = getDiningMenuAsync(locationUrl)
                Log.d("TAG","menu list: ${menuList.size}")
                menuDao.insertMenu(Menu(locationName, MenuTypeConverters().fromList(menuList), currentDate))
                dataLoadedState.value = true
            }
        }
    }



    Column {
        if (dataLoadedState.value) {
            // If the data has been loaded from the cache, display the menu
            SwipableTabBar(menuList, navController, locationName)
        } else {
            // Otherwise, display a loading indicator
            Surface() {
                Column() {
                    TopBar(titleText = locationName, color = MaterialTheme.colorScheme.primary, navController = navController)
                }
            }
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
}

//replaced with diningmenuroom
/*
@Composable
fun DiningMenu(navController: NavController, menuList: Array<MutableList<String>>, collegeName: String) {
    Log.d("TAG", "Hello, World!")
//    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"


    Column {
        SwipableTabBar(menuList, navController, collegeName)
//        TabBar(menuList[0],menuList[1],menuList[2],menuList[3], navController, collegeName)
//        tabsWithSwiping()
//        DisplayMenu(inputUrl = nl, time = Time.DINNER)
    }

}
 */


