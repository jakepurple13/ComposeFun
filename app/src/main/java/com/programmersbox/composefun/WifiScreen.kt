package com.programmersbox.composefun

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@Composable
fun WifiScreen(navController: NavController, vm: WifiViewModel = viewModel()) {
    ScaffoldTop(
        screen = Screen.WifiScreen,
        navController = navController,
        topBarActions = {
            IconButton(onClick = { vm.sortBy = if (vm.sortBy == WifiSortBy.Default) WifiSortBy.Name else WifiSortBy.Default }) {
                Icon(
                    when (vm.sortBy) {
                        WifiSortBy.Name -> Icons.Default.SortByAlpha
                        WifiSortBy.Default -> Icons.Default.Sort
                    },
                    null
                )
            }
        }
    ) { p ->
        val wifiPermissionState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        LaunchedEffect(Unit) { wifiPermissionState.launchMultiplePermissionRequest() }

        if (wifiPermissionState.allPermissionsGranted) {
            val context = LocalContext.current
            val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
            DisposableEffect(Unit) {
                val wifiScanReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                        if (success) {
                            vm.scanSuccess(wifiManager)
                        } else {
                            vm.scanFailure(wifiManager)
                        }
                        vm.refreshing = false
                    }
                }

                val intentFilter = IntentFilter()
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                context.registerReceiver(wifiScanReceiver, intentFilter)
                onDispose { context.unregisterReceiver(wifiScanReceiver) }
            }

            LaunchedEffect(Unit) {
                wifiManager.startScan()
                vm.refreshing = true
            }
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = vm.refreshing),
                onRefresh = {
                    wifiManager.startScan()
                    vm.refreshing = true
                },
                indicatorPadding = p
            ) {
                LazyColumn(
                    contentPadding = p,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) { items(vm.scanResults.let { if (vm.sortBy == WifiSortBy.Name) it.sortedBy(ScanResult::SSID) else it }) { WifiItem(it) } }
            }
        } else {
            Card(
                onClick = { wifiPermissionState.launchMultiplePermissionRequest() },
                modifier = Modifier
                    .padding(p)
                    .padding(4.dp)
                    .fillMaxWidth()
            ) { Text("Please Accept Permissions") }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WifiItem(scanResult: ScanResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            text = { Text(scanResult.SSID) },
            overlineText = { Text(scanResult.level.toString()) },
            secondaryText = { Text(scanResult.BSSID) }
        )
    }
}

class WifiViewModel : ViewModel() {
    var refreshing by mutableStateOf(false)
    var sortBy by mutableStateOf(WifiSortBy.Default)
    val scanResults = mutableStateListOf<ScanResult>()

    fun scanSuccess(wifiManager: WifiManager) {
        scanResults.clear()
        scanResults.addAll(wifiManager.scanResults)
    }

    fun scanFailure(wifiManager: WifiManager) {
        scanResults.clear()
        scanResults.addAll(wifiManager.scanResults)
    }
}

enum class WifiSortBy { Name, Default }