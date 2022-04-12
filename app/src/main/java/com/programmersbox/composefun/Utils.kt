package com.programmersbox.composefun

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

sealed class Screen(val route: String, val name: String) {
    object AirBarScreen : Screen("airbar", "AirBar")
    object MainScreen : Screen("mainscreen", "Playground")
    object BroadcastReceiverScreen : Screen("broadcastreceiver", "Broadcast Receiver")

    companion object {
        val items = arrayOf(AirBarScreen, BroadcastReceiverScreen)
    }
}

@Composable
fun ScaffoldTop(screen: Screen, navController: NavController, block: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screen.name) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        content = block
    )
}