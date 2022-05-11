package com.programmersbox.composefun.games

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen
import com.programmersbox.composefun.dataStore
import com.programmersbox.composefun.ui.theme.Alizarin
import com.programmersbox.composefun.ui.theme.ComposeFunTheme
import com.programmersbox.composefun.ui.theme.Emerald
import com.programmersbox.composefun.ui.theme.Sunflower
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextInt

private const val YAHTZEE_HIGH_SCORE_LIMIT = 25
private const val DOT_LOOK = "●"

enum class YahtzeeState { RollOne, RollTwo, RollThree, Stop }

class YahtzeeViewModel : ViewModel() {

    var rolling by mutableStateOf(false)

    var showGameOverDialog by mutableStateOf(true)

    var state by mutableStateOf(YahtzeeState.RollOne)

    val scores = YahtzeeScores()

    val hand = mutableStateListOf(
        Dice(0, location = "1"),
        Dice(0, location = "2"),
        Dice(0, location = "3"),
        Dice(0, location = "4"),
        Dice(0, location = "5")
    )

    val hold = mutableStateListOf<Dice>()

    fun reroll() {
        viewModelScope.launch {
            rolling = true
            (0 until hand.size).map { i ->
                async(Dispatchers.IO) {
                    if (hand[i] !in hold) {
                        for (d in 0..5) {
                            delay(50L)
                            hand[i].value = Random.nextInt(1..6)
                        }
                    }
                }
            }.awaitAll()
            rolling = false
            state = when (state) {
                YahtzeeState.RollOne -> YahtzeeState.RollTwo
                YahtzeeState.RollTwo -> YahtzeeState.RollThree
                YahtzeeState.RollThree -> YahtzeeState.Stop
                YahtzeeState.Stop -> YahtzeeState.RollOne
            }
        }
    }

    fun placeOnes() {
        scores.getOnes(hand)
        reset()
    }

    fun placeTwos() {
        scores.getTwos(hand)
        reset()
    }

    fun placeThrees() {
        scores.getThrees(hand)
        reset()
    }

    fun placeFours() {
        scores.getFours(hand)
        reset()
    }

    fun placeFives() {
        scores.getFives(hand)
        reset()
    }

    fun placeSixes() {
        scores.getSixes(hand)
        reset()
    }

    fun placeThreeOfKind() {
        scores.getThreeOfAKind(hand)
        reset()
    }

    fun placeFourOfKind() {
        scores.getFourOfAKind(hand)
        reset()
    }

    fun placeFullHouse() {
        scores.getFullHouse(hand)
        reset()
    }

    fun placeSmallStraight() {
        scores.getSmallStraight(hand)
        reset()
    }

    fun placeLargeStraight() {
        scores.getLargeStraight(hand)
        reset()
    }

    fun placeYahtzee() {
        scores.getYahtzee(hand)
        reset()
    }

    fun placeChance() {
        scores.getChance(hand)
        reset()
    }

    fun reset() {
        hold.clear()
        hand.forEach { it.value = 0 }
        state = YahtzeeState.RollOne
    }

    fun resetGame() {
        reset()
        scores.resetScores()
        showGameOverDialog = true
    }

}

class Dice(value: Int = Random.nextInt(1..6), val location: String) {
    var value by mutableStateOf(value)
}

val DICE_LOOK = booleanPreferencesKey("dice_look")
val DataStore<Preferences>.diceLook get() = data.map { it[DICE_LOOK] ?: true }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun YahtzeeScreen(navController: NavController, vm: YahtzeeViewModel = viewModel()) {
    val context = LocalContext.current
    val dao = remember { YahtzeeDatabase.getInstance(context).yahtzeeDao() }

    val scope = rememberCoroutineScope()
    val state = rememberScaffoldState()

    BackHandler(state.drawerState.isOpen) { scope.launch { state.drawerState.close() } }

    val diceLook by context.dataStore.diceLook.collectAsState(initial = false)

    var smallScore = vm.scores.run { ones + twos + threes + fours + fives + sixes }
    if (smallScore >= 63) smallScore += 35
    val largeScore = vm.scores.run { threeOfKind + fourOfKind + fullHouse + smallStraight + largeStraight + yahtzee + chance }

    var newGameDialog by remember { mutableStateOf(false) }

    if (newGameDialog) {
        AlertDialog(
            onDismissRequest = { newGameDialog = false },
            title = { Text("Want to start a new game?") },
            text = { Text("You have ${largeScore + smallScore} points. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.resetGame()
                        newGameDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = { newGameDialog = false }) { Text("No") } }
        )
    }

    ScaffoldTop(
        scaffoldState = state,
        screen = Screen.YahtzeeScreen,
        navController = navController,
        drawer = {
            val highScores by dao.getAllScores().collectAsState(initial = emptyList())

            LaunchedEffect(highScores) { highScores.drop(YAHTZEE_HIGH_SCORE_LIMIT).fastMap { dao.deleteScore(it) } }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("High Scores") },
                        actions = { Text(highScores.size.toString()) }
                    )
                }
            ) { p ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = p
                ) {
                    item {
                        Card(
                            elevation = 10.dp,
                            border = BorderStroke(2.dp, MaterialTheme.colors.background),
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.toggleable(diceLook) { b -> scope.launch { context.dataStore.edit { it[DICE_LOOK] = b } } }
                            ) {
                                Text(if (diceLook) "Numbers" else "Dots", modifier = Modifier.padding(end = 2.dp))
                                Switch(checked = diceLook, onCheckedChange = null)
                            }
                        }
                    }

                    items(highScores) { HighScoreItem(it) { scope.launch { dao.deleteScore(it) } } }
                }
            }
        },
        topBarActions = {
            IconButton(onClick = { newGameDialog = true }) { Icon(Icons.Default.OpenInNew, null) }
            IconButton(onClick = { scope.launch { state.drawerState.open() } }) { Icon(Icons.Default.Settings, null) }
        },
        bottomBar = { BottomBarDiceRow(vm, diceLook) },
    ) { p ->
        if (
            vm.scores.run {
                placedYahtzee && placedChance &&
                        placedLargeStraight && placedSmallStraight &&
                        placedFullHouse &&
                        placedFourOfKind && placedThreeOfKind &&
                        placedOnes && placedTwos && placedThrees && placedFours && placedFives && placedSixes &&
                        vm.showGameOverDialog
            }
        ) {
            LaunchedEffect(Unit) {
                dao.insertScore(
                    YahtzeeScoreItem(
                        time = System.currentTimeMillis(),
                        score = largeScore + smallScore,
                        ones = vm.scores.ones,
                        twos = vm.scores.twos,
                        threes = vm.scores.threes,
                        fours = vm.scores.fours,
                        fives = vm.scores.fives,
                        sixes = vm.scores.sixes,
                        threeKind = vm.scores.threeOfKind,
                        fourKind = vm.scores.fourOfKind,
                        fullHouse = vm.scores.fullHouse,
                        smallStraight = vm.scores.smallStraight,
                        largeStraight = vm.scores.largeStraight,
                        yahtzee = vm.scores.yahtzee,
                        chance = vm.scores.chance
                    )
                )
            }

            AlertDialog(
                onDismissRequest = { vm.showGameOverDialog = false },
                title = { Text("Game Over") },
                text = { Text("You got a score of ${largeScore + smallScore}") },
                confirmButton = { TextButton(onClick = vm::resetGame) { Text("Play Again") } },
                dismissButton = {
                    TextButton(
                        onClick = {
                            navController.popBackStack()
                            vm.showGameOverDialog = false
                        }
                    ) { Text("Stop Playing") }
                }
            )
        }

        Column(
            modifier = Modifier.padding(p),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                SmallScores(vm, smallScore)
                LargeScores(vm, largeScore)
            }

            Text("Total Score: ${animateIntAsState(largeScore + smallScore).value}")
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun BottomBarDiceRow(vm: YahtzeeViewModel, diceLooks: Boolean) {
    BottomAppBar {
        vm.hand.forEach {

            val customModifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(1f)
                .border(
                    width = animateDpAsState(targetValue = if (it in vm.hold) 4.dp else 0.dp).value,
                    color = animateColorAsState(targetValue = if (it in vm.hold) Emerald else Color.Transparent).value,
                    shape = RoundedCornerShape(7.dp)
                )

            if (diceLooks) {
                Dice(
                    it,
                    modifier = customModifier
                ) { if (it in vm.hold) vm.hold.remove(it) else vm.hold.add(it) }
            } else {
                DiceDots(
                    it,
                    modifier = customModifier
                ) { if (it in vm.hold) vm.hold.remove(it) else vm.hold.add(it) }
            }
        }

        IconButton(
            onClick = vm::reroll,
            modifier = Modifier.weight(1f),
            enabled = vm.state != YahtzeeState.Stop
        ) {
            Icon(
                Icons.Default.PlayCircle,
                null,
                tint = animateColorAsState(
                    when (vm.state) {
                        YahtzeeState.RollOne -> Emerald
                        YahtzeeState.RollTwo -> Sunflower
                        YahtzeeState.RollThree -> Alizarin
                        YahtzeeState.Stop -> LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                    }
                ).value
            )
        }
    }
}

@Composable
fun RowScope.SmallScores(vm: YahtzeeViewModel, smallScore: Int) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.Start
    ) {
        val groupedCheck = vm.hand.groupingBy { it.value }
            .eachCount()
            .toList()
            .sortedWith(compareBy({ it.second }, { it.first }))
            .reversed()
            .map { it.first }

        val highest = groupedCheck.elementAtOrNull(0)
        val medium = groupedCheck.elementAtOrNull(1)
        val lowest = groupedCheck.elementAtOrNull(2)

        fun canScore(value: Int) = highest == value || medium == value || lowest == value
        fun scoreColor(value: Int) = when {
            highest == value -> Emerald
            medium == value -> Sunflower
            lowest == value -> Alizarin
            else -> Color.Transparent
        }

        ScoreButton(
            category = "Ones",
            enabled = !vm.scores.placedOnes,
            score = vm.scores.ones,
            canScore = canScore(1) && !vm.rolling,
            customBorderColor = scoreColor(1),
            onClick = vm::placeOnes
        )

        ScoreButton(
            category = "Twos",
            enabled = !vm.scores.placedTwos,
            score = vm.scores.twos,
            canScore = canScore(2) && !vm.rolling,
            customBorderColor = scoreColor(2),
            onClick = vm::placeTwos
        )

        ScoreButton(
            category = "Threes",
            enabled = !vm.scores.placedThrees,
            score = vm.scores.threes,
            canScore = canScore(3) && !vm.rolling,
            customBorderColor = scoreColor(3),
            onClick = vm::placeThrees
        )

        ScoreButton(
            category = "Fours",
            enabled = !vm.scores.placedFours,
            score = vm.scores.fours,
            canScore = canScore(4) && !vm.rolling,
            customBorderColor = scoreColor(4),
            onClick = vm::placeFours
        )

        ScoreButton(
            category = "Fives",
            enabled = !vm.scores.placedFives,
            score = vm.scores.fives,
            canScore = canScore(5) && !vm.rolling,
            customBorderColor = scoreColor(5),
            onClick = vm::placeFives
        )

        ScoreButton(
            category = "Sixes",
            enabled = !vm.scores.placedSixes,
            score = vm.scores.sixes,
            canScore = canScore(6) && !vm.rolling,
            customBorderColor = scoreColor(6),
            onClick = vm::placeSixes
        )

        AnimatedVisibility(smallScore >= 63) { Text("+35 for >= 63") }

        val originalScore = if (smallScore >= 63) " (${animateIntAsState(smallScore).value - 35})" else ""
        Text("Small Score: ${animateIntAsState(smallScore).value}$originalScore")
    }
}

@Composable
fun RowScope.LargeScores(vm: YahtzeeViewModel, largeScore: Int) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.End
    ) {
        ScoreButton(
            category = "Three of a Kind",
            enabled = !vm.scores.placedThreeOfKind,
            score = vm.scores.threeOfKind,
            canScore = vm.scores.canGetThreeKind(vm.hand) && vm.state != YahtzeeState.RollOne && !vm.rolling,
            onClick = vm::placeThreeOfKind
        )

        ScoreButton(
            category = "Four of a Kind",
            enabled = !vm.scores.placedFourOfKind,
            score = vm.scores.fourOfKind,
            canScore = vm.scores.canGetFourKind(vm.hand) && vm.state != YahtzeeState.RollOne && !vm.rolling,
            onClick = vm::placeFourOfKind
        )

        ScoreButton(
            category = "Full House",
            enabled = !vm.scores.placedFullHouse,
            score = vm.scores.fullHouse,
            canScore = vm.scores.canGetFullHouse(vm.hand) && vm.state != YahtzeeState.RollOne && !vm.rolling,
            onClick = vm::placeFullHouse
        )

        ScoreButton(
            category = "Small Straight",
            enabled = !vm.scores.placedSmallStraight,
            score = vm.scores.smallStraight,
            canScore = vm.scores.canGetSmallStraight(vm.hand) && vm.state != YahtzeeState.RollOne && !vm.rolling,
            onClick = vm::placeSmallStraight
        )

        ScoreButton(
            category = "Large Straight",
            enabled = !vm.scores.placedLargeStraight,
            score = vm.scores.largeStraight,
            canScore = vm.scores.canGetLargeStraight(vm.hand) && vm.state != YahtzeeState.RollOne && !vm.rolling,
            onClick = vm::placeLargeStraight
        )

        ScoreButton(
            category = "Yahtzee",
            enabled = !vm.scores.placedYahtzee || vm.scores.canGetYahtzee(vm.hand) && vm.hand.none { it.value == 0 },
            score = vm.scores.yahtzee,
            canScore = vm.scores.canGetYahtzee(vm.hand) && vm.state != YahtzeeState.RollOne && !vm.rolling,
            onClick = vm::placeYahtzee
        )

        ScoreButton(
            category = "Chance",
            enabled = !vm.scores.placedChance,
            score = vm.scores.chance,
            onClick = vm::placeChance
        )

        Text("Large Score: ${animateIntAsState(largeScore).value}")
    }
}

@ExperimentalMaterialApi
@Composable
fun HighScoreItem(item: YahtzeeScoreItem, onDelete: () -> Unit) {
    var deleteDialog by remember { mutableStateOf(false) }

    val time = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat.getDateTimeInstance().format(item.time)
        } else {
            java.text.SimpleDateFormat.getDateTimeInstance().format(item.time)
        }
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text("Delete ${item.score} at $time") },
            text = { Text("Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        deleteDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = { deleteDialog = false }) { Text("No") } }
        )
    }

    var showMore by remember { mutableStateOf(false) }

    Card(
        elevation = 10.dp,
        border = BorderStroke(2.dp, MaterialTheme.colors.background)
    ) {
        Column {
            ListItem(
                icon = { IconButton(onClick = { deleteDialog = true }) { Icon(Icons.Default.Close, null) } },
                text = { Text("Score: ${item.score}") },
                overlineText = { Text("Time: $time") },
                trailing = {
                    IconButton(
                        onClick = { showMore = !showMore },
                        modifier = Modifier.rotate(animateFloatAsState(targetValue = if (showMore) 180f else 0f).value)
                    ) { Icon(Icons.Default.ArrowDropDown, null) }
                }
            )
            AnimatedVisibility(visible = showMore) {
                Divider()
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Ones: ${item.ones}")
                        Text("Twos: ${item.twos}")
                        Text("Threes: ${item.threes}")
                        Text("Fours: ${item.fours}")
                        Text("Fives: ${item.fives}")
                        Text("Sixes: ${item.sixes}")
                        val smallScore = with(item) { ones + twos + threes + fours + fives + sixes }
                        if (smallScore >= 63) {
                            Text("+35 for >= 63")
                        }
                        val originalScore = if (smallScore >= 63) " ($smallScore)" else ""
                        Text("Small Score: ${if (smallScore >= 63) smallScore + 35 else smallScore}$originalScore")
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("Three of a Kind: ${item.threeKind}")
                        Text("Four of a Kind: ${item.fourKind}")
                        Text("Full House: ${item.fullHouse}")
                        Text("Small Straight: ${item.smallStraight}")
                        Text("Large Straight: ${item.largeStraight}")
                        Text("Yahtzee: ${item.yahtzee}")
                        Text("Chance: ${item.chance}")
                        val largeScore = with(item) { threeKind + fourKind + fullHouse + smallStraight + largeStraight + yahtzee + chance }
                        Text("Large Score: $largeScore")
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreButton(
    category: String,
    enabled: Boolean,
    canScore: Boolean = false,
    customBorderColor: Color = Emerald,
    score: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(
            ButtonDefaults.OutlinedBorderSize,
            animateColorAsState(
                if (canScore && enabled) customBorderColor
                else MaterialTheme.colors.primary.copy(alpha = ButtonDefaults.OutlinedBorderOpacity)
            ).value
        )
    ) { Text("$category: ${animateIntAsState(score).value}") }
}

@ExperimentalMaterialApi
@Composable
fun Dice(dice: Dice, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        elevation = 5.dp,
        enabled = dice.value != 0,
        modifier = Modifier
            .size(50.dp)
            .then(modifier),
    ) { Box(contentAlignment = Alignment.Center) { Text(text = if (dice.value == 0) "" else dice.value.toString(), textAlign = TextAlign.Center) } }
}

@ExperimentalMaterialApi
@Composable
fun DiceDots(dice: Dice, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        elevation = 5.dp,
        enabled = dice.value != 0,
        modifier = Modifier
            .size(50.dp)
            .then(modifier),
    ) {
        when (dice.value) {
            1 -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) { Text(DOT_LOOK, textAlign = TextAlign.Center) }
            }
            2 -> {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            3 -> {
                Box(modifier = Modifier.padding(4.dp)) {
                    Text(DOT_LOOK, modifier = Modifier.align(Alignment.TopEnd), textAlign = TextAlign.Center)
                    Text(DOT_LOOK, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
                    Text(DOT_LOOK, modifier = Modifier.align(Alignment.BottomStart), textAlign = TextAlign.Center)
                }
            }
            4 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
            5 -> {
                Box(modifier = Modifier.padding(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                    Text(DOT_LOOK, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
            6 -> {
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(DOT_LOOK, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
@Preview
fun DicePreview() {
    ComposeFunTheme {
        Row {
            DiceDots(dice = Dice(1, "1"))
            DiceDots(dice = Dice(2, "2"))
            DiceDots(dice = Dice(3, "3"))
            DiceDots(dice = Dice(4, "4"))
            DiceDots(dice = Dice(5, "5"))
            DiceDots(dice = Dice(6, "6"))
        }
    }
}

class YahtzeeScores {
    var placedThreeOfKind by mutableStateOf(false)
    var threeOfKind by mutableStateOf(0)
    var placedFourOfKind by mutableStateOf(false)
    var fourOfKind by mutableStateOf(0)
    var placedFullHouse by mutableStateOf(false)
    var fullHouse by mutableStateOf(0)
    var placedSmallStraight by mutableStateOf(false)
    var smallStraight by mutableStateOf(0)
    var placedLargeStraight by mutableStateOf(false)
    var largeStraight by mutableStateOf(0)
    var placedYahtzee by mutableStateOf(false)
    var yahtzee by mutableStateOf(0)
    var placedChance by mutableStateOf(false)
    var chance by mutableStateOf(0)

    var placedOnes by mutableStateOf(false)
    var ones by mutableStateOf(0)
    var placedTwos by mutableStateOf(false)
    var twos by mutableStateOf(0)
    var placedThrees by mutableStateOf(false)
    var threes by mutableStateOf(0)
    var placedFours by mutableStateOf(false)
    var fours by mutableStateOf(0)
    var placedFives by mutableStateOf(false)
    var fives by mutableStateOf(0)
    var placedSixes by mutableStateOf(false)
    var sixes by mutableStateOf(0)

    private fun getSmallNum(dice: Collection<Dice>, num: Int): Int = dice.filter { it.value == num }.sumOf { it.value }

    fun getOnes(dice: Collection<Dice>): Int = getSmallNum(dice, 1).apply {
        ones = this
        placedOnes = true
    }

    fun getTwos(dice: Collection<Dice>): Int = getSmallNum(dice, 2).apply {
        twos = this
        placedTwos = true
    }

    fun getThrees(dice: Collection<Dice>): Int = getSmallNum(dice, 3).apply {
        threes = this
        placedThrees = true
    }

    fun getFours(dice: Collection<Dice>): Int = getSmallNum(dice, 4).apply {
        fours = this
        placedFours = true
    }

    fun getFives(dice: Collection<Dice>): Int = getSmallNum(dice, 5).apply {
        fives = this
        placedFives = true
    }

    fun getSixes(dice: Collection<Dice>): Int = getSmallNum(dice, 6).apply {
        sixes = this
        placedSixes = true
    }

    fun canGetThreeKind(dice: Collection<Dice>): Boolean {
        val values = dice.groupingBy { it.value }.eachCount().values
        return 3 in values || 4 in values || 5 in values
    }

    fun getThreeOfAKind(dice: Collection<Dice>): Int = if (canGetThreeKind(dice)) {
        dice.sumOf { it.value }
    } else {
        0
    }.apply {
        threeOfKind = this
        placedThreeOfKind = true
    }

    fun canGetFourKind(dice: Collection<Dice>): Boolean {
        val values = dice.groupingBy { it.value }.eachCount().values
        return 4 in values || 5 in values
    }

    fun getFourOfAKind(dice: Collection<Dice>): Int = if (canGetFourKind(dice)) {
        dice.sumOf { it.value }
    } else {
        0
    }.apply {
        fourOfKind = this
        placedFourOfKind = true
    }

    fun canGetYahtzee(dice: Collection<Dice>): Boolean = 5 in dice.groupingBy { it.value }.eachCount().values

    fun getYahtzee(dice: Collection<Dice>): Int = (if (canGetYahtzee(dice)) if (placedYahtzee) 100 else 50 else 0)
        .apply {
            placedYahtzee = true
            yahtzee += this
        }

    fun canGetFullHouse(dice: Collection<Dice>): Boolean {
        val values = dice.groupingBy { it.value }.eachCount().values
        return 3 in values && 2 in values
    }

    fun getFullHouse(dice: Collection<Dice>): Int = (if (canGetFullHouse(dice)) 25 else 0).apply {
        fullHouse = this
        placedFullHouse = true
    }

    fun canGetLargeStraight(dice: Collection<Dice>): Boolean {
        val filteredDice = dice.sortedBy { it.value }
        return longestSequence(filteredDice.toTypedArray()) == 4
    }

    fun getLargeStraight(dice: Collection<Dice>): Int = (if (canGetLargeStraight(dice)) 40 else 0).apply {
        largeStraight = this
        placedLargeStraight = true
    }

    fun canGetSmallStraight(dice: Collection<Dice>): Boolean {
        val filteredDice = dice.sortedBy { it.value }
        return longestSequence(filteredDice.toTypedArray()) in 3..4
    }

    fun getSmallStraight(dice: Collection<Dice>): Int = (if (canGetSmallStraight(dice)) 30 else 0).apply {
        smallStraight = this
        placedSmallStraight = true
    }

    fun getChance(dice: Collection<Dice>): Int = dice.sumOf { it.value }.apply {
        chance = this
        placedChance = true
    }

    private fun longestSequence(a: Array<Dice>): Int {
        Arrays.sort(a, compareBy { it.value })
        var longest = 0
        var sequence = 0
        for (i in 1 until a.size) {
            when (a[i].value - a[i - 1].value) {
                0 -> Unit/*ignore duplicates*/
                1 -> sequence += 1
                else -> if (sequence > longest) {
                    longest = sequence
                    sequence = 0
                }
            }
        }
        return max(longest, sequence)
    }

    fun resetScores() {
        yahtzee = 0
        placedYahtzee = false
        chance = 0
        placedChance = false
        largeStraight = 0
        placedLargeStraight = false
        smallStraight = 0
        placedSmallStraight = false
        fullHouse = 0
        placedFullHouse = false
        fourOfKind = 0
        placedFourOfKind = false
        threeOfKind = 0
        placedThreeOfKind = false
        sixes = 0
        placedSixes = false
        fives = 0
        placedFives = false
        fours = 0
        placedFours = false
        threes = 0
        placedThrees = false
        twos = 0
        placedTwos = false
        ones = 0
        placedOnes = false
    }

}

@Preview
@Composable
fun YahtzeePreview() {
    ComposeFunTheme {
        YahtzeeScreen(navController = rememberNavController())
    }
}

@Database(
    entities = [YahtzeeScoreItem::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 2, to = 4), AutoMigration(from = 3, to = 4)]
)
abstract class YahtzeeDatabase : RoomDatabase() {

    abstract fun yahtzeeDao(): YahtzeeDao

    companion object {

        @Volatile
        private var INSTANCE: YahtzeeDatabase? = null

        fun getInstance(context: Context): YahtzeeDatabase =
            INSTANCE ?: synchronized(this) { INSTANCE ?: buildDatabase(context).also { INSTANCE = it } }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, YahtzeeDatabase::class.java, "yahtzee.db")
                .build()
    }

}

@Dao
interface YahtzeeDao {

    @Insert
    suspend fun insertScore(item: YahtzeeScoreItem)

    @Query("select * from YahtzeeScores order by yahtzee_score desc")
    fun getAllScores(): Flow<List<YahtzeeScoreItem>>

    @Delete
    suspend fun deleteScore(item: YahtzeeScoreItem)

}

@Entity(tableName = "YahtzeeScores")
data class YahtzeeScoreItem(
    @PrimaryKey
    @ColumnInfo(name = "time")
    val time: Long,
    @ColumnInfo(name = "yahtzee_score")
    val score: Int,
    @ColumnInfo(name = "ones", defaultValue = "0")
    val ones: Int,
    @ColumnInfo(name = "twos", defaultValue = "0")
    val twos: Int,
    @ColumnInfo(name = "threes", defaultValue = "0")
    val threes: Int,
    @ColumnInfo(name = "fours", defaultValue = "0")
    val fours: Int,
    @ColumnInfo(name = "fives", defaultValue = "0")
    val fives: Int,
    @ColumnInfo(name = "sixes", defaultValue = "0")
    val sixes: Int,
    @ColumnInfo(name = "threeofakind", defaultValue = "0")
    val threeKind: Int,
    @ColumnInfo(name = "fourofakind", defaultValue = "0")
    val fourKind: Int,
    @ColumnInfo(name = "fullhouse", defaultValue = "0")
    val fullHouse: Int,
    @ColumnInfo(name = "smallStraight", defaultValue = "0")
    val smallStraight: Int,
    @ColumnInfo(name = "largeStraight", defaultValue = "0")
    val largeStraight: Int,
    @ColumnInfo(name = "yahtzee", defaultValue = "0")
    val yahtzee: Int,
    @ColumnInfo(name = "chance", defaultValue = "0")
    val chance: Int
)