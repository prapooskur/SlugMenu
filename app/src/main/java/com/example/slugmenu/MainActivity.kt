package com.example.slugmenu

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.slugmenu.ui.theme.SlugMenuTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlin.system.measureTimeMillis

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /*
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        window.statusBarColor = Color.Transparent.toArgb()

         */



        setContent {
            SlugMenuTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Init("home")
                }
            }
        }
    }
}

@Composable
fun TransparentBar() {

}


@Composable
fun Init(startDestination: String) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        addScreens(navController)
    }
}

fun NavGraphBuilder.addScreens(navController: NavHostController) {
    val nineLewisMenus: Array<MutableList<String>>
    val cowellStevMenus: Array<MutableList<String>>
    val crownMerrillMenus: Array<MutableList<String>>
    val porterKresgeMenus: Array<MutableList<String>>
    val scrapeTime = measureTimeMillis {
        runBlocking {
            val nineLewisJob =
                async { getMenuAsync("40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1") }
            val cowellStevJob =
                async { getMenuAsync("05&locationName=Cowell%2fStevenson+Dining+Hall&naFlag=1") }
            val crownMerrillJob =
                async { getMenuAsync("20&locationName=Crown%2fMerrill+Dining+Hall&naFlag=1") }
            val porterKresgeJob =
                async { getMenuAsync("25&locationName=Porter%2fKresge+Dining+Hall&naFlag=1") }

            nineLewisMenus = nineLewisJob.await()
            cowellStevMenus = cowellStevJob.await()
            crownMerrillMenus = crownMerrillJob.await()
            porterKresgeMenus = porterKresgeJob.await()
        }
    }
    Log.d("TAG", "Scrape time: "+scrapeTime+"ms.")


    composable("home") {
        HomeScreen(navController)
    }

    composable("ninelewis") {
        NineLewis(navController, nineLewisMenus)
    }

    composable("cowellstev") {
        CowellStev(navController, cowellStevMenus)
    }

    composable("crownmerrill") {
        CrownMerrill(navController, crownMerrillMenus)
    }
    composable("porterkresge") {
        PorterKresge(navController, porterKresgeMenus)
    }
    /*
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
    Init("home")

}