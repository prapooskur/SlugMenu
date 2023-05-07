package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.navigation.NavController

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


