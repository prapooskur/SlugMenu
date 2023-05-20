package com.pras.slugmenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch

@Composable
fun HoursDialog() {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursBottomSheet(openBottomSheet: MutableState<Boolean>, bottomSheetState: SheetState, chosen: Int) {
    val scope = rememberCoroutineScope()

    val nineTenHours =  arrayOf(
        "Monday - Friday",
        arrayOf("Breakfast: 7-11 AM","Continuous Dining: 11–11:30 AM","Lunch: 11:30 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM","Late Night: 8–11 PM"),
        "Saturday/Sunday",
        arrayOf("Breakfast: 7–10 AM","Brunch: 10 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM","Late Night: 8–11 PM")
    )
    val cowellStevHours = arrayOf(
        "Monday - Thursday",
        arrayOf("Breakfast: 7-11 AM","Continuous Dining: 11–11:30 AM","Lunch: 11:30 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM","Late Night: 8–11 PM"),
        "Friday",
        arrayOf("Breakfast: 7-11 AM","Continuous Dining: 11–11:30 AM","Lunch: 11:30 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM"),
        "Saturday",
        arrayOf("Breakfast: 7–10 AM","Brunch: 10 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM"),
        "Sunday",
        arrayOf("Breakfast: 7–10 AM","Brunch: 10 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM","Late Night: 8–11 PM"))
    val crownMerrillHours = arrayOf(
        "Monday - Friday",
        arrayOf("Breakfast: 7-11 AM","Continuous Dining: 11–11:30 AM","Lunch: 11:30 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–8 PM")
    )
    val porterKresgeHours = arrayOf(
        "Monday - Thursday",
        arrayOf("Breakfast: 7-11 AM","Continuous Dining: 11–11:30AM","Lunch: 11:30AM–2PM","Continuous Dining: 2–5PM","Dinner: 5–8PM","Late Night: 8–11PM"),
        "Friday",
        arrayOf("Breakfast: 7-11 AM","Continuous Dining: 11–11:30AM","Lunch: 11:30AM–2PM","Continuous Dining: 2–5PM","Dinner: 5–7PM"),
        "Saturday",
        arrayOf("Breakfast: 7–10 AM","Brunch: 10 AM–2 PM","Continuous Dining: 2–5 PM","Dinner: 5–7 PM"),
        "Sunday",
        arrayOf("Breakfast: 7–10AM","Brunch: 10AM–2PM","Continuous Dining: 2–5PM","Dinner: 5–8PM","Late Night: 8–11 PM")
    )

    val perkHours = arrayOf(
        "Monday - Friday",
        arrayOf("Open: 8 AM - 5 PM")
    )
    val terraFrescaHours = arrayOf(
        "Monday - Thursday",
        arrayOf("Open: 8 AM - 5 PM"),
        "Friday",
        arrayOf("Open: 8 AM - 4 PM")
    )
    val porterMarketHours = arrayOf(
        "Monday - Friday",
        arrayOf("Open: 8 AM - 6:30 PM"),
        "Saturday",
        arrayOf("Open: 10 AM - 5 PM"),

        )
    val stevCoffeeHours = arrayOf(
        "Monday - Friday",
        arrayOf("Open: 8 AM - 5 PM"),
    )
    val globalVillageHours = arrayOf(
        "Monday - Thursday",
        arrayOf("Open: 8 AM - 8:30 PM"),
        "Friday",
        arrayOf("Open: 8 AM - 5 PM")
    )

    val oakesCafeHours = arrayOf(
        "Monday - Thursday",
        arrayOf("Open: 8 AM - 8 PM"),
        "Friday",
        arrayOf("Open: 9 AM - 7 PM")
    )

    val hoursArray = arrayOf(nineTenHours,cowellStevHours,crownMerrillHours,porterKresgeHours,perkHours,terraFrescaHours,porterMarketHours,stevCoffeeHours,globalVillageHours,oakesCafeHours)

    ModalBottomSheet(
        onDismissRequest = { openBottomSheet.value = false },
        sheetState = bottomSheetState,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                // Note: If you provide logic outside of onDismissRequest to remove the sheet,
                // you must additionally handle intended state cleanup, if any.
                onClick = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            openBottomSheet.value = false
                        }
                    }
                }
            ) {
                Text("Hide Bottom Sheet")
            }
        }
        val choice = hoursArray[chosen]
        LazyColumn {
            items(hoursArray[chosen].size) { item ->
                if (choice[item] is Array<*>) {
                    choice.forEachIndexed { index, element ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = element.toString(),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        )
                    }
                } else {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = choice[item].toString(),
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                }
            }
        }
    }
}