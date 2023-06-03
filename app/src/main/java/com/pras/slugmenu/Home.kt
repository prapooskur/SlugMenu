package com.pras.slugmenu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    runBlocking {
        val gridLayoutChoice = preferencesDataStore.getListPreference.first()
        useGridLayout.value = gridLayoutChoice
    }
    //Collapsing toolbar rewrite later?
    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Slug Menu",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 20.sp
                        )
                    },

                    actions = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                        }
                    },

                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                )

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

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
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
