package com.programmersbox.composefun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.HtmlText
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.util.author
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import com.mikepenz.aboutlibraries.util.withContext
import com.programmersbox.composefun.games.*
import com.programmersbox.composefun.ui.theme.ComposeFunTheme

class MainActivity : ComponentActivity() {
    @OptIn(
        ExperimentalComposeUiApi::class,
        ExperimentalFoundationApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalMaterialNavigationApi::class,
        ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var showBottomNav by remember { mutableStateOf(true) }

            val sys = rememberSystemUiController()
            val primary = MaterialTheme.colors.primaryVariant
            var c by remember { mutableStateOf(primary) }
            val ac by animateColorAsState(c)
            LaunchedEffect(ac) { sys.setStatusBarColor(ac) }

            ComposeFunTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val bottomSheetNavigator = rememberBottomSheetNavigator()
                    val navController = rememberAnimatedNavController(bottomSheetNavigator)
                    com.google.accompanist.navigation.material.ModalBottomSheetLayout(bottomSheetNavigator) {
                        BottomNavScaffold(navController = navController, showBottomNav = showBottomNav) { innerPadding ->
                            AnimatedNavHost(
                                navController = navController,
                                startDestination = Screen.MainScreen.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.MainScreen.route) { MainScreen(navController) }
                                composable(Screen.GameScreen.route) { GameScreen(navController) }
                                composable(Screen.AirBarScreen.route) { AirBarLayout(navController) }
                                bottomSheet(Screen.BroadcastReceiverScreen.route) { BroadcastReceiverScreen(navController) }
                                composable(Screen.AnimatedLazyListScreen.route) {
                                    val lifecycleOwner = LocalLifecycleOwner.current

                                    // If `lifecycleOwner` changes, dispose and reset the effect
                                    DisposableEffect(lifecycleOwner) {
                                        // Create an observer that triggers our remembered callbacks
                                        // for sending analytics events
                                        val observer = LifecycleEventObserver { _, event ->
                                            c = when (event) {
                                                Lifecycle.Event.ON_CREATE -> Color.Yellow
                                                Lifecycle.Event.ON_START -> Color.Green
                                                Lifecycle.Event.ON_RESUME -> Color.Blue
                                                Lifecycle.Event.ON_PAUSE -> Color.Magenta
                                                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> primary
                                                Lifecycle.Event.ON_ANY -> Color.White
                                            }
                                        }

                                        // Add the observer to the lifecycle
                                        lifecycleOwner.lifecycle.addObserver(observer)

                                        // When the effect leaves the Composition, remove the observer
                                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                                    }
                                    AnimatedLazyListScreen(navController)
                                }
                                bottomSheet(Screen.GroupButtonScreen.route) { GroupButtonScreen(navController) }
                                composable(Screen.SettingsScreen.route) { SettingsScreen(navController) }
                                composable(Screen.BannerBoxScreen.route) { BannerBoxScreen(navController) }
                                composable(Screen.CompositionLocalScreen.route) { CompositionLocalScreen(navController) }
                                composable(Screen.BlackjackScreen.route) {
                                    BottomNavVisibility(onShow = { showBottomNav = true }, onHide = { showBottomNav = false })
                                    Blackjack(navController)
                                }
                                composable(Screen.PokerScreen.route) {
                                    BottomNavVisibility(onShow = { showBottomNav = true }, onHide = { showBottomNav = false })
                                    Poker(navController)
                                }
                                composable(Screen.CalculationScreen.route) {
                                    BottomNavVisibility(onShow = { showBottomNav = true }, onHide = { showBottomNav = false })
                                    CalculationScreen(navController)
                                }
                                composable(Screen.MastermindScreen.route) {
                                    BottomNavVisibility(onShow = { showBottomNav = true }, onHide = { showBottomNav = false })
                                    MastermindScreen(navController)
                                }
                                composable(Screen.YahtzeeScreen.route) {
                                    BottomNavVisibility(onShow = { showBottomNav = true }, onHide = { showBottomNav = false })
                                    YahtzeeScreen(navController)
                                }
                                composable(Screen.DadJokesScreen.route) { DadJokesScreen(navController) }
                                composable(Screen.DidYouKnowScreen.route) { DidYouKnowScreen(navController) }
                                composable(Screen.JokeOfTheDayScreen.route) { JokeOfTheDayScreen(navController) }
                                composable(Screen.EvilInsultScreen.route) { EvilInsultScreen(navController) }
                                composable(Screen.ChuckNorrisScreen.route) { ChuckNorrisScreen(navController) }
                                composable(Screen.PermissionScreen.route) { PermissionScreen(navController) }
                                composable(
                                    Screen.ShadowScreen.route,
                                    enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Up) },
                                    exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Down) },
                                    popEnterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Down) },
                                    popExitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Up) }
                                ) { ShadowScreen(navController) }
                                composable(
                                    Screen.MotionScreen.route,
                                    enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Start) },
                                    exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Start) },
                                    popEnterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.End) },
                                    popExitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.End) }
                                ) { MotionScreen(navController) }
                                composable(Screen.CrashScreen.route) { LaunchedEffect(Unit) { throw RuntimeException("Innocent Crash!") } }
                                composable(Screen.WifiScreen.route) { WifiScreen(navController) }
                                composable(Screen.BleScreen.route) { BleScreen(navController) }
                                composable(Screen.BluetoothScreen.route) { BluetoothScreen(navController) }
                                composable(Screen.PlaceholderScreen.route) { PlaceholderScreen(navController) }
                                composable(Screen.AboutLibrariesScreen.route) { AboutLibrariesScreen(navController) }
                                composable(Screen.InsetScreen.route) {
                                    DisposableEffect(Unit) {
                                        WindowCompat.setDecorFitsSystemWindows(window, false)
                                        sys.setSystemBarsColor(Color.Transparent)
                                        onDispose {
                                            WindowCompat.setDecorFitsSystemWindows(window, true)
                                            sys.setSystemBarsColor(Color.Black)
                                        }
                                    }
                                    InsetScreen(navController)
                                }
                                composable(Screen.PagerScreen.route) { PagerScreen(navController) }
                                composable(Screen.HiLoScreen.route) { HiLoScreen(navController) }
                                composable(Screen.WarScreen.route) {
                                    BottomNavVisibility(onShow = { showBottomNav = true }, onHide = { showBottomNav = false })
                                    WarScreen(navController)
                                }
                                composable(Screen.MatchingScreen.route) {
                                    BottomNavVisibility(onShow = { showBottomNav = true }, onHide = { showBottomNav = false })
                                    MatchingScreen(navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text(Screen.MainScreen.name) }) }) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = it,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) { items(Screen.items) { Button(onClick = { navController.navigate(it.route) }) { Text(it.name, textAlign = TextAlign.Center) } } }
    }
}

@ExperimentalFoundationApi
@Composable
fun GameScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text(Screen.GameScreen.name) }) }) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = it,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) { items(Screen.gameItems) { Button(onClick = { navController.navigate(it.route) }) { Text(it.name, textAlign = TextAlign.Center) } } }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AboutLibrariesScreen(navController: NavController) {
    ScaffoldTop(
        screen = Screen.AboutLibrariesScreen,
        navController = navController,
    ) { p ->
        val colors = LibraryDefaults.libraryColors()
        var libraries by remember { mutableStateOf<Libs?>(null) }

        val context = LocalContext.current
        LaunchedEffect(libraries) { libraries = Libs.Builder().withContext(context).build() }
        val libs = libraries?.libraries
        if (libs != null) {
            LazyColumn(
                Modifier.fillMaxSize(),
                state = rememberLazyListState(),
                contentPadding = p,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(libs) { library ->
                    val openDialog = rememberSaveable { mutableStateOf(false) }
                    Card(
                        backgroundColor = colors.backgroundColor,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
                    ) {
                        val typography = MaterialTheme.typography
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openDialog.value = true }
                                .padding(LibraryDefaults.ContentPadding)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = library.name,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .weight(1f),
                                    style = typography.h6,
                                    color = colors.contentColor,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val version = library.artifactVersion
                                if (version != null) {
                                    Text(
                                        version,
                                        modifier = Modifier.padding(start = 8.dp),
                                        style = typography.body2,
                                        color = colors.contentColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            val author = library.author
                            if (author.isNotBlank()) {
                                Text(
                                    text = author,
                                    style = typography.body2,
                                    color = colors.contentColor
                                )
                            }
                            if (library.licenses.isNotEmpty()) {
                                Row(modifier = Modifier.padding(top = 8.dp)) {
                                    library.licenses.forEach {
                                        Badge(
                                            modifier = Modifier.padding(end = 4.dp),
                                            contentColor = colors.badgeContentColor,
                                            backgroundColor = colors.badgeBackgroundColor
                                        ) {
                                            Text(text = it.name)
                                        }
                                    }
                                }
                            }
                            if (!library.description.isNullOrBlank()) {
                                Text(
                                    text = library.description.orEmpty(),
                                    style = typography.body2,
                                    color = colors.contentColor,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            if (!library.website.isNullOrBlank()) {
                                Text(
                                    text = library.website.orEmpty(),
                                    style = typography.subtitle2,
                                    color = colors.contentColor,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    if (openDialog.value) {
                        val scrollState = rememberScrollState()
                        AlertDialog(
                            onDismissRequest = { openDialog.value = false },
                            confirmButton = { TextButton(onClick = { openDialog.value = false }) { Text("OK") } },
                            text = {
                                Column(
                                    modifier = Modifier.verticalScroll(scrollState)
                                ) { HtmlText(library.licenses.firstOrNull()?.htmlReadyLicenseContent.orEmpty()) }
                            },
                            modifier = Modifier.padding(16.dp),
                            properties = DialogProperties(usePlatformDefaultWidth = false),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavScaffold(navController: NavController, showBottomNav: Boolean, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                showBottomNav,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                BottomNavigation {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    Screen.mainItems.forEach { screen ->
                        BottomNavigationItem(
                            icon = { screen.icon?.let { Icon(it, null) } },
                            label = { Text(screen.name) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        content = content
    )
}