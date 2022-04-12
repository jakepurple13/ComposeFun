package com.programmersbox.composefun

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlin.random.Random

class AirBarController(
    progress: Double = 0.0,
    progressCoordinates: Float = 0f,
    internal val isHorizontal: Boolean = false,
    animateProgress: Boolean = true
) {

    var animateProgress by mutableStateOf(animateProgress)
    internal var internalProgress by mutableStateOf(progress)
    internal var progressCoordinates by mutableStateOf(progressCoordinates)

    var progress: Double
        get() = internalProgress
        set(value) {
            progressCoordinates = reverseCalculateValues(value).toFloat()
            internalProgress = value
        }

    internal var bottomY = 0f
    internal var rightX = 0f

    private fun reverseCalculateValues(realPercentage: Double): Double {
        val p = if (isHorizontal)
            realPercentage * rightX / 100
        else
            bottomY - realPercentage * bottomY / 100

        return String.format("%.2f", p).toDouble()
    }

}

@Composable
fun rememberAirBarController(
    progress: Double = 0.0,
    progressCoordinates: Float = 0f,
    isHorizontal: Boolean = false,
    animateProgress: Boolean = true
) = remember { AirBarController(progress, progressCoordinates, isHorizontal, animateProgress) }

@ExperimentalComposeUiApi
@Composable
fun AirBar(
    modifier: Modifier = Modifier,
    controller: AirBarController = rememberAirBarController(),
    fillColor: Color = MaterialTheme.colors.primary,
    fillColorGradient: List<Color>? = null,
    backgroundColor: Color = MaterialTheme.colors.background,
    cornerRadius: CornerRadius = CornerRadius(x = 40.dp.value, y = 40.dp.value),
    minValue: Double = 0.0,
    maxValue: Double = 100.0,
    icon: (@Composable () -> Unit)? = null,
    valueChanged: (Double) -> Unit = { controller.progress = it }
) {

    fun calculateValues(touchY: Float, touchX: Float): Double {
        val rawPercentage = if (controller.isHorizontal) {
            String.format("%.2f", (touchX.toDouble() / controller.rightX.toDouble()) * 100).toDouble()
        } else {
            String.format("%.2f", 100 - ((touchY.toDouble() / controller.bottomY.toDouble()) * 100)).toDouble()
        }

        val percentage = if (rawPercentage < 0) 0.0 else if (rawPercentage > 100) 100.0 else rawPercentage

        return String.format("%.2f", ((percentage / 100) * (maxValue - minValue) + minValue)).toDouble()
    }

    val vertical = if (controller.isHorizontal) 0f else controller.progressCoordinates
    val horizontal = if (controller.isHorizontal) controller.progressCoordinates else controller.rightX

    val bottomToTop = if (controller.animateProgress) animateFloatAsState(vertical).value else vertical
    val startToEnd = if (controller.animateProgress) animateFloatAsState(horizontal).value else horizontal

    Box(modifier = modifier, contentAlignment = if (controller.isHorizontal) Alignment.CenterStart else Alignment.BottomCenter) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { event ->
                if (!controller.isHorizontal) {
                    when {
                        event.y in 0.0..controller.bottomY.toDouble() -> {
                            valueChanged(calculateValues(event.y, event.x))
                            true
                        }
                        event.y > 100 -> {
                            valueChanged(calculateValues(event.y, event.x))
                            true
                        }
                        event.y < 0 -> {
                            valueChanged(calculateValues(event.y, event.x))
                            true
                        }
                        else -> false
                    }
                } else {
                    when {
                        event.x in 0.0..controller.rightX.toDouble() -> {
                            valueChanged(calculateValues(event.y, event.x))
                            true
                        }
                        event.x > 100 -> {
                            valueChanged(calculateValues(event.y, event.x))
                            true
                        }
                        event.x < 0 -> {
                            valueChanged(calculateValues(event.y, event.x))
                            true
                        }
                        else -> false
                    }
                }
            }) {
            controller.bottomY = size.height
            controller.rightX = size.width
            if (controller.internalProgress > 0.0) controller.progress = controller.internalProgress

            val path = Path()
            path.addRoundRect(
                roundRect = RoundRect(
                    0F,
                    0F,
                    size.width,
                    size.height,
                    cornerRadius
                )
            )
            drawContext.canvas.drawPath(path, Paint().apply {
                color = backgroundColor
                isAntiAlias = true
            })
            drawContext.canvas.clipPath(path = path, ClipOp.Intersect)
            drawContext.canvas.drawRect(
                0F,
                bottomToTop,
                startToEnd,
                size.height,
                Paint().apply {
                    color = fillColor
                    isAntiAlias = true
                    if (!fillColorGradient.isNullOrEmpty() && fillColorGradient.size > 1) {
                        shader = LinearGradientShader(
                            from = Offset(0f, 0f),
                            to = Offset(size.width, size.height),
                            colors = fillColorGradient
                        )
                    }
                })
        }

        icon?.let { Box(modifier = if (!controller.isHorizontal) Modifier.padding(bottom = 15.dp) else Modifier.padding(start = 15.dp)) { it() } }
    }
}

@ExperimentalComposeUiApi
@Composable
fun AirBar(
    progress: Float,
    modifier: Modifier = Modifier,
    isHorizontal: Boolean = false,
    fillColor: Color = MaterialTheme.colors.primary,
    fillColorGradient: List<Color>? = null,
    backgroundColor: Color = MaterialTheme.colors.background,
    cornerRadius: CornerRadius = CornerRadius(x = 40.dp.value, y = 40.dp.value),
    minValue: Double = 0.0,
    maxValue: Double = 100.0,
    icon: (@Composable () -> Unit)? = null,
    valueChanged: (Float) -> Unit
) {

    var bottomY = 0f
    var rightX = 0f

    fun reverseCalculateValues(realPercentage: Float): Float {
        val p = if (isHorizontal)
            realPercentage * rightX / 100
        else
            bottomY - realPercentage * bottomY / 100

        return String.format("%.2f", p).toFloat()
    }

    fun calculateValues(touchY: Float, touchX: Float): Float {
        val rawPercentage = if (isHorizontal) {
            String.format("%.2f", (touchX.toDouble() / rightX.toDouble()) * 100).toDouble()
        } else {
            String.format("%.2f", 100 - ((touchY.toDouble() / bottomY.toDouble()) * 100)).toDouble()
        }

        val percentage = if (rawPercentage < 0) 0.0 else if (rawPercentage > 100) 100.0 else rawPercentage

        return String.format("%.2f", ((percentage / 100) * (maxValue - minValue) + minValue)).toFloat()
    }

    Box(modifier = modifier, contentAlignment = if (isHorizontal) Alignment.CenterStart else Alignment.BottomCenter) {
        Canvas(modifier = modifier.pointerInteropFilter { event ->
            if (!isHorizontal) {
                when {
                    event.y in 0.0..bottomY.toDouble() -> {
                        valueChanged(calculateValues(event.y, event.x))
                        true
                    }
                    event.y > 100 -> {
                        valueChanged(calculateValues(event.y, event.x))
                        true
                    }
                    event.y < 0 -> {
                        valueChanged(calculateValues(event.y, event.x))
                        true
                    }
                    else -> false
                }
            } else {
                when {
                    event.x in 0.0..rightX.toDouble() -> {
                        valueChanged(calculateValues(event.y, event.x))
                        true
                    }
                    event.x > 100 -> {
                        valueChanged(calculateValues(event.y, event.x))
                        true
                    }
                    event.x < 0 -> {
                        valueChanged(calculateValues(event.y, event.x))
                        true
                    }
                    else -> false
                }
            }
        }) {
            bottomY = size.height
            rightX = size.width

            val path = Path()
            path.addRoundRect(
                roundRect = RoundRect(
                    0F,
                    0F,
                    size.width,
                    size.height,
                    cornerRadius
                )
            )
            drawContext.canvas.drawPath(path, Paint().apply {
                color = backgroundColor
                isAntiAlias = true
            })
            drawContext.canvas.clipPath(path = path, ClipOp.Intersect)
            drawContext.canvas.drawRect(
                0F,
                if (isHorizontal) 0f else reverseCalculateValues(progress),
                if (isHorizontal) reverseCalculateValues(progress) else rightX,
                size.height,
                Paint().apply {
                    color = fillColor
                    isAntiAlias = true
                    if (!fillColorGradient.isNullOrEmpty() && fillColorGradient.size > 1) {
                        shader = LinearGradientShader(
                            from = Offset(0f, 0f),
                            to = Offset(size.width, size.height),
                            colors = fillColorGradient
                        )
                    }
                })
        }

        icon?.let { Box(modifier = if (!isHorizontal) Modifier.padding(bottom = 15.dp) else Modifier.padding(start = 15.dp)) { it() } }
    }
}

@ExperimentalComposeUiApi
@Composable
@Preview
fun AirBarLayout(navController: NavController = rememberNavController()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Screen.AirBarScreen.name) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { p ->
        val airBar = rememberAirBarController(45.0)
        val airBar2 = rememberAirBarController(90.0, isHorizontal = true)
        val airBar3 = rememberAirBarController(40.0, isHorizontal = true)
        val airBar4 = rememberAirBarController(40.0)
        var progress by remember { mutableStateOf(35f) }

        var randomNumber by remember { mutableStateOf(50.0) }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(p)
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${String.format("%.2f", animateFloatAsState(targetValue = airBar.progress.toFloat()).value)}%")
                Text("${String.format("%.2f", animateFloatAsState(targetValue = airBar2.progress.toFloat()).value)}%")
                Text("${String.format("%.2f", progress)}%")
            }
            val prim = MaterialTheme.colors.primary
            val back = Color.Blue

            var primaryColor by remember { mutableStateOf(prim) }
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