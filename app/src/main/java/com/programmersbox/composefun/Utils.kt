package com.programmersbox.composefun

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

sealed class Screen(val route: String, val name: String) {
    object MainScreen : Screen("mainscreen", "Playground")
    object AirBarScreen : Screen("airbar", "AirBar Playground")
    object BroadcastReceiverScreen : Screen("broadcastreceiver", "Broadcast Receiver")
    object AnimatedLazyListScreen : Screen("animatedlazylist", "Animated LazyList")
    object GroupButtonScreen : Screen("groupbutton", "Group Buttons")
    object SettingsScreen : Screen("settings", "Settings Screen")
    object BannerBoxScreen : Screen("bannerbox", "Banner Box Screen")
    object ShadowScreen : Screen("shadow", "Shadow Screen")
    object BlackjackScreen : Screen("blackjack", "Blackjack Screen")
    object PokerScreen : Screen("poker", "Video Poker Screen")
    object CompositionLocalScreen : Screen("composition", "Composition Local")
    object CalculationScreen : Screen("calculation", "Calculation Screen")
    object MastermindScreen : Screen("mastermind", "Mastermind Screen")
    object DadJokesScreen : Screen("dadjokes", "Dad Jokes Screen")
    object DidYouKnowScreen : Screen("didyouknow", "Did You Know Screen")
    object MotionScreen : Screen("motion", "Motion Layout")

    companion object {
        val items = arrayOf(
            AirBarScreen,
            BroadcastReceiverScreen,
            AnimatedLazyListScreen,
            GroupButtonScreen,
            SettingsScreen,
            BannerBoxScreen,
            ShadowScreen,
            CompositionLocalScreen,
            DadJokesScreen,
            DidYouKnowScreen,
            MotionScreen,
            BlackjackScreen,
            PokerScreen,
            CalculationScreen,
            MastermindScreen
        )
    }
}

@Composable
fun ScaffoldTop(
    screen: Screen,
    navController: NavController,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    backgroundColor: Color = MaterialTheme.colors.background,
    drawer: (@Composable ColumnScope.() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    topBarActions: @Composable RowScope.() -> Unit = {},
    block: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(screen.name) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                actions = topBarActions
            )
        },
        drawerContent = drawer,
        backgroundColor = backgroundColor,
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

@Composable
fun Int.animateAsState() = animateIntAsState(targetValue = this)

inline fun <reified V : ViewModel> factoryCreate(crossinline build: () -> V) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(V::class.java)) {
            return build() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}

fun Any?.toJson(): String = Gson().toJson(this)

inline fun <reified T> String?.fromJson(): T? = try {
    Gson().fromJson(this, object : TypeToken<T>() {}.type)
} catch (e: Exception) {
    null
}

suspend inline fun <reified T> getApi(url: String, noinline headers: HeadersBuilder.() -> Unit = {}): T? {
    val client = HttpClient()
    val response: HttpResponse = client.get(url) { headers(headers) }
    return response.bodyAsText().fromJson<T>()
}