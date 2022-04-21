package com.programmersbox.composefun

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layoutId
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.navigation.NavController

@OptIn(ExperimentalMotionApi::class)
@Composable
fun MotionScreen(navController: NavController) {

    var switch by remember { mutableStateOf(false) }

    var progress by remember(switch) { mutableStateOf(if (switch) 1f else 0f) }

    ScaffoldTop(
        screen = Screen.MotionScreen,
        navController = navController,
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { switch = !switch },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Change Layout") }
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

            Slider(
                value = progress,
                onValueChange = { progress = it },
                modifier = Modifier.layoutId("slider")
            )

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
        bottom.linkTo(parent.bottom)
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
        bottom.linkTo(parent.bottom)
    }
}