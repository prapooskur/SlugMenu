package com.pras.slugmenu

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(preferencesDataStore: PreferencesDatastore) {
    val useCollapsingTopBar = remember { mutableStateOf(false) }
    runBlocking {
        useCollapsingTopBar.value = preferencesDataStore.getToolbarPreference.first()
    }

    //TODO: Complete collapsing top bar rewrite
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })

    val scaffoldModifier = if (useCollapsingTopBar.value) {
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        Modifier.fillMaxSize()
    }

    val navController = rememberAnimatedNavController()

    val appVersion = BuildConfig.VERSION_NAME

    Scaffold(
        modifier = scaffoldModifier,
        // custom insets necessary to render behind nav bar
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            if (useCollapsingTopBar.value) {
                CollapsingLargeTopBar(titleText = "About", navController = navController, scrollBehavior = scrollBehavior)
            } else {
                Surface(shadowElevation = 4.dp) {
                    TopBar(titleText = "About", navController = navController)
                }
            }
        },
        content = { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                item {
                    AboutItem(appVersion = appVersion)
                }
                item {
                    GithubItem(context = LocalContext.current)
                }
            }
        }
    )
}

//Not currently necessary, since the update checker already shows the version number
@Composable
fun AboutItem(appVersion: String) {
    ListItem(
        leadingContent = {
            Icon(
                Icons.Outlined.Info,
                contentDescription = "About",
            )
        },
        headlineContent = {
            Text(text = "Slug Menu")
        },
        supportingContent = {
            Text(text = "Version $appVersion")
        },
    )
}

@Composable
fun GithubItem(context: Context) {
    ListItem(
        leadingContent = {
            // add Github icon here
            Icon(
                painterResource(R.drawable.github_icon),
                contentDescription = "Github",
                modifier = Modifier.size(24.dp)
            )
        },
        headlineContent = { Text(text = "Source Code") },
        supportingContent = { Text(text = "Released under the GNU General Public License v3.0") },
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/prapooskur/SlugMenu")
            ContextCompat.startActivity(context, intent, null)
        }
    )
}