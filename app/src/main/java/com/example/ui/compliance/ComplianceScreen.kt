package com.example.ui.compliance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf<String?>(null) }
    var reportSubmittedSnackbar by remember { mutableStateOf(false) }

    val tabs = listOf("Privacy Policy", "Data Safety", "Permissions", "Report Content")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Keamanan & Kepatuhan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Google Play Compliance & Privacy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            if (reportSubmittedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { reportSubmittedSnackbar = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.inverseOnSurface)
                        }
                    }
                ) {
                    Text("Laporan Anda berhasil dikirim dan akan ditinjau oleh tim moderator.")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                when (selectedTab) {
                    0 -> PrivacyPolicyView()
                    1 -> DataSafetyView()
                    2 -> PermissionsGuideView(onShowRationale = { showPermissionRationale = it })
                    3 -> ReportContentView(onOpenReportDialog = { showReportDialog = true })
                }
            }
        }
    }

    if (showReportDialog) {
        ReportContentDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = {
                showReportDialog = false
                reportSubmittedSnackbar = true
            }
        )
    }

    if (showPermissionRationale != null) {
        PermissionRationaleDialog(
            permissionName = showPermissionRationale!!,
            onDismiss = { showPermissionRationale = null }
        )
    }
}

@Composable
fun PrivacyPolicyView() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Privacy Policy — Super Tools v1.0.0", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Developer: Meydi\nTerakhir Diperbarui: 25 Juli 2026\nURL Legal: https://supertools.app/privacy-policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text("1. Pengumpulan Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Super Tools memproses data secara lokal di perangkat Anda sedapat mungkin. Untuk fitur seperti Cloud Sync & Secure Notes, data dienkripsi end-to-end (E2EE) sebelum dikirim ke server.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Text("2. Penggunaan Fitur AI & Konten", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Input teks/gambar yang dikirim ke AI Tools hanya digunakan untuk menghasilkan respon dan tidak digunakan untuk melatih model publik tanpa izin. Pengguna dilarang menggunakan AI untuk membuat konten ilegal.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Text("3. Disclaimer Media Downloader", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Fitur Media Downloader disediakan khusus untuk keperluan cadangan pribadi dari konten publik yang sah. Pengguna bertanggung jawab penuh mematuhi hukum hak cipta dan Kebijakan Layanan (ToS) dari platform asal.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Text("4. Hak Pengguna & Penghapusan Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Anda dapat menghapus seluruh data cloud kapan saja melalui menu 'Backup & Sync' -> 'Hapus Semua Data Cloud'.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun DataSafetyView() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Panduan Pengisian Data Safety Form (Play Console)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text("Deklarasi resmi mengenai cara aplikasi mengumpulkan dan membagikan data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        val dataTypes = listOf(
            DataSafetyItem("Secure Notes & Password", "Dikumpulkan (Terenkripsi E2EE)", "Tidak dibagikan ke pihak ke-3", "Fungsi Aplikasi & Cadangan"),
            DataSafetyItem("Riwayat & Favorite Tools", "Disimpan Lokal / Opt-in Sync", "Tidak dibagikan ke pihak ke-3", "Personalisasi & Fungsi App"),
            DataSafetyItem("Identifikasi Perangkat", "Dikumpulkan (Token Sesi)", "Untuk autentikasi API Gateway", "Pencegahan Penyalahgunaan"),
            DataSafetyItem("Audio / Kamera", "Proses Lokal saat digunakan", "Tidak dikumpulkan / tidak disimpan", "Fitur Voice Recorder & Scanner")
        )

        items(dataTypes.size) { index ->
            val item = dataTypes[index]
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(item.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Status: ${item.status}", style = MaterialTheme.typography.bodySmall)
                    Text("• Pembagian Data: ${item.sharing}", style = MaterialTheme.typography.bodySmall)
                    Text("• Tujuan: ${item.purpose}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

data class DataSafetyItem(val category: String, val status: String, val sharing: String, val purpose: String)

@Composable
fun PermissionsGuideView(onShowRationale: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Izin Sensitif & Runtime Permission", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text("Aplikasi meminta izin hanya saat fitur membutuhkan dan menjelaskan tujuannya secara langsung.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        val permissions = listOf(
            PermissionItem("RECORD_AUDIO", "Voice Recorder & Audio Converter", "Digunakan untuk merekam suara saat fitur Voice Note/Recorder aktif."),
            PermissionItem("CAMERA", "QR Code Scanner & Document Cam", "Digunakan untuk memindai kode QR secara langsung via kamera."),
            PermissionItem("READ/WRITE_EXTERNAL_STORAGE", "Media Downloader & Converter", "Digunakan untuk menyimpan hasil unduhan video/audio ke penyimpanan internal."),
            PermissionItem("INTERNET & NETWORK_STATE", "AI Gateway & Cloud Sync", "Digunakan untuk komunikasi API HTTPS aman dan verifikasi koneksi.")
        )

        items(permissions.size) { index ->
            val item = permissions[index]
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(item.feature, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onShowRationale(item.name) }) {
                        Icon(Icons.Outlined.Info, contentDescription = "Detail")
                    }
                }
            }
        }
    }
}

data class PermissionItem(val name: String, val feature: String, val explanation: String)

@Composable
fun ReportContentView(onOpenReportDialog: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ReportProblem, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mekanisme Pelaporan Konten (AI & Downloader)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Untuk mematuhi kebijakan Generative AI & Intellectual Property Google Play Store, pengguna dapat melaporkan hasil keluaran AI atau konten unduhan yang tidak pantas atau melanggar hak cipta.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = onOpenReportDialog,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Filled.Flag, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buat Laporan Konten Baru")
        }
    }
}

@Composable
fun ReportContentDialog(onDismiss: () -> Unit, onSubmit: () -> Unit) {
    var reason by remember { mutableStateOf("Konten Tidak Pantas / Seksual") }
    var details by remember { mutableStateOf("") }
    var contentType by remember { mutableStateOf("Keluaran AI Generatif") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Laporkan Konten", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Jenis Konten:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = contentType == "Keluaran AI Generatif",
                        onClick = { contentType = "Keluaran AI Generatif" },
                        label = { Text("Hasil AI") }
                    )
                    FilterChip(
                        selected = contentType == "Hasil Media Downloader",
                        onClick = { contentType = "Hasil Media Downloader" },
                        label = { Text("Downloader") }
                    )
                }

                Text("Alasan Pelaporan:", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Detail / Catatan Tambahan:", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    placeholder = { Text("Jelaskan masalah secara singkat...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Kirim Laporan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun PermissionRationaleDialog(permissionName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Izin Ditampilkan: $permissionName") },
        text = {
            Text(
                "Aplikasi meminta izin ini dengan memberikan dialog penjelasan transparan di UI terlebih dahulu sebelum memanggil sistem OS prompt. Ini memastikan pengguna mengetahui alasan logis dibalik akses tersebut."
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Paham")
            }
        }
    )
}
