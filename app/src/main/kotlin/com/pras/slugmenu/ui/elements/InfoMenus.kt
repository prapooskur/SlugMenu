package com.pras.slugmenu.ui.elements

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pras.slugmenu.data.sources.AllHoursList
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException

private const val TAG = "InfoMenus"

fun shortToast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun exceptionText(e: Exception): String {
    return when (e) {
        is UnresolvedAddressException -> "No Internet connection"
        is SocketTimeoutException -> "Connection timed out"
        is UnknownHostException -> "Failed to resolve URL"
        is CertificateException -> "Website's SSL certificate is invalid"
        is CertPathValidatorException -> "Website's SSL certificate is invalid"
        is SSLHandshakeException -> "SSL handshake failed"
        else -> "Exception: $e"
    }
}

@Composable
fun WaitzDialog(
    showDialog: MutableState<Boolean>,
    locationName: String,
    waitzLoading: Boolean,
    waitzException: Boolean,
    waitzData: List<Map<String, List<String>>>
) {
    //val locIndex = if (locationName == "Cowell/Stev") { "Cowell/Stevenson" } else { locationName }

    val locIndex = when (locationName) {
        "Cowell/Stev" -> "Cowell/Stevenson"
        /*
        "Cowell/Stev" -> "Cowell/Stevenson"
        "Cowell/Stevenson" -> "Cowell/Stevenson College"
        "Crown/Merrill" -> "Crown/Merrill College"
        "Porter/Kresge" -> "Porter/Kresge College"
        "Global Village Cafe" -> "McHenry Library - Global Village Cafe"
         */
        else -> locationName
    }

//    LaunchedEffect(showDialog.value) { Log.d(TAG, "dialog launched for $locationName ${showDialog.value}") }

    if (showDialog.value && waitzException) {
        showDialog.value = false
        Log.d(TAG, "waitz error")
    } else if (showDialog.value && waitzData.size == 2) {
        val locationData = waitzData[0][locIndex] ?: waitzData[0]["$locIndex College"]
        val compareData = waitzData[1][locIndex]

        if (waitzLoading) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                },
                title = {
                    Text(text = "⚫ Waitz: $locationName")
                },
                text = {
                    CircularProgressIndicator(strokeCap = StrokeCap.Round)
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
                        Log.d(TAG, "waitz error: $locationData, $compareData")
                    } else {
                        if (compareData[3] == "only one location" || compareData[3].startsWith(locationName)) {
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
                                        "Best location: ${compareData[3].substring(0,compareData[3].length-18)}",
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
    } else if (showDialog.value) {
        Log.d(TAG, "??? ${waitzData}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursBottomSheet(
    openBottomSheet: MutableState<Boolean>,
    bottomSheetState: SheetState,
    locationName: String,
    hoursLoading: Boolean,
    hoursException: Boolean,
    allHoursList: AllHoursList
) {

    if (openBottomSheet.value && hoursException) {
        shortToast("Failed to get updated hours, falling back to hardcoded data", LocalContext.current)
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

            HorizontalDivider(
                thickness = 2.dp
            )

            if (!hoursLoading) {
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
                                        HorizontalDivider()
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
                                        HorizontalDivider()
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
                            "Carson/Oakes" -> allHoursList.carsonoakes
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
                                HorizontalDivider()
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
                                HorizontalDivider()
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
                        }
                    }
                    /*item {
                        // add some padding to the bottom
                        Text("")
                    }*/
                }
            } else {
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
}
