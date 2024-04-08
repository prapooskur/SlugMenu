package com.pras.slugmenu

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val TAG = "NonDiningMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonDiningMenu(navController: NavController, locationName: String, locationUrl: String) {
    Log.d(TAG, "Opening NonDiningMenu with room!")

    val currentDate = LocalDate.now().toString()

    val menuDatabase = MenuDatabase.getInstance(LocalContext.current)
    val menuDao = menuDatabase.menuDao()

    var menuList by remember { mutableStateOf<List<List<String>>>(listOf(listOf())) }
    val dataLoadedState = remember { mutableStateOf(false) }

    val toastContext = LocalContext.current

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val menu = menuDao.getMenu(locationName)
            if (menu != null && menu.cacheDate == currentDate) {
                menuList = MenuTypeConverters().fromString(menu.menus)
                dataLoadedState.value = true
            } else {
                try {
                    menuList = getSingleMenuAsync(locationUrl)
                    menuDao.insertMenu(
                        Menu(
                            locationName,
                            MenuTypeConverters().fromList(menuList),
                            currentDate
                        )
                    )
                } catch (e: Exception) {
                    val exceptionFound = exceptionText(e)
                    withContext(Dispatchers.Main) {
                        shortToast(exceptionFound, toastContext)
                    }
                }
                dataLoadedState.value = true
            }
        }
    }

    val showBottomSheet = remember { mutableStateOf(false) }

    val waitzList = setOf("Porter Market", "Global Village Cafe", "Stevenson Coffee House")
    val useWaitz = waitzList.contains(locationName)
    val showWaitzDialog = remember { mutableStateOf(false) }

    Column {
        if (dataLoadedState.value) {
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    if (useWaitz) {
                        TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
                    } else {
                        TopBarClean(titleText = locationName, navController = navController)
                    }
                },
                content = { padding ->
                    if (menuList.isNotEmpty() && menuList[0].isNotEmpty()) {
                        PrintPriceMenu(itemList = menuList[0], padding)
                    } else {
                        PrintPriceMenu(itemList = mutableListOf(), padding)
                    }
                },
                //floating action button - shows hours bottom sheet
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showBottomSheet.value = !showBottomSheet.value },
                        modifier = Modifier.systemBarsPadding()
                    ) {
                        Icon(painterResource(R.drawable.schedule), "Hours")
                    }
                },
                floatingActionButtonPosition = FabPosition.End
            )

            val bottomSheetState = rememberModalBottomSheetState()

            Column(modifier = Modifier.fillMaxHeight()) {
                HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = bottomSheetState, locationName = locationName)
            }
            WaitzDialog(showDialog = showWaitzDialog, locationName = locationName)

        } else {
            TopBarClean(titleText = locationName, navController = navController)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
