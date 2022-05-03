package com.programmersbox.composefun.games

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen
import com.programmersbox.composefun.ui.theme.ComposeFunTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextInt

enum class YahtzeeState { RollOne, RollTwo, RollThree, Stop }

class YahtzeeViewModel : ViewModel() {

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
        for (i in 0 until hand.size) {
            if (hand[i] !in hold) {
                hand[i].value = Random.nextInt(1..6)
            }
        }

        state = when (state) {
            YahtzeeState.RollOne -> YahtzeeState.RollTwo
            YahtzeeState.RollTwo -> YahtzeeState.RollThree
            YahtzeeState.RollThree -> YahtzeeState.Stop
            YahtzeeState.Stop -> YahtzeeState.RollOne
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
    }

}

class Dice(value: Int = Random.nextInt(1..6), val location: String) {
    var value by mutableStateOf(value)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun YahtzeeScreen(navController: NavController, vm: YahtzeeViewModel = viewModel()) {
    val context = LocalContext.current
    val dao = remember { YahtzeeDatabase.getInstance(context).yahtzeeDao() }
    val highScores by dao.getAllScores().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    val state = rememberScaffoldState()

    BackHandler(state.drawerState.isOpen) { scope.launch { state.drawerState.close() } }

    ScaffoldTop(
        scaffoldState = state,
        screen = Screen.YahtzeeScreen,
        navController = navController,
        drawer = {
            Scaffold(topBar = { TopAppBar { Text("High Scores") } }) { p ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = p
                ) {
                    items(highScores.sortedByDescending(YahtzeeScoreItem::score)) {
                        Card(elevation = 4.dp) {
                            ListItem(
                                text = { Text("Score: ${it.score}") },
                                overlineText = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Text("Time: ${SimpleDateFormat.getDateTimeInstance().format(it.time)}")
                                    } else {
                                        Text("Time: ${java.text.SimpleDateFormat.getDateTimeInstance().format(it.time)}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar {
                vm.hand.forEach {
                    Dice(
                        it,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .weight(1f)
                            .border(
                                width = animateDpAsState(targetValue = if (it in vm.hold) 4.dp else 0.dp).value,
                                color = animateColorAsState(targetValue = if (it in vm.hold) Color.Green else Color.Transparent).value,
                                shape = RoundedCornerShape(7.dp)
                            )
                    ) { if (it in vm.hold) vm.hold.remove(it) else vm.hold.add(it) }
                }

                IconButton(
                    onClick = vm::reroll,
                    modifier = Modifier.weight(1f),
                    enabled = vm.state != YahtzeeState.Stop
                ) { Icon(Icons.Default.PlayCircle, null) }
            }
        },
    ) { p ->

        var smallScore = vm.scores.run { ones + twos + threes + fours + fives + sixes }

        if (smallScore >= 63) smallScore += 35

        val largeScore = vm.scores.run { threeOfKind + fourOfKind + fullHouse + smallStraight + largeStraight + yahtzee + chance }

        if (
            vm.scores.run {
                placedYahtzee && placedChance &&
                        placedLargeStraight && placedSmallStraight &&
                        placedFullHouse &&
                        placedFourOfKind && placedThreeOfKind &&
                        placedOnes && placedTwos && placedThrees && placedFours && placedFives && placedSixes
            }
        ) {
            LaunchedEffect(Unit) { dao.insertScore(YahtzeeScoreItem(System.currentTimeMillis(), largeScore + smallScore)) }
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Game Over") },
                text = { Text("You got a score of ${largeScore + smallScore}") },
                confirmButton = { TextButton(onClick = vm::resetGame) { Text("Play Again") } },
                dismissButton = { TextButton(onClick = { navController.popBackStack() }) { Text("Stop Playing") } }
            )
        }

        Column(
            modifier = Modifier.padding(p),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    ScoreButton(
                        category = "Ones",
                        enabled = !vm.scores.placedOnes,
                        score = vm.scores.ones,
                        onClick = vm::placeOnes
                    )

                    ScoreButton(
                        category = "Twos",
                        enabled = !vm.scores.placedTwos,
                        score = vm.scores.twos,
                        onClick = vm::placeTwos
                    )

                    ScoreButton(
                        category = "Threes",
                        enabled = !vm.scores.placedThrees,
                        score = vm.scores.threes,
                        onClick = vm::placeThrees
                    )

                    ScoreButton(
                        category = "Fours",
                        enabled = !vm.scores.placedFours,
                        score = vm.scores.fours,
                        onClick = vm::placeFours
                    )

                    ScoreButton(
                        category = "Fives",
                        enabled = !vm.scores.placedFives,
                        score = vm.scores.fives,
                        onClick = vm::placeFives
                    )

                    ScoreButton(
                        category = "Sixes",
                        enabled = !vm.scores.placedSixes,
                        score = vm.scores.sixes,
                        onClick = vm::placeSixes
                    )

                    AnimatedVisibility(smallScore >= 63) { Text("$smallScore >= 63! +35") }

                    Text("Small Score: ${animateIntAsState(smallScore).value}")
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    ScoreButton(
                        category = "Three of a Kind",
                        enabled = !vm.scores.placedThreeOfKind,
                        score = vm.scores.threeOfKind,
                        canScore = vm.scores.canGetThreeKind(vm.hand) && vm.state != YahtzeeState.RollOne,
                        onClick = vm::placeThreeOfKind
                    )

                    ScoreButton(
                        category = "Four of a Kind",
                        enabled = !vm.scores.placedFourOfKind,
                        score = vm.scores.fourOfKind,
                        canScore = vm.scores.canGetFourKind(vm.hand) && vm.state != YahtzeeState.RollOne,
                        onClick = vm::placeFourOfKind
                    )

                    ScoreButton(
                        category = "Full House",
                        enabled = !vm.scores.placedFullHouse,
                        score = vm.scores.fullHouse,
                        canScore = vm.scores.canGetFullHouse(vm.hand) && vm.state != YahtzeeState.RollOne,
                        onClick = vm::placeFullHouse
                    )

                    ScoreButton(
                        category = "Small Straight",
                        enabled = !vm.scores.placedSmallStraight,
                        score = vm.scores.smallStraight,
                        canScore = vm.scores.canGetSmallStraight(vm.hand) && vm.state != YahtzeeState.RollOne,
                        onClick = vm::placeSmallStraight
                    )

                    ScoreButton(
                        category = "Large Straight",
                        enabled = !vm.scores.placedLargeStraight,
                        score = vm.scores.largeStraight,
                        canScore = vm.scores.canGetLargeStraight(vm.hand) && vm.state != YahtzeeState.RollOne,
                        onClick = vm::placeLargeStraight
                    )

                    ScoreButton(
                        category = "Yahtzee",
                        enabled = !vm.scores.placedYahtzee || vm.scores.canGetYahtzee(vm.hand) && vm.hand.none { it.value == 0 },
                        score = vm.scores.yahtzee,
                        canScore = vm.scores.canGetYahtzee(vm.hand) && vm.state != YahtzeeState.RollOne,
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

            Text("Total Score: ${animateIntAsState(largeScore + smallScore).value}")
        }
    }
}

@Composable
fun ScoreButton(category: String, enabled: Boolean, canScore: Boolean = false, score: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(
            ButtonDefaults.OutlinedBorderSize,
            animateColorAsState(if (canScore && enabled) Color.Green else MaterialTheme.colors.primary.copy(alpha = ButtonDefaults.OutlinedBorderOpacity)).value
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
        modifier = Modifier
            .size(50.dp)
            .then(modifier),
    ) { Box(contentAlignment = Alignment.Center) { Text(text = if (dice.value == 0) "" else dice.value.toString(), textAlign = TextAlign.Center) } }
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
                0 -> {/*ignore duplicates*/
                }
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
    version = 2,
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

    @Query("Select * from YahtzeeScores")
    fun getAllScores(): Flow<List<YahtzeeScoreItem>>

}

@Entity(tableName = "YahtzeeScores")
data class YahtzeeScoreItem(
    @PrimaryKey
    @ColumnInfo(name = "time")
    val time: Long,
    @ColumnInfo(name = "yahtzee_score")
    val score: Int
)