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
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@Composable
fun WifiScreen(navController: NavController) {
    var refreshing by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(WifiSortBy.Default) }
    ScaffoldTop(
        screen = Screen.WifiScreen,
        navController = navController,
        topBarActions = {
            IconButton(onClick = { sortBy = if (sortBy == WifiSortBy.Default) WifiSortBy.Name else WifiSortBy.Default }) {
                Icon(
                    when (sortBy) {
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
            val wifiNetworks by wifiNetworks(wifiManager = wifiManager) { refreshing = false }

            LaunchedEffect(Unit) {
                wifiManager.startScan()
                refreshing = true
            }
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = refreshing),
                onRefresh = {
                    wifiManager.startScan()
                    refreshing = true
                },
                indicatorPadding = p
            ) {
                LazyColumn(
                    contentPadding = p,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) { items(wifiNetworks.let { if (sortBy == WifiSortBy.Name) it.sortedBy(ScanResult::SSID) else it }) { WifiItem(it) } }
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

enum class WifiSortBy { Name, Default }

@Composable
fun wifiNetworks(wifiManager: WifiManager, finishScan: () -> Unit): State<List<ScanResult>> {
    val scanResults = remember { mutableStateOf(emptyList<ScanResult>()) }
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                scanResults.value = if (success) {
                    wifiManager.scanResults
                } else {
                    wifiManager.scanResults
                }
                finishScan()
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)
        onDispose { context.unregisterReceiver(wifiScanReceiver) }
    }
    return scanResults
}