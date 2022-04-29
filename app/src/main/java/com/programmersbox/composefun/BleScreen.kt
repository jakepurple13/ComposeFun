package com.programmersbox.composefun

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun BleScreen(navController: NavController, vm: BleViewModel = viewModel()) {
    val bleDevices by vm.advertisements.collectAsState()
    val bleStatus by vm.status.collectAsState()
    ScaffoldTop(
        screen = Screen.BleScreen,
        navController = navController,
        topBarActions = { Text("${animateIntAsState(bleDevices.size).value} device(s)") }
    ) { p ->
        val blePermissionState =
            rememberMultiplePermissionsState(
                listOfNotNull(
                    Manifest.permission.BLUETOOTH,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_SCAN else null,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        LaunchedEffect(Unit) { blePermissionState.launchMultiplePermissionRequest() }

        if (blePermissionState.allPermissionsGranted) {

            DisposableEffect(Unit) {
                vm.start()
                onDispose { vm.stop() }
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = bleStatus is ScanStatus.Scanning),
                onRefresh = {
                    if (bleStatus !is ScanStatus.Scanning) {
                        vm.clear()
                        vm.start()
                    }
                },
                indicatorPadding = p
            ) {
                LazyColumn(
                    contentPadding = p,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) { items(bleDevices) { BleItem(it) } }
            }
        } else {
            Card(
                onClick = { blePermissionState.launchMultiplePermissionRequest() },
                modifier = Modifier
                    .padding(p)
                    .padding(4.dp)
                    .fillMaxWidth()
            ) { Text("Please Accept Permissions") }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun BleItem(advertisement: Advertisement) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            text = { Text(advertisement.name.orEmpty()) },
            overlineText = { Text(advertisement.address) },
            secondaryText = { Text(advertisement.uuids.joinToString("\n")) }
        )
    }
}


sealed class ScanStatus {
    object Stopped : ScanStatus()
    object Scanning : ScanStatus()
    data class Failed(val message: CharSequence) : ScanStatus()
}

class BleViewModel : ViewModel() {

    companion object {
        private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)
    }

    private val scanner = Scanner()
    private val found = hashMapOf<String, Advertisement>()

    private val _status = MutableStateFlow<ScanStatus>(ScanStatus.Stopped)
    val status = _status.asStateFlow()

    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList())
    val advertisements = _advertisements.asStateFlow()

    fun start() {
        if (_status.value == ScanStatus.Scanning) return // Scan already in progress.
        _status.value = ScanStatus.Scanning

        viewModelScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                scanner
                    .advertisements
                    .catch { cause -> _status.value = ScanStatus.Failed(cause.message ?: "Unknown error") }
                    .onCompletion { cause -> if (cause == null || cause is CancellationException) _status.value = ScanStatus.Stopped }
                    .collect { advertisement ->
                        found[advertisement.address] = advertisement
                        _advertisements.value = found.values.toList()
                    }
            }
        }
    }

    fun stop() {
        viewModelScope.cancel()
    }

    fun clear() {
        stop()
        _advertisements.value = emptyList()
    }
}
