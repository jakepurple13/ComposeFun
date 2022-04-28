package com.programmersbox.composefun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.programmersbox.composefun.games.Blackjack
import com.programmersbox.composefun.games.CalculationScreen
import com.programmersbox.composefun.games.MastermindScreen
import com.programmersbox.composefun.games.Poker
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
                        BottomNavScaffold(navController = navController) { innerPadding ->
                            AnimatedNavHost(
                                navController = navController,
                                startDestination = Screen.MainScreen.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.MainScreen.route) { MainScreen(navController) }
                                composable(Screen.AirBarScreen.route) { AirBarLayout(navController) }
                                bottomSheet(Screen.BroadcastReceiverScreen.route) { BroadcastReceiverScreen(navController) }
                                composable(Screen.AnimatedLazyListScreen.route) {
                                    DisposableEffect(Unit) {
                                        c = Color.Blue
                                        onDispose { c = primary }
                                    }
                                    AnimatedLazyListScreen(navController)
                                }
                                bottomSheet(Screen.GroupButtonScreen.route) { GroupButtonScreen(navController) }
                                composable(Screen.SettingsScreen.route) { SettingsScreen(navController) }
                                composable(Screen.BannerBoxScreen.route) { BannerBoxScreen(navController) }
                                composable(Screen.CompositionLocalScreen.route) { CompositionLocalScreen(navController) }
                                composable(Screen.BlackjackScreen.route) { Blackjack(navController) }
                                composable(Screen.PokerScreen.route) { Poker(navController) }
                                composable(Screen.CalculationScreen.route) { CalculationScreen(navController) }
                                composable(Screen.MastermindScreen.route) { MastermindScreen(navController) }
                                composable(Screen.DadJokesScreen.route) { DadJokesScreen(navController) }
                                composable(Screen.DidYouKnowScreen.route) { DidYouKnowScreen(navController) }
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

@Composable
fun BottomNavScaffold(navController: NavController, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                Screen.mainItems.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                when (screen) {
                                    Screen.MainScreen -> Icons.Default.Favorite
                                    Screen.SettingsScreen -> Icons.Default.Settings
                                    else -> Icons.Default.Adb
                                },
                                null
                            )
                        },
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
        },
        content = content
    )
}