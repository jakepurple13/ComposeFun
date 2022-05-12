package com.programmersbox.composefun.games

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen
import com.programmersbox.composefun.dataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt

val DICE_ROLLER_COUNT = intPreferencesKey("dice_roller_count")
val Context.diceRollerCount get() = dataStore.data.map { it[DICE_ROLLER_COUNT] ?: 1 }

class DiceRollerViewModel : ViewModel() {

    val diceList = mutableStateListOf<Dice>()
    var rollAll by mutableStateOf(0)

    fun setup(count: Int) {
        repeat(count) { diceList.add(Dice(Random.nextInt(1..6), "")) }
    }

    fun addDice() {
        diceList.add(Dice(Random.nextInt(1..6), ""))
    }

    fun removeDice() {
        diceList.removeLastOrNull()
    }

    fun rollAll() {
        rollAll++
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiceRollerScreen(navController: NavController, vm: DiceRollerViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val diceLook by context.dataStore.diceLook.collectAsState(initial = false)
    val diceRollerCount by context.diceRollerCount.collectAsState(initial = 1)
    LaunchedEffect(Unit) { vm.setup(context.diceRollerCount.first()) }

    ScaffoldTop(
        screen = Screen.DiceRollerScreen,
        navController = navController,
        topBarActions = {
            IconButton(onClick = { vm.rollAll() }) { Icon(Icons.Default.Casino, null) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.toggleable(diceLook) { b -> scope.launch { context.dataStore.edit { it[DICE_LOOK] = b } } }
            ) {
                Text(if (diceLook) "Numbers" else "Dots", modifier = Modifier.padding(end = 2.dp))
                Switch(checked = diceLook, onCheckedChange = null)
            }
        },
        bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = {
                        vm.removeDice()
                        if (diceRollerCount > 1) scope.launch { context.dataStore.edit { it[DICE_ROLLER_COUNT] = diceRollerCount - 1 } }
                    },
                    modifier = Modifier.weight(1f)
                ) { Icon(Icons.Default.RemoveCircle, null) }
                Text(
                    diceRollerCount.toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = {
                        vm.addDice()
                        scope.launch { context.dataStore.edit { it[DICE_ROLLER_COUNT] = diceRollerCount + 1 } }
                    },
                    modifier = Modifier.weight(1f)
                ) { Icon(Icons.Default.AddCircle, null) }
            }
        },
        drawer = {
            TopAppBar { Text("Stats") }
            ListItem { Text("Sum of current dice: ${vm.diceList.sumOf { it.value }}") }

            @Composable
            fun CountDice(value: Int) {
                val countDice = vm.diceList.count { it.value == value }
                ListItem { Text("$value dice: $countDice == ${countDice * value}") }
            }
            CountDice(1)
            CountDice(2)
            CountDice(3)
            CountDice(4)
            CountDice(5)
            CountDice(6)
        }
    ) { p ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(56.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = p
        ) {
            items(vm.diceList) { dice ->
                var count by remember { mutableStateOf(0) }

                LaunchedEffect(count, vm.rollAll) {
                    for (d in 0..5) {
                        delay(50L)
                        dice.value = Random.nextInt(1..6)
                    }
                }

                if (diceLook) {
                    Dice(dice = dice) { count++ }
                } else {
                    DiceDots(dice = dice) { count++ }
                }
            }
        }
    }
}