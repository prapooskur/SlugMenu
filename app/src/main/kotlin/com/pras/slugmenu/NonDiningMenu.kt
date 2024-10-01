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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pras.slugmenu.ui.elements.HoursBottomSheet
import com.pras.slugmenu.ui.elements.PrintMenu
import com.pras.slugmenu.ui.elements.TopBarClean
import com.pras.slugmenu.ui.elements.TopBarWaitz
import com.pras.slugmenu.ui.elements.WaitzDialog
import com.pras.slugmenu.ui.elements.shortToast
import com.pras.slugmenu.ui.viewmodels.MenuViewModel

private const val TAG = "NonDiningMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonDiningMenu(
    navController: NavController,
    locationName: String,
    locationUrl: String,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory)
) {

    val toastContext = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    val waitzList = setOf("Porter Market", "Global Village Cafe", "Stevenson Coffee House")
    val useWaitz = waitzList.contains(locationName)

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchMenu(locationName, locationUrl)
        if (useWaitz) {
            viewModel.fetchWaitz()
        }
        viewModel.fetchHours()
    }

    LaunchedEffect(key1 = uiState.value.error) {
        if (uiState.value.error.isNotEmpty()) {
            shortToast(uiState.value.error, toastContext)
            viewModel.clearError()
        }
    }

    val showBottomSheet = rememberSaveable { mutableStateOf(false) }

    val showWaitzDialog = rememberSaveable { mutableStateOf(false) }

    val haptics = LocalHapticFeedback.current

    Column {
        if (!uiState.value.menuLoading) {
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    if (useWaitz) {
                        TopBarWaitz(
                            titleText = locationName,
                            onBack = { navController.navigateUp() },
                            onToggle = {
                                showWaitzDialog.value = !showWaitzDialog.value
                                Log.d(TAG,showWaitzDialog.value.toString())
                            }
                        )
                    } else {
                        TopBarClean(titleText = locationName, onBack = { navController.navigateUp() })
                    }
                },
                content = { padding ->
                    Box(Modifier.padding(padding)) {
                        if (uiState.value.menus.isNotEmpty()) {
                            PrintMenu(
                                itemList = uiState.value.menus[0],
                                onFavorite = {
                                    viewModel.insertFavorite(it)
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onFullCollapse = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )
                        } else {
                            PrintMenu(
                                itemList = emptyList(),
                                onFavorite = { /* do nothing */},
                                onFullCollapse = { /* do nothing */ }
                            )
                        }
                    }
                    /*if (menuList.isNotEmpty() && menuList[0].isNotEmpty()) {
                        PrintPriceMenu(itemList = menuList[0], padding)
                    } else {
                        PrintPriceMenu(itemList = mutableListOf(), padding)
                    }*/
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

            Column(modifier = Modifier.fillMaxHeight()) {
                HoursBottomSheet(
                    openBottomSheet = showBottomSheet,
                    bottomSheetState = rememberModalBottomSheetState(),
                    locationName = locationName.substringBefore(" "),
                    hoursLoading = uiState.value.hoursLoading,
                    hoursException = uiState.value.error.isNotEmpty(),
                    allHoursList = uiState.value.hours
                )
            }
            WaitzDialog(
                showDialog = showWaitzDialog,
                locationName = locationName.replace("Stevenson", "Stev"),
                waitzLoading = uiState.value.waitzLoading,
                waitzException = uiState.value.error.isNotEmpty(),
                waitzData = uiState.value.waitz
            )
        } else {
            TopBarClean(titleText = locationName, onBack = { navController.navigateUp() })
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(strokeCap = StrokeCap.Round)
            }
        }
    }
}
