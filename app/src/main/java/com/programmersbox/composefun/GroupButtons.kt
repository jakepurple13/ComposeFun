package com.programmersbox.composefun

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

class GroupButtonModel<T>(val item: T, val iconContent: @Composable () -> Unit)

@Composable
fun <T> GroupButton(
    modifier: Modifier = Modifier,
    selected: T,
    options: List<GroupButtonModel<T>>,
    selectedColor: Color = MaterialTheme.colors.primaryVariant,
    unselectedColor: Color = MaterialTheme.colors.surface,
    shape: CornerBasedShape = RoundedCornerShape(20.0.dp),
    onClick: (T) -> Unit
) {
    Row(modifier) {
        val noCorner = CornerSize(0.dp)

        options.fastForEachIndexed { i, option ->
            OutlinedButton(
                modifier = Modifier,
                onClick = { onClick(option.item) },
                shape = shape.copy(
                    topStart = if (i == 0) shape.topStart else noCorner,
                    topEnd = if (i == options.size - 1) shape.topEnd else noCorner,
                    bottomStart = if (i == 0) shape.bottomStart else noCorner,
                    bottomEnd = if (i == options.size - 1) shape.bottomEnd else noCorner
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = animateColorAsState(if (selected == option.item) selectedColor else unselectedColor).value
                )
            ) { option.iconContent() }
        }
    }
}

@Composable
@Preview
fun GroupButtonScreen(navController: NavController = rememberNavController()) {
    ScaffoldTop(screen = Screen.GroupButtonScreen, navController = navController) { p ->
        Column(modifier = Modifier.padding(p)) {
            val list = listOf(0, 1, 2, 3, 4, 5)
            var f by remember { mutableStateOf(list.random()) }
            GroupButton(selected = f, options = list.map { GroupButtonModel(it) { Text("$it") } }) { f = it }
        }
    }
}
