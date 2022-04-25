package com.programmersbox.composefun

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.navigation.NavController
import com.google.accompanist.permissions.*

@OptIn(ExperimentalMotionApi::class, ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@Composable
fun PermissionScreen(navController: NavController) {
    ScaffoldTop(
        screen = Screen.PermissionScreen,
        navController = navController,
    ) { p ->
        Column(
            modifier = Modifier.padding(p),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
            PermissionItem(permissionState = cameraPermissionState)
            val readWriteState = rememberMultiplePermissionsState(
                permissions = listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            )
            MultiplePermissionItem(permissionState = readWriteState)
            val smsPermissionState = rememberPermissionState(android.Manifest.permission.SEND_SMS)
            PermissionItem(permissionState = smsPermissionState)
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun PermissionItem(permissionState: PermissionState) {
    val context = LocalContext.current
    Card(
        onClick = {
            when (permissionState.status) {
                PermissionStatus.Granted -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }
                is PermissionStatus.Denied -> permissionState.launchPermissionRequest()
            }
        }
    ) {
        ListItem(
            text = { Text("Permission: ${permissionState.permission.removePrefix("android.permission.")}") },
            icon = {
                when (permissionState.status) {
                    PermissionStatus.Granted -> Icon(Icons.Default.CheckCircle, null)
                    is PermissionStatus.Denied -> Icon(Icons.Default.Cancel, null)
                }
            },
            secondaryText = {
                if (permissionState.status is PermissionStatus.Denied) {
                    Text(if (permissionState.status.shouldShowRationale) "This is important! Please Grant!" else "Please Grant!")
                } else {
                    Text("Press again to go to Deny Permission")
                }
            }
        )
    }
}

@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun MultiplePermissionItem(permissionState: MultiplePermissionsState) {
    val context = LocalContext.current
    Card(
        onClick = {
            when (permissionState.allPermissionsGranted) {
                true -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }
                false -> permissionState.launchMultiplePermissionRequest()
            }
        }
    ) {
        ListItem(
            text = { Text("Permission: ${permissionState.permissions.joinToString(", ") { it.permission.removePrefix("android.permission.") }}") },
            icon = {
                when (permissionState.allPermissionsGranted) {
                    true -> Icon(Icons.Default.CheckCircle, null)
                    false -> Icon(Icons.Default.Cancel, null)
                }
            },
            secondaryText = {
                if (!permissionState.allPermissionsGranted) {
                    Text(if (permissionState.shouldShowRationale) "This is important! Please Grant!" else "Please Grant!")
                } else {
                    Text("Press again to go to Deny Permission")
                }
            }
        )
    }
}