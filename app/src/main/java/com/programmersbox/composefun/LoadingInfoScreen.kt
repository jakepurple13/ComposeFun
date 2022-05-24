package com.programmersbox.composefun

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class LoadingState {
    Loading, LoadingComplete, Loaded;

    fun next() = when (this) {
        Loading -> LoadingComplete
        LoadingComplete -> Loaded
        Loaded -> Loading
    }
}

class LoadingViewModel : ViewModel() {

    var showLoading by mutableStateOf(LoadingState.Loading)
    var loadingProgress by mutableStateOf(0f)

    suspend fun reset() {
        showLoading = LoadingState.Loading
        loadingProgress = 0f
        delay(5000)
        showLoading = LoadingState.LoadingComplete
        withContext(Dispatchers.Default) {
            repeat(100) {
                loadingProgress += 0.01f
                delay(10)
            }
        }
        delay(1000)
        showLoading = LoadingState.Loaded
    }
}

@Composable
fun LoadingInfoScreen(navController: NavController, vm: LoadingViewModel = viewModel()) {

    var primaryColor by remember { mutableStateOf(Random.nextColor(a = 255)) }
    var backgroundColor by remember { mutableStateOf(Random.nextColor(a = 255)) }

    val primaryColorAnimation by animateColorAsState(targetValue = primaryColor)
    val backgroundColorAnimation by animateColorAsState(targetValue = backgroundColor)
    val scope = rememberCoroutineScope()
    val animatedProgress by animateFloatAsState(targetValue = vm.loadingProgress, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec)

    LaunchedEffect(Unit) { vm.reset() }

    M3ScaffoldTop(
        screen = Screen.LoadingInfoScreen,
        navController = navController,
        bottomBar = {
            BottomAppBar(
                icons = {
                    Button(
                        onClick = {
                            primaryColor = Random.nextColor(a = 255)
                            backgroundColor = Random.nextColor(a = 255)
                        }
                    ) {
                        Icon(Icons.Default.Shuffle, null)
                        Text("Random Colors")
                    }
                },
                floatingActionButton = { ExtendedFloatingActionButton(onClick = { scope.launch { vm.reset() } }) { Text("Show Loader") } }
            )
        }
    ) { p ->
        Box(
            modifier = Modifier
                .padding(p)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (vm.showLoading) {
                LoadingState.Loading -> {
                    DiamondProgressIndicator(
                        innerColor = primaryColorAnimation,
                        outerColor = backgroundColorAnimation,
                        modifier = Modifier.size(100.dp),
                        animationSpec = tween(1500)
                    )
                }
                LoadingState.LoadingComplete -> {
                    DiamondProgressIndicator(
                        progress = animatedProgress,
                        innerColor = primaryColorAnimation,
                        outerColor = backgroundColorAnimation,
                        modifier = Modifier.size(100.dp)
                    )
                }
                LoadingState.Loaded -> {
                    Text("Complete!")
                }
            }
        }

    }
}

@Composable
fun SatisfyingScreen(navController: NavController) {
    M3ScaffoldTop(screen = Screen.SatisfyingScreen, navController = navController) { p ->
        val inner = remember { Random.nextColor(a = 255) }
        val outer = remember { Random.nextColor(a = 255) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DiamondProgressIndicator(
                        innerColor = inner,
                        outerColor = outer,
                        modifier = Modifier.size(100.dp),
                        animationSpec = tween(1500)
                    )
                    DiamondProgressIndicator(
                        innerColor = inner,
                        outerColor = outer,
                        modifier = Modifier.size(100.dp),
                        animationSpec = tween(1500)
                    )
                }
            }
        }
    }
}