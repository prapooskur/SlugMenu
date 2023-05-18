package com.pras.slugmenu

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.channels.UnresolvedAddressException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
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
    var noInternet by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = DateTimeFormatter.ofPattern("M-dd-yyyy");
    val encodedDate = LocalDate.now().format(dateFormat).replace("-", "%2f")
    val currentDateState = remember { mutableStateOf(encodedDate) }
    Log.d("TAG", "current date: $encodedDate")

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val menu = menuDao.getMenu(locationName)
            //return cached menu if it was cached today, get new data if it wasn't
            if (menu != null && menu.cacheDate == currentDate) {
                menuList = MenuTypeConverters().fromString(menu.menus)
                dataLoadedState.value = true
            } else {
                try {
                    menuList = getDiningMenuAsync(locationUrl)
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
            // If the data has been loaded from the cache, display the menu
            Log.d("TAG", (System.currentTimeMillis() / 1000L).toString())
            Scaffold(
                topBar = {
                    TopBar(titleText = locationName, color = MaterialTheme.colorScheme.primary, navController = navController)
                },
                content = {padding ->
                    SwipableTabBar(menuArray = menuList, navController = navController, collegeName = locationName, padding = padding)
                },
                //floating action button, currently does nothing
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showDatePicker = !showDatePicker }
                    ) {
                        Icon(Icons.Filled.DateRange,"Calendar")
                    }
                },
                floatingActionButtonPosition = FabPosition.End
            )

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
                DatePickerDialog(
                    onDismissRequest = {
                        // Dismiss the dialog when the user clicks outside the dialog or on the back
                        // button. If you want to disable that functionality, simply use an empty
                        // onDismissRequest.
                        showDatePicker = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                Log.d("DATE","date picked: "+(datePickerState.selectedDateMillis?.div(1000L)).toString())
                                showDatePicker = false
                            },
                            enabled = confirmEnabled.value
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }

                ) {
                    DatePicker(state = datePickerState)
                }
            }

        } else {
            // Otherwise, display a loading indicator
            Surface {
                Column {
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

@Composable
fun ShortToast(text: String) {
    Toast.makeText(LocalContext.current, text, Toast.LENGTH_SHORT).show()
}

fun TimestampDate(timestamp: Long): String {
    val date = LocalDate.ofEpochDay(timestamp)
    return date.toString()
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


