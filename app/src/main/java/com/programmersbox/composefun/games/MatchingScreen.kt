package com.programmersbox.composefun.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen
import com.programmersbox.composefun.ui.theme.Emerald
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.util.*

class MatchingViewModel : ViewModel() {

    val deck = mutableStateListOf<Card>()

    val matched = mutableStateListOf<Card>()
    var flippedCard by mutableStateOf<Card?>(null)
    var flippedCard2 by mutableStateOf<Card?>(null)
    var flipBack by mutableStateOf(0)

    private var tickerChannel: ReceiveChannel<Unit>? = null
    var timer by mutableStateOf(0)

    var isComplete by mutableStateOf(false)

    var excessTimer by mutableStateOf(0)

    fun start() {
        if (tickerChannel == null) {
            tickerChannel = ticker(1, 0, viewModelScope.coroutineContext)
        }
        tickerChannel?.let {
            viewModelScope.launch {
                try {
                    it.consumeAsFlow().collect {
                        if (!isComplete) timer++
                    }
                } catch (e: Throwable) {
                }
            }
        }
    }

    init {
        reset()
        start()
    }

    fun reset() {
        deck.clear()
        matched.clear()
        flippedCard = null
        flippedCard2 = null
        val cards = (1..7).map { Card[it] }.toMutableStateList()
        deck.addAll(cards + cards)
        deck.shuffle()
        timer = 0
        flipBack = 0
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MatchingScreen(navController: NavController, vm: MatchingViewModel = viewModel()) {

    if (vm.isComplete) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Finished!") },
            text = {
                val delay = (vm.timer - (vm.excessTimer * 1500)).stringForTime()
                Text("It took you ${vm.timer.stringForTime()} to beat! ($delay) If you remove flip delay")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.isComplete = false
                        vm.reset()
                    }
                ) { Text("Play Again") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.isComplete = false
                        navController.popBackStack()
                    }
                ) { Text("Stop Playing") }
            }
        )
    }

    LaunchedEffect(vm.flippedCard, vm.flippedCard2) {
        if (vm.flippedCard != null && vm.flippedCard2 != null) {
            if (vm.flippedCard!! == vm.flippedCard2!!) {
                vm.matched.add(vm.flippedCard!!)
            } else {
                delay(1500)
                vm.excessTimer++
            }
            vm.flippedCard = null
            vm.flippedCard2 = null
            vm.flipBack++
        }
    }

    SideEffect { vm.isComplete = vm.matched.size == vm.deck.size / 2 }

    ScaffoldTop(
        screen = Screen.MatchingScreen,
        navController = navController,
        topBarActions = { Text((vm.timer - (vm.excessTimer * 1500)).stringForTime()) },
    ) { p ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = p
        ) {
            items(vm.deck) {

                var cardFace by remember(vm.matched, vm.flipBack) {
                    mutableStateOf(if (it in vm.matched) CardFace.Front else CardFace.Back)
                }

                FlipCard(
                    cardFace = cardFace,
                    back = {
                        EmptyCard {
                            if (it !in vm.matched) {
                                if (vm.flippedCard2 == null) cardFace = CardFace.Front
                                if (vm.flippedCard == null) {
                                    vm.flippedCard = it
                                } else if (vm.flippedCard2 == null) {
                                    vm.flippedCard2 = it
                                }
                            }
                        }
                    },
                    front = {
                        PlayingCard(
                            it,
                            modifier = Modifier.border(
                                width = animateDpAsState(targetValue = if (it in vm.matched) 2.dp else 0.dp).value,
                                color = animateColorAsState(targetValue = if (it in vm.matched) Emerald else Color.Transparent).value
                            )
                        )
                    }
                )

            }
        }
    }
}

fun <T : Number> T.stringForTime(): String {
    var millisecond = this.toLong()
    if (millisecond < 0 || millisecond >= 24 * 60 * 60 * 1000) return "00:00"
    millisecond /= 1000
    var minute = (millisecond / 60).toInt()
    val hour = minute / 60
    val second = (millisecond % 60).toInt()
    minute %= 60
    val stringBuilder = StringBuilder()
    val mFormatter = Formatter(stringBuilder, Locale.getDefault())
    return if (hour > 0) {
        mFormatter.format("%02d:%02d:%02d", hour, minute, second)
    } else {
        mFormatter.format("%02d:%02d", minute, second)
    }.toString()
}