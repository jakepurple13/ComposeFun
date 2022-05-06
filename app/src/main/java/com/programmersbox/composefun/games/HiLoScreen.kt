package com.programmersbox.composefun.games

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.programmersbox.composefun.LocalCard
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen
import com.programmersbox.composefun.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val WIN_STREAK = intPreferencesKey("hilo_win_streak")
val DataStore<Preferences>.winStreak get() = data.map { it[WIN_STREAK] ?: 0 }

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun HiLoScreen(navController: NavController) {

    val deck = remember { Deck.defaultDeck().also { it.shuffle() } }

    val deckSize by remember { derivedStateOf { deck.size } }

    LaunchedEffect(deckSize) {
        if (deckSize == 0) {
            deck.addDeck(Deck.defaultDeck())
            deck.shuffle()
        }
    }

    var card by remember { mutableStateOf(deck.draw()) }

    var win by remember { mutableStateOf(0) }
    var streakWin by remember { mutableStateOf(0) }
    var lose by remember { mutableStateOf(0) }
    val dataStore = LocalContext.current.dataStore
    val winStreak by dataStore.winStreak.collectAsState(initial = 0)

    val state = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(streakWin) { if (streakWin > winStreak) dataStore.edit { it[WIN_STREAK] = win } }

    fun showEnd(text: String) {
        scope.launch {
            state.snackbarHostState.currentSnackbarData?.dismiss()
            state.snackbarHostState.showSnackbar(text, duration = SnackbarDuration.Short)
        }
    }

    ScaffoldTop(
        scaffoldState = state,
        screen = Screen.HiLoScreen,
        navController = navController,
        bottomBar = {
            BottomAppBar {
                Text("Win(s): $win", textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text("Lose(s): $lose", textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text("Win Streak: $streakWin", textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        },
        drawer = {
            TopAppBar { Text("HiLo Statistics") }
            Text("Highest Win Streak: $winStreak", textAlign = TextAlign.Center)
        },
        topBarActions = { Text("$deckSize card(s) left in deck") }
    ) { p ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    onClick = {
                        val newCard = deck.draw()
                        if (newCard <= card) {
                            win++
                            streakWin++
                            showEnd("Win")
                        } else {
                            lose++
                            streakWin = 0
                            showEnd("Lose")
                        }
                        card = newCard
                    },
                    shape = RoundedCornerShape(7.dp),
                    elevation = 5.dp,
                    modifier = Modifier.size(100.dp, 150.dp)
                ) { Box(modifier = Modifier.fillMaxSize()) { Text("Lower", modifier = Modifier.align(Alignment.Center)) } }
                CompositionLocalProvider(LocalCard provides card) {
                    AnimatedContent(
                        LocalCard.current,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
                            } else {
                                slideInVertically { height -> -height } + fadeIn() with slideOutVertically { height -> height } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        }
                    ) { target -> PlayingCard(target) }
                }
                Card(
                    onClick = {
                        val newCard = deck.draw()
                        if (newCard >= card) {
                            win++
                            streakWin++
                            showEnd("Win")
                        } else {
                            lose++
                            streakWin = 0
                            showEnd("Lose")
                        }
                        card = newCard
                    },
                    shape = RoundedCornerShape(7.dp),
                    elevation = 5.dp,
                    modifier = Modifier.size(100.dp, 150.dp)
                ) { Box(modifier = Modifier.fillMaxSize()) { Text("Higher", modifier = Modifier.align(Alignment.Center)) } }
            }
        }
    }
}