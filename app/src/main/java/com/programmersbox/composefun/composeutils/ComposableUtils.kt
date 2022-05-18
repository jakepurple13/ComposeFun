package com.programmersbox.composefun.composeutils

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.slider.ColorfulSlider
import com.smarttoolfactory.slider.MaterialSliderDefaults
import com.smarttoolfactory.slider.SliderBrushColor
import kotlin.math.roundToInt

/**
 * [CheckeredColorfulSlider] that displays [red] change in
 * [RGB](https://en.wikipedia.org/wiki/RGB_color_model) color model.
 * @param red in [0..1f]
 * @param onValueChange callback that returns change in [red] when Slider is dragged
 */
@Composable
fun SliderRedRGB(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0) red: Float,
    onValueChange: (Float) -> Unit
) {
    val sliderRedGradient = sliderRedGradient()
    CheckeredColorfulSlider(
        modifier = modifier,
        value = red,
        onValueChange = onValueChange,
        brush = sliderRedGradient
    )
}

/**
 * [CheckeredColorfulSlider] that displays [green] change in
 * [RGB](https://en.wikipedia.org/wiki/RGB_color_model) color model.
 * @param green in [0..1f]
 * @param onValueChange callback that returns change in [green] when Slider is dragged
 */
@Composable
fun SliderGreenRGB(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0) green: Float,
    onValueChange: (Float) -> Unit
) {
    val sliderGreenGradient = sliderGreenGradient()
    CheckeredColorfulSlider(
        modifier = modifier,
        value = green,
        onValueChange = onValueChange,
        brush = sliderGreenGradient
    )
}

/**
 * [CheckeredColorfulSlider] that displays [blue] change in
 * [RGB](https://en.wikipedia.org/wiki/RGB_color_model) color model.
 * @param blue in [0..1f]
 * @param onValueChange callback that returns change in [blue] when Slider is dragged
 */
@Composable
fun SliderBlueRGB(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0) blue: Float,
    onValueChange: (Float) -> Unit
) {
    val sliderBlueGradient = sliderBlueGradient()
    CheckeredColorfulSlider(
        modifier = modifier,
        value = blue,
        onValueChange = onValueChange,
        brush = sliderBlueGradient
    )
}

/**
 * [CheckeredColorfulSlider] that displays [alpha] change in
 * [RGB](https://en.wikipedia.org/wiki/RGB_color_model) color model.
 * @param red in [0..1f]
 * @param green in [0..1f]
 * @param blue in [0..1f]
 * @param alpha in [0..1f]
 * @param onValueChange callback that returns change in [alpha] when Slider is dragged
 */
@Composable
fun SliderAlphaRGB(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0) red: Float,
    @FloatRange(from = 0.0, to = 1.0) green: Float,
    @FloatRange(from = 0.0, to = 1.0) blue: Float,
    @FloatRange(from = 0.0, to = 1.0) alpha: Float,
    onValueChange: (Float) -> Unit
) {
    val sliderAlphaRGBGradient = sliderAlphaRGBGradient(red, green, blue)
    CheckeredColorfulSlider(
        modifier = modifier,
        value = alpha,
        onValueChange = onValueChange,
        brush = sliderAlphaRGBGradient,
        drawChecker = true
    )
}

/**
 * Slider implementation that uses [ColorfulSlider] to have create **Slider** with gradient
 * colors with custom thumb radius and track height, and draws checker pattern behind the
 * track of [ColorfulSlider] if [drawChecker] is set to true
 * @param value that is read by [ColorfulSlider]
 * @param valueRange is the values are selected from
 * @param onValueChange is triggered when slider is dragged returns float between [valueRange]
 * @param brush is used for drawing gradient for track of [ColorfulSlider]
 * @param drawChecker when set to true draws checker behind track of [ColorfulSlider] to
 * display alpha
 */
@Composable
fun CheckeredColorfulSlider(
    modifier: Modifier = Modifier,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit,
    brush: Brush,
    drawChecker: Boolean = false
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        if (drawChecker) {
            Box(
                modifier = Modifier
                    .width(maxWidth)
                    .height(12.dp)
                    .drawChecker(shape = RoundedCornerShape(6.dp))
            )
        }

        ColorfulSlider(
            value = value,
            modifier = Modifier,
            thumbRadius = 12.dp,
            trackHeight = 12.dp,
            onValueChange = { value ->
                onValueChange((value * 100.0f).roundToInt() / 100.0f)
            },
            valueRange = valueRange,
            coerceThumbInTrack = true,
            colors = MaterialSliderDefaults.materialColors(
                activeTrackColor = SliderBrushColor(brush = brush),
                inactiveTrackColor = SliderBrushColor(color = Color.Transparent)
            ),
            drawInactiveTrack = false
        )
    }
}

/**
 * Modify element to add checker with appearance specified with a [shape], and [size]
 * and clip it.
 * @param size size of the checker. If size is [DpSize.Unspecified] **10.dp** is used by
 * default.
 * @param shape shape of the checker.
 */
fun Modifier.drawChecker(shape: Shape, size: DpSize = DpSize.Unspecified) = this
    .clip(shape)
    .then(
        drawWithCache {
            this.onDrawBehind {
                val width = this.size.width
                val height = this.size.height

                val checkerWidth =
                    (if (size != DpSize.Unspecified) size.width.toPx() else 10.dp.toPx())
                        .coerceAtMost(width / 2)

                val checkerHeight =
                    (if (size != DpSize.Unspecified) size.height.toPx() else 10.dp.toPx())
                        .coerceAtMost(height / 2)

                val horizontalSteps = (width / checkerWidth).toInt()
                val verticalSteps = (height / checkerHeight).toInt()

                for (y in 0..verticalSteps) {
                    for (x in 0..horizontalSteps) {
                        val isGrayTile = ((x + y) % 2 == 1)
                        drawRect(
                            color = if (isGrayTile) Color.LightGray else Color.White,
                            topLeft = Offset(x * checkerWidth, y * checkerHeight),
                            size = Size(checkerWidth, checkerHeight)
                        )
                    }
                }
            }
        }
    )

fun sliderRedGradient(
    alpha: Float = 1f,
    start: Offset = Offset.Zero,
    end: Offset = Offset.Infinite
): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color.hsl(hue = 0f, saturation = 1f, lightness = 0f, alpha = alpha),
            Color.hsl(hue = 0f, saturation = 1f, lightness = .5f, alpha = alpha)
        ),
        start = start,
        end = end
    )
}

fun sliderGreenGradient(
    alpha: Float = 1f,
    start: Offset = Offset.Zero,
    end: Offset = Offset.Infinite
): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color.hsl(hue = 120f, saturation = 1f, lightness = 0f, alpha = alpha),
            Color.hsl(hue = 120f, saturation = 1f, lightness = .5f, alpha = alpha)
        ),
        start = start,
        end = end
    )
}

fun sliderBlueGradient(
    alpha: Float = 1f,
    start: Offset = Offset.Zero,
    end: Offset = Offset.Infinite
): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color.hsl(hue = 240f, saturation = 1f, lightness = 0f, alpha = alpha),
            Color.hsl(hue = 240f, saturation = 1f, lightness = .5f, alpha = alpha)
        ),
        start = start,
        end = end
    )
}

fun sliderAlphaRGBGradient(
    red: Float,
    green: Float,
    blue: Float,
    start: Offset = Offset.Zero,
    end: Offset = Offset.Infinite
): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color(red, green, blue, 0f),
            Color(red, green, blue, 1f)
        ),
        start = start,
        end = end
    )
}