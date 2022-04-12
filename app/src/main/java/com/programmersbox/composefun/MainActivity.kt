package com.programmersbox.composefun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
                        composable(Screen.MainScreen.route) { MainScreen(navController = navController) }
                        composable(Screen.AirBarScreen.route) { AirBarLayout(navController) }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text(Screen.MainScreen.name)}) }) {
        LazyVerticalGrid(
            cells = GridCells.Fixed(3),
            contentPadding = it
        ) {
            item { Button(onClick = { navController.navigate(Screen.AirBarScreen.route) }) { Text(Screen.AirBarScreen.name) } }
        }
    }
}

sealed class Screen(val route: String, val name: String) {
    object AirBarScreen : Screen("airbar", "AirBar")
    object MainScreen : Screen("mainscreen", "Playground")
}
