package com.pras.slugmenu

import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertificateException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.SSLHandshakeException

@Composable
fun ShortToast(text: String) {
    Toast.makeText(LocalContext.current, text, Toast.LENGTH_SHORT).show()
}

@Composable
fun WaitzDialog(showDialog: MutableState<Boolean>, locationName: String, menuDatabase: MenuDatabase) {

    val locIndex = if (locationName == "Cowell/Stev") { "Cowell/Stevenson" } else { locationName }

    val dataLoadedState = remember { mutableStateOf(false) }

    val waitzDao = menuDatabase.waitzDao()
    var waitzData by remember { mutableStateOf<Array<MutableMap<String,MutableList<String>>>>(arrayOf(mutableMapOf(),mutableMapOf())) }
    var exceptionFound by remember { mutableStateOf("No Exception") }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val currentTime = LocalDateTime.now().format(dateFormatter).toString()

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val cachedWaitzData = waitzDao.getData(currentTime)
            if (cachedWaitzData != null && cachedWaitzData.cacheTime == currentTime) {
                waitzData = arrayOf(WaitzTypeConverters().fromWaitzString(cachedWaitzData.live),WaitzTypeConverters().fromWaitzString(cachedWaitzData.compare))
                dataLoadedState.value = true
            } else {
                try {
                    waitzDao.dropWaitz()
                    waitzData = getWaitzDataAsync()
                    waitzDao.insertWaitz(
                        Waitz (
                            currentTime,
                            WaitzTypeConverters().fromWaitzList(waitzData[0]),
                            WaitzTypeConverters().fromWaitzList(waitzData[1])
                        )
                    )
                //TODO: unify these into one catch block?
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

    if (showDialog.value && exceptionFound != "No Exception") {
        showDialog.value = false
        ShortToast(text = exceptionFound)
    } else if (showDialog.value && !dataLoadedState.value) {
        showDialog.value = false
        ShortToast(text = "Data not loaded yet")
    } else if (showDialog.value && dataLoadedState.value) {
        val locationData = waitzData[0][locIndex]
        val compareData = waitzData[1][locIndex]

        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                showDialog.value = false
            },
            title = {
                if (locationData.isNullOrEmpty() || compareData.isNullOrEmpty() || locationData.size < 4 || compareData.size < 4 || locationData[3] == "false") {
                    Text(text = "⚫ Waitz: $locationName")
                } else if (locationData[0].toInt() <= 45){
                    Text(text = "🟢 Waitz: $locationName")
                } else if (locationData[0].toInt() <= 80){
                    Text(text = "🟡 Waitz: $locationName")
                } else {
                    Text(text = "🔴 Waitz: $locationName")
                }
            },
            text = {
                if (locationData.isNullOrEmpty() || compareData.isNullOrEmpty() || locationData.size < 4 || compareData.size < 4 || locationData[3] == "false") {
                    Text(
                        text = "No data available.",
                        fontSize = 16.sp
                    )
                } else {
                    if (compareData[3] == "only one location") {
                        Text(
                            text = "Busyness: ${locationData[0]}%\n" +
                                    "People: ${locationData[1]}/${locationData[2]}\n" +
                                    "Next hour: ${compareData[0].substring(18)}\n" +
                                    "Today: ${compareData[1].substring(9)}\n" +
                                    "Peak hours: ${compareData[2].substring(15)}",
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = "Busyness: ${locationData[0]}%\n" +
                                    "People: ${locationData[1]}/${locationData[2]}\n" +
                                    "Next hour: ${compareData[0].substring(18)}\n" +
                                    "Today: ${compareData[1].substring(9)}\n" +
                                    "Peak hours: ${compareData[2].substring(15)}\n" +
                                    "Best location: ${compareData[3].substringBefore(" is best right now")}",
                            fontSize = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                    }
                ) {
                    Text("Close")
                }

            }
        )


    }
}

@Composable
fun WaitzDialogRewrite(showDialog: MutableState<Boolean>, locationName: String, menuDatabase: MenuDatabase) {

    val locIndex = if (locationName == "Cowell/Stev") { "Cowell/Stevenson" } else { locationName }

    val dataLoadedState = remember { mutableStateOf(false) }

    val waitzDao = menuDatabase.waitzDao()
    var waitzData by remember { mutableStateOf<Array<MutableMap<String,MutableList<String>>>>(arrayOf(mutableMapOf(),mutableMapOf())) }
    var exceptionFound by remember { mutableStateOf("No Exception") }

    if (showDialog.value) {
        LaunchedEffect(Unit) {
            dataLoadedState.value = false
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val currentTime = LocalDateTime.now().format(dateFormatter).toString()
            // Launch a coroutine to retrieve the menu from the database
            withContext(Dispatchers.IO) {
                val cachedWaitzData = waitzDao.getData(currentTime)
                if (cachedWaitzData != null && cachedWaitzData.cacheTime == currentTime) {
                    waitzData = arrayOf(WaitzTypeConverters().fromWaitzString(cachedWaitzData.live),WaitzTypeConverters().fromWaitzString(cachedWaitzData.compare))
                    dataLoadedState.value = true
                } else {
                    try {
                        waitzDao.dropWaitz()
                        waitzData = getWaitzDataAsync()
                        waitzDao.insertWaitz(
                            Waitz (
                                currentTime,
                                WaitzTypeConverters().fromWaitzList(waitzData[0]),
                                WaitzTypeConverters().fromWaitzList(waitzData[1])
                            )
                        )
                        //TODO: unify these into one catch block?
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

        val locationData = waitzData[0][locIndex]
        val compareData = waitzData[1][locIndex]

        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                showDialog.value = false
            },
            title = {
                if (!dataLoadedState.value || locationData.isNullOrEmpty() || compareData.isNullOrEmpty() || locationData.size < 4 || compareData.size < 4 || locationData[3] == "false") {
                    Text(text = "⚫ Waitz: $locationName")
                } else if (locationData[0].toInt() <= 45) {
                    Text(text = "🟢 Waitz: $locationName")
                } else if (locationData[0].toInt() <= 80) {
                    Text(text = "🟡 Waitz: $locationName")
                } else {
                    Text(text = "🔴 Waitz: $locationName")
                }
            },
            text = {
                if (!dataLoadedState.value) {
                    CircularProgressIndicator()
                } else if (exceptionFound != "No Exception") {
                    Text(
                        text = exceptionFound,
                        fontSize = 16.sp
                    )
                } else if (locationData.isNullOrEmpty() || compareData.isNullOrEmpty() || locationData.size < 4 || compareData.size < 4 || locationData[3] == "false") {
                    Text(
                        text = "No data available.",
                        fontSize = 16.sp
                    )
                } else {
                    if (compareData[3] == "only one location") {
                        Text(
                            text = "Busyness: ${locationData[0]}%\n" +
                                    "People: ${locationData[1]}/${locationData[2]}\n" +
                                    "Next hour: ${compareData[0].substring(18)}\n" +
                                    "Today: ${compareData[1].substring(9)}\n" +
                                    "Peak hours: ${compareData[2].substring(15)}",
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = "Busyness: ${locationData[0]}%\n" +
                                    "People: ${locationData[1]}/${locationData[2]}\n" +
                                    "Next hour: ${compareData[0].substring(18)}\n" +
                                    "Today: ${compareData[1].substring(9)}\n" +
                                    "Peak hours: ${compareData[2].substring(15)}\n" +
                                    "Best location: ${compareData[3].substringBefore(" is best right now")}",
                            fontSize = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                    }
                ) {
                    Text("Close")
                }

            }
        )


    }
}

// TODO: rework so it closes on back press
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursBottomSheet(openBottomSheet: MutableState<Boolean>, bottomSheetState: SheetState, locationName: String) {
    val nineTenHours =  arrayOf(
        "Monday - Friday",
        "Breakfast: 7-11 AM","Continuous Dining: 11–11:30 AM","Lunch: 11:30 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM","Late Night: 8–11 PM",
        "Saturday/Sunday",
        "Breakfast: 7–10 AM","Brunch: 10 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM","Late Night: 8–11 PM"
    )
    val cowellStevHours = arrayOf(
        "Monday - Thursday",
        "Breakfast: 7-11 AM","Continuous Dining: 11–11:30 AM","Lunch: 11:30 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM","Late Night: 8–11 PM",
        "Friday",
        "Breakfast: 7-11 AM","Continuous Dining: 11–11:30 AM","Lunch: 11:30 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM",
        "Saturday",
        "Breakfast: 7–10 AM","Brunch: 10 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM",
        "Sunday",
        "Breakfast: 7–10 AM","Brunch: 10 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM","Late Night: 8–11 PM"
    )
    val crownMerrillHours = arrayOf(
        "Monday - Friday",
        "Breakfast: 7-11 AM","Continuous Dining: 11–11:30 AM","Lunch: 11:30 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM"
    )
    val porterKresgeHours = arrayOf(
        "Monday - Thursday",
        "Breakfast: 7-11 AM","Continuous Dining: 11–11:30AM","Lunch: 11:30AM–2PM","Continuous Dining: 2–5PM","Dinner: 5–8PM","Late Night: 8–11PM",
        "Friday",
        "Breakfast: 7-11 AM","Continuous Dining: 11–11:30AM","Lunch: 11:30AM–2PM","Continuous Dining: 2–5PM","Dinner: 5–7PM",
        "Saturday",
        "Breakfast: 7–10 AM","Brunch: 10 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–7 PM",
        "Sunday",
        "Breakfast: 7–10AM","Brunch: 10AM–2PM","Continuous Dining: 2–5PM","Dinner: 5–8PM","Late Night: 8–11 PM"
    )

    val perkHours = arrayOf(
        "Monday - Friday",
        "Open: 8 AM - 5 PM"
    )
    val terraFrescaHours = arrayOf(
        "Monday - Thursday",
        "Open: 8 AM - 5 PM",
        "Friday",
        "Open: 8 AM - 4 PM"
    )
    val porterMarketHours = arrayOf(
        "Monday - Friday",
        "Open: 8 AM - 6:30 PM",
        "Saturday",
        "Open: 10 AM - 5 PM",
    )
    val stevCoffeeHours = arrayOf(
        "Monday - Friday",
        "Open: 8 AM - 5 PM",
    )
    val globalVillageHours = arrayOf(
        "Monday - Thursday",
        "Open: 8 AM - 8:30 PM",
        "Friday",
        "Open: 8 AM - 5 PM"
    )

    val oakesCafeHours = arrayOf(
        "Monday - Thursday",
        "Open: 8 AM - 8 PM",
        "Friday",
        "Open: 9 AM - 7 PM"
    )

    val hoursDictionary = mapOf(
        "Nine/Lewis" to nineTenHours,
        "Cowell/Stevenson" to cowellStevHours,
        "Cowell/Stev" to cowellStevHours,
        "Crown/Merrill" to crownMerrillHours,
        "Porter/Kresge" to porterKresgeHours,
        "Perk Coffee Bars" to perkHours,
        "Terra Fresca" to terraFrescaHours,
        "Porter Market" to porterMarketHours,
        "Stevenson Coffee House" to stevCoffeeHours,
        "Global Village Cafe" to globalVillageHours,
        "Oakes Cafe" to oakesCafeHours
    )

    if (openBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = bottomSheetState,
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = "Hours for $locationName",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            )
            LazyColumn {
                items(hoursDictionary.getOrDefault(locationName, arrayOf()).size) { item ->
                    val element = hoursDictionary.getOrDefault(locationName, arrayOf())[item]
                    val isTitle = !element.contains(":")
                    if (isTitle && item != 0) {
                        Divider()
                    }
                    ListItem(
                        headlineContent = {
                            Text(
                                text = element,
                                fontWeight = if (isTitle) {
                                    FontWeight.ExtraBold
                                } else {
                                    FontWeight.Normal
                                },
                                fontSize = if (isTitle) {
                                    16.sp
                                } else {
                                    14.sp
                                },
                                textAlign = if (isTitle) {
                                    TextAlign.Center
                                } else {
                                    TextAlign.Left
                                },
                            )
                        }
                    )
                    if (isTitle) {
                        Divider()
                    }
                }
            }
        }
    }
}