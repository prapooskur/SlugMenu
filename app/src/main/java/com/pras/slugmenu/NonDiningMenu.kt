package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun NonDiningMenu(navController: NavController, menu: MutableList<String>, name: String) {
    Log.d("TAG", "Opening CoffeeMenu!")
//    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"
    Column() {
        TopBar(titleText = name, navController = navController)
        PrintPriceMenu(itemList = menu)

    }

}
