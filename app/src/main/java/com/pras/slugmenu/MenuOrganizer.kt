package com.pras.slugmenu

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/*
@Serializable
data class LocationOrderList(val locationOrder: List<LocationOrderItem>)
 */

private const val TAG = "MenuOrganizer"

@Serializable
data class LocationOrderItem(val navLocation: String, val locationName: String, var visible: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuOrganizer(navController: NavController, preferencesDataStore: PreferencesDatastore) {

    var locationOrder: List<LocationOrderItem>
    val useCollapsingTopBar = remember { mutableStateOf(false) }
    runBlocking {
        locationOrder = Json.decodeFromString(preferencesDataStore.getLocationOrder.first())
        useCollapsingTopBar.value = preferencesDataStore.getToolbarPreference.first()
    }

    val resetPressed = remember { mutableStateOf(false) }

    // make it so that you can't accidentally press items while navigating out of settings
    val clickable = remember { mutableStateOf(true) }
    TouchBlocker(navController = navController, delay = FADETIME.toLong(), clickable = clickable)

    Log.d(TAG,"location order: $locationOrder")

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })

    val scaffoldModifier = if (useCollapsingTopBar.value) {
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        Modifier.fillMaxSize()
    }

    Scaffold(
        modifier = scaffoldModifier,
        // custom insets necessary to render behind nav bar
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            if (useCollapsingTopBar.value) {
                CollapsingLargeTopBar(titleText = "Organize Menu Items", navController = navController, scrollBehavior = scrollBehavior, isOrganizer = true, resetPressed = resetPressed, isClickable = clickable, delay = FADETIME.toLong())
            } else {
                Surface(shadowElevation = 4.dp) {
                    TopBar(titleText = "Organize Menu Items", navController = navController, isOrganizer = true, resetPressed = resetPressed, isClickable = clickable, delay = FADETIME.toLong())
                }
            }
        },
        content = { paddingValues ->
            ReorderableLocationList(locationOrderInput = locationOrder, preferencesDataStore = preferencesDataStore, paddingValues = paddingValues, resetPressed = resetPressed)
        }
    )
}

@Composable
fun ReorderableLocationList(locationOrderInput: List<LocationOrderItem>, preferencesDataStore: PreferencesDatastore, paddingValues: PaddingValues, resetPressed: MutableState<Boolean> = mutableStateOf(false)) {

    val locationOrderState = remember { mutableStateOf(locationOrderInput) }
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    Log.d(TAG,"$paddingValues")

    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            locationOrderState.value = locationOrderState.value.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            coroutineScope.launch {
                preferencesDataStore.setLocationOrder(Json.encodeToString(locationOrderState.value.toList()))
            }

        }
    )

    Column(modifier = Modifier.padding(paddingValues)) {
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {

            // todo: find a workaround for the first item not animating bug
            items(locationOrderState.value, {it.navLocation}) { item ->
                var isVisible by remember { mutableStateOf(item.visible) }
                ReorderableItem(state, key = item.navLocation) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                    if (isDragging) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    Column(
                        modifier = Modifier
//                            .shadow(elevation.value)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(elevation.value))
                    ) {
                        if (resetPressed.value) {
                            isVisible = true
                            item.visible = true
                            Log.d(TAG,"reset button pressed, reset ${item.locationName} checkbox status")
                        }
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Menu, contentDescription = "Handle")
                            },
                            headlineContent = {
                                Text(item.locationName)
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = isVisible,
                                    onCheckedChange = null
                                )
                            },
                            modifier = Modifier.clickable {
                                isVisible = !isVisible
                                item.visible = isVisible
                                coroutineScope.launch {
                                    preferencesDataStore.setLocationOrder(Json.encodeToString(locationOrderState.value))
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation.value))
                        )

                    }
                }
            }
        }
    }
    if (resetPressed.value) {
        LaunchedEffect(Unit) {
            // default values
            locationOrderState.value = listOf(
                LocationOrderItem(
                    navLocation = "ninelewis",
                    locationName = "Nine / Lewis",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "cowellstev",
                    locationName = "Cowell / Stevenson",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "crownmerrill",
                    locationName = "Crown / Merrill",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "porterkresge",
                    locationName = "Porter / Kresge",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "perkcoffee",
                    locationName = "Perk Coffee Bars",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "terrafresca",
                    locationName = "Terra Fresca",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "portermarket",
                    locationName = "Porter Market",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "stevcoffee",
                    locationName = "Stevenson Coffee House",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "globalvillage",
                    locationName = "Global Village Cafe",
                    visible = true
                ),
                LocationOrderItem(
                    navLocation = "oakescafe",
                    locationName = "Oakes Cafe",
                    visible = true
                )
            )
            preferencesDataStore.setLocationOrder(Json.encodeToString(locationOrderState.value.toList()))
            resetPressed.value = false
            Log.d(TAG, "reset pressed, set preferences to default and set resetpressed to false.")
        }
    }
}