package com.programmersbox.composefun.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow

@ExperimentalMaterialApi
@Composable
fun PlayingCard(card: Card, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        elevation = 5.dp,
        modifier = Modifier
            .size(100.dp, 150.dp)
            .then(modifier),
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = card.toSymbolString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                textAlign = TextAlign.Start
            )
            FlowRow(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { repeat(card.value) { Text(text = card.suit.unicodeSymbol, textAlign = TextAlign.Center) } }
            Text(
                text = card.toSymbolString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun EmptyCard(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        elevation = 5.dp,
        modifier = Modifier
            .size(100.dp, 150.dp)
            .then(modifier)
    ) {}
}