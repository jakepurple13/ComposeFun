package com.programmersbox.composefun.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen

class CalculationViewModel : ViewModel() {

    val deck: Deck<Card> = Deck.defaultDeck().also { it.shuffle() }
    var pickUpCard: Card? by mutableStateOf(null)
    val firstFoundation = mutableStateListOf(deck.randomDraw { it.value == 1 })
    val secondFoundation = mutableStateListOf(deck.randomDraw { it.value == 2 })
    val thirdFoundation = mutableStateListOf(deck.randomDraw { it.value == 3 })
    val fourthFoundation = mutableStateListOf(deck.randomDraw { it.value == 4 })

    val firstHold = mutableStateListOf<Card>()
    val secondHold = mutableStateListOf<Card>()
    val thirdHold = mutableStateListOf<Card>()
    val fourthHold = mutableStateListOf<Card>()

    fun drawCard() {
        if (pickUpCard == null) pickUpCard = if (deck.isNotEmpty) deck.draw() else null
    }

    fun foundationClick(cardList: SnapshotStateList<Card>, count: Int) {
        if (pickUpCard != null) {
            val valueCheck = nextVal(cardList.last().value, count)
            if (valueCheck == pickUpCard!!.value && cardList.last().value != 13) {
                cardList.add(pickUpCard!!)
                pickUpCard = null
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CalculationScreen(navController: NavController, vm: CalculationViewModel = viewModel()) {
    val state = rememberScaffoldState()
    if (
        vm.firstFoundation.last().value == 13 &&
        vm.secondFoundation.last().value == 13 &&
        vm.thirdFoundation.last().value == 13 &&
        vm.fourthFoundation.last().value == 13
    ) {
        LaunchedEffect(vm.firstFoundation, vm.secondFoundation, vm.thirdFoundation, vm.fourthFoundation) {
            state.snackbarHostState.showSnackbar("You Win!", duration = SnackbarDuration.Indefinite)
        }
    }

    ScaffoldTop(
        scaffoldState = state,
        screen = Screen.CalculationScreen,
        navController = navController,
        backgroundColor = animateColorAsState(
            if (
                vm.firstFoundation.last().value == 13 &&
                vm.secondFoundation.last().value == 13 &&
                vm.thirdFoundation.last().value == 13 &&
                vm.fourthFoundation.last().value == 13
            ) {
                Color(0xFF81c784)
            } else {
                MaterialTheme.colors.background
            },
            animationSpec = infiniteRepeatable(
                tween(durationMillis = 2000),
                repeatMode = RepeatMode.Reverse
            )
        ).value,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (vm.pickUpCard != null) {
                    PlayingCard(vm.pickUpCard!!)
                } else {
                    EmptyCard()
                }

                Card(
                    onClick = { vm.drawCard() },
                    shape = RoundedCornerShape(7.dp),
                    elevation = 5.dp,
                    modifier = Modifier.size(100.dp, 150.dp),
                    backgroundColor = Color(0xFF64b5f6).copy(alpha = vm.deck.size.toFloat() / 48f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "${vm.deck.size}",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(2.dp)
                .padding(p),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    PlayingCard(vm.firstFoundation.last()) {
                        vm.foundationClick(vm.firstFoundation, 1)
                    }
                }
                item {
                    PlayingCard(vm.secondFoundation.last()) {
                        vm.foundationClick(vm.secondFoundation, 2)
                    }
                }
                item {
                    PlayingCard(vm.thirdFoundation.last()) {
                        vm.foundationClick(vm.thirdFoundation, 3)
                    }
                }
                item {
                    PlayingCard(vm.fourthFoundation.last()) {
                        vm.foundationClick(vm.fourthFoundation, 4)
                    }
                }
                item {
                    if (vm.firstFoundation.last().value != 13) {
                        Text(
                            "${nextVal(vm.firstFoundation.last().value, 1)}",
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item {
                    if (vm.secondFoundation.last().value != 13) {
                        Text(
                            "${nextVal(vm.secondFoundation.last().value, 2)}",
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item {
                    if (vm.thirdFoundation.last().value != 13) {
                        Text(
                            "${nextVal(vm.thirdFoundation.last().value, 3)}",
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item {
                    if (vm.fourthFoundation.last().value != 13) {
                        Text(
                            "${nextVal(vm.fourthFoundation.last().value, 4)}",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                HoldingCards(vm, vm.firstHold)
                HoldingCards(vm, vm.secondHold)
                HoldingCards(vm, vm.thirdHold)
                HoldingCards(vm, vm.fourthHold)
            }
        }
    }
}

private fun nextVal(currentVal: Int, upBy: Int): Int {
    val added = currentVal + upBy
    return if (added > 13) {
        added - 13
    } else {
        added
    }
}

@ExperimentalMaterialApi
@Composable
fun HoldingCards(vm: CalculationViewModel, cardHold: SnapshotStateList<Card>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy((-125).dp)
    ) {
        if (cardHold.isEmpty()) {
            item {
                EmptyCard {
                    vm.pickUpCard?.let {
                        cardHold.add(it)
                        vm.pickUpCard = null
                    }
                }
            }
        } else {
            items(cardHold) {
                PlayingCard(it) {
                    if (vm.pickUpCard == null) {
                        vm.pickUpCard = cardHold.removeLast()
                    } else {
                        cardHold.add(vm.pickUpCard!!)
                        vm.pickUpCard = null
                    }
                }
            }
        }
    }
}