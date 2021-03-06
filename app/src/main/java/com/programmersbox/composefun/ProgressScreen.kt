package com.programmersbox.composefun

import android.content.res.Configuration
import android.graphics.PointF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathSegment
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.programmersbox.composefun.composeutils.SliderBlueRGB
import com.programmersbox.composefun.composeutils.SliderGreenRGB
import com.programmersbox.composefun.composeutils.SliderRedRGB
import kotlin.random.Random

@Preview(device = Devices.NEXUS_5X, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProgressScreen(navController: NavController = rememberNavController()) {
    M3ScaffoldTop(screen = Screen.ProgressScreen, navController = navController) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(p)
        ) {
            var diamond by remember { mutableStateOf(Random.nextFloat()) }

            var primaryColor by remember { mutableStateOf(Random.nextColor(a = 255)) }
            var backgroundColor by remember { mutableStateOf(Random.nextColor(a = 255)) }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { diamond = Random.nextFloat() }) { Text("Random Progress") }
                Button(
                    onClick = {
                        primaryColor = Random.nextColor(a = 255)
                        backgroundColor = Random.nextColor(a = 255)
                    }
                ) { Text("Random Colors") }
            }

            var showColors by remember { mutableStateOf(false) }

            SwitchSetting(settingTitle = { Text("Edit Colors") }, value = showColors, updateValue = { showColors = it })
            AnimatedVisibility(visible = showColors) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Primary Color")
                    SliderRedRGB(red = primaryColor.red, onValueChange = { primaryColor = primaryColor.copy(red = it) })
                    SliderGreenRGB(green = primaryColor.green, onValueChange = { primaryColor = primaryColor.copy(green = it) })
                    SliderBlueRGB(blue = primaryColor.blue, onValueChange = { primaryColor = primaryColor.copy(blue = it) })
                    Text("Background Color")
                    SliderRedRGB(red = backgroundColor.red, onValueChange = { backgroundColor = backgroundColor.copy(red = it) })
                    SliderGreenRGB(green = backgroundColor.green, onValueChange = { backgroundColor = backgroundColor.copy(green = it) })
                    SliderBlueRGB(blue = backgroundColor.blue, onValueChange = { backgroundColor = backgroundColor.copy(blue = it) })
                }
            }

            Slider(value = diamond, onValueChange = { diamond = it })
            Text("${diamond * 100f}%")

            val primaryColorAnimation by animateColorAsState(targetValue = primaryColor)
            val backgroundColorAnimation by animateColorAsState(targetValue = backgroundColor)
            val diamondProgress by animateFloatAsState(targetValue = diamond, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec)

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {

                DiamondLoader(
                    progress = diamondProgress,
                    progressColor = primaryColorAnimation,
                    emptyColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp)
                )

                CircularProgressIndicator(
                    progress = diamondProgress,
                    color = primaryColorAnimation,
                    modifier = Modifier.size(100.dp)
                )

                CircularProgressIndicator(
                    color = primaryColorAnimation,
                    modifier = Modifier.size(100.dp)
                )

            }

            LinearProgressIndicator(
                color = primaryColorAnimation,
                trackColor = backgroundColorAnimation,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = diamondProgress,
                color = primaryColorAnimation,
                trackColor = backgroundColorAnimation,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                OuterDiamondProgressIndicator(
                    progress = diamondProgress,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp)
                )
                InnerDiamondProgressIndicator(
                    progress = diamondProgress,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp)
                )
                DiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DiamondProgressIndicator(
                    progress = diamondProgress,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp)
                )
            }

            Slider(value = diamond, onValueChange = { diamond = it })
            Text("${diamond * 100f}%")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp),
                    image = ImageBitmap.imageResource(id = android.R.drawable.ic_menu_add)
                )
                DiamondProgressIndicator(
                    progress = diamondProgress,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp),
                    image = ImageBitmap.imageResource(id = android.R.drawable.ic_menu_add)
                )
                DiamondLoader(
                    progress = diamondProgress,
                    progressColor = primaryColorAnimation,
                    emptyColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp),
                    image = ImageBitmap.imageResource(id = android.R.drawable.ic_menu_add)
                )
            }

            Slider(value = diamond, onValueChange = { diamond = it })
            Text("${diamond * 100f}%")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                OuterDiamondProgressIndicator(
                    progress = diamondProgress,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp),
                    image = ImageBitmap.imageResource(id = android.R.drawable.ic_menu_add)
                )
                OuterDiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                InnerDiamondProgressIndicator(
                    progress = diamondProgress,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp),
                    image = ImageBitmap.imageResource(id = android.R.drawable.ic_menu_add)
                )
                InnerDiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DiamondProgressIndicator(
                    progress = diamondProgress,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp),
                    image = ImageBitmap.imageResource(id = android.R.drawable.ic_menu_add)
                )
                DiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ReverseDiamondProgressIndicator(
                    progress = diamondProgress,
                    reverse = true,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    modifier = Modifier.size(100.dp)
                )
                ReverseDiamondProgressIndicator(
                    progress = diamondProgress,
                    reverse = false,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    modifier = Modifier.size(100.dp)
                )
            }

            Slider(value = diamond, onValueChange = { diamond = it })
            Text("${diamond * 100f}%")

            var sizeOfLoaders by remember { mutableStateOf(100f) }
            Text("Size (Default 100): ${sizeOfLoaders.toInt()}")
            Slider(
                value = sizeOfLoaders,
                onValueChange = { sizeOfLoaders = it },
                valueRange = 25f..200f
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(animateDpAsState(targetValue = sizeOfLoaders.dp).value)
                )
                DiamondProgressIndicator(
                    progress = diamondProgress,
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(animateDpAsState(targetValue = sizeOfLoaders.dp).value)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(200.dp),
                    animationSpec = keyframes {
                        durationMillis = (1332 * 0.5).toInt() * 2 * 2
                        0f at (1332 * 0.5).toInt() * 2 with CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                        200f at durationMillis
                    }
                )
                DiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(200.dp),
                    animationSpec = keyframes {
                        durationMillis = (1332 * 0.5).toInt() * 2
                        0f at (1332 * 0.5).toInt() * 2 with CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                        200f at durationMillis
                    }
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp),
                    animationSpec = keyframes {
                        durationMillis = (1332 * 0.5).toInt() * 2 * 2
                        0f at (1332 * 0.5).toInt() with CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                        200f at durationMillis
                    }
                )

                DiamondProgressIndicator(
                    innerColor = primaryColorAnimation,
                    outerColor = backgroundColorAnimation,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(100.dp),
                    animationSpec = tween(1500)
                )
            }
        }
    }
}

@Composable
fun InnerDiamondProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    innerColor: Color = MaterialTheme.colorScheme.primary,
    outerColor: Color = MaterialTheme.colorScheme.background,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null
) {
    DiamondProgress(modifier.progressSemantics(progress), strokeWidth, image) {
        val halfHeight = it.halfHeight
        val halfWidth = it.halfWidth
        drawRhombus(
            x = halfWidth,
            y = halfHeight,
            width = halfWidth - it.strokeWidth,
            height = halfHeight - it.strokeWidth,
            paint = outerColor,
            stroke = it.stroke
        )
        drawProgress(
            progress * 100f,
            x = halfWidth,
            y = halfHeight,
            width = halfWidth - it.strokeWidth,
            height = halfHeight - it.strokeWidth,
            paint = innerColor,
            stroke = it.stroke
        )
    }
}

@Composable
fun InnerDiamondProgressIndicator(
    modifier: Modifier = Modifier,
    innerColor: Color = MaterialTheme.colorScheme.primary,
    outerColor: Color = MaterialTheme.colorScheme.background,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null,
    animationSpec: DurationBasedAnimationSpec<Float> = keyframes {
        durationMillis = (1332 * 0.5).toInt() * 2 * 2
        0f at (1332 * 0.5).toInt() with CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
        200f at durationMillis
    }
) {
    DiamondProgressIndeterminate(modifier, strokeWidth, image, animationSpec) { it, startAngle ->
        val halfHeight = it.halfHeight
        val halfWidth = it.halfWidth

        drawRhombus(
            x = halfWidth,
            y = halfHeight,
            width = halfWidth - it.strokeWidth,
            height = halfHeight - it.strokeWidth,
            paint = outerColor,
            stroke = it.stroke
        )
        val naturalValue = startAngle % 100f
        if (startAngle >= 100f) {
            drawProgressIndeterminateReverse(
                progress = naturalValue,
                x = halfWidth,
                y = halfHeight,
                width = halfWidth - it.strokeWidth,
                height = halfHeight - it.strokeWidth,
                paint = innerColor,
                stroke = it.stroke
            )
        } else {
            drawProgressIndeterminate(
                progress = 100f - naturalValue,
                x = halfWidth,
                y = halfHeight,
                width = halfWidth - it.strokeWidth,
                height = halfHeight - it.strokeWidth,
                paint = innerColor,
                stroke = it.stroke
            )
        }
    }
}

@Composable
fun ReverseDiamondProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    reverse: Boolean = true,
    innerColor: Color = MaterialTheme.colorScheme.primary,
    outerColor: Color = MaterialTheme.colorScheme.background,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null
) {
    DiamondProgress(modifier.progressSemantics(progress), strokeWidth, image) {
        val (width, height) = it.width to it.height
        val halfHeight = it.halfHeight
        val halfWidth = it.halfWidth

        val naturalValue = progress * 100f
        drawProgressIndeterminate(
            progress = if (reverse) 100f - naturalValue else naturalValue,
            x = halfWidth,
            y = halfHeight,
            width = width - it.strokeWidth,
            height = height - it.strokeWidth,
            paint = outerColor,
            stroke = it.stroke
        )
        drawProgressIndeterminate(
            progress = if (!reverse) 100f - naturalValue else naturalValue,
            x = halfWidth,
            y = halfHeight,
            width = halfWidth - it.strokeWidth,
            height = halfHeight - it.strokeWidth,
            paint = innerColor,
            stroke = it.stroke
        )
    }
}

@Composable
fun DiamondProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    innerColor: Color = MaterialTheme.colorScheme.primary,
    outerColor: Color = MaterialTheme.colorScheme.background,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null
) {
    DiamondProgress(modifier.progressSemantics(progress), strokeWidth, image) {
        val (width, height) = it.width to it.height
        val halfHeight = it.halfHeight
        val halfWidth = it.halfWidth

        val naturalValue = progress * 100f
        drawProgressIndeterminate(
            progress = naturalValue,
            x = halfWidth,
            y = halfHeight,
            width = width - it.strokeWidth,
            height = height - it.strokeWidth,
            paint = outerColor,
            stroke = it.stroke
        )
        drawProgressIndeterminate(
            progress = naturalValue,
            x = halfWidth,
            y = halfHeight,
            width = halfWidth - it.strokeWidth,
            height = halfHeight - it.strokeWidth,
            paint = innerColor,
            stroke = it.stroke
        )
    }
}

@Composable
fun DiamondProgressIndicator(
    modifier: Modifier = Modifier,
    innerColor: Color = MaterialTheme.colorScheme.primary,
    outerColor: Color = MaterialTheme.colorScheme.background,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null,
    animationSpec: DurationBasedAnimationSpec<Float> = keyframes {
        durationMillis = (1332 * 0.5).toInt() * 2 * 2
        0f at (1332 * 0.5).toInt() with CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
        200f at durationMillis
    }
) {
    DiamondProgressIndeterminate(modifier, strokeWidth, image, animationSpec) { it, startAngle ->
        val (width, height) = it.width to it.height
        val halfHeight = it.halfHeight
        val halfWidth = it.halfWidth

        val naturalValue = startAngle % 100f
        if (startAngle >= 100f) {
            drawProgressIndeterminateReverse(
                progress = 100f - naturalValue,
                x = halfWidth,
                y = halfHeight,
                width = width - it.strokeWidth,
                height = height - it.strokeWidth,
                paint = outerColor,
                stroke = it.stroke
            )
            drawProgressIndeterminateReverse(
                progress = naturalValue,
                x = halfWidth,
                y = halfHeight,
                width = halfWidth - it.strokeWidth,
                height = halfHeight - it.strokeWidth,
                paint = innerColor,
                stroke = it.stroke
            )
        } else {
            drawProgressIndeterminate(
                progress = naturalValue,
                x = halfWidth,
                y = halfHeight,
                width = width - it.strokeWidth,
                height = height - it.strokeWidth,
                paint = outerColor,
                stroke = it.stroke
            )
            drawProgressIndeterminate(
                progress = 100f - naturalValue,
                x = halfWidth,
                y = halfHeight,
                width = halfWidth - it.strokeWidth,
                height = halfHeight - it.strokeWidth,
                paint = innerColor,
                stroke = it.stroke
            )
        }
    }
}

@Composable
fun OuterDiamondProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    innerColor: Color = MaterialTheme.colorScheme.primary,
    outerColor: Color = MaterialTheme.colorScheme.background,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null
) {
    DiamondProgress(modifier.progressSemantics(progress), strokeWidth, image) {
        val (width, height) = it.width to it.height
        val halfHeight = it.halfHeight
        val halfWidth = it.halfWidth
        drawProgress(
            100f,
            x = halfWidth,
            y = halfHeight,
            width = halfWidth - it.strokeWidth,
            height = halfHeight - it.strokeWidth,
            paint = outerColor,
            stroke = it.stroke
        )
        drawProgress(
            progress * 100f,
            x = halfWidth,
            y = halfHeight,
            width = width - it.strokeWidth,
            height = height - it.strokeWidth,
            paint = innerColor,
            stroke = it.stroke
        )
    }
}

@Composable
fun OuterDiamondProgressIndicator(
    modifier: Modifier = Modifier,
    innerColor: Color = MaterialTheme.colorScheme.primary,
    outerColor: Color = MaterialTheme.colorScheme.background,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null,
    animationSpec: DurationBasedAnimationSpec<Float> = keyframes {
        durationMillis = (1332 * 0.5).toInt() * 2 * 2
        0f at (1332 * 0.5).toInt() with CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
        200f at durationMillis
    }
) {
    DiamondProgressIndeterminate(modifier, strokeWidth, image, animationSpec) { it, startAngle ->
        val (width, height) = it.width to it.height
        val halfHeight = it.halfHeight
        val halfWidth = it.halfWidth
        drawProgress(
            100f,
            x = halfWidth,
            y = halfHeight,
            width = halfWidth - it.strokeWidth,
            height = halfHeight - it.strokeWidth,
            paint = outerColor,
            stroke = it.stroke
        )
        val naturalValue = startAngle % 100f
        if (startAngle >= 100f) {
            drawProgressIndeterminateReverse(
                progress = 100f - naturalValue,
                x = halfWidth,
                y = halfHeight,
                width = width - it.strokeWidth,
                height = height - it.strokeWidth,
                paint = innerColor,
                stroke = it.stroke
            )
        } else {
            drawProgressIndeterminate(
                progress = naturalValue,
                x = halfWidth,
                y = halfHeight,
                width = width - it.strokeWidth,
                height = height - it.strokeWidth,
                paint = innerColor,
                stroke = it.stroke
            )
        }
    }
}

private class DiamondData(
    val strokeWidth: Float,
    val width: Float,
    val height: Float,
    val stroke: Stroke
) {
    val halfWidth by lazy { width / 2f }
    val halfHeight by lazy { height / 2f }
}

@Composable
private fun DiamondProgress(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null,
    block: DrawScope.(DiamondData) -> Unit
) {
    val imagePaint = newStrokePaint(strokeWidth.value)
    val stroke = with(LocalDensity.current) { Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt) }
    val loadingWidthChange = strokeWidth.value

    Canvas(
        Modifier
            .size(100.dp, 100.dp)
            .then(modifier)
    ) {
        val (width, height) = size
        val halfHeight = width / 2f
        val halfWidth = height / 2f

        drawContext.canvas.withSaveLayer(bounds = drawContext.size.toRect(), paint = Paint()) {
            image?.let { addImage(it, halfWidth, halfHeight, halfWidth, halfHeight, imagePaint) }
            block(DiamondData(loadingWidthChange, width, height, stroke))
        }
    }
}

@Composable
private fun DiamondProgressIndeterminate(
    modifier: Modifier,
    strokeWidth: Dp,
    image: ImageBitmap?,
    animationSpec: DurationBasedAnimationSpec<Float>,
    block: DrawScope.(DiamondData, Float) -> Unit
) {
    val transition = rememberInfiniteTransition()
    val startAngle = transition.animateFloat(
        0f,
        200f,
        infiniteRepeatable(animation = animationSpec)
    )

    DiamondProgress(modifier.progressSemantics(), strokeWidth, image) { block(it, startAngle.value) }
}

@Composable
fun DiamondLoader(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    emptyColor: Color = MaterialTheme.colorScheme.background,
    strokeWidth: Dp = 4.dp,
    image: ImageBitmap? = null
) {
    //size == 50
    //strokeWidth == 4.dp
    val imagePaint = newStrokePaint(strokeWidth.value)
    val emptyStroke = with(LocalDensity.current) { Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt) }
    val progressStroke = with(LocalDensity.current) { Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt) }
    val loadingWidthChange = strokeWidth.value// / 2
    Canvas(modifier.progressSemantics(progress * 100f)) {
        val (width, height) = size
        val halfHeight = width / 2f
        val halfWidth = height / 2f

        //val arcDimen = size.width - 2 * loadingWidthChange

        drawContext.canvas.withSaveLayer(bounds = drawContext.size.toRect(), paint = Paint()) {
            image?.let { addImage(it, halfWidth, halfHeight, halfWidth, halfHeight, imagePaint) }
            drawRhombus(
                x = halfWidth,
                y = halfHeight,
                width = halfWidth - loadingWidthChange,
                height = halfHeight - loadingWidthChange,
                paint = emptyColor,
                stroke = emptyStroke
            )
            drawProgress2(
                progress * 100f,
                x = halfWidth,
                y = halfHeight,
                width = width - loadingWidthChange,
                height = height - loadingWidthChange,
                paint = progressColor,
                stroke = progressStroke
            )
        }
    }
}

private fun newStrokePaint(width: Float) = Stroke(
    width = width * 2,
    cap = StrokeCap.Butt
)

private fun DrawScope.drawRhombus(x: Float, y: Float, width: Float, height: Float, paint: Color, stroke: Stroke) {
    val path = Path()
    path.moveTo(x, y + height) // Top
    path.lineTo(x - width, y) // Left
    path.lineTo(x, y - height) // Bottom
    path.lineTo(x + width, y) // Right
    path.lineTo(x, y + height) // Back to Top
    path.close()
    drawPath(path, paint, style = stroke)
    path.reset()
}

private fun DrawScope.addImage(imageAsset: ImageBitmap, x: Float, y: Float, width: Float, height: Float, stroke: Stroke) {
    val path = Path()
    path.moveTo(x, y + height) // Top
    path.lineTo(x - width, y) // Left
    path.lineTo(x, y - height) // Bottom
    path.lineTo(x + width, y) // Right
    path.lineTo(x, y + height) // Back to Top
    path.close()
    clipPath(path) { drawImage(imageAsset, topLeft = Offset(x - imageAsset.width / 2, y - imageAsset.height / 2), style = stroke) }
    path.reset()
}

private fun PathSegment.toPoint(progress: Float, max: Int, next: Int): PointF {
    val fProg = if (progress >= max) 1f else {
        (progress - (next * 25)) / 25f
    }

    return PointF(
        start.x + fProg * (end.x - start.x),
        start.y + fProg * (end.y - start.y)
    )
}

private fun DrawScope.drawProgress(progress: Float, x: Float, y: Float, width: Float, height: Float, paint: Color, stroke: Stroke) {

    val halfWidth = width / 2
    val halfHeight = height / 2
    val path = Path()
    path.moveTo(x, y - halfHeight)

    //top to right
    if (progress > 0) {
        val pathSegment = PathSegment(
            PointF(x, y - halfHeight), 0f,
            PointF(x + halfWidth, y), 1f
        )
        val p2 = pathSegment.toPoint(progress, 25, 0)
        path.lineTo(p2.x, p2.y)
    }

    //right to bottom
    if (progress > 25) {
        val pathSegment = PathSegment(
            PointF(x + halfWidth, y), 0f,
            PointF(x, y + halfHeight), 1f
        )
        val p2 = pathSegment.toPoint(progress, 50, 1)
        path.lineTo(p2.x, p2.y)
    }

    //bottom to left
    if (progress > 50) {
        val pathSegment = PathSegment(
            PointF(x, y + halfHeight), 0f,
            PointF(x - halfWidth, y), 1f
        )
        val p2 = pathSegment.toPoint(progress, 75, 2)
        path.lineTo(p2.x, p2.y)
    }

    //left to top
    if (progress > 75) {
        val pathSegment = PathSegment(
            PointF(x - halfWidth, y), 0f,
            PointF(x, y - halfHeight), 1f
        )
        val p2 = pathSegment.toPoint(progress, 100, 3)
        path.lineTo(p2.x, p2.y)
    }

    //finished!
    if (progress >= 100) {
        path.close()
    }

    drawPath(path, paint, style = stroke)
    path.reset()
}

private fun DrawScope.drawProgressIndeterminate(
    progress: Float,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    paint: Color,
    stroke: Stroke
) {

    val halfWidth = width / 2
    val halfHeight = height / 2
    val path = Path()
    path.moveTo(x, y - halfHeight)

    //top to right
    if (progress > 0) {
        val pathSegment = PathSegment(
            PointF(x, y - halfHeight), 0f,
            PointF(x + halfWidth, y), 1f
        )
        val p2 = pathSegment.toPoint(progress, 25, 0)
        path.lineTo(p2.x, p2.y)
    }

    //right to bottom
    if (progress > 25) {
        val pathSegment = PathSegment(
            PointF(x + halfWidth, y), 0f,
            PointF(x, y + halfHeight), 1f
        )
        val p2 = pathSegment.toPoint(progress, 50, 1)
        path.lineTo(p2.x, p2.y)
    }

    //bottom to left
    if (progress > 50) {
        val pathSegment = PathSegment(
            PointF(x, y + halfHeight), 0f,
            PointF(x - halfWidth, y), 1f
        )
        val p2 = pathSegment.toPoint(progress, 75, 2)
        path.lineTo(p2.x, p2.y)
    }

    //left to top
    if (progress > 75) {
        val pathSegment = PathSegment(
            PointF(x - halfWidth, y), 0f,
            PointF(x, y - halfHeight), 1f
        )
        val p2 = pathSegment.toPoint(progress, 100, 3)
        path.lineTo(p2.x, p2.y)
    }

    //finished!
    if (progress >= 100) {
        path.close()
    }

    drawPath(path, paint, style = stroke)
    path.reset()
}

private fun DrawScope.drawProgressIndeterminateReverse(
    progress: Float,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    paint: Color,
    stroke: Stroke
) {

    val halfWidth = width / 2
    val halfHeight = height / 2
    val path = Path()
    path.moveTo(x, y - halfHeight)

    //top to left
    if (progress > 0) {
        val pathSegment = PathSegment(
            PointF(x, y - halfHeight), 0f,
            PointF(x - halfWidth, y), 1f
        )
        val p2 = pathSegment.toPoint(progress, 25, 0)
        path.lineTo(p2.x, p2.y)
    }

    //left to bottom
    if (progress > 25) {
        val pathSegment = PathSegment(
            PointF(x - halfWidth, y), 0f,
            PointF(x, y + halfHeight), 1f
        )
        val p2 = pathSegment.toPoint(progress, 50, 1)
        path.lineTo(p2.x, p2.y)
    }

    //bottom to right
    if (progress > 50) {
        val pathSegment = PathSegment(
            PointF(x, y + halfHeight), 0f,
            PointF(x + halfWidth, y), 1f
        )
        val p2 = pathSegment.toPoint(progress, 75, 2)
        path.lineTo(p2.x, p2.y)
    }

    //right to top
    if (progress > 75) {
        val pathSegment = PathSegment(
            PointF(x + halfWidth, y), 0f,
            PointF(x, y - halfHeight), 1f
        )
        val p2 = pathSegment.toPoint(progress, 100, 3)
        path.lineTo(p2.x, p2.y)
    }

    //finished!
    if (progress >= 100) {
        path.close()
    }

    drawPath(path, paint, style = stroke)
    path.reset()
}

private fun DrawScope.drawProgress2(progress: Float, x: Float, y: Float, width: Float, height: Float, paint: Color, stroke: Stroke) {

    val halfWidth = width / 2
    val halfHeight = height / 2
    val path = Path()
    path.moveTo(x, y - halfHeight)

    //top to right
    if (progress > 0) {
        val pathSegment = PathSegment(
            PointF(x, y - halfHeight), 0f,
            PointF(x + halfWidth, y), 1f
        )
        val p2 = pathSegment.toPoint(progress, 25, 0)
        path.lineTo(p2.x, p2.y)
    }

    //right to bottom
    if (progress > 25) {
        val pathSegment = PathSegment(
            PointF(x + halfWidth, y), 0f,
            PointF(x, y + halfHeight), 1f
        )
        val p2 = pathSegment.toPoint(progress, 50, 1)
        path.lineTo(p2.x, p2.y)
    }

    //bottom to left
    if (progress > 50) {
        val pathSegment = PathSegment(
            PointF(x, y + halfHeight), 0f,
            PointF(x - halfWidth, y), 1f
        )
        val p2 = pathSegment.toPoint(progress, 75, 2)
        path.lineTo(p2.x, p2.y)
    }

    //left to top
    if (progress > 75) {
        val pathSegment = PathSegment(
            PointF(x - halfWidth, y), 0f,
            PointF(x, y - halfHeight), 1f
        )
        val p2 = pathSegment.toPoint(progress, 100, 3)
        path.lineTo(p2.x, p2.y)
    }

    //finished!
    if (progress >= 100) {
        path.close()
    }

    drawPath(path, paint, style = stroke)
    path.reset()
}