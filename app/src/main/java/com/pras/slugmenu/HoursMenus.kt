package com.pras.slugmenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/*
@Composable
fun HoursDialog(openDialog: MutableState<Boolean>) {

}

 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitzDialog(showDialog: MutableState<Boolean>, waitzData: Array<MutableList<out MutableList<out Any>>>, locationName: String) {

    val locIndex: Int = when (locationName) {
        "Nine/Lewis" -> 0
        "Cowell/Stevenson" -> 1
        "Cowell/Stev" -> 1
        "Porter/Kresge" -> 2
        "Crown/Merrill" -> 3
        "Oakes Cafe" -> 4

        // if this was somehow run with a different name, default to nine/ten
        else -> 0
    }

    val locationData = waitzData[0]
    val compareData = waitzData[1]

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                showDialog.value = false
            },
            title = {
                Text(text = "Waitz Availability")
            },
            text = {
                Text(text = "Krill yourself")
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