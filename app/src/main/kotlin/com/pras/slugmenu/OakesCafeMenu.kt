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
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertificateException
import java.time.LocalDate
import javax.net.ssl.SSLHandshakeException

private const val TAG = "OakesCafeMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OakesCafeMenu(navController: NavController, locationName: String, locationUrl: String, menuDatabase: MenuDatabase) {
    Log.d(TAG, "Opening OakesCafeMenu with room!")

    val currentDate = LocalDate.now().toString()
    val menuDao = menuDatabase.menuDao()

    var menuList by remember { mutableStateOf<List<List<String>>>(listOf(listOf(),listOf())) }
    val dataLoadedState = remember { mutableStateOf(false) }
    var exceptionFound by remember { mutableStateOf("No Exception") }

    val showBottomSheet = remember { mutableStateOf(false) }
    val showWaitzDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Launch a coroutine to retrieve the menu from the database
        withContext(Dispatchers.IO) {
            val menu = menuDao.getMenu(locationName)
            if (menu != null && menu.cacheDate == currentDate) {
                menuList = MenuTypeConverters().fromString(menu.menus)
                Log.d(TAG, "menu list: $menuList")
                dataLoadedState.value = true
            } else {
                try {
                    menuList = getOakesMenuAsync(locationUrl)
                    Log.d(TAG, "menu list: ${menuList.size}")
                    menuDao.insertMenu(
                        Menu(
                            locationName,
                            MenuTypeConverters().fromList(menuList),
                            currentDate
                        )
                    )
                } catch (e: Exception) {
                    exceptionFound = when (e) {
                        is UnresolvedAddressException -> "No Internet connection"
                        is SocketTimeoutException -> "Connection timed out"
                        is UnknownHostException -> "Failed to resolve URL"
                        is CertificateException -> "Website's SSL certificate is invalid"
                        is SSLHandshakeException -> "SSL handshake failed"
                        else -> "Exception: $e"
                    }
                }
                dataLoadedState.value = true
            }
        }
    }
    if (exceptionFound != "No Exception") {
        ShortToast(exceptionFound, LocalContext.current)
    }

    Column {
        if (dataLoadedState.value) {
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),

                topBar = {
                    TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
                },
                content = {padding ->
                    Log.d("padding",padding.toString())
                    if (menuList.isNotEmpty()) {
                        PriceTabBar(menuList,padding)
                    } else {
                        PriceTabBar(listOf(listOf("Not Open Today"), listOf()),padding)
                    }
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
            val bottomSheetState = rememberModalBottomSheetState()
            Column(modifier = Modifier.fillMaxHeight()) {
                HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = bottomSheetState, locationName = locationName)
            }
        }
        else {
            TopBarWaitz(titleText = locationName, navController = navController, showWaitzDialog = showWaitzDialog)
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

    WaitzDialog(showDialog = showWaitzDialog, locationName = locationName)
}