package com.example.slugmenu

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.slugmenu.ui.theme.SlugMenuTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.typeOf
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
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        addScreens(navController, context)
    }
}

fun NavGraphBuilder.addScreens(navController: NavHostController, context: Context) {

    /*
    val date = LocalDate.now().toString()

    val menuCache = File(context.cacheDir, "menuCache")
    val dateCache = File(context.cacheDir, "dateCache")

    if (!menuCache.exists()) {
        menuCache.createNewFile()
    }

    if (!dateCache.exists()) {
        dateCache.createNewFile()

        /*
        val dateWriter = BufferedWriter(FileWriter(dateCache))
        dateWriter.write(date)
        dateWriter.close()
         */
    }

    val menuReader = BufferedReader(FileReader(menuCache))
    val dateReader = BufferedReader(FileReader(dateCache))

    val menuWriter = BufferedWriter(FileWriter(menuCache))
    val dateWriter = BufferedWriter(FileWriter(dateCache))

    try {
        Log.d("TAG", "dateReader output: "+dateReader.readLine())
        if (dateReader.readLine() == date) {
            Log.d("TAG", "Date Cache hit")
        } else {
            Log.d("TAG", "Date Cache miss, writing $date to dateWriter")
            dateWriter.write(date)
        }
    } catch (e: IOException) {
        Log.e("TAG", "Error writing to date cache: ${e.message}")
    } finally {
        menuReader.close()
        dateReader.close()
        menuWriter.close()
        dateWriter.close()
    }

     */



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