package com.pras.slugmenu

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.pras.slugmenu.ui.theme.SlugMenuTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


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
            var useMaterialYou = remember { mutableStateOf(true) }
            runBlocking {
                val materialYouEnabled = userSettings.getMaterialYouPreference.first()
                useMaterialYou.value = materialYouEnabled
            }

            SlugMenuTheme(userSettings = userSettings, dynamicColor = useMaterialYou.value) {
                MenuBarColor(color = MaterialTheme.colorScheme.primary)
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Init("home", useMaterialYou, userSettings)
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



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Init(startDestination: String, useMaterialYou: MutableState<Boolean>, userSettings: PreferencesDatastore) {
//    val oldNavController = rememberNavController()
    val navController = rememberAnimatedNavController()
    val context = LocalContext.current

    val menuDb = MenuDatabase.getInstance(context)
    
    val tweenAmount = 350

    /*
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        addScreens(navController, context)
    }
     */
    AnimatedNavHost(navController = navController, startDestination = startDestination) {
        composable(
            "home",
            enterTransition = { null }
        ) { HomeScreen(navController = navController) }
        composable(
            "ninelewis",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            DiningMenuRoom(
                navController,
                "Nine/Lewis",
                "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1",
                menuDb
            )
        }
        composable(
            "cowellstev",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            DiningMenuRoom(
                navController,
                "Cowell/Stevenson",
                "05&locationName=Cowell%2fStevenson+Dining+Hall&naFlag=1",
                menuDb
            )
        }
        composable(
            "crownmerrill",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            DiningMenuRoom(
                navController,
                "Crown/Merrill",
                "20&locationName=Crown%2fMerrill+Dining+Hall&naFlag=1",
                menuDb
            )
        }
        composable(
            "porterkresge",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            DiningMenuRoom(
                navController,
                "Porter/Kresge",
                "25&locationName=Porter%2fKresge+Dining+Hall&naFlag=1",
                menuDb
            )
        }
        // dining halls end here

        composable(
            "perkcoffee",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            NonDiningMenuRoom(
                navController,
                "Perk Coffee Bars",
                "22&locationName=Perk+Coffee+Bars&naFlag=1",
                menuDb
            )
        }
        composable(
            "terrafresca",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            NonDiningMenuRoom(
                navController,
                "Terra Fresca",
                "45&locationName=UCen+Coffee+Bar&naFlag=1",
                menuDb
            )
        }
        composable(
            "portermarket",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            NonDiningMenuRoom(
                navController,
                "Porter Market",
                "50&locationName=Porter+Market&naFlag=1",
                menuDb
            )
        }
        composable(
            "stevcoffee",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            NonDiningMenuRoom(
                navController,
                "Stevenson Coffee House",
                "26&locationName=Stevenson+Coffee+House&naFlag=1",
                menuDb
            )
        }
        composable(
            "globalvillage",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            NonDiningMenuRoom(
                navController,
                "Global Village Cafe",
                "46&locationName=Global+Village+Cafe&naFlag=1",
                menuDb
            )
        }
        // nondiningmenus end here

        composable(
            "oakescafe",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(tweenAmount)
                        )

                    else -> null
                }
            }
        ) {
            OakesCafeMenuRoom(
                navController,
                "Oakes Cafe",
                "23&locationName=Oakes+Cafe&naFlag=1",
                menuDb
            )
        }

        //settings menu
        composable("settings") {
            SettingsScreen(
                navController,
                useMaterialYou,
                menuDb,
                userSettings
            )
        }

    }
}



/*
fun NavGraphBuilder.addScreens(navController: NavHostController, context: Context) {

    val menuDb = MenuDatabase.getInstance(context)

    composable("home",) {
        HomeScreen(navController),
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

 */

/*
@Preview
@Composable
fun DefaultPreview() {
    Init("home")
}
 */