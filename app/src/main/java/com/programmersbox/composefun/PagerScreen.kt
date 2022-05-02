package com.programmersbox.composefun

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerScreen(navController: NavController) {
    ScaffoldTop(
        screen = Screen.PagerScreen,
        navController = navController,
    ) { p ->
        val pager = rememberPagerState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
        ) {
            HorizontalPager(
                count = 5,
                state = pager,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) { Text("Hello $it", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center) }

            HorizontalPagerIndicator(
                pagerState = pager,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
