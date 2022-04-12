package com.programmersbox.composefun

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

sealed class Screen(val route: String, val name: String) {
    object MainScreen : Screen("mainscreen", "Playground")
    object AirBarScreen : Screen("airbar", "AirBar Playground")
    object BroadcastReceiverScreen : Screen("broadcastreceiver", "Broadcast Receiver")
    object AnimatedLazyListScreen : Screen("animatedlazylist", "Animated LazyList")
    object GroupButtonScreen : Screen("groupbutton", "Group Buttons")

    companion object {
        val items = arrayOf(AirBarScreen, BroadcastReceiverScreen, AnimatedLazyListScreen, GroupButtonScreen)
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