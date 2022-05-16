package com.programmersbox.composefun.games

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.programmersbox.composefun.M3ScaffoldTop
import com.programmersbox.composefun.Screen
import com.programmersbox.composefun.getApi
import kotlinx.coroutines.launch

data class Anagrams(val word: String?, val length: Number?, val conundrum: Boolean?)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun AnagramSolverScreen(navController: NavController) {
    var text by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var anagramList by remember { mutableStateOf<List<Anagrams>>(emptyList()) }

    M3ScaffoldTop(
        screen = Screen.AnagramSolverScreen,
        navController = navController,
        topBarActions = { Text("${anagramList.size} words") },
        bottomBar = {
            CustomBottomAppBar(
                icons = {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Enter Text") },
                        singleLine = true,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                            .imePadding()
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                anagramList = getAnagram(text).orEmpty()
                                isRefreshing = false
                            }
                        }
                    ) { Icon(Icons.Default.Search, null) }
                }
            )
        }
    ) { p ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = {},
            swipeEnabled = false,
            indicatorPadding = p
        ) {
            LazyColumn(
                contentPadding = p,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                anagramList.groupBy { it.length }.forEach {
                    stickyHeader {
                        SmallTopAppBar(
                            title = { Text("${it.key} letter words") },
                            actions = { Text("${it.value.size} words") }
                        )
                    }
                    items(it.value) { item -> AnagramWord(item = item) }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Composable
fun AnagramWord(item: Anagrams) {
    ElevatedCard {
        ListItem(
            text = { Text(item.word.orEmpty()) }
        )
    }
}

suspend fun getAnagram(letters: String) = getApi<List<Anagrams>>("https://danielthepope-countdown-v1.p.rapidapi.com/solve/$letters?variance=-1") {
    append("X-RapidAPI-Host", "danielthepope-countdown-v1.p.rapidapi.com")
    append("X-RapidAPI-Key", "cefe1904a6msh94a1484f93d57dbp16f734jsn098d9ecefd68")
}

// Padding minus IconButton's min touch target expansion
private val BottomAppBarHorizontalPadding = 16.dp - 12.dp

// Padding minus IconButton's min touch target expansion
private val BottomAppBarVerticalPadding = 16.dp - 12.dp

// Padding minus content padding
private val FABHorizontalPadding = 16.dp - BottomAppBarHorizontalPadding
private val FABVerticalPadding = 12.dp - BottomAppBarVerticalPadding

@Composable
fun CustomBottomAppBar(
    icons: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 3.dp,
    contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
) = CustomBottomAppBar(
    modifier = modifier,
    containerColor = containerColor,
    contentColor = contentColor,
    tonalElevation = tonalElevation,
    contentPadding = contentPadding
) {
    icons()
    if (floatingActionButton != null) {
        Spacer(Modifier.weight(1f, true))
        Box(
            Modifier
                .fillMaxHeight()
                .padding(
                    top = FABVerticalPadding,
                    end = FABHorizontalPadding
                ),
            contentAlignment = Alignment.TopStart
        ) { floatingActionButton() }
    }
}

@Composable
fun CustomBottomAppBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 3.dp,
    contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shape = AbsoluteCutCornerShape(0.dp),
        modifier = modifier
    ) {
        Row(
            Modifier
                .height(90.dp)
                .fillMaxWidth()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}