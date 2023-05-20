package com.pras.slugmenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/*
@Composable
fun HoursDialog(openDialog: MutableState<Boolean>) {

}

 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursBottomSheet(openBottomSheet: MutableState<Boolean>, bottomSheetState: SheetState, chosen: Int) {
    val scope = rememberCoroutineScope()

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

    val hoursArray = arrayOf(nineTenHours,cowellStevHours,crownMerrillHours,porterKresgeHours,perkHours,terraFrescaHours,porterMarketHours,stevCoffeeHours,globalVillageHours,oakesCafeHours)

    ModalBottomSheet(
        onDismissRequest = { openBottomSheet.value = false },
        sheetState = bottomSheetState,
    ) {
        ListItem (
            headlineContent = {
                Text(text = "Hours for Dining Hall", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
        )
        LazyColumn {
            items(hoursArray[chosen].size) { item ->
                val element = hoursArray[chosen][item]
                val isTitle = !element.contains(":")
                if (isTitle && item != 0) {
                    Divider()
                }
                ListItem(
                    headlineContent = {
                        Text(
                            text = element,
                            fontWeight = if (isTitle) { FontWeight.ExtraBold } else { FontWeight.Normal },
                            fontSize = if (isTitle) { 16.sp } else { 14.sp },
                            textAlign = if (isTitle) { TextAlign.Center } else { TextAlign.Left },
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