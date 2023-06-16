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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

    Log.d(TAG,"location order: $locationOrder")

    //TODO: Complete collapsing top bar rewrite
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
                CollapsingLargeTopBar(titleText = "Organize Menu Items", navController = navController, scrollBehavior = scrollBehavior, isOrganizer = true)
            } else {
                Surface(shadowElevation = 4.dp) {
                    TopBar(titleText = "Organize Menu Items", navController = navController, isOrganizer = true)
                }
            }
        },
        content = { paddingValues ->
            ReorderableLocationList(locationOrderInput = locationOrder, preferencesDataStore = preferencesDataStore, paddingValues = paddingValues)
//            TestReorderableList(paddingValues = paddingValues)
        }
    )
}

@Composable
fun ReorderableLocationList(locationOrderInput: List<LocationOrderItem>, preferencesDataStore: PreferencesDatastore, paddingValues: PaddingValues) {

    val locationOrderState = remember { mutableStateOf(locationOrderInput) }
    val coroutineScope = rememberCoroutineScope()

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
            items(locationOrderState.value.size, {locationOrderState.value[it].navLocation}) { item ->
                Log.d(TAG, "item key: ${locationOrderState.value[item].navLocation}")
                var isVisible by remember { mutableStateOf(locationOrderState.value[item].visible) }
                ReorderableItem(state, key = item) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 46.dp else 0.dp)
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Menu, contentDescription = "Handle")
                            },
                            headlineContent = {
                                Text(locationOrderState.value[item].locationName)
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = isVisible,
                                    onCheckedChange = {
                                        isVisible = !isVisible
                                        locationOrderState.value[item].visible = isVisible
                                        coroutineScope.launch {
                                            preferencesDataStore.setLocationOrder(Json.encodeToString(locationOrderState.value))
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                isVisible = !isVisible
                                locationOrderState.value[item].visible = isVisible
                                coroutineScope.launch {
                                    preferencesDataStore.setLocationOrder(Json.encodeToString(locationOrderState.value))
                                }
                            }
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun TestReorderableList(paddingValues: PaddingValues) {
    // working test implementation
    val data = remember { mutableStateOf(List(100) { "Item $it" }) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data.value = data.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    })


    Column(modifier = Modifier.padding(paddingValues)) {
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            items(data.value.size, { data.value[it] }) { item ->
                ReorderableItem(state, key = item) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 46.dp else 0.dp)
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        ListItem(
                            headlineContent = { Text(data.value[item]) }
                        )
                    }
                }
            }
        }
    }
}