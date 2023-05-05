package com.pras.slugmenu

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pras.slugmenu.ui.theme.SlugMenuTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate
import kotlin.system.measureTimeMillis



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        


        setContent {
            SlugMenuTheme {
                MenuBarColor(color = MaterialTheme.colorScheme.primary)
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Init("home")
                }
            }
        }

    }
    /*
    var pressedTime: Long = 0
    override fun onBackPressed() {
        // on below line we are checking if the press time is greater than 2 sec
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            // if time is greater than 2 sec we are closing the application.
            super.onBackPressed()
            finish()
        } else {
            // in else condition displaying a toast message.
            Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        // on below line initializing our press time variable
        pressedTime = System.currentTimeMillis();
    }

     */
}



@Composable
fun MenuBarColor(color: Color) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(color = color)
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


    val date = LocalDate.now().toString()

    val menuCache = File(context.cacheDir, "menuCache")
    val dateCache = File(context.cacheDir, "dateCache")

    if (!menuCache.exists()) {
        menuCache.createNewFile()
    }

    if (!dateCache.exists()) {
        dateCache.createNewFile()
        Log.d("TAG","creating cache file")

        /*
        val dateWriter = BufferedWriter(FileWriter(dateCache))
        dateWriter.write(date)
        dateWriter.close()
         */

    }

    var cachedData: Array<Array<MutableList<String>>> = arrayOf(arrayOf())

    val dateCheckReader = BufferedReader(FileReader(dateCache))
    val menuReader = FileReader(menuCache)
    val gson = Gson()
    val menuString = menuReader.readText()


    var menuCached: Boolean = false
//    var dateCached: Boolean = false

    var nineLewisMenus: Array<MutableList<String>> = arrayOf(mutableListOf())
    var cowellStevMenus: Array<MutableList<String>> = arrayOf(mutableListOf())
    var crownMerrillMenus: Array<MutableList<String>> = arrayOf(mutableListOf())
    var porterKresgeMenus: Array<MutableList<String>> = arrayOf(mutableListOf())
    var perkCoffeeMenu: Array<MutableList<String>> = arrayOf(mutableListOf())
    var terraFrescaMenu: Array<MutableList<String>> = arrayOf(mutableListOf())
    var porterMarketMenu: Array<MutableList<String>> = arrayOf(mutableListOf())
    var stevCoffeeMenu: Array<MutableList<String>> = arrayOf(mutableListOf())
    var globalVillageMenu: Array<MutableList<String>> = arrayOf(mutableListOf())
    var oakesCafeMenu: Array<MutableList<String>> = arrayOf(mutableListOf())

    try {
        val cachedDate = dateCheckReader.readLine()
        if (cachedDate == date && menuString != "") {
//            Log.d("TAG","menu string: $menuString")
            val type = object : TypeToken<Array<Array<MutableList<String>>>>() {}.type
            cachedData = gson.fromJson(menuString, type)
            menuCached = true
        } else {
            menuCached = false
        }
    } catch (e: IOException) {
        Log.e("TAG", "Error writing to date cache: ${e.message}")
    } finally {
        dateCheckReader.close()
        menuReader.close()
    }

    val dateReader = BufferedReader(FileReader(dateCache))

    try {
        val cachedDate = dateReader.readLine()
        Log.d("TAG", "dateReader cached output: $cachedDate")
        if (cachedDate == date) {
            Log.d("TAG", "Date Cache hit")

        } else {
            val dateWriter = BufferedWriter(FileWriter(dateCache))
            Log.d("TAG", "Date Cache miss, writing $date to dateWriter")
            dateWriter.write(date)
            dateWriter.close()

        }
    } catch (e: IOException) {
        Log.e("TAG", "Error writing to date cache: ${e.message}")
    } finally {
        dateReader.close()
    }

    if (!menuCached) {
        val scrapeTime = measureTimeMillis {
            runBlocking {
                val nineLewisJob =
                    async { getDiningMenuAsync("40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1") }
                val cowellStevJob =
                    async { getDiningMenuAsync("05&locationName=Cowell%2fStevenson+Dining+Hall&naFlag=1") }
                val crownMerrillJob =
                    async { getDiningMenuAsync("20&locationName=Crown%2fMerrill+Dining+Hall&naFlag=1") }
                val porterKresgeJob =
                    async { getDiningMenuAsync("25&locationName=Porter%2fKresge+Dining+Hall&naFlag=1") }
                val perkCoffeeJob =
                    async { getSingleMenuAsync("22&locationName=Perk+Coffee+Bars&naFlag=1") }
                val terraFrescaJob =
                    async { getSingleMenuAsync("45&locationName=UCen+Coffee+Bar&naFlag=1") }
                val porterMarketJob =
                    async { getSingleMenuAsync("50&locationName=Porter+Market&naFlag=1") }
                val stevCoffeeJob =
                    async { getSingleMenuAsync("26&locationName=Stevenson+Coffee+House&naFlag=1") }
                val globalVillageJob =
                    async { getSingleMenuAsync("46&locationName=Global+Village+Cafe&naFlag=1") }
                val oakesCafeJob =
                    async { getOakesMenuAsync("23&locationName=Oakes+Cafe&naFlag=1") }

                nineLewisMenus = nineLewisJob.await()
                cowellStevMenus = cowellStevJob.await()
                crownMerrillMenus = crownMerrillJob.await()
                porterKresgeMenus = porterKresgeJob.await()
                perkCoffeeMenu = perkCoffeeJob.await()
                terraFrescaMenu = terraFrescaJob.await()
                porterMarketMenu = porterMarketJob.await()
                stevCoffeeMenu = stevCoffeeJob.await()
                globalVillageMenu = globalVillageJob.await()
                oakesCafeMenu = oakesCafeJob.await()

            }
            val menuWriter = FileWriter(menuCache)
            menuWriter.write(gson.toJson(arrayOf(nineLewisMenus,cowellStevMenus,crownMerrillMenus,porterKresgeMenus,perkCoffeeMenu,terraFrescaMenu,porterMarketMenu,stevCoffeeMenu,globalVillageMenu,oakesCafeMenu)))
            menuWriter.close()
        }
        Log.d("TAG", "Scrape time: " + scrapeTime + "ms.")
    } else {
        nineLewisMenus = cachedData[0]
        cowellStevMenus = cachedData[1]
        crownMerrillMenus = cachedData[2]
        porterKresgeMenus = cachedData[3]
        perkCoffeeMenu = cachedData[4]
        terraFrescaMenu = cachedData[5]
        porterMarketMenu = cachedData[6]
        stevCoffeeMenu = cachedData[7]
        globalVillageMenu = cachedData[8]
        oakesCafeMenu = cachedData[9]
        Log.d("TAG", "Menu cache hit.")
    }


    composable("home") {
        HomeScreen(navController)
    }
    composable("settings") {
        SettingsScreen(navController)
    }

    composable("ninelewis") {
        DiningMenu(navController, nineLewisMenus, "Nine/Lewis")
    }

    composable("cowellstev") {
        DiningMenu(navController, cowellStevMenus, "Cowell/Stevenson")
    }

    composable("crownmerrill") {
        DiningMenu(navController, crownMerrillMenus, "Crown/Merrill")
    }
    composable("porterkresge") {
        DiningMenu(navController, porterKresgeMenus, "Porter/Kresge")
    }

    composable("perkcoffee") {
        NonDiningMenu(navController, perkCoffeeMenu[0], "Perk Coffee Bars")
    }

    composable("terrafresca") {
        NonDiningMenu(navController, terraFrescaMenu[0], "Terra Fresca")
    }

    composable("portermarket") {
        NonDiningMenu(navController, porterMarketMenu[0], "Porter Market")
    }

    composable("stevcoffee") {
        NonDiningMenu(navController, stevCoffeeMenu[0], "Stevenson Coffee House")
    }
    composable("globalvillage") {
        NonDiningMenu(navController, globalVillageMenu[0], "Global Village Cafe")
    }
    composable("oakescafe") {
        OakesCafeMenu(navController, oakesCafeMenu, "Oakes Cafe")
    }
    /*
     */
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Init("home")

}