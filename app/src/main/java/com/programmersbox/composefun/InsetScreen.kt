package com.programmersbox.composefun

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.rememberImeNestedScrollConnection

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimatedInsets::class)
@Composable
fun InsetScreen(navController: NavController) {
    ScaffoldTop(
        screen = Screen.InsetScreen,
        navController = navController,
        bottomBar = {
            Surface {
                var text by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding()
                )
            }
        },
    ) { p ->
        LazyColumn(
            contentPadding = p,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(connection = rememberImeNestedScrollConnection())
        ) {
            items(15) {
                Card {
                    ListItem(
                        text = { Text("Hello $it", modifier = Modifier.fillMaxWidth()) },
                        icon = { Icon(Icons.Default.BrokenImage, null) }
                    )
                }
            }
        }
    }
}
