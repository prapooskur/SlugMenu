package com.pras.slugmenu

import android.content.Context
import android.os.Bundle
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pras.slugmenu.ui.theme.SlugMenuTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController



private const val SETTINGS_NAME = "user_settings"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_NAME
)

class MainActivity : ComponentActivity() {

    lateinit var userSettings: PreferencesDatastore
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        userSettings = PreferencesDatastore(dataStore)

        setContent {
            SlugMenuTheme(userSettings = userSettings) {
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

    //figure out how to get this working only on home screen
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

    val menuDb = MenuDatabase.getInstance(context)

    composable("home") {
        HomeScreen(navController)
    }
    composable("settings") {
        SettingsScreen(navController)
    }

    composable("ninelewis") {
        DiningMenuRoom(navController, "Nine/Lewis","40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1",menuDb)
    }
    composable("cowellstev") {
        DiningMenuRoom(navController, "Cowell/Stevenson","05&locationName=Cowell%2fStevenson+Dining+Hall&naFlag=1",menuDb)
    }
    composable("crownmerrill") {
        DiningMenuRoom(navController, "Crown/Merrill","20&locationName=Crown%2fMerrill+Dining+Hall&naFlag=1",menuDb)
    }
    composable("porterkresge") {
        DiningMenuRoom(navController, "Porter/Kresge","25&locationName=Porter%2fKresge+Dining+Hall&naFlag=1",menuDb)
    }

    composable("perkcoffee") {
        NonDiningMenuRoom(navController, "Perk Coffee Bars","22&locationName=Perk+Coffee+Bars&naFlag=1",menuDb)
    }
    composable("terrafresca") {
        NonDiningMenuRoom(navController, "Terra Fresca","45&locationName=UCen+Coffee+Bar&naFlag=1",menuDb)
    }
    composable("portermarket") {
        NonDiningMenuRoom(navController, "Porter Market","50&locationName=Porter+Market&naFlag=1",menuDb)
    }
    composable("stevcoffee") {
        NonDiningMenuRoom(navController, "Stevenson Coffee House","26&locationName=Stevenson+Coffee+House&naFlag=1",menuDb)
    }
    composable("globalvillage") {
        NonDiningMenuRoom(navController, "Global Village Cafe","46&locationName=Global+Village+Cafe&naFlag=1",menuDb)
    }

    composable("oakescafe") {
        OakesCafeMenuRoom(navController, "Oakes Cafe","23&locationName=Oakes+Cafe&naFlag=1",menuDb)
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Init("home")
}