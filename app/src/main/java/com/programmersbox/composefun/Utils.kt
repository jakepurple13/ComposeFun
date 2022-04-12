package com.programmersbox.composefun

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController

sealed class Screen(val route: String, val name: String) {
    object MainScreen : Screen("mainscreen", "Playground")
    object AirBarScreen : Screen("airbar", "AirBar Playground")
    object BroadcastReceiverScreen : Screen("broadcastreceiver", "Broadcast Receiver")
    object AnimatedLazyListScreen : Screen("animatedlazylist", "Animated LazyList")
    object GroupButtonScreen : Screen("groupbutton", "Group Buttons")
    object SettingsScreen : Screen("settings", "Settings Screen")
    object BannerBoxScreen : Screen("bannerbox", "Banner Box Screen")

    companion object {
        val items = arrayOf(
            AirBarScreen,
            BroadcastReceiverScreen,
            AnimatedLazyListScreen,
            GroupButtonScreen,
            SettingsScreen,
            BannerBoxScreen
        )
    }
}

@Composable
fun ScaffoldTop(screen: Screen, navController: NavController, bottomBar: @Composable () -> Unit = {}, block: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screen.name) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        bottomBar = bottomBar,
        content = block
    )
}

@Composable
fun ShowWhen(visibility: Boolean, content: @Composable () -> Unit) {
    Column(modifier = Modifier.animateContentSize()) { if (visibility) content() }
}

enum class ComponentState { Pressed, Released }

/**
 * Allows a press and hold action
 */
@Composable
fun Modifier.combineClickable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication = LocalIndication.current,
    onLongPress: (ComponentState) -> Unit = {},
    onClick: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null
): Modifier = indication(
    interactionSource = interactionSource,
    indication = indication
)
    .pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { onLongPress(ComponentState.Pressed) },
            onPress = {
                val press = PressInteraction.Press(it)
                interactionSource.tryEmit(press)
                tryAwaitRelease()
                onLongPress(ComponentState.Released)
                interactionSource.tryEmit(PressInteraction.Release(press))
            },
            onTap = onClick?.let { c -> { c() } },
            onDoubleTap = onDoubleTap?.let { d -> { d() } }
        )
    }