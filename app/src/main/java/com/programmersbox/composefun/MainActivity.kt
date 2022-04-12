package com.programmersbox.composefun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.programmersbox.composefun.ui.theme.ComposeFunTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeFunTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // A surface container using the 'background' color from the theme
                    val airBar = rememberAirBarController(45.0)
                    val airBar2 = rememberAirBarController(90.0, isHorizontal = true)
                    val airBar3 = rememberAirBarController(40.0, isHorizontal = true)
                    val airBar4 = rememberAirBarController(40.0)
                    var progress by remember { mutableStateOf(35f) }

                    var randomNumber by remember { mutableStateOf(50.0) }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${String.format("%.2f", animateFloatAsState(targetValue = airBar.progress.toFloat()).value)}%")
                            Text("${String.format("%.2f", animateFloatAsState(targetValue = airBar2.progress.toFloat()).value)}%")
                            Text("${String.format("%.2f", progress)}%")
                        }
                        val p = MaterialTheme.colors.primary
                        val back = Color.Blue

                        var primaryColor by remember { mutableStateOf(p) }
                        var backgroundColor by remember { mutableStateOf(back) }

                        val primary by animateColorAsState(primaryColor)
                        val background by animateColorAsState(backgroundColor)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Button(
                                onClick = {
                                    airBar.progress = randomNumber
                                    airBar2.progress = randomNumber
                                    airBar3.progress = randomNumber
                                    airBar4.progress = randomNumber
                                    progress = randomNumber.toFloat()
                                    randomNumber = Random.nextDouble(0.0, 100.0)
                                }
                            ) { Text("Set to ${String.format("%.2f", randomNumber).toDouble()}%") }

                            Text("Animate")
                            Switch(
                                checked = airBar.animateProgress && airBar2.animateProgress,
                                onCheckedChange = {
                                    airBar.animateProgress = it
                                    airBar2.animateProgress = it
                                }
                            )

                            Button(
                                onClick = {
                                    primaryColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 255)
                                    backgroundColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 255)
                                }
                            ) { Text("Random Color") }

                        }

                        AirBar(
                            controller = airBar,
                            modifier = Modifier
                                .height(200.dp)
                                .width(80.dp),
                            fillColor = primary,
                            backgroundColor = background,
                            icon = { Icon(Icons.Default.Refresh, null) }
                        ) { airBar.progress = it }

                        Row {
                            Column {
                                AirBar(
                                    controller = airBar2,
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(80.dp),
                                    backgroundColor = background,
                                    fillColor = primary,
                                    icon = { Icon(Icons.Default.Refresh, null) }
                                ) { airBar2.progress = it }

                                AirBar(
                                    controller = airBar2,
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(80.dp),
                                    backgroundColor = primary,
                                    fillColor = background,
                                    icon = { Icon(Icons.Default.Refresh, null) }
                                ) { airBar2.progress = it }
                            }

                            AirBar(
                                controller = airBar3,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(horizontal = 4.dp)
                                    .height(160.dp),
                                backgroundColor = background,
                                fillColor = primary
                            )

                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AirBar(
                                if (airBar2.animateProgress && airBar.animateProgress)
                                    animateFloatAsState(targetValue = airBar.progress.toFloat()).value
                                else
                                    airBar.progress.toFloat(),
                                modifier = Modifier
                                    .height(200.dp)
                                    .width(80.dp),
                                backgroundColor = primary,
                                fillColor = background,
                                icon = { Icon(Icons.Default.Refresh, null) }
                            ) { airBar.progress = it.toDouble() }

                            AirBar(
                                if (airBar2.animateProgress && airBar.animateProgress)
                                    animateFloatAsState(targetValue = progress).value
                                else
                                    progress,
                                modifier = Modifier
                                    .height(200.dp)
                                    .width(80.dp),
                                backgroundColor = primary,
                                fillColor = background,
                                icon = { Text("${String.format("%.2f", progress)}%") }
                            ) { progress = it }

                            AirBar(
                                controller = airBar4,
                                backgroundColor = background,
                                fillColor = primary,
                                modifier = Modifier
                                    .height(200.dp)
                                    .width(80.dp)
                            )

                            AirBar(
                                controller = rememberAirBarController(40.0),
                                backgroundColor = background,
                                fillColor = primary
                            )
                        }
                    }
                }
            }
        }
    }
}
