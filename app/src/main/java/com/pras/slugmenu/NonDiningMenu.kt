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
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.security.cert.CertificateException
import java.time.LocalDate
import javax.net.ssl.SSLHandshakeException

private const val TAG = "NonDiningMenu"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonDiningMenuRoom(navController: NavController, locationName: String, locationUrl: String, menuDatabase: MenuDatabase) {
    Log.d(TAG, "Opening NonDiningMenu with room!")

    val currentDate = LocalDate.now().toString()
    val menuDao = menuDatabase.menuDao()

    var menuList by remember { mutableStateOf<List<List<String>>>(listOf(listOf())) }
    val dataLoadedState = remember { mutableStateOf(false) }
    var exceptionFound by remember { mutableStateOf("No Exception") }

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
                //TODO: unify these into one catch block?
                } catch (e: UnresolvedAddressException) {
                    exceptionFound = "No Internet connection"
                } catch (e: SocketTimeoutException) {
                    exceptionFound = "Connection timed out"
                } catch (e: UnknownHostException) {
                    exceptionFound = "Failed to resolve URL"
                } catch (e: CertificateException) {
                    exceptionFound = "Website's SSL certificate is invalid"
                } catch (e: SSLHandshakeException) {
                    exceptionFound = "SSL handshake failed"
                } catch (e: Exception) {
                    exceptionFound = "Exception: $e"
                }
                dataLoadedState.value = true
            }
        }
    }
    if (exceptionFound != "No Exception") {
        ShortToast(exceptionFound, LocalContext.current)
    }

    val showBottomSheet = remember { mutableStateOf(false) }

    Column {
        if (dataLoadedState.value) {
            Scaffold(
                // custom insets necessary to render behind nav bar
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    TopBarClean(titleText = locationName, navController = navController)
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

            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            Column(modifier = Modifier.fillMaxHeight()) {
                HoursBottomSheet(openBottomSheet = showBottomSheet, bottomSheetState = bottomSheetState, locationName = locationName)
            }
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
