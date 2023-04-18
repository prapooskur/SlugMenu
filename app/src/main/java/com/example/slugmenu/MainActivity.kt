package com.example.slugmenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key.Companion.Home
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.slugmenu.ui.theme.SlugMenuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /*
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        window.statusBarColor = Color.Transparent.toArgb()

         */



        setContent {
            val navController = rememberNavController()

            SlugMenuTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    //Text(text = "UCSC Menu")
                    Column() {
                    //    Spacer(modifier = Modifier.height(160.dp))
                        TwoByTwoGrid()
                    }

                }
            }
        }
    }
}

@Composable
fun TransparentBar() {

}


@Composable
fun Init() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        addScreens(navController)
    }
}



fun NavGraphBuilder.addScreens(navController: NavHostController) {
    composable("home") {
        HomeScreen(navController)
    }
    /*
    composable("ninelewis") {
        NineLewis(navController)
    }
    composable("cowellstev") {
        CowellStev(navController)
    }
    composable("crownmerrill") {
        CrownMerrill(navController)
    }
    composable("porterkresge") {
        PorterKresge(navController)
    }
    composable("perkcoffee") {
        PerkCoffee(navController)
    }
    composable("terrafresca") {
        TerraFresca(navController)
    }
    composable("portermarket") {
        PorterMarket(navController)
    }
    composable("stevcoffee") {
        StevCoffee(navController)
    }
    composable("globalvillage") {
        GlobalVillage(navController)
    }
    composable("oakescafe") {
        OakesCafe(navController)
    }

     */
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

}