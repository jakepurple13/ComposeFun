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
import kotlinx.coroutines.withContext

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

@Composable
fun DidYouKnowScreen(navController: NavController) {

    var count by remember { mutableStateOf(0) }

    val didYouKnow by getDidYouKnowFact(count)

    ScaffoldTop(
        screen = Screen.DidYouKnowScreen,
        navController = navController,
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { count++ },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Get New Fact") }
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
                when (didYouKnow) {
                    is Result.Error -> Text("Please Try Again", textAlign = TextAlign.Center)
                    is Result.Loading -> CircularProgressIndicator()
                    is Result.Success -> Text((didYouKnow as Result.Success<DidYouKnowFact>).value.text, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

data class DidYouKnowFact(
    val id: String,
    val text: String,
    val source_url: String,
    val language: String,
    val permalink: String
)

@Composable
fun getDidYouKnowFact(key: Any): State<Result<DidYouKnowFact>> {

    // Creates a State<T> with Result.Loading as initial value
    // If either `url` or `imageRepository` changes, the running producer
    // will cancel and will be re-launched with the new inputs.
    return produceState<Result<DidYouKnowFact>>(Result.Loading(), key) {

        value = Result.Loading()

        // In a coroutine, can make suspend calls
        val dyk = withContext(Dispatchers.IO) { getApi<DidYouKnowFact>("https://uselessfacts.jsph.pl/random.json?language=en") }

        // Update State with either an Error or Success result.
        // This will trigger a recomposition where this State is read
        value = if (dyk == null) {
            Result.Error()
        } else {
            Result.Success(dyk)
        }
    }
}


sealed class Result<T> {
    class Error<T> : Result<T>()
    class Loading<T> : Result<T>()
    class Success<T>(val value: T) : Result<T>()
}
