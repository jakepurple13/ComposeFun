package com.programmersbox.composefun

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlin.random.Random

fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = composed {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparent = color.copy(alpha = 0f).toArgb()
    this.drawBehind {
        this.drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparent

            frameworkPaint.setShadowLayer(
                shadowRadius.toPx(),
                offsetX.toPx(),
                offsetY.toPx(),
                shadowColor
            )
            it.drawRoundRect(
                0f,
                0f,
                this.size.width,
                this.size.height,
                borderRadius.toPx(),
                borderRadius.toPx(),
                paint
            )
        }
    }
}

@Composable
@Preview
fun ShadowScreen(navController: NavController = rememberNavController()) {

    val prim = MaterialTheme.colors.primary
    var color by remember { mutableStateOf(prim) }

    var alpha by remember { mutableStateOf(0.2f) }
    var borderRadius by remember { mutableStateOf(0.dp) }
    var shadowRadius by remember { mutableStateOf(20.dp) }
    var offsetY by remember { mutableStateOf(0.dp) }
    var offsetX by remember { mutableStateOf(0.dp) }

    ScaffoldTop(
        screen = Screen.ShadowScreen,
        navController = navController,
        bottomBar = {

            val elevationOverlay = LocalElevationOverlay.current
            val absoluteElevation = LocalAbsoluteElevation.current + 8.dp
            val backgroundColor = if (MaterialTheme.colors.primarySurface == MaterialTheme.colors.surface && elevationOverlay != null) {
                elevationOverlay.apply(MaterialTheme.colors.primarySurface, absoluteElevation)
            } else {
                MaterialTheme.colors.primarySurface
            }

            Column(
                modifier = Modifier
                    .background(backgroundColor)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = { color = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 255) }
                ) { Text("Random Color: $color") }

                Text("Alpha (Default 20%): ${String.format("%.2f", alpha)}")
                Slider(
                    value = alpha * 100,
                    onValueChange = { alpha = it / 100 },
                    valueRange = 0f..100f
                )

                Text("Border Radius (Default is 0): ${String.format("%.2f", borderRadius.value)}")
                Slider(
                    value = borderRadius.value,
                    onValueChange = { borderRadius = it.dp },
                    valueRange = 0f..100f
                )

                Text("Shadow Radius (Default is 20): ${String.format("%.2f", shadowRadius.value)}")
                Slider(
                    value = shadowRadius.value,
                    onValueChange = { shadowRadius = it.dp },
                    valueRange = 0f..100f
                )

                Text("OffsetX (Default is 0): ${String.format("%.2f", offsetX.value)}")
                Slider(
                    value = offsetX.value,
                    onValueChange = { offsetX = it.dp },
                    valueRange = 0f..100f
                )

                Text("OffsetY (Default is 0): ${String.format("%.2f", offsetY.value)}")
                Slider(
                    value = offsetY.value,
                    onValueChange = { offsetY = it.dp },
                    valueRange = 0f..100f
                )

            }
        }
    ) { p ->
        Box(
            Modifier
                .padding(p)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .coloredShadow(
                        color = animateColorAsState(color).value,
                        alpha = animateFloatAsState(alpha).value,
                        borderRadius = animateDpAsState(borderRadius).value,
                        shadowRadius = animateDpAsState(shadowRadius).value,
                        offsetX = animateDpAsState(offsetX).value,
                        offsetY = animateDpAsState(offsetY).value
                    )
            )
        }
    }
}