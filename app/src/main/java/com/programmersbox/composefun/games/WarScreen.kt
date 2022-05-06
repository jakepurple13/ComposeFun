package com.programmersbox.composefun.games

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen
import kotlinx.coroutines.*

class WarPlayer(val deck: Deck<Card>) {
    val winnings = mutableStateListOf<Card>()

    var cardPlayed by mutableStateOf<Card?>(null)

    val fullSize get() = deck.size + winnings.size

    fun draw() {
        cardPlayed = deck.draw()
    }

    fun addWinningsToDeck() {
        deck.addCards(winnings)
        winnings.clear()
    }

    fun addToWinnings(cards: List<Card>) {
        winnings.addAll(cards)
    }

    fun reset() {
        cardPlayed = null
    }

    fun resetGame(cards: List<Card>) {
        deck.clear()
        winnings.clear()
        deck.addCards(cards)
    }
}

class WarViewModel : ViewModel() {

    private val deck = Deck.defaultDeck().also { it.trueRandomShuffle() }

    val player = WarPlayer(Deck(deck.draw(26)))
    val computer = WarPlayer(Deck(deck.draw(26)))

    fun resetGame() {
        deck.addDeck(Deck.defaultDeck())
        deck.trueRandomShuffle()
        player.resetGame(deck.draw(26))
        computer.resetGame(deck.draw(26))
    }

    fun draw(winner: (String) -> Unit) {
        viewModelScope.launch {
            if (player.cardPlayed == null && computer.cardPlayed == null) {
                player.draw()
                computer.draw()
            }
            delay(1000)
            checkForWinner(listOf(player.cardPlayed!!, computer.cardPlayed!!), winner)
        }
    }

    private suspend fun checkForWinner(winnings: List<Card>, winner: (String) -> Unit) {
        viewModelScope.launch {
            when {
                //Player Wins
                player.cardPlayed!!.aceValue > computer.cardPlayed!!.aceValue -> {
                    winner("Player wins!")
                    player.addToWinnings(winnings)
                    player.reset()
                    computer.reset()
                }
                //Computer Wins
                player.cardPlayed!!.aceValue < computer.cardPlayed!!.aceValue -> {
                    winner("Computer wins!")
                    computer.addToWinnings(winnings)
                    player.reset()
                    computer.reset()
                }
                //WAR!!!
                player.cardPlayed!!.aceValue == computer.cardPlayed!!.aceValue -> {
                    winner("WAR!")
                    val value = player.cardPlayed!!.aceValue
                    suspend fun drawCards(player: WarPlayer): List<Card> {
                        val cards = mutableListOf(player.cardPlayed!!)
                        val loopValue = if (player.fullSize < value) player.fullSize else value
                        for (i in 1 until loopValue) {
                            player.draw()
                            delay(1000)
                            cards.add(player.cardPlayed!!)
                        }
                        return cards
                    }

                    val cards = listOf(player, computer).map { i -> async(Dispatchers.IO) { drawCards(i) } }.awaitAll()

                    checkForWinner(cards.flatten(), winner)
                }
            }
        }
    }
}

val Card.aceValue get() = if (value == 1) 14 else value

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WarScreen(navController: NavController, vm: WarViewModel = viewModel()) {

    val playerDeckSize by remember { derivedStateOf { vm.player.deck.size } }
    val computerDeckSize by remember { derivedStateOf { vm.computer.deck.size } }

    LaunchedEffect(playerDeckSize) {
        if (playerDeckSize == 0) {
            vm.player.addWinningsToDeck()
        }
    }

    LaunchedEffect(computerDeckSize) {
        if (computerDeckSize == 0) {
            vm.computer.addWinningsToDeck()
        }
    }

    var gameOverDialog by remember { mutableStateOf(false) }

    LaunchedEffect(vm.player.fullSize, vm.computer.fullSize) {
        if (vm.player.fullSize == 0 || vm.computer.fullSize == 0) {
            gameOverDialog = true
        }
    }

    if (gameOverDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("The Game is Over!") },
            text = {
                val victor = when {
                    vm.player.fullSize == 0 -> "Lost"
                    vm.computer.fullSize == 0 -> "Won"
                    else -> ""
                }
                Text("You $victor")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        gameOverDialog = false
                        vm.resetGame()
                    }
                ) { Text("Play Again") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        gameOverDialog = false
                        navController.popBackStack()
                    }
                ) { Text("Stop Playing") }
            }
        )
    }

    val state = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    ScaffoldTop(
        scaffoldState = state,
        screen = Screen.WarScreen,
        navController = navController,
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy((-50).dp)
            ) {
                items(vm.computer.winnings) { PlayingCard(it) }
                item {
                    WarEmptyCard {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("${vm.computer.winnings.size} card(s)")
                        }
                    }
                }
            }

            val centerModifier = Modifier.align(Alignment.CenterHorizontally)

            Text("Computer", modifier = centerModifier)

            val computerCardFace by remember(vm.computer.cardPlayed) {
                mutableStateOf(if (vm.computer.cardPlayed == null) CardFace.Back else CardFace.Front)
            }

            FlipCard(
                modifier = centerModifier,
                cardFace = computerCardFace,
                back = {
                    WarEmptyCard(
                        modifier = centerModifier
                    ) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("$computerDeckSize card(s) left") } }
                },
                front = {
                    if (vm.computer.cardPlayed != null) PlayingCard(vm.computer.cardPlayed!!, modifier = centerModifier)
                    else EmptyCard(modifier = centerModifier)
                }
            )

            Text("")

            val cardFace by remember(vm.player.cardPlayed) {
                mutableStateOf(if (vm.player.cardPlayed == null) CardFace.Back else CardFace.Front)
            }

            FlipCard(
                modifier = centerModifier,
                cardFace = cardFace,
                back = {
                    WarEmptyCard(
                        modifier = centerModifier,
                        onClick = {
                            vm.draw {
                                scope.launch {
                                    state.snackbarHostState.currentSnackbarData?.dismiss()
                                    state.snackbarHostState.showSnackbar(it)
                                }
                            }
                        }
                    ) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("$playerDeckSize card(s) left") } }
                },
                front = {
                    if (vm.player.cardPlayed != null) PlayingCard(vm.player.cardPlayed!!, modifier = centerModifier)
                    else WarEmptyCard(modifier = centerModifier)
                }
            )

            Text("Player", modifier = centerModifier)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy((-50).dp)
            ) {
                items(vm.player.winnings) { PlayingCard(it) }
                item {
                    WarEmptyCard {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("${vm.player.winnings.size} card(s)")
                        }
                    }
                }
            }

        }
    }
}

@ExperimentalMaterialApi
@Composable
fun WarEmptyCard(modifier: Modifier = Modifier, onClick: () -> Unit = {}, content: @Composable () -> Unit = {}) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        elevation = 5.dp,
        modifier = Modifier
            .size(100.dp, 150.dp)
            .then(modifier),
        content = content
    )
}

enum class CardFace(val angle: Float) {
    Front(0f) {
        override val next: CardFace
            get() = Back
    },
    Back(180f) {
        override val next: CardFace
            get() = Front
    };

    abstract val next: CardFace
}

enum class RotationAxis { AxisX, AxisY }

@ExperimentalMaterialApi
@Composable
fun FlipCard(
    cardFace: CardFace,
    modifier: Modifier = Modifier,
    axis: RotationAxis = RotationAxis.AxisY,
    back: @Composable () -> Unit = {},
    front: @Composable () -> Unit = {},
) {
    val rotation = animateFloatAsState(
        targetValue = cardFace.angle,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing,
        )
    )
    Card(
        modifier = modifier
            .graphicsLayer {
                if (axis == RotationAxis.AxisX) {
                    rotationX = rotation.value
                } else {
                    rotationY = rotation.value
                }
                cameraDistance = 12f * density
            },
    ) {
        if (rotation.value <= 90f) {
            front()
        } else {
            Box(
                Modifier.graphicsLayer {
                    if (axis == RotationAxis.AxisX) {
                        rotationX = 180f
                    } else {
                        rotationY = 180f
                    }
                },
            ) { back() }
        }
    }
}