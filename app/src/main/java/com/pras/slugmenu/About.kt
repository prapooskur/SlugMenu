package com.pras.slugmenu

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val TAG = "About"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController, preferencesDataStore: PreferencesDatastore) {
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

    val appVersion = BuildConfig.VERSION_NAME
    val context = LocalContext.current

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
                    GithubItem(context = context)
                }
                /*
                item {
                    ContactItem(context = context)
                }
                 */
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
            val webpage: Uri = Uri.parse("https://github.com/prapooskur/SlugMenu")
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            context.startActivity(intent)
        }
    )
}

@Composable
fun ContactItem(context: Context) {
//    val packageManager = context.packageManager
    ListItem(
        leadingContent = {
            // add email icon here
            Icon(
                Icons.Outlined.Email,
                contentDescription = "Mail",
                modifier = Modifier.size(24.dp)
            )
        },
        headlineContent = { Text(text = "Contact") },
        supportingContent = { Text(text = "Send suggestions, feedback, etc. to slugmenudev@gmail.com") },
        modifier = Modifier.clickable {
            val mail = Uri.parse("mailto:slugmenudev@gmail.com")
            val intent = Intent(Intent.ACTION_SENDTO, mail)
            context.startActivity(intent)
            /*
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
             */

        }
    )
}