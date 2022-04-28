package com.programmersbox.composefun

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.navigation.NavController

@OptIn(ExperimentalMotionApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MotionScreen(navController: NavController) {

    var switch by remember { mutableStateOf(false) }

    var progress by remember(switch) { mutableStateOf(if (switch) 1f else 0f) }

    var airBarOrSlider by remember { mutableStateOf(false) }

    val airBar = rememberAirBarController(progress.toDouble() * 100, isHorizontal = true)

    LaunchedEffect(airBarOrSlider, progress) { airBar.progress = progress.toDouble() * 100 }

    ScaffoldTop(
        screen = Screen.MotionScreen,
        navController = navController,
        topBarActions = { Text("${String.format("%.1f", animateFloatAsState(progress).value * 100)}%") },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { switch = !switch },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 1.dp)
                ) { Text("Change Layout") }

                Button(
                    onClick = { airBarOrSlider = !airBarOrSlider },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 1.dp)
                ) { Text("Use ${if (airBarOrSlider) "Slider" else "AirBar"}") }
            }
        }
    ) { p ->
        MotionLayout(
            start = layoutOne(),
            end = layoutTwo(),
            progress = animateFloatAsState(progress).value,
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
        ) {

            Button(
                onClick = { progress = .5f },
                modifier = Modifier
                    .layoutId("button")
                    .rotate(360f * animateFloatAsState(progress).value)
            ) { Text("Button") }

            Text("Text", Modifier.layoutId("text"))

            Box(
                modifier = Modifier
                    .layoutId("slider")
                    .height(40.dp)
            ) {
                Crossfade(targetState = airBarOrSlider) { choice ->
                    if (choice) {
                        AirBar(
                            fillColor = MaterialTheme.colors.primary,
                            backgroundColor = MaterialTheme.colors.background,
                            controller = airBar,
                            valueChanged = { progress = it.toFloat() / 100f },
                        )
                    } else {
                        Slider(
                            value = progress,
                            onValueChange = { progress = it }
                        )
                    }
                }
            }
        }
    }
}

fun layoutOne() = ConstraintSet {
    val button = createRefFor("button")
    val text = createRefFor("text")
    val slider = createRefFor("slider")

    constrain(button) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(text.top)
    }

    constrain(text) {
        top.linkTo(button.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(slider.top)
    }

    constrain(slider) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom, margin = 4.dp)
    }
}

fun layoutTwo() = ConstraintSet {
    val button = createRefFor("button")
    val text = createRefFor("text")
    val slider = createRefFor("slider")

    constrain(button) {
        top.linkTo(text.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(slider.top)
    }

    constrain(text) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(button.top)
    }

    constrain(slider) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom, margin = 4.dp)
    }
}