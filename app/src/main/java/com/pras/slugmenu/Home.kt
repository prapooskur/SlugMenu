package com.pras.slugmenu

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    val useCollapsingTopBar = remember { mutableStateOf(false) }
    val locationOrder: List<LocationOrderItem>
    val mutableCleanLocationOrder: MutableList<LocationOrderItem> = mutableListOf()
    runBlocking {
        useGridLayout.value = preferencesDataStore.getListPreference.first()
        useCollapsingTopBar.value = preferencesDataStore.getToolbarPreference.first()
        locationOrder = Json.decodeFromString(preferencesDataStore.getLocationOrder.first())
    }

    for (location in locationOrder) {
        if (location.visible) {
            mutableCleanLocationOrder.add(location)
        }
    }
    val cleanLocationOrder = mutableCleanLocationOrder.toList()

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
                CollapsingLargeTopBar(titleText = "Slug Menu", navController = navController, scrollBehavior = scrollBehavior, isHome = true)
            } else {
                Surface(shadowElevation = 4.dp) {
                    TopBar(titleText = "Slug Menu", navController = navController, isHome = true)
                }
            }
        },
        content = {innerPadding ->
            if (useGridLayout.value) {
                TwoByTwoGrid(navController = navController, innerPadding = innerPadding, locationOrder = cleanLocationOrder)
            } else {
                CardList(navController = navController, innerPadding = innerPadding, locationOrder = cleanLocationOrder)
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
fun TwoByTwoGrid(navController: NavController, innerPadding: PaddingValues, locationOrder: List<LocationOrderItem>) {

    var clickable by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    Log.d(TAG, "location order: $locationOrder")

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // combine the padding values given by scaffold with the padding for bottom bar, so parts of
    // the grid aren't stuck behind a transparent bottom bar
    val paddingAmount = 10.dp
    val contentPadding = PaddingValues(start = paddingAmount, top = paddingAmount, end = paddingAmount, bottom = paddingAmount+navPadding)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
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

        /*
        // original, static implementation
        items(10) { index ->

            val location: String = if (index < 10) {
                locationnav[index]
            } else {
                "cowellstev"
            }
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
                        text = locations[index],
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp
                    )
                }
            }
        }

         */
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardList(navController: NavController, innerPadding: PaddingValues, locationOrder: List<LocationOrderItem>) {

    var clickable by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
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

    /*
    // original, static implementation
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(locationnav.size) { index ->

            val location: String = if (index < 10) {
                locationnav[index]
            } else {
                "cowellstev"
            }

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
                        text = locations[index],
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
     */
}
