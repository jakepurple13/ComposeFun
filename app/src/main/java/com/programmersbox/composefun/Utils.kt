package com.programmersbox.composefun

import android.content.Context
import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

sealed class Screen(val route: String, val name: String, val icon: ImageVector? = null) {
    object MainScreen : Screen("mainscreen", "Playground", Icons.Default.Favorite)
    object GameScreen : Screen("gamescreen", "Games", Icons.Default.Games)
    object AirBarScreen : Screen("airbar", "AirBar Playground")
    object BroadcastReceiverScreen : Screen("broadcastreceiver", "Broadcast Receiver")
    object AnimatedLazyListScreen : Screen("animatedlazylist", "Animated LazyList")
    object GroupButtonScreen : Screen("groupbutton", "Group Buttons")
    object SettingsScreen : Screen("settings", "Settings Screen", Icons.Default.Settings)
    object BannerBoxScreen : Screen("bannerbox", "Banner Box Screen")
    object ShadowScreen : Screen("shadow", "Shadow Screen")
    object BlackjackScreen : Screen("blackjack", "Blackjack Screen")
    object PokerScreen : Screen("poker", "Video Poker Screen")
    object CompositionLocalScreen : Screen("composition", "Composition Local")
    object CalculationScreen : Screen("calculation", "Calculation Screen")
    object MastermindScreen : Screen("mastermind", "Mastermind Screen")
    object DadJokesScreen : Screen("dadjokes", "Dad Jokes Screen")
    object DidYouKnowScreen : Screen("didyouknow", "Did You Know Screen")
    object JokeOfTheDayScreen : Screen("jokeoftheday", "Joke of the Day Screen")
    object EvilInsultScreen : Screen("evilinsult", "Evil Insult Screen")
    object ChuckNorrisScreen : Screen("chucknorris", "Chuck Norris Screen")
    object UncleIrohScreen : Screen("uncleiroh", "Uncle Iroh's Wisdom")
    object MotionScreen : Screen("motion", "Motion Layout")
    object PermissionScreen : Screen("permission", "Permissions Screen")
    object CrashScreen : Screen("crash", "Crash Application")
    object WifiScreen : Screen("wifi", "Wifi Network Screen")
    object BleScreen : Screen("bluetoothle", "Bluetooth LE Screen")
    object BluetoothScreen : Screen("bluetooth", "Bluetooth Screen")
    object PlaceholderScreen : Screen("placeholder", "Placeholder Screen")
    object InsetScreen : Screen("inset", "Window Insets Screen")
    object PagerScreen : Screen("pager", "Horizontal Pager Screen")
    object YahtzeeScreen : Screen("yahtzee", "Yahtzee Screen")
    object HiLoScreen : Screen("hilo", "HiLo Game Screen")
    object WarScreen : Screen("war", "War Game Screen")
    object MatchingScreen : Screen("matching", "Matching Screen")
    object AboutLibrariesScreen : Screen("aboutlibraries", "Libraries Used Screen")
    object DiceRollerScreen : Screen("diceroller", "Dice Roller Screen")
    object AvatarScreen : Screen("avatar", "Avatar Airbender")

    companion object {
        val items = arrayOf(
            AirBarScreen,
            BroadcastReceiverScreen,
            AnimatedLazyListScreen,
            GroupButtonScreen,
            BannerBoxScreen,
            ShadowScreen,
            CompositionLocalScreen,
            DadJokesScreen,
            DidYouKnowScreen,
            JokeOfTheDayScreen,
            EvilInsultScreen,
            ChuckNorrisScreen,
            UncleIrohScreen,
            PermissionScreen,
            CrashScreen,
            WifiScreen,
            BleScreen,
            BluetoothScreen,
            PlaceholderScreen,
            InsetScreen,
            PagerScreen,
            MotionScreen,
            AboutLibrariesScreen,
            AvatarScreen
        )

        val gameItems = arrayOf(
            BlackjackScreen,
            PokerScreen,
            HiLoScreen,
            WarScreen,
            MatchingScreen,
            CalculationScreen,
            MastermindScreen,
            YahtzeeScreen,
            DiceRollerScreen
        )

        val mainItems = listOf(
            MainScreen,
            GameScreen,
            SettingsScreen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ScaffoldTop(
    screen: Screen,
    navController: NavController,
    topAppBarScrollBehavior: TopAppBarScrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() },
    containerColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.background,
    bottomBar: @Composable () -> Unit = {},
    topBarActions: @Composable RowScope.() -> Unit = {},
    block: @Composable (PaddingValues) -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(screen.name) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                actions = topBarActions,
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        containerColor = containerColor,
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

@Composable
fun BottomNavVisibility(
    onShow: () -> Unit,
    onHide: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // If `lifecycleOwner` changes, dispose and reset the effect
    DisposableEffect(lifecycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> onHide()
                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY, Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_ANY -> onShow()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("playground")

class LifecycleEventsScope {
    internal var onCreate: () -> Unit = {}
    fun create(block: () -> Unit) = run { onCreate = block }
    internal var onStart: () -> Unit = {}
    fun start(block: () -> Unit) = run { onStart = block }
    internal var onResume: () -> Unit = {}
    fun resume(block: () -> Unit) = run { onResume = block }
    internal var onPause: () -> Unit = {}
    fun pause(block: () -> Unit) = run { onPause = block }
    internal var onStop: () -> Unit = {}
    fun stop(block: () -> Unit) = run { onStop = block }
    internal var onDestroy: () -> Unit = {}
    fun destroy(block: () -> Unit) = run { onDestroy = block }
    internal var onAny: () -> Unit = {}
    fun any(block: () -> Unit) = run { onAny = block }
}

@Composable
fun LifecycleEventsDsl(block: LifecycleEventsScope.() -> Unit) {
    val lifecycleEvent = remember { LifecycleEventsScope().apply(block) }
    LifecycleEvents(
        onCreate = lifecycleEvent.onCreate,
        onStart = lifecycleEvent.onStart,
        onResume = lifecycleEvent.onResume,
        onPause = lifecycleEvent.onPause,
        onStop = lifecycleEvent.onStop,
        onDestroy = lifecycleEvent.onDestroy,
        onAny = lifecycleEvent.onAny
    )
}

@Composable
fun LifecycleEvents(
    onCreate: () -> Unit = {},
    onStart: () -> Unit = {},
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
    onDestroy: () -> Unit = {},
    onAny: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> onCreate()
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_PAUSE -> onPause()
                Lifecycle.Event.ON_STOP -> onStop()
                Lifecycle.Event.ON_DESTROY -> onDestroy()
                Lifecycle.Event.ON_ANY -> onAny()
            }
            if (event == Lifecycle.Event.ON_ANY) onAny()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}


val currentColorScheme: ColorScheme
    @Composable
    get() {
        val darkTheme = isSystemInDarkTheme()
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
            darkTheme -> darkColorScheme(
                primary = Color(0xff90CAF9),
                secondary = Color(0xff90CAF9)
            )
            else -> lightColorScheme(
                primary = Color(0xff2196F3),
                secondary = Color(0xff90CAF9)
            )
        }
    }
