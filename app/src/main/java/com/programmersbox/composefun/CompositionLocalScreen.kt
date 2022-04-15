package com.programmersbox.composefun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

val LocalDeck = compositionLocalOf { Deck.defaultDeck() }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CompositionLocalScreen(navController: NavController) {
    CompositionLocalProvider(LocalDeck provides remember { Deck(listOf(Card.RandomCard, Card.RandomCard, Card.RandomCard)) }) {
        val deck = LocalDeck.current
        ScaffoldTop(
            screen = Screen.CompositionLocalScreen,
            navController = navController,
            bottomBar = {
                BottomAppBar {
                    Button(
                        onClick = { deck.add(Card.RandomCard) },
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .weight(1f)
                    ) { Text("Add a Card") }
                    Button(
                        onClick = { if (deck.isNotEmpty) deck.randomDraw() },
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .weight(1f)
                    ) { Text("Remove a Card") }
                }
            }
        ) { p ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                contentPadding = p,
                modifier = Modifier.padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) { items(deck.deck) { PlayingCard(card = it) } }
        }
    }
}