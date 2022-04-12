package com.programmersbox.composefun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.programmersbox.composefun.ui.theme.ComposeFunTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeFunTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
                        composable(Screen.MainScreen.route) { MainScreen(navController) }
                        composable(Screen.AirBarScreen.route) { AirBarLayout(navController) }
                        composable(Screen.BroadcastReceiverScreen.route) { BroadcastReceiverScreen(navController) }
                        composable(Screen.AnimatedLazyListScreen.route) { AnimatedLazyListScreen(navController) }
                        composable(Screen.GroupButtonScreen.route) { GroupButtonScreen(navController) }
                        composable(Screen.SettingsScreen.route) { SettingsScreen(navController) }
                        composable(Screen.BannerBoxScreen.route) { BannerBoxScreen(navController) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text(Screen.MainScreen.name) }) }) {
        LazyVerticalGrid(
            cells = GridCells.Fixed(3),
            contentPadding = it,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) { items(Screen.items) { Button(onClick = { navController.navigate(it.route) }) { Text(it.name, textAlign = TextAlign.Center) } } }
    }
}
