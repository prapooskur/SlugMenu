package com.example.slugmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    TwoByTwoGrid(navController)
}

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

            val location: String = if (index < 4) {
                locationnav[index]
            } else {
                "cowellstev"
            }
            Button (
                onClick =  { navController.navigate(location) },
                modifier = Modifier.aspectRatio(1f),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Yellow)

            ) {
                Text(text = locations[index], color = Color.Black)
            }
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
