package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "Home"

data class SelectedItem(val id: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, preferencesDataStore: PreferencesDatastore) {
    val useGridLayout = preferencesDataStore.getListPreference.collectAsStateWithLifecycle(initialValue = true)
    val useCollapsingTopBar = preferencesDataStore.getToolbarPreference.collectAsStateWithLifecycle(initialValue = true)
    var locationOrder by remember { mutableStateOf(runBlocking {Json.decodeFromString<List<LocationOrderItem>>(preferencesDataStore.getLocationOrder.first())}) }

    // add rachel carson, hide global village
    LaunchedEffect(locationOrder) {
        if (!locationOrder.contains(
                LocationOrderItem(
                    navLocation = "carsonoakes",
                    locationName = "Carson/Oakes",
                    visible = true
                )
            ) && !locationOrder.contains(
                LocationOrderItem(
                    navLocation = "carsonoakes",
                    locationName = "Carson/Oakes",
                    visible = false
                )
            )
        ) {
            for (i in locationOrder) {
                if (i.locationName == "Global Village Cafe") {
                    i.visible = false
                }
            }
            locationOrder = locationOrder.toMutableList().apply {
                add(
                    4,
                    LocationOrderItem(
                        navLocation = "carsonoakes",
                        locationName = "Carson/Oakes",
                        visible = true
                    )
                )
            }
            withContext(Dispatchers.IO) {
                preferencesDataStore.setLocationOrder(Json.encodeToString(locationOrder))
            }
        }
    }

    val visibleLocationOrder = locationOrder.filter { it.visible }

    val iconMap = mapOf(
        //since the global village menu no longer exists, i've mapped the RCC icon to it for now until I can get a proper one.
        "ninelewis"     to R.drawable.ninelewis,
        "cowellstev"    to R.drawable.cowellstevenson,
        "crownmerrill"  to R.drawable.crownmerrill,
        "porterkresge"  to R.drawable.porterkresge,
        "carsonoakes"   to R.drawable.globalvillagecafe,
        "perkcoffee"    to R.drawable.perkcoffeebars,
        "terrafresca"   to R.drawable.terrafresca,
        "portermarket"  to R.drawable.portermarket,
        "stevcoffee"    to R.drawable.stevensoncoffeehouse,
        "globalvillage" to R.drawable.globalvillagecafe,
        "oakescafe"     to R.drawable.oakescafe,
        "settings"      to R.drawable.settings
    )

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
                CollapsingLargeTopBar(
                    titleText = "Slug Menu",
                    navController = navController,
                    scrollBehavior = scrollBehavior,
                    isHome = true
                )
            } else {
                Surface(shadowElevation = 4.dp) {
                    TopBar(
                        titleText = "Slug Menu",
                        navController = navController,
                        isHome = true
                    )
                }
            }
        },
        content = { innerPadding ->
            if (useGridLayout.value) {
                TwoByTwoGridWithIcons(
                    navController = navController,
                    innerPadding = innerPadding,
                    locationOrder = visibleLocationOrder,
                    iconMap = iconMap
                )
            } else {
                CardListWithIcons(
                    navController = navController,
                    innerPadding = innerPadding,
                    locationOrder = visibleLocationOrder,
                    iconMap = iconMap
                )
            }
        }
    )
}

@Composable
fun TwoByTwoGridWithIcons(navController: NavController, innerPadding: PaddingValues, locationOrder: List<LocationOrderItem>, iconMap: Map<String, Int>) {

    var clickable by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    Log.d(TAG, "location order: $locationOrder")

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // combine the padding values given by scaffold with the padding for bottom bar, so parts of
    // the grid aren't stuck behind the bottom bar
    val paddingAmount = 10.dp
    val contentPadding = PaddingValues(start = paddingAmount, top = paddingAmount, end = paddingAmount, bottom = paddingAmount+navPadding)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding)
    ) {

        items(locationOrder.size) { index ->
            val location: String = locationOrder[index].navLocation
            val name: String = locationOrder[index].locationName
                .replace("/", "\n")
//                .replace("Perks", "Perk Coffee Bars")

            // try to get icon from dictionary, default to nine/lewis icon if it isn't listed for some reason to avoid crash
            // in practice, this should never need to fall back to the default.
            val icon = iconMap.getOrDefault(location, R.drawable.ninelewis)

            Card(
                onClick = {
                    if (clickable) {
                        clickable = false
                        navController.navigate(location)
                        coroutineScope.launch {
                            // delay time set in mainactivity.kt
                            delay(DELAYTIME.toLong())
                            clickable = true
                        }
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .aspectRatio(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // without this, the nine/lewis icon looks larger than the other icons
                    val imageModifier = if (icon == R.drawable.ninelewis) {
                        Modifier
                            .weight(1.8f)
                            .aspectRatio(1f)
                            .padding(12.dp)
                    } else {
                        Modifier
                            .weight(1.8f)
                            .aspectRatio(1f)
                    }

                    // no content description provided - image is purely decorative
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                        alignment = Alignment.Center,
                        modifier = imageModifier
                    )

                    Text(
                        text = name,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp,
                        // without this, long text might get too close to the sides
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CardListWithIcons(navController: NavController, innerPadding: PaddingValues, locationOrder: List<LocationOrderItem>, iconMap: Map<String, Int>) {

    var clickable by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // combine the padding values given by scaffold with the padding for bottom bar, so parts of
    // the grid aren't stuck behind the bottom bar
    val paddingAmount = 10.dp
    val contentPadding = PaddingValues(start = paddingAmount, top = paddingAmount, end = paddingAmount, bottom = paddingAmount+navPadding)


    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(locationOrder.size) { index ->
            val location: String = locationOrder[index].navLocation
            val name: String = locationOrder[index].locationName.replace("Perks", "Perk Coffee Bars")

            // try to get icon from dictionary, default to nine/lewis icon if it isn't listed for some reason to avoid crash
            // in practice, this should never need to fall back to the default.
            val icon = iconMap.getOrDefault(location, R.drawable.ninelewis)

            Card(
                onClick = {
                    if (clickable) {
                        clickable = false
                        navController.navigate(location)
                        coroutineScope.launch {
                            // tween time set in mainactivity.kt
                            delay(350)
                            clickable = true
                        }
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    // todo get this to work properly and not cut off width
//                    .heightIn(max = 120.dp)
                    .aspectRatio(4f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    // without this, the nine/lewis icon looks larger than the others despite being the same weight
                    // extra padding instead of lower weight prevents it from taking up less space than the other icons
                    val imageModifier = if (icon == R.drawable.ninelewis) {
                        Modifier
                            .aspectRatio(1f)
                            .weight(0.4f)
                            .padding(22.dp)
                    } else {
                        Modifier
                            .aspectRatio(1f)
                            .weight(0.4f)
                            .padding(14.dp)
                    }

                    // a little start padding helps text not be too close to the icon
                    val textModifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)

                    // no content description provided - image is purely decorative
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                        alignment = Alignment.Center,
                        modifier = imageModifier
                    )

                    Text(
                        text = name,
                        textAlign = TextAlign.Left,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp,
                        modifier = textModifier
                    )
                }
            }
        }
    }
}

@Composable
fun AdaptiveCardList(clickAction: (SelectedItem) -> Unit, selectedItem: String, innerPadding: PaddingValues, inputLocationOrder: List<LocationOrderItem>, iconMap: Map<String, Int>) {

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // combine the padding values given by scaffold with the padding for bottom bar, so parts of
    // the grid aren't stuck behind the bottom bar
    val paddingAmount = 10.dp
    val contentPadding = PaddingValues(start = paddingAmount, top = paddingAmount, end = paddingAmount, bottom = paddingAmount+navPadding)

    val locationOrder = inputLocationOrder.plus(LocationOrderItem("settings", "Settings", true))

    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(locationOrder.size) { index ->
            val location: String = locationOrder[index].navLocation
            val name: String = locationOrder[index].locationName.replace("Perks", "Perk Coffee Bars")
            val selected = selectedItem == locationOrder[index].navLocation

            // try to get icon from dictionary, default to nine/lewis icon if it isn't listed for some reason to avoid crash
            // in practice, this should never need to fall back to the default.
            val icon = iconMap.getOrDefault(location, R.drawable.ninelewis)

            Card(
                onClick = {
                    clickAction(SelectedItem(location))
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    // todo get this to work properly and not cut off width
//                    .heightIn(max = 120.dp)
                    .aspectRatio(4f)
                    .fillMaxWidth(),
                colors = if (selected) {
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(30.dp)
                    )
                } else {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                }

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    // without this, the nine/lewis icon looks larger than the others despite being the same weight
                    // extra padding instead of lower weight prevents it from taking up less space than the other icons
                    val imageModifier = if (icon == R.drawable.ninelewis) {
                        Modifier
                            .aspectRatio(1f)
                            .weight(0.4f)
                            .padding(22.dp)
                    } else {
                        Modifier
                            .aspectRatio(1f)
                            .weight(0.4f)
                            .padding(14.dp)
                    }

                    // a little start padding helps text not be too close to the icon
                    val textModifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)

                    // no content description provided - image is purely decorative
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                        alignment = Alignment.Center,
                        modifier = imageModifier
                    )

                    Text(
                        text = name,
                        textAlign = TextAlign.Left,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp,
                        modifier = textModifier
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun GridPreview() {
    val navController = rememberNavController()
    val innerPadding = PaddingValues()

    val locationOrder = listOf(
        LocationOrderItem(navLocation = "ninelewis", locationName = "Nine / Lewis", visible = true),
        LocationOrderItem(navLocation = "cowellstev", locationName = "Cowell/Stevenson", visible = true),
        LocationOrderItem(navLocation = "crownmerrill", locationName = "Crown/Merrill", visible = true),
        LocationOrderItem(navLocation = "porterkresge", locationName = "Porter/Kresge", visible = true),
        LocationOrderItem(navLocation = "perkcoffee", locationName = "Perk Coffee Bars", visible = true),
        LocationOrderItem(navLocation = "terrafresca", locationName = "Terra Fresca", visible = true),
        LocationOrderItem(navLocation = "portermarket", locationName = "Porter Market", visible = true),
        LocationOrderItem(navLocation = "stevcoffee", locationName = "Stevenson Coffee House", visible = true),
        LocationOrderItem(navLocation = "globalvillage", locationName = "Global Village Cafe", visible = true),
        LocationOrderItem(navLocation = "oakescafe", locationName = "Oakes Cafe", visible = true)
    )
    val iconMap = mapOf(
        "ninelewis"     to R.drawable.ninelewis,
        "cowellstev"    to R.drawable.cowellstevenson,
        "crownmerrill"  to R.drawable.crownmerrill,
        "porterkresge"  to R.drawable.porterkresge,
        "perkcoffee"    to R.drawable.perkcoffeebars,
        "terrafresca"   to R.drawable.terrafresca,
        "portermarket"  to R.drawable.portermarket,
        "stevcoffee"    to R.drawable.stevensoncoffeehouse,
        "globalvillage" to R.drawable.globalvillagecafe,
        "oakescafe"     to R.drawable.oakescafe
    )

    TwoByTwoGridWithIcons(navController = navController, innerPadding = innerPadding, locationOrder = locationOrder, iconMap = iconMap)
    //CardListWithIcons(navController = navController, innerPadding = innerPadding, locationOrder = locationOrder, iconMap = iconMap)
}

@Preview
@Composable
fun ListPreview() {
    val navController = rememberNavController()
    val innerPadding = PaddingValues()

    val locationOrder = listOf(
        LocationOrderItem(navLocation = "ninelewis", locationName = "Nine / Lewis", visible = true),
        LocationOrderItem(navLocation = "cowellstev", locationName = "Cowell/Stevenson", visible = true),
        LocationOrderItem(navLocation = "crownmerrill", locationName = "Crown/Merrill", visible = true),
        LocationOrderItem(navLocation = "porterkresge", locationName = "Porter/Kresge", visible = true),
        LocationOrderItem(navLocation = "perkcoffee", locationName = "Perk Coffee Bars", visible = true),
        LocationOrderItem(navLocation = "terrafresca", locationName = "Terra Fresca", visible = true),
        LocationOrderItem(navLocation = "portermarket", locationName = "Porter Market", visible = true),
        LocationOrderItem(navLocation = "stevcoffee", locationName = "Stevenson Coffee House", visible = true),
        LocationOrderItem(navLocation = "globalvillage", locationName = "Global Village Cafe", visible = true),
        LocationOrderItem(navLocation = "oakescafe", locationName = "Oakes Cafe", visible = true)
    )
    val iconMap = mapOf(
        "ninelewis"     to R.drawable.ninelewis,
        "cowellstev"    to R.drawable.cowellstevenson,
        "crownmerrill"  to R.drawable.crownmerrill,
        "porterkresge"  to R.drawable.porterkresge,
        "perkcoffee"    to R.drawable.perkcoffeebars,
        "terrafresca"   to R.drawable.terrafresca,
        "portermarket"  to R.drawable.portermarket,
        "stevcoffee"    to R.drawable.stevensoncoffeehouse,
        "globalvillage" to R.drawable.globalvillagecafe,
        "oakescafe"     to R.drawable.oakescafe
    )

    CardListWithIcons(navController = navController, innerPadding = innerPadding, locationOrder = locationOrder, iconMap = iconMap)
}