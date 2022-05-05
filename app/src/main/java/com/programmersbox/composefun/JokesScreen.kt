package com.programmersbox.composefun

import androidx.compose.foundation.layout.*
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

    JokeScreens(
        navController = navController,
        screen = Screen.DadJokesScreen,
        buttonText = "Get New Joke",
        onNewJokeClick = { vm.getNewJoke() }
    ) {
        if (vm.loading) {
            CircularProgressIndicator()
        } else {
            Text(vm.joke, textAlign = TextAlign.Center)
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
fun DidYouKnowScreen(navController: NavController) {
    var count by remember { mutableStateOf(0) }
    val didYouKnow by getApiJoke(count) { getApi<DidYouKnowFact>("https://uselessfacts.jsph.pl/random.json?language=en") }

    JokeScreens(
        navController = navController,
        screen = Screen.DidYouKnowScreen,
        buttonText = "Get New Fact",
        onNewJokeClick = { count++ }
    ) {
        when (didYouKnow) {
            is Result.Error -> Text("Please Try Again", textAlign = TextAlign.Center)
            is Result.Loading -> CircularProgressIndicator()
            is Result.Success -> Text((didYouKnow as Result.Success<DidYouKnowFact>).value.text, textAlign = TextAlign.Center)
        }
    }
}

data class Success(val total: Number?)
data class JokeBase(val success: Success?, val contents: Contents?)
data class Contents(val jokes: List<Jokes>?, val copyright: String?)

data class Joke(
    val title: String?,
    val lang: String?,
    val length: String?,
    val clean: String?,
    val racial: String?,
    val id: String?,
    val text: String?
)

data class Jokes(
    val description: String?,
    val language: String?,
    val background: String?,
    val category: String?,
    val date: String?,
    val joke: Joke?
)

@Composable
fun JokeOfTheDayScreen(navController: NavController) {
    val joke by getApiJoke(Unit) { getApi<JokeBase>("https://api.jokes.one/jod") }

    JokeScreens(
        navController = navController,
        screen = Screen.JokeOfTheDayScreen,
        buttonText = "One Joke!",
        onNewJokeClick = {}
    ) {
        when (joke) {
            is Result.Error -> Text("Please Try Again", textAlign = TextAlign.Center)
            is Result.Loading -> CircularProgressIndicator()
            is Result.Success -> Text(
                (joke as Result.Success<JokeBase>).value.contents?.jokes?.firstOrNull()?.joke?.text.orEmpty(),
                textAlign = TextAlign.Center
            )
        }
    }
}

data class EvilInsult(
    val number: String?,
    val language: String?,
    val insult: String?,
    val created: String?,
    val shown: String?,
    val createdby: String?,
    val active: String?,
    val comment: String?
)

@Composable
fun EvilInsultScreen(navController: NavController) {
    var count by remember { mutableStateOf(0) }
    val evilInsult by getApiJoke(count) { getApi<EvilInsult>("https://evilinsult.com/generate_insult.php?lang=en&type=json") }

    JokeScreens(
        navController = navController,
        screen = Screen.EvilInsultScreen,
        buttonText = "Get New Insult",
        onNewJokeClick = { count++ }
    ) {
        when (evilInsult) {
            is Result.Error -> Text("Please Try Again", textAlign = TextAlign.Center)
            is Result.Loading -> CircularProgressIndicator()
            is Result.Success -> Text((evilInsult as Result.Success<EvilInsult>).value.insult.orEmpty(), textAlign = TextAlign.Center)
        }
    }
}

data class ChuckNorrisBase(val type: String?, val value: ChuckNorris?)
data class ChuckNorris(val id: Number?, val joke: String?, val categories: List<Any>?)

@Composable
fun ChuckNorrisScreen(navController: NavController) {
    var count by remember { mutableStateOf(0) }
    val chuckNorris by getApiJoke(count) { getApi<ChuckNorrisBase>("http://api.icndb.com/jokes/random") }

    JokeScreens(
        navController = navController,
        screen = Screen.ChuckNorrisScreen,
        buttonText = "Get New Chuck Norris Fact",
        onNewJokeClick = { count++ }
    ) {
        when (chuckNorris) {
            is Result.Error -> Text("Please Try Again", textAlign = TextAlign.Center)
            is Result.Loading -> CircularProgressIndicator()
            is Result.Success -> Text((chuckNorris as Result.Success<ChuckNorrisBase>).value.value?.joke.orEmpty(), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun JokeScreens(
    navController: NavController,
    screen: Screen,
    buttonText: String,
    onNewJokeClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    ScaffoldTop(
        screen = screen,
        navController = navController,
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = onNewJokeClick,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(buttonText) }
            }
        }
    ) { p ->
        Card(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .padding(4.dp)
        ) { Box(contentAlignment = Alignment.Center, content = content) }
    }
}

@Composable
fun <T> getApiJoke(key: Any, request: suspend () -> T?): State<Result<T>> {

    // Creates a State<T> with Result.Loading as initial value
    // If either `url` or `imageRepository` changes, the running producer
    // will cancel and will be re-launched with the new inputs.
    return produceState<Result<T>>(Result.Loading, key) {
        //We start by making value Loading so that it will show the loading screen everytime
        value = Result.Loading

        // In a coroutine, can make suspend calls
        val joke = withContext(Dispatchers.IO) { request() }

        // Update State with either an Error or Success result.
        // This will trigger a recomposition where this State is read
        value = if (joke == null) {
            Result.Error
        } else {
            Result.Success(joke)
        }
    }
}

sealed class Result<out R> {
    class Success<out T>(val value: T) : Result<T>()
    object Error : Result<Nothing>()
    object Loading : Result<Nothing>()
}
