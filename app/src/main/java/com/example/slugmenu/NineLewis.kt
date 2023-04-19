package com.example.slugmenu

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun NineLewis(navController: NavController) {
    Log.d("TAG", "Hello, World!")
    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"

    Column() {
        TabBar()
        Spacer(modifier = Modifier.height(25.dp))
        DisplayMenu(inputUrl = nl, time = Time.LATENIGHT)
    }

}


@Composable
fun TabBar() {
    var state by remember { mutableStateOf(0) }
    val titles = listOf("Breakfast", "Lunch", "Dinner", "Late Night")

    val menuItems = remember { mutableStateOf(mutableListOf<String>()) }

    Column {
        TabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = { Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally), textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

    }
}





@Composable
fun DisplayMenu(inputUrl: String, time: Time) {

    var itemList by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        itemList = withContext(Dispatchers.IO) { // run the blocking network call on a background thread
            getWebData(inputUrl, time)
        }
    }

    LazyColumn {
       items(itemList.size) {item ->
           val itemval = "$item"
           var boldness = FontWeight.Normal
           if (itemval.contains("--")) {
               boldness = FontWeight.ExtraBold
           }
           Text (
               text = itemList[item]+"\n",
               fontWeight = boldness,
               color = Color.White
           )
       }

    }
}

/*
@Composable
fun showMenuData(inputUrl: String, time: Time) {
    val output: MutableList<String> = getWebData(inputUrl, time)
    DisplayMenu(output)
}
 */
