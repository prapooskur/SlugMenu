package com.pras.slugmenu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    //Collapsing toolbar rewrite later?

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                TopBarHome(titleText = "Slug Menu", navController)
            }
        },
        content = {innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding) // padding applied here
            ) {
//                CardList(navController = navController)
                TwoByTwoGrid(navController = navController)
            }
        }
    )
}

// this is how tonal elevation is done, hardcode it in for primarycolor somehow?
/*
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return surfaceTint.copy(alpha = alpha).compositeOver(surface)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHome(titleText: String, navController: NavController, color: Color = MaterialTheme.colorScheme.primary) {
    TopAppBar(
        title = {
            Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        actions = {
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary
                )

            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = color),
        /*
        backgroundColor = color,
        elevation = 8.dp

         */
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoByTwoGrid(navController: NavController) {
    val locations = arrayOf("Nine/Lewis","Cowell/Stevenson","Crown/Merrill","Porter/Kresge","Perks","Terra Fresca","Porter Market", "Stevenson Coffee House", "Global Village Cafe", "Oakes Cafe")
    val locationnav = arrayOf("ninelewis","cowellstev","crownmerrill","porterkresge","perkcoffee","terrafresca","portermarket","stevcoffee","globalvillage","oakescafe")
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = locations[index], color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            /*
            Button (
                onClick =  { navController.navigate(location) },
                modifier = Modifier.aspectRatio(1.5f),
//                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Yellow)

            ) {
                Text(text = locations[index], color = Color.Black)
            }
             */


            /*
            Box(
                Modifier
                    .background(Color.Yellow, RoundedCornerShape(8.dp))
                    .aspectRatio(1f)
            ) {
                Text(
                    text = locations[index],
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentSize(align = Alignment.Center),
                    color = Color.Black
                )
            }

             */
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardList(navController: NavController) {
    val locations = arrayOf("Nine/Lewis","Cowell/Stevenson","Crown/Merrill","Porter/Kresge","Perks","Terra Fresca","Porter Market", "Stevenson Coffee House", "Global Village Cafe", "Oakes Cafe")
    val locationnav = arrayOf("ninelewis","cowellstev","crownmerrill","porterkresge","perkcoffee","terrafresca","portermarket","stevcoffee","globalvillage","oakescafe")
    LazyColumn(
        /*
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
         */
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(locationnav.size) { index ->

            val location: String = if (index < 4) {
                locationnav[index]
            } else {
                "cowellstev"
            }

            Card(
                onClick = { navController.navigate(location) },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .aspectRatio(4f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = locations[index], color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
