package com.programmersbox.composefun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

val LocalDeck = compositionLocalOf { Deck.defaultDeck() }
val LocalCard = staticCompositionLocalOf { Card(1, Suit.SPADES) }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CompositionLocalScreen(navController: NavController) {
    val originalDeck = LocalDeck.current
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
            Column(
                modifier = Modifier
                    .padding(2.dp)
                    .padding(p),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Original Card | LocalChanged Card")
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    PlayingCard(LocalCard.current)
                    CompositionLocalProvider(LocalCard provides remember { Card.RandomCard }) {
                        PlayingCard(LocalCard.current)
                    }
                }

                Text("LocalChanged Deck")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(deck.deck) { PlayingCard(card = it) }
                }

                Text("Original Deck")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(originalDeck.deck) { PlayingCard(card = it) }
                }

            }
        }
    }
}