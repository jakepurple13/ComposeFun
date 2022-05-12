package com.programmersbox.composefun.games

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.navigation.NavController
import com.programmersbox.composefun.ScaffoldTop
import com.programmersbox.composefun.Screen
import com.programmersbox.composefun.dataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt

val DICE_ROLLER_COUNT = intPreferencesKey("dice_roller_count")
val Context.diceRollerCount get() = dataStore.data.map { it[DICE_ROLLER_COUNT] ?: 1 }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiceRollerScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val diceLook by context.dataStore.diceLook.collectAsState(initial = false)
    val diceRollerCount by context.diceRollerCount.collectAsState(initial = 1)

    var rollAll by remember { mutableStateOf(0) }

    ScaffoldTop(
        screen = Screen.DiceRollerScreen,
        navController = navController,
        topBarActions = {
            IconButton(onClick = { rollAll++ }) { Icon(Icons.Default.Casino, null) }
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
                    onClick = { scope.launch { context.dataStore.edit { it[DICE_ROLLER_COUNT] = diceRollerCount - 1 } } },
                    modifier = Modifier.weight(1f)
                ) { Icon(Icons.Default.RemoveCircle, null) }
                Text(
                    diceRollerCount.toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { scope.launch { context.dataStore.edit { it[DICE_ROLLER_COUNT] = diceRollerCount + 1 } } },
                    modifier = Modifier.weight(1f)
                ) { Icon(Icons.Default.AddCircle, null) }
            }
        }
    ) { p ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(56.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = p
        ) {
            items(diceRollerCount) {
                val dice = remember { Dice(Random.nextInt(1..6), "") }
                var count by remember { mutableStateOf(0) }

                LaunchedEffect(count, rollAll) {
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