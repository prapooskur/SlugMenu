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
import com.pras.slugmenu.ui.elements.SwipableTabBar
import com.pras.slugmenu.ui.elements.TopBarClean
import com.pras.slugmenu.ui.elements.TopBarWaitz
import com.pras.slugmenu.ui.elements.WaitzDialog
import com.pras.slugmenu.ui.elements.shortToast
import com.pras.slugmenu.ui.viewmodels.MenuViewModel

private const val TAG = "OakesCafeMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OakesCafeMenu(
    navController: NavController,
    locationName: String,
    locationUrl: String,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory)
) {
    Log.d(TAG, "Opening OakesCafeMenu with room!")

    val toastContext = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    val showBottomSheet = rememberSaveable { mutableStateOf(false) }
    val showWaitzDialog = rememberSaveable { mutableStateOf(false) }

    val haptics = LocalHapticFeedback.current

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchMenu(locationName, locationUrl)
    }

    LaunchedEffect(key1 = uiState.value.error) {
        if (uiState.value.error.isNotEmpty()) {
            shortToast(uiState.value.error, toastContext)
            viewModel.clearError()
        }
    }

    Column {
        if (!uiState.value.menuLoading) {
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),

                topBar = {
                    TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
                },
                content = { padding ->
                    SwipableTabBar(
                        menuArray = uiState.value.menus,
                        onFavorite = {
                            viewModel.insertFavorite(it)
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onFullCollapse = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        padding = padding
                    )
                },
                //floating action button - shows hours bottom sheet on press
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showBottomSheet.value = !showBottomSheet.value },
                        modifier = Modifier.systemBarsPadding()
                    ) {
                        Icon(painterResource(R.drawable.schedule),"Hours")
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
        }
        else {
            // Otherwise, display a loading indicator
            TopBarClean(titleText = locationName, navController = navController)
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