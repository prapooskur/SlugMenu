package com.pras.slugmenu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabPosition
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun tabsWithSwiping() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Hello", "There", "World")
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(tabIndex) // 2.
    val scope = rememberCoroutineScope()
    Column {
        TabRow(
            selectedTabIndex = tabIndex,
            indicator = { tabPositions -> // 3.
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(
                        pagerState,
                        tabPositions
                    )
                )
            }) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index; scope.launch{pagerState.scrollToPage(index)} },
                    text = { Text(text = title)
                    })
            }
        }
        HorizontalPager( // 4.
            pageCount = tabTitles.size,
            state = pagerState,
        ) { tabIndex ->
            Text(
                tabIndex.toString(),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }
    }
}