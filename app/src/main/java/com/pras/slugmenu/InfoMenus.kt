package com.pras.slugmenu

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertificateException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import javax.net.ssl.SSLHandshakeException

private const val TAG = "InfoMenus"

fun ShortToast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

@Composable
fun WaitzDialog(showDialog: MutableState<Boolean>, locationName: String) {

    val locIndex = if (locationName == "Cowell/Stev") { "Cowell/Stevenson" } else { locationName }

    val dataLoadedState = remember { mutableStateOf(false) }

    val menuDatabase = MenuDatabase.getInstance(LocalContext.current)
    val waitzDao = menuDatabase.waitzDao()
    var waitzData by remember { mutableStateOf<List<Map<String,List<String>>>>(listOf(mapOf(),mapOf())) }
    var exceptionFound by remember { mutableStateOf("No Exception") }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val currentTime = LocalDateTime.now().format(dateFormatter).toString()

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val cachedWaitzData = waitzDao.getData("dh-oakes")
            if (cachedWaitzData != null && cachedWaitzData.cacheTime == currentTime) {
                waitzData = listOf(WaitzTypeConverters().fromWaitzString(cachedWaitzData.live),WaitzTypeConverters().fromWaitzString(cachedWaitzData.compare))
                dataLoadedState.value = true
            } else {
                try {
                    waitzData = getWaitzDataAsync()
                    waitzDao.insertWaitz(
                        Waitz (
                            "dh-oakes",
                            currentTime,
                            WaitzTypeConverters().fromWaitzList(waitzData[0]),
                            WaitzTypeConverters().fromWaitzList(waitzData[1])
                        )
                    )
                } catch (e: Exception) {
                    exceptionFound = when (e) {
                        is UnresolvedAddressException -> "No Internet connection"
                        is SocketTimeoutException -> "Connection timed out"
                        is UnknownHostException -> "Failed to resolve URL"
                        is CertificateException -> "Website's SSL certificate is invalid"
                        is SSLHandshakeException -> "SSL handshake failed"
                        else -> "Exception: $e"
                    }
                }
                dataLoadedState.value = true
            }

        }
    }

    if (showDialog.value && exceptionFound != "No Exception") {
        showDialog.value = false
        ShortToast(exceptionFound, LocalContext.current)
        Log.d(TAG, exceptionFound)
    } else if (showDialog.value) {
        val locationData = waitzData[0][locIndex]
        val compareData = waitzData[1][locIndex]

        if (!dataLoadedState.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                },
                title = {
                    Text(text = "⚫ Waitz: $locationName")
                },
                text = {
                    CircularProgressIndicator()
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
        } else {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                },
                title = {
                    if (locationData.isNullOrEmpty() || compareData.isNullOrEmpty() || (locationData.size < 4) || (compareData.size < 4) || (locationData[3] == "false")) {
                        Text(text = "⚫ Waitz: $locationName")
                    } else {
                        when {
                            locationData[0].toInt() <= 45 -> Text(text = "🟢 Waitz: $locationName")
                            locationData[0].toInt() <= 80 -> Text(text = "🟡 Waitz: $locationName")
                            else -> Text(text = "🔴 Waitz: $locationName")
                        }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursBottomSheet(openBottomSheet: MutableState<Boolean>, bottomSheetState: SheetState, locationName: String) {
    val menuDatabase = MenuDatabase.getInstance(LocalContext.current
    )
    val hoursDao = menuDatabase.hoursDao()
    val currentDate = LocalDate.now()

    val dataLoadedState = remember { mutableStateOf(false) }
    var allHoursList by remember { mutableStateOf(AllHoursList()) }
    var exceptionFound by remember { mutableStateOf("No Exception") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val cachedHoursData = hoursDao.getHours("tesr")
            if (cachedHoursData != null && Period.between(LocalDate.parse(cachedHoursData.cacheDate), currentDate).days < 7) {
                allHoursList = HoursTypeConverters().fromHoursString(cachedHoursData.hours)
                dataLoadedState.value = true
            } else {
                try {
                    allHoursList = getHoursData()
                    hoursDao.insertHours(
                        Hours(
                            "tesr",
                            HoursTypeConverters().fromHoursList(allHoursList),
                            currentDate.toString()
                        )
                    )
                } catch (e: Exception) {
                    exceptionFound = when (e) {
                        is UnresolvedAddressException -> "No Internet connection"
                        is SocketTimeoutException -> "Connection timed out"
                        is UnknownHostException -> "Failed to resolve URL"
                        is CertificateException -> "Website's SSL certificate is invalid"
                        is SSLHandshakeException -> "SSL handshake failed"
                        else -> "Exception: $e"
                    }
                }
                dataLoadedState.value = true
            }
        }
    }

    if (openBottomSheet.value && exceptionFound != "No Exception") {
//        openBottomSheet.value = false
        ShortToast("Failed to get hours, falling back to cached data", LocalContext.current)
        Log.d(TAG, exceptionFound)
    }

    if (openBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                openBottomSheet.value = false
            },
            sheetState = bottomSheetState,
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = "Hours for $locationName",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }
            )

            Divider(
                thickness = 2.dp
            )

            if (dataLoadedState.value) {
                LazyColumn {
                    // custom handling for perks, since three separate locations exist
                    if (locationName == "Perk Coffee Bars") {
                        val titlesList = listOf("Perk Baskin Engineering", "Perk Physical Sciences", "Perk Earth and Marine Sciences")
                        val locationHoursList = listOf(allHoursList.perkbe,allHoursList.perkpsb,allHoursList.perkems)
                        if (locationHoursList.all { it.daysList.isEmpty() }) {
                            item {
                                ListItem (
                                    headlineContent = {
                                        Text(
                                            text = "No data available",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                )
                            }
                        } else {
                            for (i in locationHoursList.indices) {
                                val locationTitle = titlesList[i]
                                val daysList = locationHoursList[i].daysList
                                val combinedDays = daysList.joinToString("\n")
                                if (daysList.isNotEmpty()) {
                                    item {
                                        Divider()
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    text = locationTitle,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 17.sp,
                                                    lineHeight = 30.sp
                                                )
                                            }
                                        )
                                        Divider()
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    text = combinedDays,
                                                    fontWeight = FontWeight.Normal,
                                                    lineHeight = 30.sp
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val locationHoursList = when (locationName) {
                            "Nine/Lewis" -> allHoursList.ninelewis
                            "Cowell/Stevenson" -> allHoursList.cowellstev
                            "Cowell/Stev" -> allHoursList.cowellstev
                            "Crown/Merrill" -> allHoursList.crownmerrill
                            "Porter/Kresge" -> allHoursList.porterkresge
                            "Terra Fresca" -> allHoursList.terrafresca
                            "Porter Market" -> allHoursList.portermarket
                            "Stevenson Coffee House" -> allHoursList.stevcoffee
                            "Global Village Cafe" -> allHoursList.globalvillage
                            "Oakes Cafe" -> allHoursList.oakescafe
                            // fallback to nine/lewis hours if it's an unexpected location
                            else -> allHoursList.ninelewis
                        }

                        val daysList = locationHoursList.daysList
                        val hoursList = locationHoursList.hoursList

                        if (daysList.isEmpty() && hoursList.isEmpty()) {
                            item {
                                ListItem (
                                    headlineContent = {
                                        Text(
                                            text = "No data available",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                )
                            }
                        } else if (hoursList.isNotEmpty()) {
                            // Dining menu
                            items(daysList.size) { item ->
                                val days = daysList[item]
                                val hours = hoursList[item]
                                Divider()
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = days,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            lineHeight = 30.sp
                                        )
                                    }
                                )
                                Divider()
                                val combinedHours = hours.joinToString("\n")
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = combinedHours,
                                            fontWeight = FontWeight.Normal,
                                            lineHeight = 30.sp
                                        )
                                    }
                                )
                            }
                        } else {
                            val combinedHours = daysList.joinToString("\n")
                            // Non dining menu
                            item {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = combinedHours,
                                            lineHeight = 30.sp
                                        )
                                    }
                                )
                            }
                            /*
                            items(daysList.size) { item ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = daysList[item],
                                            lineHeight = 30.sp
                                        )
                                    }
                                )
                            }

                             */
                        }
                    }
                    item {
                        // add some padding to the bottom
                        Text("")
                    }
                }
            } else {
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
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursBottomSheet(openBottomSheet: MutableState<Boolean>, bottomSheetState: SheetState, locationName: String) {

    val closedHours = listOf(
        "Closed"
    )

    // in summer dining halls all use same hours, so unified them here
    val summerDiningHours = listOf(
        "Monday-Sunday",
        "Breakfast: 7-9 AM\nContinuous Dining: 9–11:30 AM\nLunch: 11:30 AM–1:30 PM\nContinuous Dining: 1:30–5 PM\nDinner: 5–7 PM"
    )

    val nineLewisHours = listOf(
        "Monday - Friday",
        "Breakfast: 7-11 AM\nContinuous Dining: 11–11:30 AM\nLunch: 11:30 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM\nLate Night: 8–11 PM",
        "Saturday/Sunday",
        "Breakfast: 7–10 AM\nBrunch: 10 AM–2 PM\nContinuous Dining: 2–5 PM\nDinner: 5–8 PM\nLate Night: 8–11 PM"
    )
    val nineLewisSummerHours = summerDiningHours

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
                "Nine/Lewis"        to nineLewisHours,
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
                "Nine/Lewis"        to nineLewisSummerHours,
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
            onDismissRequest = {
                openBottomSheet.value = false
            },
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
*/
