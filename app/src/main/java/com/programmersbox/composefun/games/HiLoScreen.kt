package com.programmersbox.composefun.games

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.programmersbox.composefun.LocalCard
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun HiLoScreen(navController: NavController) {

    val deck = remember {
        Deck.defaultDeck().also {
            it.addDeckListener {
                onDraw { _, size ->
                    if (size == 0) {
                        it.addDeck(Deck.defaultDeck())
                        it.shuffle()
                    }
                }
            }
            it.shuffle()
        }
    }

    var card by remember { mutableStateOf(deck.draw()) }

    var win by remember { mutableStateOf(0) }
    var lose by remember { mutableStateOf(0) }

    val state = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    fun showEnd(text: String) {
        scope.launch { state.snackbarHostState.showSnackbar(text, duration = SnackbarDuration.Short) }
    }

    ScaffoldTop(
        scaffoldState = state,
        screen = Screen.HiLoScreen,
        navController = navController,
        bottomBar = { BottomAppBar { Text("Win(s): $win | Lose(s): $lose") } },
        topBarActions = { Text("${deck.size} card(s) left in deck") }
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
                            showEnd("Win")
                        } else {
                            lose++
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
                            showEnd("Win")
                        } else {
                            lose++
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