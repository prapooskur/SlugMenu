package com.pras.slugmenu

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.channels.UnresolvedAddressException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiningMenuRoom(navController: NavController, locationName: String, locationUrl: String, menuDatabase: MenuDatabase) {
    Log.d("TAG", "Hello, World from room!")
//    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"

    val currentDate = LocalDate.now()
    val menuDao = menuDatabase.menuDao()

    // Define a state to hold the retrieved Menu object
    var menuList by remember { mutableStateOf<Array<MutableList<String>>>(arrayOf(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())) }

    // Define a state to hold a flag indicating whether the data has been loaded from the cache
    val dataLoadedState = remember { mutableStateOf(false) }
    var noInternet by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = DateTimeFormatter.ofPattern("M-dd-yyyy")
    val titleDateFormat = DateTimeFormatter.ofPattern("M/dd/yy")
    val encodedDate = currentDate.format(dateFormat).replace("-", "%2f")
    Log.d("TAG", "current date: $encodedDate")

    val showBottomSheet = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val menu = menuDao.getMenu(locationName)
            //return cached menu if it was cached today, get new data if it wasn't
            if (menu != null && menu.cacheDate == currentDate.toString()) {
                menuList = MenuTypeConverters().fromString(menu.menus)
                dataLoadedState.value = true
            } else {
                try {
                    menuList = getDiningMenuAsync(locationUrl)
                    menuDao.insertMenu(
                        Menu(
                            locationName,
                            MenuTypeConverters().fromList(menuList),
                            currentDate.toString()
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
                    TopBar(titleText = locationName, color = MaterialTheme.colorScheme.primary, navController = navController, showBottomSheet = showBottomSheet)
                },
                content = {padding ->
                    SwipableTabBar(menuArray = menuList, padding = padding)
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
                                val instant =
                                    datePickerState.selectedDateMillis?.let {
                                        Instant.ofEpochMilli(
                                            it
                                        )
                                    }
                                val date = dateFormat.format(LocalDateTime.ofInstant(instant, ZoneId.of("GMT")))
                                Log.d("DATE", "date picked: $date")
                                val dateUrl = date.toString().replace("-", "%2f")

                                Log.d("TAG", "date url: $dateUrl")

                                val titleDate = titleDateFormat.format(LocalDateTime.ofInstant(instant, ZoneId.of("GMT")))

                                val locationDateName = "${locationName.substringBefore(" ").replace("Cowell/Stevenson","Cowell/Stev")} $titleDate"
                                Log.d("TAG", "location date name: $locationDateName")
                                val encodedLocationName = URLEncoder.encode(locationDateName, "UTF-8")

                                Log.d("TAG","route: \"customdiningdate/$locationUrl/$dateUrl/$encodedLocationName\"")
                                navController.navigate("customdiningdate/$locationUrl/$dateUrl/$encodedLocationName")

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


            HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = rememberModalBottomSheetState(), locationName = locationName)


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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiningMenuCustomDate(navController: NavController, locationUrl: String, dateUrl: String, locationName: String) {

    val locationName = URLDecoder.decode(locationName, "UTF-8")
    val locationUrl = locationUrl.replace("/", "%2f")


    Log.d("TAG", "Manually choosing date! $locationUrl $dateUrl")
//    val nl = "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"

    val fullUrl = "$locationUrl&WeeksMenus=UCSC+-+This+Week's+Menus&myaction=read&dtdate=$dateUrl"

    Log.d("TAG","full url: $fullUrl")

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = DateTimeFormatter.ofPattern("M-dd-yyyy")
    val titleDateFormat = DateTimeFormatter.ofPattern("M/dd/yy")

    val dataLoadedState = remember { mutableStateOf(false) }
    var noInternet by remember { mutableStateOf(false) }

    val showBottomSheet = remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    var menuList: Array<MutableList<String>> by remember { mutableStateOf(arrayOf(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())) }

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            try {
                menuList = getDiningMenuAsync(fullUrl)
            } catch (e: UnresolvedAddressException) {
                noInternet = true
            }
            dataLoadedState.value = true
        }
    }
    if (noInternet) {
        ShortToast("No internet connection")
    }

    Column {
        if (dataLoadedState.value) {
            // If the data has been loaded from the internet, display the menu
            Scaffold(
                topBar = {
                    TopBar(titleText = locationName, color = MaterialTheme.colorScheme.primary, navController = navController, showBottomSheet = showBottomSheet)
                },
                content = {padding ->
                    SwipableTabBar(menuArray = menuList, padding = padding)
                },
                //floating action button, currently does nothing
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showDatePicker = !showDatePicker },
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
                                val instant =
                                    datePickerState.selectedDateMillis?.let {
                                        Instant.ofEpochMilli(
                                            it
                                        )
                                    }
                                val date = dateFormat.format(LocalDateTime.ofInstant(instant, ZoneId.of("GMT")))
                                val titleDate = titleDateFormat.format(LocalDateTime.ofInstant(instant, ZoneId.of("GMT")))
                                Log.d("DATE", "date picked: $titleDate")
                                val newDateUrl = date.toString().replace("-", "%2f")

                                Log.d("TAG", "date url: $dateUrl")

                                // this breaks the world
                                /*
                                DiningMenuCustomDate(
                                    navController = navController,
                                    locationUrl = locationUrl,
                                    dateUrl = dateUrl,
                                    locationName = "${locationName.substringBefore(" ")} ${date.toString()}",
                                )
                                 */

                                val locationDateName = "${locationName.substringBefore(" ").replace("Cowell/Stevenson","Cowell/Stev")} $titleDate"
                                Log.d("TAG", "location date name: $locationDateName")
                                val encodedLocationName = URLEncoder.encode(locationDateName, "UTF-8")
                                val strippedLocationUrl = locationUrl.substringBefore("&WeeksMenus=UCSC+-+This+Week's+Menus&myaction=read&dtdate=")

                                navController.navigateUp()
                                navController.navigate("customdiningdate/$strippedLocationUrl/$newDateUrl/$encodedLocationName")

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

    HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = rememberModalBottomSheetState(), locationName = locationName.substringBefore(" "))
}
