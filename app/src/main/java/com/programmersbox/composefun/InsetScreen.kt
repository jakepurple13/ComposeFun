package com.programmersbox.composefun

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    Scaffold(
        topBar = {
            com.google.accompanist.insets.ui.TopAppBar(
                title = { Text(Screen.InsetScreen.name) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                contentPadding = WindowInsets.statusBars.asPaddingValues()
            )
        },
        bottomBar = {
            Surface {
                var text by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Watch me animate...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding()
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { p ->
        Column {
            LazyColumn(
                contentPadding = p,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(connection = rememberImeNestedScrollConnection())
            ) {
                items(40) {
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
}
