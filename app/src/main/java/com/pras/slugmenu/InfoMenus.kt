package com.pras.slugmenu

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertificateException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import javax.net.ssl.SSLHandshakeException

fun ShortToast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

@Composable
fun WaitzDialog(showDialog: MutableState<Boolean>, locationName: String, menuDatabase: MenuDatabase) {

    val locIndex = if (locationName == "Cowell/Stev") { "Cowell/Stevenson" } else { locationName }

    val dataLoadedState = remember { mutableStateOf(false) }

    val waitzDao = menuDatabase.waitzDao()
    var waitzData by remember { mutableStateOf<List<Map<String,List<String>>>>(listOf(mapOf(),mapOf())) }
    var exceptionFound by remember { mutableStateOf("No Exception") }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val currentTime = LocalDateTime.now().format(dateFormatter).toString()

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val cachedWaitzData = waitzDao.getData(currentTime)
            if (cachedWaitzData != null && cachedWaitzData.cacheTime == currentTime) {
                waitzData = listOf(WaitzTypeConverters().fromWaitzString(cachedWaitzData.live),WaitzTypeConverters().fromWaitzString(cachedWaitzData.compare))
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
        ShortToast(exceptionFound, LocalContext.current)
    } else if (showDialog.value && !dataLoadedState.value) {
        showDialog.value = false
        ShortToast(text = "Data not loaded yet", LocalContext.current)
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
                //todo: figure out emojicompat
                if (locationData.isNullOrEmpty() || compareData.isNullOrEmpty() || (locationData.size < 4) || (compareData.size < 4) || (locationData[3] == "false")) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursBottomSheet(openBottomSheet: MutableState<Boolean>, bottomSheetState: SheetState, locationName: String) {

    val closedHours = listOf(
        "Closed"
    )

    // in summer dining halls all use same hours, so unified them here
    // todo: update with accurate hours? atm this is just a best guess
    val summerDiningHours = listOf(
        "Monday-Sunday",
        "Breakfast: 7-9 AM\nContinuous Dining: 9–11:30 AM\nLunch: 11:30 AM–1:30 PM\nContinuous Dining: 1:30–5 PM\nDinner: 5–7 PM"
    )

    val nineTenHours = listOf(
        "Monday - Friday",
        "Breakfast: 7-11 AM\nContinuous Dining: 11–11:30 AM\nLunch: 11:30 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM\nLate Night: 8–11 PM",
        "Saturday/Sunday",
        "Breakfast: 7–10 AM\nBrunch: 10 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM\nLate Night: 8–11 PM"
    )
    val nineTenSummerHours = summerDiningHours

    val cowellStevHours = listOf(
        "Monday - Thursday",
        "Breakfast: 7-11 AM\nContinuous Dining: 11–11:30 AM\nLunch: 11:30 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM\nLate Night: 8–11 PM",
        "Friday",
        "Breakfast: 7-11 AM\nContinuous Dining: 11–11:30 AM\nLunch: 11:30 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM",
        "Saturday",
        "Breakfast: 7–10 AM\nBrunch: 10 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM",
        "Sunday",
        "Breakfast: 7–10 AM\nBrunch: 10 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM\nLate Night: 8–11 PM"
    )
    // is cowell closed? The menu site doesn't show a menu this summer, but they were open last summer
    // crown was closed last summer, so maybe only 3 dhs are open per summer?
    val cowellStevSummerHours = closedHours

    val crownMerrillHours = listOf(
        "Monday - Friday",
        "Breakfast: 7-11 AM\nContinuous Dining: 11–11:30 AM\nLunch: 11:30 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM"
    )
    val crownMerrillSummerHours = summerDiningHours

    val porterKresgeHours = listOf(
        "Monday - Thursday",
        "Breakfast: 7-11 AM\nContinuous Dining: 11–11:30AM\nLunch: 11:30AM–2PM\nContinuous Dining: 2–5PM\nDinner: 5–8PM\nLate Night: 8–11PM",
        "Friday",
        "Breakfast: 7-11 AM\nContinuous Dining: 11–11:30AM\nLunch: 11:30AM–2PM\nContinuous Dining: 2–5PM\nDinner: 5–7PM",
        "Saturday",
        "Breakfast: 7–10 AM\nBrunch: 10 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–7 PM",
        "Sunday",
        "Breakfast: 7–10AM\nBrunch: 10AM–2PM\nContinuous Dining: 2–5PM\nDinner: 5–8PM\nLate Night: 8–11 PM"
    )
    val porterKresgeSummerHours = summerDiningHours


    val perkHours = listOf(
        "Monday - Friday",
        "Open: 8 AM - 5 PM"
    )
    val perkSummerHours = listOf(
        "Monday - Friday",
        "Open: 8 AM - 2:30 PM"
    )


    val terraFrescaHours = listOf(
        "Monday - Thursday",
        "Open: 8 AM - 5 PM",
        "Friday",
        "Open: 8 AM - 4 PM"
    )
    val terraFrescaSummerHours = closedHours

    val porterMarketHours = listOf(
        "Monday - Friday",
        "Open: 8 AM - 6:30 PM",
        "Saturday",
        "Open: 10 AM - 5 PM",
    )
    val porterMarketSummerHours = closedHours

    val stevCoffeeHours = listOf(
        "Monday - Friday",
        "Open: 8 AM - 5 PM",
    )
    val stevCoffeeSummerHours = closedHours

    val globalVillageHours = listOf(
        "Monday - Thursday",
        "Open: 8 AM - 8:30 PM",
        "Friday",
        "Open: 8 AM - 5 PM"
    )
    val globalVillageSummerHours = listOf(
        "Monday - Friday",
        "Open: 8 AM - 2:30 PM"
    )


    val oakesCafeHours = listOf(
        "Monday - Thursday",
        "Open: 8 AM - 8 PM",
        "Friday",
        "Open: 9 AM - 7 PM"
    )
    val oakesCafeSummerHours = closedHours

    val currentDate = MonthDay.from(LocalDate.now())
    val summerStartDate = MonthDay.of(6,25)
    val summerEndDate = MonthDay.of(9,2)

    val isSummer = (currentDate.isAfter(summerStartDate) && currentDate.isBefore(summerEndDate))

    val hoursDictionary: Map<String, List<String>> = if (!isSummer)
        {
            mapOf(
                "Nine/Lewis"        to nineTenHours,
                "Cowell/Stevenson"  to cowellStevHours,
                "Cowell/Stev"       to cowellStevHours,
                "Crown/Merrill"     to crownMerrillHours,
                "Porter/Kresge"     to porterKresgeHours,
                "Perk Coffee Bars"  to perkHours,
                "Terra Fresca"      to terraFrescaHours,
                "Porter Market"     to porterMarketHours,
                "Stevenson Coffee House" to stevCoffeeHours,
                "Global Village Cafe" to globalVillageHours,
                "Oakes Cafe"        to oakesCafeHours
            )
        } else {
            mapOf(
                "Nine/Lewis"        to nineTenSummerHours,
                "Cowell/Stevenson"  to cowellStevSummerHours,
                "Cowell/Stev"       to cowellStevSummerHours,
                "Crown/Merrill"     to crownMerrillSummerHours,
                "Porter/Kresge"     to porterKresgeSummerHours,
                "Perk Coffee Bars"  to perkSummerHours,
                "Terra Fresca"      to terraFrescaSummerHours,
                "Porter Market"     to porterMarketSummerHours,
                "Stevenson Coffee House" to stevCoffeeSummerHours,
                "Global Village Cafe" to globalVillageSummerHours,
                "Oakes Cafe"        to oakesCafeSummerHours
            )
        }

    if (openBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = bottomSheetState,
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = if (!isSummer) { "Hours for $locationName" } else { "Summer Hours for $locationName" },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            )

            Divider(
                thickness = 2.dp
            )

            LazyColumn {
                items(hoursDictionary.getOrDefault(locationName, listOf()).size) { item ->
                    val element = hoursDictionary.getOrDefault(locationName, listOf())[item]
                    val maxElement = hoursDictionary.getOrDefault(locationName, listOf()).size
                    val isClosed = element == "Closed"
                    val isTitle = !(element.contains(":") || isClosed)
                    if (isTitle && item != 0) {
                        Divider()
                    }
                    ListItem(
                        headlineContent = {
                            Text(
                                text = element,
                                fontWeight = if (isTitle || isClosed) {
                                    FontWeight.ExtraBold
                                } else {
                                    FontWeight.Normal
                                },
                                /*
                                fontSize = if (isTitle) {
                                    16.sp
                                } else {
                                    14.sp
                                },

                                 */
                                textAlign = if (isTitle) {
                                    TextAlign.Center
                                } else {
                                    TextAlign.Left
                                },
                                lineHeight = 30.sp
                            )
                        }
                    )
                    if (isTitle) {
                        Divider()
                    }

                    // add a bit of bottom padding to dining hall hours sheets
                    if (maxElement-1 == item && locationName.contains("/")) {
                        Text("")
                    }
                }
            }
        }
    }
}