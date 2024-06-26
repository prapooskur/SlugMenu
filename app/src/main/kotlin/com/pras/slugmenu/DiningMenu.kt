package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pras.slugmenu.ui.elements.LongPressFloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "DiningMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiningMenu(navController: NavController, locationName: String, locationUrl: String) {

    val currentDate = LocalDate.now()

    val menuDatabase = MenuDatabase.getInstance(LocalContext.current)
    val menuDao = menuDatabase.menuDao()
    val favoritesDao = menuDatabase.favoritesDao()

    // Define a state to hold the retrieved Menu
    var menuList by remember { mutableStateOf<List<List<String>>>(listOf(listOf(), listOf())) }

    // Define a state indicating whether the data has been loaded from the cache
    val dataLoadedState = remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = DateTimeFormatter.ofPattern("M-dd-yyyy")
    val titleDateFormat = DateTimeFormatter.ofPattern("M/dd/yy")
    val encodedDate = currentDate.format(dateFormat).replace("-", "%2f")
    Log.d(TAG, "current date: $encodedDate")

    val showBottomSheet = remember { mutableStateOf(false) }

    val showWaitzDialog = remember { mutableStateOf(false) }

    val toastContext = LocalContext.current

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
                } catch (e: Exception) {
                    val exceptionFound = exceptionText(e)
                    withContext(Dispatchers.Main) {
                        shortToast(exceptionFound, toastContext)
                    }
                }
                dataLoadedState.value = true
            }
        }
    }

    Column {
        if (dataLoadedState.value) {
            // If the data has been loaded from the cache, display the menu
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),

                topBar = {
                    TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
                },
                content = {padding ->
                    SwipableTabBar(menuArray = menuList, favoritesDao = favoritesDao, padding = padding)
                },
                //floating action button
                // opens date picker on click, opens bottom sheet on long click
                floatingActionButton = {
                    LongPressFloatingActionButton(
                        onClick = { showDatePicker = !showDatePicker },
                        onLongClick = { showBottomSheet.value = !showBottomSheet.value },
                        modifier = Modifier.systemBarsPadding()
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

                                Log.d(TAG, "date url: $dateUrl")

                                val titleDate = titleDateFormat.format(LocalDateTime.ofInstant(instant, ZoneId.of("GMT")))

                                val locationDateName = "${locationName.substringBefore(" ").replace("Cowell/Stevenson","Cowell/Stev")} $titleDate"
                                Log.d(TAG, "location date name: $locationDateName")
                                val encodedLocationName = URLEncoder.encode(locationDateName, "UTF-8")

//                                Log.d(TAG,"route: \"customdiningdate/$locationUrl/$dateUrl/$encodedLocationName\"")
//                                navController.navigate("customdiningdate/$locationUrl/$dateUrl/$encodedLocationName")
                                val encodedLocation = CustomDiningDate(locationUrl, dateUrl, encodedLocationName)
                                Log.d(TAG,"route: $encodedLocation")
                                navController.navigate(encodedLocation)

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

            Column(modifier = Modifier.fillMaxHeight()) {
                HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = rememberModalBottomSheetState(), locationName = locationName.substringBefore(" "))
            }
            WaitzDialog(showDialog = showWaitzDialog, locationName = locationName.replace("Stevenson", "Stev"))


        } else {
            // Otherwise, display a loading indicator
            Surface {
                Column {
                    TopBarClean(titleText = locationName, navController = navController)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiningMenuCustomDate(navController: NavController, inputLocationUrl: String, dateUrl: String, inputLocationName: String) {

    val menuDatabase = MenuDatabase.getInstance(LocalContext.current)
    val favoritesDao = menuDatabase.favoritesDao()

    val locationName = URLDecoder.decode(inputLocationName, "UTF-8")
    val locationUrl = inputLocationUrl.replace("/", "%2f")


    Log.d(TAG, "Manually choosing date! $locationUrl $dateUrl")

    val fullUrl = "$locationUrl&WeeksMenus=UCSC+-+This+Week's+Menus&myaction=read&dtdate=$dateUrl"

    Log.d(TAG,"full url: $fullUrl")

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = DateTimeFormatter.ofPattern("M-dd-yyyy")
    val titleDateFormat = DateTimeFormatter.ofPattern("M/dd/yy")

    val dataLoadedState = remember { mutableStateOf(false) }

    val showBottomSheet = remember { mutableStateOf(false) }

    var menuList: List<List<String>> by remember { mutableStateOf(listOf(listOf(), listOf(), listOf(), listOf())) }

    val toastContext = LocalContext.current

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            try {
                menuList = getDiningMenuAsync(fullUrl)
            } catch (e: Exception) {
                val exceptionFound = exceptionText(e)
                withContext(Dispatchers.Main) {
                    shortToast(exceptionFound, toastContext)
                }
            }
            dataLoadedState.value = true
        }
    }

    Column {
        if (dataLoadedState.value) {
            // If the data has been loaded from the internet, display the menu
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    TopBarClean(titleText = locationName, navController = navController)
                },
                content = {padding ->
                    SwipableTabBar(menuArray = menuList, favoritesDao = favoritesDao, padding = padding)
                },
                //floating action button, currently shows date picker on short press and hours on long press

                floatingActionButton = {
                    LongPressFloatingActionButton(
                        onClick = { showDatePicker = !showDatePicker },
                        onLongClick = { showBottomSheet.value = !showBottomSheet.value },
                        modifier = Modifier.systemBarsPadding()
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

                                Log.d(TAG, "date url: $dateUrl")

                                val locationDateName = "${locationName.substringBefore(" ").replace("Cowell/Stevenson","Cowell/Stev")} $titleDate"
                                Log.d(TAG, "location date name: $locationDateName")
                                val encodedLocationName = URLEncoder.encode(locationDateName, "UTF-8")
                                val strippedLocationUrl = locationUrl.substringBefore("&WeeksMenus=UCSC+-+This+Week's+Menus&myaction=read&dtdate=")

                                navController.navigateUp()
//                                navController.navigate("customdiningdate/$strippedLocationUrl/$newDateUrl/$encodedLocationName")
                                val encodedLocation = CustomDiningDate(
                                    strippedLocationUrl,
                                    newDateUrl,
                                    encodedLocationName
                                )
                                Log.d(TAG, "route: $encodedLocation")
                                navController.navigate(encodedLocation)

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

            val bottomSheetState = rememberModalBottomSheetState()
            Column(modifier = Modifier.fillMaxHeight()) {
                HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = bottomSheetState, locationName = locationName.substringBefore(" "))
            }

        } else {
            // Otherwise, display a loading indicator
            Surface {
                Column {
                    TopBarClean(titleText = locationName, navController = navController)
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