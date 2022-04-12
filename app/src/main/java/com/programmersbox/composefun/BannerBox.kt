package com.programmersbox.composefun

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun BannerBox(
    showBanner: Boolean,
    modifier: Modifier = Modifier,
    bannerEnter: EnterTransition = slideInVertically(
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearOutSlowInEasing
        )
    ) { -it },
    bannerExit: ExitTransition = slideOutVertically(
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearOutSlowInEasing
        )
    ) { -it },
    banner: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        content()
        AnimatedVisibility(
            visible = showBanner,
            enter = bannerEnter,
            exit = bannerExit,
        ) { banner() }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun BannerBoxScreen(navController: NavController = rememberNavController()) {
    var showBanner by remember { mutableStateOf(false) }
    ScaffoldTop(
        screen = Screen.BannerBoxScreen,
        navController = navController,
        bottomBar = {
            BottomAppBar {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxSize()
                        .combineClickable(onLongPress = { showBanner = it == ComponentState.Pressed })
                ) { Text("Press and hold to see Banner", textAlign = TextAlign.Center) }
            }
        }
    ) { p ->
        BannerBox(
            showBanner = showBanner,
            modifier = Modifier.padding(p),
            banner = {
                Card(modifier = Modifier.align(Alignment.TopCenter)) {
                    ListItem(
                        overlineText = { Text("Overline") },
                        text = { Text("Text") },
                        secondaryText = { Text("Secondary") }
                    )
                }
            }
        ) { Text("Hello", modifier = Modifier.align(Alignment.Center)) }
    }
}