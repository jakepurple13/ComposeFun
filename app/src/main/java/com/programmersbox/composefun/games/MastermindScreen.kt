package com.programmersbox.composefun.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.programmersbox.composefun.*
import kotlinx.coroutines.launch

enum class MasterMindDifficulty(val length: Int) { Easy(4), Medium(5), Hard(6) }

class MastermindViewModel : ViewModel() {

    var difficulty by mutableStateOf(MasterMindDifficulty.Easy)

    val sequence = mutableStateListOf<Card>()
    val currentSequence = mutableStateListOf<Card>()
    val guesses = mutableStateMapOf<Int, MutableList<Card>>()
    var currentGuess = 0
    var everythingCorrect by mutableStateOf(false)

    fun reset() {
        sequence.clear()
        currentSequence.clear()
        guesses.clear()

        currentGuess = 0
        repeat(difficulty.length) { sequence.add(Card.RandomCard) }
        sequence.shuffle()
        everythingCorrect = false
        println(sequence.joinToString(", ") { it.toSymbolString() })
    }

    fun guess(card: Card) {
        if (currentSequence.size < sequence.size) {
            currentSequence.add(card)
        }
    }

    fun removeGuess() {
        currentSequence.removeLastOrNull()
    }

    fun submitGuess() {
        if (currentSequence.size == sequence.size) {
            guesses[currentGuess++] = currentSequence.toMutableList()
            currentSequence.clear()
            if (guesses[currentGuess - 1] == sequence) {
                everythingCorrect = true
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MastermindScreen(navController: NavController, vm: MastermindViewModel = viewModel()) {

    LaunchedEffect(Unit) { vm.reset() }

    if (vm.everythingCorrect) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("You got it!") },
            text = {
                Text(
                    "The answer was ${vm.sequence.joinToString(", ") { it.toSymbolString() }}!" +
                            " And you got it in $vm.currentGuess guesses!"
                )
            },
            confirmButton = { TextButton(onClick = { vm.reset() }) { Text("Play Again") } },
            dismissButton = { TextButton(onClick = { navController.popBackStack() }) { Text("Stop Playing") } }
        )
    }

    var showReset by remember { mutableStateOf(false) }
    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset?") },
            text = { Text("Are you sure you want to reset?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.reset()
                        showReset = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = { showReset = false }) { Text("No") } }
        )
    }

    val scope = rememberCoroutineScope()
    val state = rememberScaffoldState()

    ScaffoldTop(
        scaffoldState = state,
        screen = Screen.MastermindScreen,
        navController = navController,
        drawer = {
            CategorySetting { Text("Mastermind Settings") }
            ListSetting(
                settingTitle = { Text("Difficulty") },
                dialogTitle = { Text("Select Difficulty") },
                confirmText = { TextButton(onClick = { it.value = false }) { Text("Change") } },
                summaryValue = { Text(vm.difficulty.name) },
                value = vm.difficulty,
                options = MasterMindDifficulty.values().toList(),
                updateValue = { d, s ->
                    vm.difficulty = d
                    s.value = false
                }
            )
            PreferenceSetting(
                settingTitle = { Text("Reset") },
                modifier = Modifier.clickable { showReset = true }
            )
        },
        topBarActions = {
            IconButton(onClick = { scope.launch { state.drawerState.open() } }) { Icon(Icons.Default.Settings, null) }
        },
        bottomBar = {
            val d = remember { Deck.defaultDeck() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                d.deck.groupBy { it.suit }.forEach { s ->
                    LazyColumn {
                        items(s.value) {
                            Card(
                                modifier = Modifier.size(50.dp),
                                onClick = { vm.guess(it) }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        it.toSymbolString(),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Button(
                        onClick = { vm.submitGuess() },
                        enabled = vm.currentSequence.size == vm.sequence.size
                    ) { Text("Submit") }
                    Button(
                        onClick = { vm.removeGuess() },
                        enabled = vm.currentSequence.isNotEmpty()
                    ) { Text("Remove") }
                }
            }
        }
    ) { p ->
        Column(modifier = Modifier.padding(p)) {
            LazyColumn {
                vm.guesses.entries.forEach {
                    item {
                        Card {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${it.key + 1}")
                                it.value.forEachIndexed { index, card ->
                                    Card(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .border(
                                                2.dp,
                                                animateColorAsState(
                                                    when (card) {
                                                        vm.sequence[index] -> Color.Green
                                                        in vm.sequence -> Color.Yellow
                                                        else -> Color.Transparent
                                                    }
                                                ).value,
                                                RoundedCornerShape(2.dp)
                                            )
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Text(
                                                card.toSymbolString(),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${vm.currentGuess + 1}")
                            vm.currentSequence.forEach {
                                Card(modifier = Modifier.size(50.dp)) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            it.toSymbolString(),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                            if (vm.sequence.size - vm.currentSequence.size > 0) {
                                repeat(vm.sequence.size - vm.currentSequence.size) {
                                    Card(modifier = Modifier.size(50.dp)) {
                                        Box(modifier = Modifier.fillMaxSize()) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}