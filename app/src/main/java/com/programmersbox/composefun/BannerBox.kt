package com.programmersbox.composefun

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    banner: @Composable AnimatedVisibilityScope.() -> Unit,
    bannerModifier: BoxScope.() -> Modifier = { Modifier },
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
            modifier = bannerModifier(),
            content = banner
        )
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

        var topOrBottom by remember { mutableStateOf(true) }
        BannerBox(
            showBanner = showBanner,
            modifier = Modifier.padding(p),
            bannerModifier = { Modifier.align(if (topOrBottom) Alignment.TopCenter else Alignment.BottomCenter) },
            banner = {
                Card {
                    ListItem(
                        overlineText = { Text("Overline") },
                        text = { Text("Text") },
                        secondaryText = { Text("Secondary") }
                    )
                }
            },
            bannerEnter = slideInVertically(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearOutSlowInEasing
                )
            ) { if (topOrBottom) -it else it },
            bannerExit = slideOutVertically(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearOutSlowInEasing
                )
            ) { if (topOrBottom) -it else it },
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .toggleable(topOrBottom) { topOrBottom = it }
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(100.dp)
            ) {
                ListItem(
                    text = { Text(if (topOrBottom) "Top" else "Bottom") },
                    trailing = { Switch(checked = topOrBottom, onCheckedChange = null) }
                )
            }
        }
    }
}