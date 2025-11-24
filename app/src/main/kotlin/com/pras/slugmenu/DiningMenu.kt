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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pras.slugmenu.ui.elements.HoursBottomSheet
import com.pras.slugmenu.ui.elements.LongPressFloatingActionButton
import com.pras.slugmenu.ui.elements.SwipableTabBar
import com.pras.slugmenu.ui.elements.TopBarClean
import com.pras.slugmenu.ui.elements.shortToast
import com.pras.slugmenu.ui.viewmodels.MenuViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "DiningMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiningMenu(
    navController: NavController,
    locationName: String,
    locationUrl: String,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory)
) {

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val dateFormat = DateTimeFormatter.ofPattern("M-dd-yyyy")
    val titleDateFormat = DateTimeFormatter.ofPattern("M/dd/yy")

    val showBottomSheet = rememberSaveable { mutableStateOf(false) }
//    val showWaitzDialog = rememberSaveable { mutableStateOf(false) }

    val toastContext = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchMenu(locationName, locationUrl)
//        viewModel.fetchWaitz()
        viewModel.fetchHours(locationName)
    }

    LaunchedEffect(key1 = uiState.value.error) {
        if (uiState.value.error.isNotBlank()) {
            shortToast(uiState.value.error, toastContext)
            viewModel.clearError()
        }
    }

    Column {
        if (!uiState.value.menuLoading) {
            // If the data has been loaded from the cache, display the menu
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),

                topBar = {
                    TopBarClean(
                        titleText = locationName,
                        onBack = { navController.navigateUp() },
                    )
                },
                content = { padding ->
                    SwipableTabBar(
                        menuArray = uiState.value.menus,
                        onFavorite = {
                            viewModel.insertFavorite(it)
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onFullCollapse = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        padding = padding
                    )
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
                HoursBottomSheet(
                    openBottomSheet = showBottomSheet,
                    bottomSheetState = rememberModalBottomSheetState(),
                    locationName = locationName.substringBefore(" "),
                    hoursLoading = uiState.value.hoursLoading,
                    hoursException = uiState.value.error.isNotEmpty(),
                    locationHours = uiState.value.hours
                )
            }
//            WaitzDialog(
//                showDialog = showWaitzDialog,
//                locationName = locationName.replace("Stevenson", "Stev"),
//                waitzLoading = uiState.value.waitzLoading,
//                waitzException = uiState.value.error.isNotEmpty(),
//                waitzData = uiState.value.waitz
//            )


        } else {
            // Otherwise, display a loading indicator
            Surface {
                Column {
                    TopBarClean(titleText = locationName, onBack = { navController.navigateUp() })
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(strokeCap = StrokeCap.Round)
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiningMenuCustomDate(navController: NavController, inputLocationUrl: String, dateUrl: String, inputLocationName: String, viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory)) {

    val locationName = URLDecoder.decode(inputLocationName, "UTF-8")
    val locationUrl = inputLocationUrl.replace("/", "%2f")


    Log.d(TAG, "Manually choosing date! $locationUrl $dateUrl")

    val fullUrl = "$locationUrl&WeeksMenus=UCSC+-+This+Week's+Menus&myaction=read&dtdate=$dateUrl"

    Log.d(TAG,"full url: $fullUrl")

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val dateFormat = DateTimeFormatter.ofPattern("M-dd-yyyy")
    val titleDateFormat = DateTimeFormatter.ofPattern("M/dd/yy")

    val showBottomSheet = rememberSaveable { mutableStateOf(false) }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val toastContext = LocalContext.current
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchMenu(
            locationName = locationName,
            locationUrl = locationUrl,
            checkCache = false
        )
    }

    LaunchedEffect(key1 = uiState.value.error) {
        if (uiState.value.error.isNotBlank()) {
            shortToast(uiState.value.error, toastContext)
            viewModel.clearError()
        }
    }

    Column {
       if (!uiState.value.menuLoading) {
            // If the data has been loaded from the internet, display the menu
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    TopBarClean(titleText = locationName, onBack = { navController.navigateUp() })
                },
                content = {padding ->
                    SwipableTabBar(
                        menuArray = uiState.value.menus,
                        onFavorite = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.insertFavorite(it)
                        },
                        onFullCollapse = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        padding = padding
                    )
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

            Column(modifier = Modifier.fillMaxHeight()) {
                HoursBottomSheet(
                    openBottomSheet = showBottomSheet,
                    bottomSheetState = rememberModalBottomSheetState(),
                    locationName = locationName.substringBefore(" "),
                    hoursLoading = uiState.value.hoursLoading,
                    hoursException = uiState.value.error.isNotEmpty(),
                    locationHours = uiState.value.hours
                )
            }

        } else {
            // Otherwise, display a loading indicator
            Surface {
                Column {
                    TopBarClean(titleText = locationName, onBack = { navController.navigateUp() })
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(strokeCap = StrokeCap.Round)
            }
        }
    }

}