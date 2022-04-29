package com.programmersbox.composefun


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@Composable
fun BluetoothScreen(navController: NavController) {
    var refreshing by remember { mutableStateOf(false) }
    ScaffoldTop(screen = Screen.BluetoothScreen, navController = navController) { p ->
        val wifiPermissionState = rememberMultiplePermissionsState(
            listOfNotNull(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT else null,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        LaunchedEffect(Unit) { wifiPermissionState.launchMultiplePermissionRequest() }

        if (wifiPermissionState.allPermissionsGranted) {

            val context = LocalContext.current

            val bluetoothManager: BluetoothManager = remember { context.getSystemService(BluetoothManager::class.java) }
            val bluetoothAdapter: BluetoothAdapter? = remember { bluetoothManager.adapter }

            val bluetoothDevices by bluetoothDevices { refreshing = false }

            LaunchedEffect(Unit) {
                bluetoothAdapter?.startDiscovery()
                refreshing = true
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = refreshing),
                onRefresh = { refreshing = true },
                swipeEnabled = false,
                indicatorPadding = p
            ) {
                LazyColumn(
                    contentPadding = p,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) { items(bluetoothDevices) { BluetoothItem(it) } }
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

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BluetoothItem(device: BluetoothDevice) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            text = { Text(device.name.orEmpty()) },
            overlineText = { Text(device.address.orEmpty()) },
            secondaryText = { Text(device.uuids.orEmpty().joinToString("\n")) }
        )
    }
}

@Composable
fun bluetoothDevices(finishScan: () -> Unit): State<List<BluetoothDevice>> {
    val scanResults = remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    val found = remember { hashMapOf<String, BluetoothDevice>() }
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val bluetoothScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        // Discovery has found a device. Get the BluetoothDevice
                        // object and its info from the Intent.
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let { found[it.address] = it }
                        scanResults.value = found.values.toList()
                    }
                }
                finishScan()
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(bluetoothScanReceiver, filter)
        onDispose { context.unregisterReceiver(bluetoothScanReceiver) }
    }
    return scanResults
}