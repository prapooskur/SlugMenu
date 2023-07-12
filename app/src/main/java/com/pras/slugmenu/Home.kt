package com.pras.slugmenu

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

private const val TAG = "Home"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, preferencesDataStore: PreferencesDatastore) {
    val useGridLayout = remember { mutableStateOf(true) }
    val useCollapsingTopBar = remember { mutableStateOf(true) }
    val locationOrder: List<LocationOrderItem>

    runBlocking {
        useGridLayout.value = preferencesDataStore.getListPreference.first()
        useCollapsingTopBar.value = preferencesDataStore.getToolbarPreference.first()
        locationOrder = Json.decodeFromString(preferencesDataStore.getLocationOrder.first())
    }

    val visibleLocationOrder = locationOrder.filter { it.visible }

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
                CollapsingLargeTopBar(titleText = "Slug Menu", navController = navController, scrollBehavior = scrollBehavior, isHome = true)
            } else {
                Surface(shadowElevation = 4.dp) {
                    TopBar(titleText = "Slug Menu", navController = navController, isHome = true)
                }
            }
        },
        content = {innerPadding ->
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

    val context = LocalContext.current
    var pressedTime: Long = 0
    BackHandler {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            // if time is greater than 2 sec we are closing the application.
            (context as? Activity)?.finish()
            Log.d(TAG, "back pressed twice, exiting")
        } else {
            // in else condition displaying a toast message.
            ShortToast("Press back again to exit", context)
            Log.d(TAG, "back pressed, toast shown")
        }
        // on below line initializing our press time variable
        pressedTime = System.currentTimeMillis()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
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
                            // tween time set in mainactivity.kt
                            delay(TWEENTIME.toLong())
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

                    Image(
                        painter = painterResource(icon),
                        contentDescription = "Location Icon",
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

@OptIn(ExperimentalMaterial3Api::class)
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

                    Image(
                        painter = painterResource(icon),
                        contentDescription = "Location Icon",
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

// old, iconless home screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoByTwoGrid(navController: NavController, innerPadding: PaddingValues, locationOrder: List<LocationOrderItem>) {

    var clickable by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    Log.d(TAG, "location order: $locationOrder")

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // combine the padding values given by scaffold with the padding for bottom bar, so parts of
    // the grid aren't stuck behind the bottom bar
    val paddingAmount = 10.dp
    val contentPadding = PaddingValues(start = paddingAmount, top = paddingAmount, end = paddingAmount, bottom = paddingAmount+navPadding)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding)
    ) {

        items(locationOrder.size) { index ->
            val location: String = locationOrder[index].navLocation
            val name: String = locationOrder[index].locationName.replace("/","\n")
            Card(
                onClick = {
                    if (clickable) {
                        clickable = false
                        navController.navigate(location)
                        coroutineScope.launch {
                            // tween time set in mainactivity.kt
                            delay(TWEENTIME.toLong())
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardList(navController: NavController, innerPadding: PaddingValues, locationOrder: List<LocationOrderItem>) {

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(locationOrder.size) { index ->
            val location: String = locationOrder[index].navLocation
            val name: String = locationOrder[index].locationName
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
                    .aspectRatio(4f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}