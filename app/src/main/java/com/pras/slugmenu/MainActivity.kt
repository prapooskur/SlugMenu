package com.pras.slugmenu

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pras.slugmenu.ui.theme.SlugMenuTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


private const val SETTINGS_NAME = "user_settings"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_NAME
)

private const val TAG = "MainActivityLog"

class MainActivity : ComponentActivity() {
    private lateinit var userSettings: PreferencesDatastore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tell app to render edge to edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        userSettings = PreferencesDatastore(dataStore)

        setContent {
            val themeChoice =    remember { mutableStateOf(0)}
            val useMaterialYou = remember { mutableStateOf(true) }
            val useAmoledTheme = remember { mutableStateOf(true) }


            // necessary to do this first, since otherwise ui takes a second to update
            runBlocking {
                useMaterialYou.value = userSettings.getMaterialYouPreference.first()
                themeChoice.value = userSettings.getThemePreference.first()
                useAmoledTheme.value = userSettings.getAmoledPreference.first()
            }

            LaunchedEffect(key1 = Unit) {
                lifecycleScope.launch {
                    // Schedule background downloads if enabled
                    if (userSettings.getBackgroundUpdatePreference.first()) {
                        Log.d(TAG, "scheduling background downloads")
                        val backgroundDownloadScheduler = BackgroundDownloadScheduler
                        backgroundDownloadScheduler.refreshPeriodicWork(applicationContext)
                    }
                }
                userSettings.getThemePreference.collect {
                    themeChoice.value = it
                }
                userSettings.getMaterialYouPreference.collect {
                    useMaterialYou.value = it
                }
            }
            SlugMenuTheme(darkTheme = when (themeChoice.value) {1 -> false 2 -> true else -> isSystemInDarkTheme() }, dynamicColor = useMaterialYou.value, amoledColor = useAmoledTheme.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    TransparentSystemBars()
                } else {
                    StatusBarColor(Color.Transparent)
                    NavigationBarColor(Color.Black)
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Init("home", themeChoice, useMaterialYou, useAmoledTheme, userSettings)
                }
            }
        }

    }
}



@Composable
fun StatusBarColor(color: Color) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(color = color)
}


@Composable
fun NavigationBarColor(color: Color) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setNavigationBarColor(color = color)
}

@Composable
fun TransparentSystemBars() {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
        )

        onDispose {}
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


const val TWEENTIME = 350
const val FADETIME = 200
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Init(startDestination: String, themeChoice: MutableState<Int>, useMaterialYou: MutableState<Boolean>, useAmoledTheme: MutableState<Boolean>, userSettings: PreferencesDatastore) {
//    val oldNavController = rememberNavController()
    val navController = rememberAnimatedNavController()
    val context = LocalContext.current

    val menuDb = MenuDatabase.getInstance(context)

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
            enterTransition = { fadeIn(animationSpec = tween(0)) },
//            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) { HomeScreen(navController = navController, preferencesDataStore = userSettings) }
        composable(
            "ninelewis",
            enterTransition = {
                when (initialState.destination.route) {
                    "home" ->
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Left,
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
                            animationSpec = tween(TWEENTIME)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "home" ->
                        slideOutOfContainer(
                            AnimatedContentScope.SlideDirection.Right,
                            animationSpec = tween(TWEENTIME)
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
        composable(
            "settings",
            enterTransition = {fadeIn(animationSpec = tween(FADETIME))},
            exitTransition = {fadeOut(animationSpec = tween(FADETIME))}

        ) {
            SettingsScreen(
                navController,
                useMaterialYou,
                useAmoledTheme,
                themeChoice,
                menuDb,
                userSettings
            )
        }

        //about menu
        composable(
            "about",
            enterTransition = {fadeIn(animationSpec = tween(FADETIME))},
            exitTransition = {fadeOut(animationSpec = tween(FADETIME))}
        ) {
            AboutScreen(navController, userSettings)
        }

        composable(
            "menuorganizer",
            enterTransition = {fadeIn(animationSpec = tween(FADETIME))},
            exitTransition = {fadeOut(animationSpec = tween(FADETIME))}
        ) {
            MenuOrganizer(navController, userSettings)
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