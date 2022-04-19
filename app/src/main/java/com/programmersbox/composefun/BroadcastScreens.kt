package com.programmersbox.composefun

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.SimpleDateFormat
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * Registers a broadcast receiver and unregisters at the end of the composable lifecycle
 *
 * @param defaultValue the default value that this starts as
 * @param intentFilter the filter for intents
 * @see IntentFilter
 * @param tick the callback from the broadcast receiver
 */
@Composable
fun <T : Any> broadcastReceiver(defaultValue: T, intentFilter: IntentFilter, tick: (context: Context, intent: Intent) -> T): State<T> {
    val item: MutableState<T> = remember { mutableStateOf(defaultValue) }
    val context = LocalContext.current

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                item.value = tick(context, intent)
            }
        }
        context.registerReceiver(receiver, intentFilter)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return item
}

/**
 * Registers a broadcast receiver and unregisters at the end of the composable lifecycle
 *
 * @param defaultValue the default value that this starts as
 * @param intentFilter the filter for intents.
 * @see IntentFilter
 * @param tick the callback from the broadcast receiver
 */
@Composable
fun <T : Any> broadcastReceiverNullable(defaultValue: T?, intentFilter: IntentFilter, tick: (context: Context, intent: Intent) -> T?): State<T?> {
    val item: MutableState<T?> = remember { mutableStateOf(defaultValue) }
    val context = LocalContext.current

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                item.value = tick(context, intent)
            }
        }
        context.registerReceiver(receiver, intentFilter)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return item
}

/**
 * Creates a broadcast receiver that gets the time every tick that Android takes and
 * unregisters the receiver when the view is at the end of its lifecycle
 */
@Composable
fun currentTime(): State<Long> {
    return broadcastReceiver(
        defaultValue = System.currentTimeMillis(),
        intentFilter = IntentFilter(Intent.ACTION_TIME_TICK),
        tick = { _, _ -> System.currentTimeMillis() }
    )
}

//----------------------------------------------------------

/**
 * This will give updates whenever the battery changes
 * @see Intent.ACTION_BATTERY_CHANGED
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@Composable
fun batteryInfo(): State<Battery?> {
    return broadcastReceiverNullable<Battery>(
        defaultValue = null,
        intentFilter = batteryIntentFilter(),
        tick = { context, intent -> batteryInformation(context, intent) }
    )
}

data class Battery(
    val percent: Float,
    val isCharging: Boolean,
    val chargeType: ChargeType,
    val health: BatteryHealth,
    val technology: String?,
    val temperature: Float,
    val voltage: Int,
    val capacity: Long
)

enum class ChargeType { USB, AC, WIRELESS, NONE }
enum class BatteryHealth { COLD, DEAD, GOOD, OVER_VOLTAGE, OVERHEAT, UNSPECIFIED_FAILURE, UNKNOWN }

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun batteryInformation(context: Context, intent: Intent?): Battery {
    //percentage
    val level: Int = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale: Int = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct: Float = level * 100 / scale.toFloat()

    //charging status
    val status: Int = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    // How are we charging?
    val chargePlug: Int = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
    val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
    val acCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
    val wirelessCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS
    val type = when {
        usbCharge -> ChargeType.USB
        acCharge -> ChargeType.AC
        wirelessCharge -> ChargeType.WIRELESS
        else -> ChargeType.NONE
    }

    //Battery Health
    val present = intent?.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
    val health = if (present == true) {
        when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> BatteryHealth.UNKNOWN
            else -> BatteryHealth.UNKNOWN
        }

    } else {
        BatteryHealth.UNKNOWN
    }

    //technology
    val technology = intent?.extras?.getString(BatteryManager.EXTRA_TECHNOLOGY)

    //temperature
    val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0

    val temp = if (temperature > 0) (temperature.toFloat() / 10f) else temperature.toFloat()

    //voltage
    val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

    //capacity
    val mBatteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    val chargeCounter = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    val capacity = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val cap = (chargeCounter.toFloat() / capacity.toFloat() * 100f).toLong()

    return Battery(batteryPct, isCharging, type, health, technology, temp, voltage, cap)
}

fun batteryIntentFilter() = IntentFilter().apply {
    addAction(Intent.ACTION_POWER_CONNECTED)
    addAction(Intent.ACTION_POWER_DISCONNECTED)
    addAction(Intent.ACTION_BATTERY_CHANGED)
}

@Composable
@Preview
fun BroadcastReceiverScreen(navController: NavController = rememberNavController()) {
    ScaffoldTop(Screen.BroadcastReceiverScreen, navController = navController) { p ->
        LazyColumn(contentPadding = p) {
            item {
                val currentTime by currentTime()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Text("Time: ${SimpleDateFormat.getDateTimeInstance().format(currentTime)}")
                } else {
                    Text("Time: ${java.text.SimpleDateFormat.getDateTimeInstance().format(currentTime)}")
                }
            }
            item { Divider() }
            item {
                val batteryInfo by batteryInfo()
                Text("Battery: $batteryInfo")
            }
        }
    }
}