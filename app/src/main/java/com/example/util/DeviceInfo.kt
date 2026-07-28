package com.example.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import java.util.Locale

data class DeviceInfo(
    val userName: String = "Meydi",
    val country: String = Locale.getDefault().displayCountry,
    val browser: String = "Native Android App",
    val device: String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
    val isOnline: Boolean = false,
    val batteryPercent: Int = 0,
    val connectionType: String = "None",
    val appVersion: String = "1.0.0"
)

fun getDeviceInfo(context: Context): DeviceInfo {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetwork
    val caps = cm.getNetworkCapabilities(activeNetwork)
    
    val isOnline = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    val connectionType = when {
        caps == null -> "None"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        else -> "Unknown"
    }

    val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
        context.registerReceiver(null, ifilter)
    }
    val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0

    val appVersion = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.0"
    } catch (e: Exception) {
        "1.0"
    }

    return DeviceInfo(
        userName = "Meydi",
        isOnline = isOnline,
        connectionType = connectionType,
        batteryPercent = batteryPct,
        appVersion = appVersion
    )
}
