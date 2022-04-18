package com.programmersbox.composefun

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DadJokeViewModel : ViewModel() {

    var joke by mutableStateOf("")
    var loading by mutableStateOf(false)

    fun getNewJoke() = viewModelScope.launch(Dispatchers.IO) {
        loading = true
        joke = getDadJoke()?.joke ?: "Something went wrong, please try again"
        loading = false
    }

}

suspend fun getDadJoke() = getApi<DadJoke>("https://icanhazdadjoke.com/") { append("Accept", "application/json") }

data class DadJoke(val id: String?, val joke: String?, val status: Number?)

@Composable
fun DadJokesScreen(navController: NavController, vm: DadJokeViewModel = viewModel()) {

    LaunchedEffect(Unit) { vm.getNewJoke() }

    ScaffoldTop(
        screen = Screen.DadJokesScreen,
        navController = navController,
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { vm.getNewJoke() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Get New Joke") }
            }
        }
    ) { p ->
        Card(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (vm.loading) {
                    CircularProgressIndicator()
                } else {
                    Text(vm.joke, textAlign = TextAlign.Center)
                }
            }
        }
    }
}