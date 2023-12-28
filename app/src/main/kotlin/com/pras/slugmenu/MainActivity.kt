package com.pras.slugmenu

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pras.slugmenu.ui.theme.SlugMenuTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException


private const val SETTINGS_NAME = "user_settings"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_NAME
)

private const val TAG = "MainActivityLog"

class MainActivity : ComponentActivity() {
    private lateinit var preferencesDatastore: PreferencesDatastore
    override fun onCreate(savedInstanceState: Bundle?) {

        lifecycleScope.launch {
            // asynchronously load settings
            dataStore.data.first()

            val backgroundDownloadsEnabled = dataStore.data
                .catch {
                    if (it is IOException) {
                        Log.e(TAG, "Error reading Background Update preferences.", it)
                        emit(emptyPreferences())
                    } else {
                        throw it
                    }
                }
                .map {preferences ->
                    preferences[booleanPreferencesKey("enable_background_updates")] ?: false
                }

            if (backgroundDownloadsEnabled.first()) {
                //refresh background downloads if enabled
                Log.d(TAG, "scheduling background downloads")
                val backgroundDownloadScheduler = BackgroundDownloadScheduler
                backgroundDownloadScheduler.refreshPeriodicWork(applicationContext)
            }
        }

        super.onCreate(savedInstanceState)

        // including IME animations, and go edge-to-edge
        // This also sets up the initial system bar style based on the platform theme
        enableEdgeToEdge()

        preferencesDatastore = PreferencesDatastore(dataStore)

        setContent {
            val themeChoice = remember { mutableIntStateOf(runBlocking { preferencesDatastore.getThemePreference.first() }) }
            val useMaterialYou = remember { mutableStateOf(runBlocking { preferencesDatastore.getMaterialYouPreference.first() }) }
            val useAmoledTheme = remember { mutableStateOf(runBlocking { preferencesDatastore.getAmoledPreference.first() }) }

            // necessary to do this first, since otherwise ui takes a second to update

            /*
            runBlocking {
                useMaterialYou.value = preferencesDatastore.getMaterialYouPreference.first()
                themeChoice.intValue = preferencesDatastore.getThemePreference.first()
                useAmoledTheme.value = preferencesDatastore.getAmoledPreference.first()
            }
             */

            LaunchedEffect(Unit) {
                preferencesDatastore.getThemePreference.collect {
                    themeChoice.intValue = it
                }
                preferencesDatastore.getMaterialYouPreference.collect {
                    useMaterialYou.value = it
                }
            }

            val useDarkTheme = when (themeChoice.intValue) {1 -> false 2 -> true else -> isSystemInDarkTheme() }

            SlugMenuTheme(darkTheme = useDarkTheme, dynamicColor = useMaterialYou.value, amoledColor = useAmoledTheme.value) {
                // Update the edge to edge configuration to match the theme
                // This is the same parameters as the default enableEdgeToEdge call, but we manually
                // resolve whether or not to show dark theme using uiState, since it can be different
                // than the configuration's dark theme value based on the user preference.
                val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
                val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
                DisposableEffect(useDarkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        ) { useDarkTheme },
                        navigationBarStyle = SystemBarStyle.auto(
                            lightScrim,
                            darkScrim,
                        ) { useDarkTheme },
                    )
                    onDispose {}
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Init("home", themeChoice, useMaterialYou, useAmoledTheme, preferencesDatastore)
                }
            }
        }

    }
}

// blocks touches while navigating back
// not sure where to put this atm, temp in mainactivity
@Composable
fun TouchBlocker(navController: NavController, delay: Long, clickable: MutableState<Boolean>) {
    val coroutineScope = rememberCoroutineScope()
    BackHandler {
        if (clickable.value) {
            clickable.value = false
            coroutineScope.launch {
                // length of the animation
                delay(delay)
                clickable.value = true
            }
        }
        navController.navigateUp()
    }

    if (!clickable.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(remember { MutableInteractionSource() }, null) {
                    // No-op: Block clicks
                    // using pointerinput instead of clickable stops the ripple effect from appearing
                }
                .background(color = Color.Transparent)
                .zIndex(Float.MAX_VALUE - 1)
        )
    }
}


const val DELAYTIME = 350
const val FADETIME = 200
@Composable
fun Init(startDestination: String, themeChoice: MutableState<Int>, useMaterialYou: MutableState<Boolean>, useAmoledTheme: MutableState<Boolean>, userSettings: PreferencesDatastore) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable(
            "home",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { HomeScreen(navController = navController, preferencesDataStore = userSettings) }
        composable(
            "ninelewis",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally(initialOffsetX = {it})

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            DiningMenu(
                navController,
                "Nine/Lewis",
                "40&locationName=College+Nine%2fJohn+R.+Lewis+Dining+Hall&naFlag=1"
            )
        }
        composable(
            "cowellstev",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally(initialOffsetX = {it})

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            DiningMenu(
                navController,
                "Cowell/Stevenson",
                "05&locationName=Cowell%2fStevenson+Dining+Hall&naFlag=1"
            )
        }
        composable(
            "crownmerrill",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally(initialOffsetX = {it})

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            DiningMenu(
                navController,
                "Crown/Merrill",
                "20&locationName=Crown%2fMerrill+Dining+Hall&naFlag=1"
            )
        }
        composable(
            "porterkresge",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally(initialOffsetX = {it})

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            DiningMenu(
                navController,
                "Porter/Kresge",
                "25&locationName=Porter%2fKresge+Dining+Hall&naFlag=1"
            )
        }
        composable(
            "carsonoakes",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally(initialOffsetX = {it})

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            DiningMenu(
                navController,
                "Carson/Oakes",
                "30&locationName=Rachel+Carson%2fOakes+Dining+Hall&naFlag=1"
            )
        }
        // dining halls end here

        composable(
            "perkcoffee",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally( initialOffsetX = {it} )

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            NonDiningMenu(
                navController,
                "Perk Coffee Bars",
                "22&locationName=Perk+Coffee+Bars&naFlag=1"
            )
        }
        composable(
            "terrafresca",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally( initialOffsetX = {it} )

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            NonDiningMenu(
                navController,
                "Terra Fresca",
                "45&locationName=UCen+Coffee+Bar&naFlag=1"
            )
        }
        composable(
            "portermarket",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally( initialOffsetX = {it} )

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            NonDiningMenu(
                navController,
                "Porter Market",
                "50&locationName=Porter+Market&naFlag=1"
            )
        }
        composable(
            "stevcoffee",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally( initialOffsetX = {it} )

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            NonDiningMenu(
                navController,
                "Stevenson Coffee House",
                "26&locationName=Stevenson+Coffee+House&naFlag=1"
            )
        }
        composable(
            "globalvillage",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally( initialOffsetX = {it} )

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            NonDiningMenu(
                navController,
                "Global Village Cafe",
                "46&locationName=Global+Village+Cafe&naFlag=1"
            )
        }
        // nondiningmenus end here

        composable(
            "oakescafe",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideInHorizontally( initialOffsetX = {it} )

                    else -> fadeIn()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutHorizontally( targetOffsetX = {it} )

                    else -> fadeOut()
                }
            }
        ) {
            OakesCafeMenu(
                navController,
                "Oakes Cafe",
                "23&locationName=Oakes+Cafe&naFlag=1"
            )
        }

        //settings menu
        composable(
            "settings",
            enterTransition = {fadeIn()},
            exitTransition = {fadeOut()}

        ) {
            SettingsScreen(
                navController,
                useMaterialYou,
                useAmoledTheme,
                themeChoice,
                userSettings
            )
        }

        //about menu
        composable(
            "about",
            enterTransition = {fadeIn()},
            exitTransition = {fadeOut()}
        ) {
            AboutScreen(navController, userSettings)
        }

        composable(
            "menuorganizer",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            MenuOrganizer(navController, userSettings)
        }

        composable(
            "favoritesmenu",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            FavoritesMenu(navController, userSettings)
        }

        //custom date dining menu
        composable("customdiningdate/{locationUrl}/{dateUrl}/{locationName}", arguments = listOf(navArgument("locationUrl") { type = NavType.StringType },navArgument("dateUrl") { type = NavType.StringType },navArgument("locationName") { type = NavType.StringType }))
        { backStackEntry ->
            DiningMenuCustomDate(
                navController,
                inputLocationUrl = backStackEntry.arguments?.getString("locationUrl") ?: "example.com",
                // date the feature was added
                dateUrl = backStackEntry.arguments?.getString("dateUrl") ?: "5-18-23",
                inputLocationName = backStackEntry.arguments?.getString("locationName") ?: "Null - this should never happen."
            )
        }

    }
}