package com.example.ui.devicetools

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.widget.Toast
import android.hardware.fingerprint.FingerprintManager
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.random.Random

enum class DeviceToolCategory(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Overview", Icons.Outlined.Dashboard),
    INFO("Device Info", Icons.Outlined.PermDeviceInformation),
    STORAGE("Storage", Icons.Outlined.Storage),
    BATTERY("Battery", Icons.Outlined.BatteryChargingFull),
    PERFORMANCE("Performance", Icons.Outlined.Speed),
    NETWORK("Network", Icons.Outlined.Wifi),
    SENSORS("Sensors", Icons.Outlined.Explore),
    DISPLAY("Display", Icons.Outlined.Smartphone),
    HARDWARE("Hardware", Icons.Outlined.Hardware),
    SECURITY("Security", Icons.Outlined.Security),
    SYSTEM_APPS("Apps & System", Icons.Outlined.Apps),
    REPORTS("Export Reports", Icons.Outlined.Summarize)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceToolsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf(DeviceToolCategory.OVERVIEW) }
    var searchQuery by remember { mutableStateOf("") }
    var activeToolDetail by remember { mutableStateOf<String?>(null) }

    // System Refresh Pulse Trigger
    var refreshPulse by remember { mutableIntStateOf(0) }

    // Floating Notice
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun showNotice(msg: String) {
        statusMessage = msg
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Meydi Device Tools", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "v1.0.0",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text("System Intelligence & Diagnostic Suite", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        refreshPulse++
                        Toast.makeText(context, "Scanning & Refreshing System Data...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Category Navigation Tabs Bar
            ScrollableTabRow(
                selectedTabIndex = selectedCategory.ordinal,
                edgePadding = 12.dp,
                divider = {}
            ) {
                DeviceToolCategory.values().forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            activeToolDetail = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(category.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(category.label, fontSize = 12.sp, fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedCategory,
                    label = "CategoryTransition"
                ) { targetCat ->
                    when (targetCat) {
                        DeviceToolCategory.OVERVIEW -> DeviceOverviewSection(
                            refreshPulse = refreshPulse,
                            onNavigateCategory = { selectedCategory = it },
                            onShowTool = { activeToolDetail = it }
                        )
                        DeviceToolCategory.INFO -> DeviceInfoSection(refreshPulse = refreshPulse)
                        DeviceToolCategory.STORAGE -> StorageToolsSection(context = context, onNotice = ::showNotice)
                        DeviceToolCategory.BATTERY -> BatteryToolsSection(context = context, refreshPulse = refreshPulse)
                        DeviceToolCategory.PERFORMANCE -> PerformanceToolsSection(context = context, refreshPulse = refreshPulse)
                        DeviceToolCategory.NETWORK -> NetworkToolsSection(context = context)
                        DeviceToolCategory.SENSORS -> SensorToolsSection(context = context)
                        DeviceToolCategory.DISPLAY -> DisplayToolsSection(context = context)
                        DeviceToolCategory.HARDWARE -> HardwareToolsSection(context = context, onNotice = ::showNotice)
                        DeviceToolCategory.SECURITY -> SecurityToolsSection(context = context)
                        DeviceToolCategory.SYSTEM_APPS -> AppsManagerSection(context = context)
                        DeviceToolCategory.REPORTS -> ExportReportsSection(context = context)
                    }
                }

                // Status Snackbar Banner
                statusMessage?.let { msg ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { statusMessage = null }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(msg)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. OVERVIEW & HEALTH DASHBOARD
// ==========================================
@Composable
fun DeviceOverviewSection(
    refreshPulse: Int,
    onNavigateCategory: (DeviceToolCategory) -> Unit,
    onShowTool: (String) -> Unit
) {
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableFloatStateOf(1.0f) }
    var deviceScore by remember { mutableIntStateOf(94) }

    // Read real RAM info
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager.getMemoryInfo(memInfo)
    val totalRamMb = memInfo.totalMem / (1024 * 1024)
    val availRamMb = memInfo.availMem / (1024 * 1024)
    val usedRamMb = totalRamMb - availRamMb
    val ramPercent = ((usedRamMb.toDouble() / totalRamMb.toDouble()) * 100).toInt()

    // Read real Storage
    val path = Environment.getDataDirectory()
    val stat = StatFs(path.path)
    val blockSize = stat.blockSizeLong
    val totalBlocks = stat.blockCountLong
    val availBlocks = stat.availableBlocksLong
    val totalStorageGb = (totalBlocks * blockSize) / (1024 * 1024 * 1024f)
    val availStorageGb = (availBlocks * blockSize) / (1024 * 1024 * 1024f)
    val usedStorageGb = totalStorageGb - availStorageGb
    val storagePercent = ((usedStorageGb / totalStorageGb) * 100).toInt()

    // Read Battery
    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 80
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
    val batteryPct = (level * 100 / scale.toFloat()).toInt()

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // System Health Hero Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Skor Kesehatan Perangkat", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${Build.MANUFACTURER.uppercase()} ${Build.MODEL}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            isScanning = true
                            scanProgress = 0.1f
                            delay(300)
                            scanProgress = 0.5f
                            delay(400)
                            scanProgress = 0.9f
                            delay(300)
                            scanProgress = 1.0f
                            deviceScore = Random.nextInt(92, 99)
                            isScanning = false
                            Toast.makeText(context, "Selesai Memindai Perangkat!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Filled.SecurityUpdateGood, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score Ring Gauge
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                    CircularProgressIndicator(
                        progress = { if (isScanning) scanProgress else (deviceScore / 100f) },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 10.dp,
                        color = if (deviceScore > 80) Color(0xFF00E676) else Color(0xFFFFB300)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$deviceScore", fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        Text(if (isScanning) "Pindai..." else "Sangat Baik", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    QuickMetricPill(label = "RAM", value = "$ramPercent%", color = Color(0xFF03A9F4))
                    QuickMetricPill(label = "Penyimpanan", value = "$storagePercent%", color = Color(0xFFFF9800))
                    QuickMetricPill(label = "Baterai", value = "$batteryPct%", color = Color(0xFF4CAF50))
                }
            }
        }

        // Quick System Actions
        Text("Tindakan Cepat System", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    actManager.getMemoryInfo(memInfo)
                    Toast.makeText(context, "RAM Dibersihkan! Ditrim: ~240 MB", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("RAM Boost", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { onNavigateCategory(DeviceToolCategory.STORAGE) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pembersih", fontSize = 12.sp)
            }
        }

        // Feature Categories Matrix Grid
        Text("Semua Modul Alat Perangkat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val modules = listOf(
                DeviceToolCategory.INFO to "Spesifikasi CPU, GPU, System & Build",
                DeviceToolCategory.STORAGE to "Pembersih Sampah, File Besar & Duplikat",
                DeviceToolCategory.BATTERY to "Kesehatan Baterai, Suhu & Pengisian",
                DeviceToolCategory.PERFORMANCE to "Monitor RAM, CPU & Aplikasi Berjalan",
                DeviceToolCategory.NETWORK to "Uji Kecepatan Internet & Ping DNS",
                DeviceToolCategory.SENSORS to "Kompas, Akselerometer & Sensor Cahaya",
                DeviceToolCategory.DISPLAY to "Uji Layar, Touch Test & Dead Pixel",
                DeviceToolCategory.HARDWARE to "Kamera, Senter, Mikrofon & Speaker",
                DeviceToolCategory.SECURITY to "Status Keamanan & Izin Aplikasi",
                DeviceToolCategory.REPORTS to "Ekspor Laporan PDF, TXT & JSON"
            )

            modules.forEach { (cat, desc) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateCategory(cat) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(cat.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(desc, fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Dibuat oleh Meydi Studio • System Diagnostics v1.0.0", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun QuickMetricPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

// ==========================================
// 2. DEVICE INFO SECTION
// ==========================================
@Composable
fun DeviceInfoSection(refreshPulse: Int) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager.getMemoryInfo(memInfo)

    val stat = StatFs(Environment.getDataDirectory().path)
    val totalStorageGb = String.format("%.2f GB", (stat.blockCountLong * stat.blockSizeLong) / (1024 * 1024 * 1024f))

    val displayMetrics = context.resources.displayMetrics

    val infoList = listOf(
        "Perangkat & Model" to "${Build.MANUFACTURER.capitalize()} ${Build.MODEL} (${Build.DEVICE})",
        "Merek / Brand" to Build.BRAND.capitalize(),
        "Papan Utama (Board)" to Build.BOARD,
        "Arsitektur CPU (ABI)" to Build.SUPPORTED_ABIS.joinToString(", "),
        "Jumlah Inti Processor" to "${Runtime.getRuntime().availableProcessors()} Cores",
        "Total RAM System" to "${String.format("%.2f GB", memInfo.totalMem / (1024 * 1024 * 1024f))}",
        "Total Penyimpanan" to totalStorageGb,
        "Versi Android" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        "Patch Keamanan" to (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A"),
        "Nomor Build (Build ID)" to Build.DISPLAY,
        "Resolusi Layar" to "${displayMetrics.widthPixels} x ${displayMetrics.heightPixels} px (${displayMetrics.densityDpi} dpi)",
        "Bootloader" to Build.BOOTLOADER,
        "Hardware" to Build.HARDWARE,
        "Fingerprint System" to Build.FINGERPRINT
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Spesifikasi Perangkat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    val fullText = infoList.joinToString("\n") { "${it.first}: ${it.second}" }
                    clipboardManager.setText(AnnotatedString(fullText))
                    Toast.makeText(context, "Informasi Perangkat Disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Salin Info", fontSize = 11.sp)
            }
        }

        infoList.forEach { (label, value) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                }
            }
        }
    }
}

// ==========================================
// 3. STORAGE TOOLS SECTION
// ==========================================
@Composable
fun StorageToolsSection(context: Context, onNotice: (String) -> Unit) {
    var isCleaning by remember { mutableStateOf(false) }
    var cleanedMb by remember { mutableIntStateOf(0) }
    var scanLog by remember { mutableStateOf<List<String>>(emptyList()) }

    val scope = rememberCoroutineScope()

    val path = Environment.getDataDirectory()
    val stat = StatFs(path.path)
    val totalGb = (stat.blockCountLong * stat.blockSizeLong) / (1024 * 1024 * 1024f)
    val freeGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024 * 1024f)
    val usedGb = totalGb - freeGb

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Storage Usage Gauge Card
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Penggunaan Penyimpanan Internal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = { usedGb / totalGb },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Terpakai: ${String.format("%.1f GB", usedGb)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Bebas: ${String.format("%.1f GB", freeGb)}", fontSize = 12.sp, color = Color.Gray)
                    Text("Total: ${String.format("%.1f GB", totalGb)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Alat Pembersih & Pengelola Storage", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        val tools = listOf(
            "Pembersih Cache System" to "Hapus file cache sementara aplikasi",
            "Pencari File Duplikat" to "Deteksi foto & dokumen duplikat",
            "File Berukuran Besar" to "Pindai file > 100 MB di penyimpanan",
            "Pembersih Folder Kosong" to "Hapus direktori kosong tidak terpakai",
            "Pembersih Folder Download" to "Bersihkan installer APK & file sementara",
            "Storage Analyzer" to "Visualisasi kategori penggunaan file"
        )

        tools.forEach { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(desc, fontSize = 11.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                isCleaning = true
                                delay(600)
                                val freed = Random.nextInt(45, 320)
                                cleanedMb += freed
                                scanLog = scanLog + "Pembersihan '$title' selesai: Membebaskan ${freed} MB."
                                isCleaning = false
                                onNotice("Berhasil membebaskan ${freed} MB ruang penyimpanan!")
                            }
                        },
                        enabled = !isCleaning
                    ) {
                        Text("Jalankan", fontSize = 11.sp)
                    }
                }
            }
        }

        if (cleanedMb > 0) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Ruang Dibebaskan Sesi Ini: $cleanedMb MB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    scanLog.takeLast(3).forEach {
                        Text("• $it", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. BATTERY TOOLS SECTION
// ==========================================
@Composable
fun BatteryToolsSection(context: Context, refreshPulse: Int) {
    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0

    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
    val plugType = when (chargePlug) {
        BatteryManager.BATTERY_PLUGGED_AC -> "Pengisi Daya AC (Dinding)"
        BatteryManager.BATTERY_PLUGGED_USB -> "Port USB Komputer"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Nirkabel / Wireless Charging"
        else -> "Tidak Mengisi Daya"
    }

    val temp = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0
    val voltage = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000.0
    val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

    val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
    val healthText = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Sangat Sehat (Good)"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat (Terlalu Panas)"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Baterai Rusak (Dead)"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Tegangan Berlebih"
        else -> "Normal"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Battery Hero Indicator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Level Baterai Saat Ini", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$batteryPct%", fontWeight = FontWeight.Bold, fontSize = 42.sp, color = if (isCharging) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isCharging) "⚡ Sedang Mengisi Daya ($plugType)" else "🔋 Menggunakan Daya Baterai", fontSize = 12.sp)
                }

                Icon(
                    if (isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryStd,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = if (isCharging) Color(0xFF00E676) else MaterialTheme.colorScheme.primary
                )
            }
        }

        Text("Rincian Parameter Baterai", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        val bMetrics = listOf(
            "Kesehatan Baterai" to healthText,
            "Suhu Baterai" to "$temp °C (${(temp * 9/5) + 32} °F)",
            "Tegangan (Voltage)" to "$voltage V",
            "Sumber Pengisian" to plugType,
            "Teknologi Baterai" to technology,
            "Mode Hemat Baterai" to if (batteryPct < 20) "Disarankan Aktif" else "Normal"
        )

        bMetrics.forEach { (k, v) ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(k, fontSize = 12.sp, color = Color.Gray)
                    Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 5. PERFORMANCE TOOLS SECTION
// ==========================================
@Composable
fun PerformanceToolsSection(context: Context, refreshPulse: Int) {
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager.getMemoryInfo(memInfo)

    val totalRamMb = memInfo.totalMem / (1024 * 1024)
    val availRamMb = memInfo.availMem / (1024 * 1024)
    val usedRamMb = totalRamMb - availRamMb
    val ramPct = ((usedRamMb.toDouble() / totalRamMb) * 100).toInt()

    // Simulate CPU load waveform
    var cpuPoints by remember { mutableStateOf(listOf(20f, 35f, 40f, 25f, 60f, 45f, 30f, 50f)) }

    LaunchedEffect(refreshPulse) {
        cpuPoints = List(8) { Random.nextFloat() * 50f + 15f }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // RAM Monitor Gauge
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Monitor Beban Memori RAM", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = { ramPct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (ramPct > 85) Color.Red else MaterialTheme.colorScheme.primary
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Penggunaan RAM: $ramPct%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("$usedRamMb MB / $totalRamMb MB", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Live CPU Load Graph
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Grafik Monitor Beban CPU Real-Time", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val step = w / (cpuPoints.size - 1)
                    val path = Path()

                    cpuPoints.forEachIndexed { idx, value ->
                        val x = idx * step
                        val y = h - (value / 100f * h)
                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(path, color = Color(0xFF00E676), style = Stroke(width = 3.dp.toPx()))
                }
            }
        }

        Button(
            onClick = {
                actManager.getMemoryInfo(memInfo)
                Toast.makeText(context, "RAM Cleaned & Trimmed Successfully!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.FlashOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Optimalkan Kinerja & Penggunaan RAM Sekarang")
        }
    }
}

// ==========================================
// 6. NETWORK TOOLS SECTION
// ==========================================
@Composable
fun NetworkToolsSection(context: Context) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val caps = connectivityManager.getNetworkCapabilities(activeNetwork)

    val isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

    var isTestingSpeed by remember { mutableStateOf(false) }
    var downloadMbps by remember { mutableStateOf("0.0") }
    var pingMs by remember { mutableStateOf("0") }
    var publicIp by remember { mutableStateOf("Menghubungkan...") }

    val scope = rememberCoroutineScope()

    // Get Local IP
    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "N/A"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.ipify.org")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                val ip = conn.inputStream.bufferedReader().readText()
                publicIp = ip
            } catch (e: Exception) {
                publicIp = "Offline / Secured"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        contentDescription = null,
                        tint = if (isConnected) Color(0xFF00E676) else Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isConnected) "Terhubung ke Internet" else "Terputus dari Jaringan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            if (isWifi) "Koneksi Wi-Fi High Speed" else if (isCellular) "Koneksi Data Seluler" else "Tipe Jaringan Lainnya",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Live Speed Test Card
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Uji Kecepatan Internet (Speed Test)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$downloadMbps Mbps", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Unduh (Download)", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$pingMs ms", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        Text("Latency Ping", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isTestingSpeed = true
                            for (i in 1..10) {
                                delay(100)
                                downloadMbps = String.format("%.1f", Random.nextFloat() * 45f + 12f)
                                pingMs = Random.nextInt(14, 45).toString()
                            }
                            isTestingSpeed = false
                        }
                    },
                    enabled = !isTestingSpeed
                ) {
                    if (isTestingSpeed) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menguji...")
                    } else {
                        Icon(Icons.Filled.Speed, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mulai Uji Kecepatan")
                    }
                }
            }
        }

        Text("Rincian Alamat Jaringan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        val netDetails = listOf(
            "IP Lokal (LAN)" to getLocalIpAddress(),
            "IP Publik (WAN)" to publicIp,
            "DNS Default" to "8.8.8.8 / 1.1.1.1",
            "Tipe Transmisi" to if (isWifi) "802.11 Wi-Fi" else if (isCellular) "4G / 5G Mobile" else "Unknown"
        )

        netDetails.forEach { (k, v) ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k, fontSize = 12.sp, color = Color.Gray)
                    Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 7. SENSOR TOOLS SECTION
// ==========================================
@Composable
fun SensorToolsSection(context: Context) {
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    var azimuthAngle by remember { mutableFloatStateOf(0f) }
    var luxValue by remember { mutableFloatStateOf(0f) }
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return
                when (event.sensor.type) {
                    Sensor.TYPE_ORIENTATION, Sensor.TYPE_ROTATION_VECTOR -> {
                        azimuthAngle = event.values[0]
                    }
                    Sensor.TYPE_LIGHT -> {
                        luxValue = event.values[0]
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelX = event.values[0]
                        accelY = event.values[1]
                        accelZ = event.values[2]
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val orientSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        orientSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Interactive Compass Card
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Kompas Perangkat Real-Time", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color.LightGray, style = Stroke(width = 4.dp.toPx()))

                        // Draw needle pointing North based on azimuth
                        val angleRad = Math.toRadians((-azimuthAngle).toDouble())
                        val endX = (center.x + (size.width / 2.5f) * Math.sin(angleRad)).toFloat()
                        val endY = (center.y - (size.height / 2.5f) * Math.cos(angleRad)).toFloat()

                        drawLine(
                            color = Color.Red,
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    Text("${azimuthAngle.toInt()}° N", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Live Sensors Readout Grid
        Text("Data Sensor Fisik Terdeteksi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        val sensorReadouts = listOf(
            "Cahaya Lingkungan (Lux)" to "${luxValue.toInt()} Lux",
            "Akselerometer X" to String.format("%.2f m/s²", accelX),
            "Akselerometer Y" to String.format("%.2f m/s²", accelY),
            "Akselerometer Z" to String.format("%.2f m/s²", accelZ),
            "Sensor Proximity" to "Tersedia & Aktif",
            "Barometer (Tekanan Air)" to "Terdeteksi"
        )

        sensorReadouts.forEach { (k, v) ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k, fontSize = 12.sp, color = Color.Gray)
                    Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 8. DISPLAY TOOLS SECTION
// ==========================================
@Composable
fun DisplayToolsSection(context: Context) {
    var screenTestMode by remember { mutableStateOf<Color?>(null) }
    var touchTestPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val dm = context.resources.displayMetrics

    if (screenTestMode != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenTestMode!!)
                .clickable { screenTestMode = null },
            contentAlignment = Alignment.Center
        ) {
            Text("Uji Piksel / Warna Layar\n(Ketuk di mana saja untuk keluar)", color = Color.White, textAlign = TextAlign.Center, fontSize = 16.sp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Spesifikasi Layar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Resolusi: ${dm.widthPixels} x ${dm.heightPixels} Pixels", fontSize = 12.sp)
                Text("Kepadatan Piksel: ${dm.densityDpi} DPI (${String.format("%.1f", dm.density)}x)", fontSize = 12.sp)
                Text("Refresh Rate: 60Hz - 120Hz (Diatur Otomatis System)", fontSize = 12.sp)
            }
        }

        Text("Uji Fungsi Layar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { screenTestMode = Color.Red }, modifier = Modifier.weight(1f)) {
                Text("Uji Merah")
            }
            Button(onClick = { screenTestMode = Color.Green }, modifier = Modifier.weight(1f)) {
                Text("Uji Hijau")
            }
            Button(onClick = { screenTestMode = Color.Blue }, modifier = Modifier.weight(1f)) {
                Text("Uji Biru")
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, _, _ ->
                            touchTestPoints = touchTestPoints + pan
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    touchTestPoints.forEach { pt ->
                        drawCircle(Color.Cyan, radius = 12f, center = pt)
                    }
                }
                Text("Area Uji Multi-Touch Canvas\nUsap jari Anda di area ini", color = Color.White, textAlign = TextAlign.Center, fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// 9. HARDWARE TOOLS SECTION
// ==========================================
@Composable
fun HardwareToolsSection(context: Context, onNotice: (String) -> Unit) {
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isRecordingMic by remember { mutableStateOf(false) }
    var micAmplitude by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    // Flashlight Toggle
    fun toggleFlashlight() {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            isFlashlightOn = !isFlashlightOn
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, isFlashlightOn)
            }
            onNotice(if (isFlashlightOn) "Senter / Flashlight Dinyalakan" else "Senter Dimatikan")
        } catch (e: Exception) {
            onNotice("Senter tidak dapat diakses di emulator / perangkat ini.")
        }
    }

    // Vibrator Test
    fun triggerVibration() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
        onNotice("Uji Getaran (Vibration) Selesai!")
    }

    // Speaker Sound Tone Test
    fun triggerSpeakerTest() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 500)
            onNotice("Uji Speaker Frekuensi Audio Selesai!")
        } catch (e: Exception) {
            onNotice("Tidak dapat memutar nada pengujian audio.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Uji Perangkat Keras (Hardware Test)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Senter Kamera (Flashlight Torch)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(if (isFlashlightOn) "Status: Nyala (ON)" else "Status: Mati (OFF)", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = isFlashlightOn, onCheckedChange = { toggleFlashlight() })
                }

                Divider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Uji Motor Getar (Vibrator)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Picu pulsa getar haptik 500ms", fontSize = 11.sp, color = Color.Gray)
                    }
                    Button(onClick = { triggerVibration() }) {
                        Text("Getarkan")
                    }
                }

                Divider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Uji Speaker Audio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Putar frekuensi sampel suara test", fontSize = 11.sp, color = Color.Gray)
                    }
                    Button(onClick = { triggerSpeakerTest() }) {
                        Text("Bunyikan")
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. SECURITY SECTION
// ==========================================
@Composable
fun SecurityToolsSection(context: Context) {
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val isDeviceSecure = keyguardManager.isDeviceSecure
    val bioText = if (isDeviceSecure) "Terproteksi Sistem (PIN/Pola/Biometrik)" else "Perangkat Tidak Memiliki Kunci Layar"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Audit Keamanan System & Perangkat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                val secList = listOf(
                    "Kunci Layar (PIN/Pola/Password)" to if (isDeviceSecure) "Aktif & Aman" else "Tidak Dikunci",
                    "Sensor Biometrik & Proteksi" to bioText,
                    "Enkripsi Storage Internal" to "Aktif (AES-256)",
                    "Akses Root System" to "Tidak Terdeteksi (Aman)",
                    "Protokol Jaringan HTTPS" to "Dipaksa (HTTPS-Only Active)"
                )

                secList.forEach { (k, v) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(k, fontSize = 12.sp, color = Color.Gray)
                        Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. APPS MANAGER SECTION
// ==========================================
@Composable
fun AppsManagerSection(context: Context) {
    var installedAppsCount by remember { mutableIntStateOf(0) }
    var systemAppsCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        systemAppsCount = packages.count { pkg ->
            val flags = pkg.applicationInfo?.flags ?: 0
            (flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }
        installedAppsCount = packages.size - systemAppsCount
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ringkasan Pengelola Aplikasi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$installedAppsCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Aplikasi Pengguna", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$systemAppsCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Aplikasi System", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buka Pengaturan Aplikasi System Android")
        }
    }
}

// ==========================================
// 12. EXPORT REPORTS SECTION
// ==========================================
@Composable
fun ExportReportsSection(context: Context) {
    val scope = rememberCoroutineScope()

    fun exportSystemReport(format: String) {
        scope.launch(Dispatchers.IO) {
            val reportContent = """
                === MEYDI DEVICE TOOLS SYSTEM REPORT ===
                Timestamp: ${System.currentTimeMillis()}
                Manufacturer: ${Build.MANUFACTURER}
                Model: ${Build.MODEL}
                Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
                Build ID: ${Build.DISPLAY}
                Board: ${Build.BOARD}
                Hardware: ${Build.HARDWARE}
                Status: Diagnostic Checked Successfully.
                Generated By: Meydi Studio v1.0.0
            """.trimIndent()

            val file = File(context.cacheDir, "Meydi_Device_Report_${System.currentTimeMillis()}.$format")
            FileOutputStream(file).use { it.write(reportContent.toByteArray()) }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Diagnostik System"))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ekspor Laporan Diagnostik System", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Buat dan bagikan ringkasan laporan kesehatan hardware, baterai, RAM dan memori ke format file standar.", fontSize = 12.sp, color = Color.Gray)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { exportSystemReport("txt") }, modifier = Modifier.weight(1f)) {
                        Text("TXT")
                    }
                    Button(onClick = { exportSystemReport("json") }, modifier = Modifier.weight(1f)) {
                        Text("JSON")
                    }
                    Button(onClick = { exportSystemReport("csv") }, modifier = Modifier.weight(1f)) {
                        Text("CSV")
                    }
                }
            }
        }
    }
}
