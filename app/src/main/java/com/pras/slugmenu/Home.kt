package com.pras.slugmenu

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, preferencesDataStore: PreferencesDatastore) {
    val useGridLayout = remember { mutableStateOf(true) }
    val useCollapsingTopBar = remember { mutableStateOf(false) }
    runBlocking {
        val gridLayoutChoice = preferencesDataStore.getListPreference.first()
        useGridLayout.value = gridLayoutChoice
        val collapsingTopBarChoice = preferencesDataStore.getToolbarPreference.first()
        useCollapsingTopBar.value = collapsingTopBarChoice
    }

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
                TwoByTwoGrid(navController = navController, innerPadding = innerPadding)
            } else {
                CardList(navController = navController, innerPadding = innerPadding)
            }
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoByTwoGrid(navController: NavController, innerPadding: PaddingValues) {
    val locationnav = arrayOf("ninelewis","cowellstev","crownmerrill","porterkresge","perkcoffee","terrafresca","portermarket","stevcoffee","globalvillage","oakescafe")
    val locations = arrayOf("Nine\nLewis","Cowell\nStevenson","Crown\nMerrill","Porter\nKresge","Perks","Terra Fresca","Porter Market", "Stevenson Coffee House", "Global Village Cafe", "Oakes Cafe")

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // combine the padding values given by scaffold with the padding for bottom bar, so parts of
    // the grid aren't stuck behind a transparent bottom bar
    val contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp+navPadding)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding)
    ) {
        items(10) { index ->

            val location: String = if (index < 10) {
                locationnav[index]
            } else {
                "cowellstev"
            }
            Card(
                onClick = { navController.navigate(location) },
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardList(navController: NavController, innerPadding: PaddingValues) {
    val locationnav = arrayOf("ninelewis","cowellstev","crownmerrill","porterkresge","perkcoffee","terrafresca","portermarket","stevcoffee","globalvillage","oakescafe")
    val locations = arrayOf("Nine\nLewis","Cowell\nStevenson","Crown\nMerrill","Porter\nKresge","Perks","Terra Fresca","Porter Market", "Stevenson Coffee House", "Global Village Cafe", "Oakes Cafe")

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
                onClick = { navController.navigate(location) },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .aspectRatio(3f),
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
}
