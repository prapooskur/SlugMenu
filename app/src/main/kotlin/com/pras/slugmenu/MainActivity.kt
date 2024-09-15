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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.calculateDisplayFeatures
import com.pras.slugmenu.data.repositories.PreferencesRepository
import com.pras.slugmenu.ui.theme.SlugMenuTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException


private const val SETTINGS_NAME = "user_settings"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_NAME
)

private const val TAG = "MainActivity"

// todo find better defaults
data class DisplayFeatures(
    val features: List<DisplayFeature> = listOf(),
    val sizeClass: WindowSizeClass = WindowSizeClass.compute(1f,1f),
    val twoPanePreference: Boolean = false
)

val LocalDisplayFeatures = compositionLocalOf { DisplayFeatures() }

class MainActivity : ComponentActivity() {
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

        val preferencesRepository = PreferencesRepository(dataStore)

        setContent {
            val initThemeChoice = runBlocking { preferencesRepository.getThemePreference.first() }
            val initUseMaterialYou = runBlocking { preferencesRepository.getMaterialYouPreference.first() }
            val initUseAmoledTheme = runBlocking { preferencesRepository.getAmoledPreference.first() }
            val initUseTwoPane = runBlocking { preferencesRepository.getPanePreference.first() }

            val themeChoice = preferencesRepository.getThemePreference.collectAsStateWithLifecycle(initThemeChoice)
            val useMaterialYou = preferencesRepository.getMaterialYouPreference.collectAsStateWithLifecycle(initUseMaterialYou)
            val useAmoledTheme = preferencesRepository.getAmoledPreference.collectAsStateWithLifecycle(initUseAmoledTheme)
            val useTwoPane = preferencesRepository.getPanePreference.collectAsStateWithLifecycle(initUseTwoPane)

            val useDarkTheme = when (themeChoice.value) {1 -> false 2 -> true else -> isSystemInDarkTheme() }

            val displayFeatures = DisplayFeatures(
                calculateDisplayFeatures(activity = this),
                currentWindowAdaptiveInfo().windowSizeClass,
                useTwoPane.value
            )
            CompositionLocalProvider(LocalDisplayFeatures provides displayFeatures) {
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
                        Init("home", preferencesRepository)
                    }
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

// navigation destinations
@Serializable
data class CustomDiningDate(val locationUrl: String, val dateUrl: String, val locationName: String)



const val DELAYTIME = 350
const val FADETIME = 200
@Composable
fun Init(startDestination: String, userSettings: PreferencesRepository) {

    val navController = rememberNavController()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val useTwoPanes = userSettings.getPanePreference.collectAsStateWithLifecycle(false)

    val showTwoPanes = (windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT && useTwoPanes.value)

    LaunchedEffect(key1 = showTwoPanes) {
        if (!showTwoPanes) {
            // currentdestination should never be null
            navController.popBackStack(route = navController.currentDestination?.route ?: "home", true)
            navController.navigate("home")
        }
    }

    // todo get this working
    LaunchedEffect(key1 = windowSizeClass) {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
            navController.popBackStack(route = "home", inclusive = false, saveState = false)
        }
    }

    if (!showTwoPanes) {
        BuildNavHost(navController, startDestination, userSettings)
    } else {

        // build two pane structure outside the nav hierarchy
        // this is so cursed

        val locationOrder = userSettings.getLocationOrder.collectAsStateWithLifecycle(
            initialValue = Json.encodeToString(
                listOf(
                    LocationOrderItem(navLocation = "ninelewis", locationName = "Nine/Lewis", visible = true),
                    LocationOrderItem(navLocation = "cowellstev", locationName = "Cowell/Stevenson", visible = true),
                    LocationOrderItem(navLocation = "crownmerrill", locationName = "Crown/Merrill", visible = true),
                    LocationOrderItem(navLocation = "porterkresge", locationName = "Porter/Kresge", visible = true),
                    LocationOrderItem(navLocation = "carsonoakes", locationName = "Carson/Oakes", visible = true),
                    LocationOrderItem(navLocation = "perkcoffee", locationName = "Perk Coffee Bars", visible = true),
                    LocationOrderItem(navLocation = "terrafresca", locationName = "Terra Fresca", visible = true),
                    LocationOrderItem(navLocation = "portermarket", locationName = "Porter Market", visible = true),
                    LocationOrderItem(navLocation = "stevcoffee", locationName = "Stevenson Coffee House", visible = true),
                    LocationOrderItem(navLocation = "globalvillage", locationName = "Global Village Cafe", visible = false),
                    LocationOrderItem(navLocation = "oakescafe", locationName = "Oakes Cafe", visible = true)
                )
            )
        )
//        val locationOrder = runBlocking { userSettings.getLocationOrder.first() }

        val locationOrderDecode: List<LocationOrderItem> = Json.decodeFromString(locationOrder.value)
        val visibleLocationOrder = locationOrderDecode.filter { it.visible }
        val initialNavLocation = remember { visibleLocationOrder.getOrNull(0)?.navLocation ?: "ninelewis" }

        val iconMap = mapOf(
            //since the global village menu no longer exists, i've mapped the RCC icon to it for now until I can get a proper one.
            "ninelewis"     to R.drawable.ninelewis,
            "cowellstev"    to R.drawable.cowellstevenson,
            "crownmerrill"  to R.drawable.crownmerrill,
            "porterkresge"  to R.drawable.porterkresge,
            "carsonoakes"   to R.drawable.globalvillagecafe,
            "perkcoffee"    to R.drawable.perkcoffeebars,
            "terrafresca"   to R.drawable.terrafresca,
            "portermarket"  to R.drawable.portermarket,
            "stevcoffee"    to R.drawable.stevensoncoffeehouse,
            "globalvillage" to R.drawable.globalvillagecafe,
            "oakescafe"     to R.drawable.oakescafe,
            "settings"      to R.drawable.settings
        )

        TwoPane(
            first = {
                Scaffold { paddingValues ->
                    AdaptiveNavCardList(
                        navController = navController,
                        innerPadding = paddingValues,
                        inputLocationOrder = visibleLocationOrder,
                        iconMap = iconMap
                    )
                }
            },
            second = {
                BuildNavHost(navController, initialNavLocation, userSettings)
            },
            strategy = HorizontalTwoPaneStrategy(0.4f),
            displayFeatures = LocalDisplayFeatures.current.features,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BuildNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home",
    userSettings: PreferencesRepository,
) {

    // instantiate viewModels

    NavHost(
        navController = navController,
        // modifier necessary to stop animation bug?
        modifier = Modifier.fillMaxSize(),
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable(
            "home",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { HomeScreen(navController = navController, preferencesRepository = userSettings) }
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
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }

        ) {
//            SettingsScreen(
//                navController,
//                useMaterialYou,
//                useAmoledTheme,
//                themeChoice,
//                userSettings
//            )
            SettingsNew(navController = navController)
        }

        //about menu
        composable(
            "about",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
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
        composable(
            "customdiningdate/{locationUrl}/{dateUrl}/{locationName}",
            arguments = listOf(navArgument("locationUrl") { type = NavType.StringType },
                navArgument("dateUrl") { type = NavType.StringType },
                navArgument("locationName") { type = NavType.StringType })
        )
        { backStackEntry ->
            DiningMenuCustomDate(
                navController,
                inputLocationUrl = backStackEntry.arguments?.getString("locationUrl") ?: "example.com",
                // date the feature was added
                dateUrl = backStackEntry.arguments?.getString("dateUrl") ?: "5-18-23",
                inputLocationName = backStackEntry.arguments?.getString("locationName") ?: "Null - this should never happen."
            )
        }

        // safe args version
        composable<CustomDiningDate>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { backStackEntry ->
            val date: CustomDiningDate = backStackEntry.toRoute()
            DiningMenuCustomDate(
                navController,
                inputLocationUrl = date.locationUrl,
                dateUrl = date.dateUrl,
                inputLocationName = date.locationName
            )
        }

    }
}

@Composable
fun AdaptiveNavCardList(navController: NavController, innerPadding: PaddingValues, inputLocationOrder: List<LocationOrderItem>, iconMap: Map<String, Int>) {

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // combine the padding values given by scaffold with the padding for bottom bar, so parts of
    // the grid aren't stuck behind the bottom bar
    val paddingAmount = 10.dp
    val contentPadding = PaddingValues(start = paddingAmount, top = paddingAmount, end = paddingAmount, bottom = paddingAmount+navPadding)

    val locationOrder = inputLocationOrder.plus(LocationOrderItem("settings", "Settings", true))

    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(locationOrder.size) { index ->
            val location: String = locationOrder[index].navLocation
            val name: String = locationOrder[index].locationName.replace("Perks", "Perk Coffee Bars")

            // try to get icon from dictionary, default to nine/lewis icon if it isn't listed for some reason to avoid crash
            // in practice, this should never need to fall back to the default.
            val icon = iconMap.getOrDefault(location, R.drawable.ninelewis)

            val currentDestination = navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(navController.currentBackStackEntry)
            val selected = currentDestination.value?.destination?.route == locationOrder[index].navLocation

            Box(Modifier.heightIn(max=120.dp)) {
                Card(
                    onClick = {
                        if (!selected) {
                            navController.navigate(locationOrder[index].navLocation)
                            Log.d(TAG, "Current destination: "+navController.currentDestination?.route.toString())
                        }
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        // todo get this to work properly and not cut off width
//                    .heightIn(max = 120.dp)
                        .aspectRatio(4f)
                        .fillMaxWidth(),
                    colors = if (selected) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(30.dp)
                        )
                    } else {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    }

                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        // without this, the nine/lewis icon looks larger than the others despite being the same weight
                        // extra padding instead of lower weight prevents it from taking up less space than the other icons
                        val imageModifier = if (icon == R.drawable.ninelewis) {
                            Modifier
                                .aspectRatio(1f)
                                .weight(0.4f)
                                .padding(22.dp)
                        } else {
                            Modifier
                                .aspectRatio(1f)
                                .weight(0.4f)
                                .padding(14.dp)
                        }

                        // a little start padding helps text not be too close to the icon
                        val textModifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)

                        // no content description provided - image is purely decorative
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                            alignment = Alignment.Center,
                            modifier = imageModifier
                        )

                        Text(
                            text = name,
                            textAlign = TextAlign.Left,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 18.sp,
                            modifier = textModifier
                        )
                    }
                }
            }
        }
    }
}
