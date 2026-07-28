package com.example.ui.serverdata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Data Models ---
data class ServerCategory(
    val id: String,
    val name: String,
    val icon: ImageVector
)

data class ServerFeature(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val icon: ImageVector,
    val status: String = "Active"
)

// --- ViewModel ---
class ServerDataViewModel : ViewModel() {
    private val _selectedCategoryId = MutableStateFlow("server")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _features = MutableStateFlow(getInitialFeatures())
    val features = _features.asStateFlow()

    private val _serverUptime = MutableStateFlow("99.99%")
    val serverUptime = _serverUptime.asStateFlow()

    val categories = listOf(
        ServerCategory("server", "Server", Icons.Outlined.Dns),
        ServerCategory("database", "Database", Icons.Outlined.Storage),
        ServerCategory("users", "Data Pengguna", Icons.Outlined.People),
        ServerCategory("appdata", "Data Aplikasi", Icons.Outlined.Apps),
        ServerCategory("sync", "Sinkronisasi", Icons.Outlined.Sync),
        ServerCategory("backup", "Backup", Icons.Outlined.Backup),
        ServerCategory("security", "Keamanan", Icons.Outlined.Security),
        ServerCategory("monitoring", "Monitoring", Icons.Outlined.MonitorHeart),
        ServerCategory("admin", "Admin Dashboard", Icons.Outlined.AdminPanelSettings),
        ServerCategory("notif", "Notifikasi", Icons.Outlined.Notifications),
        ServerCategory("optimasi", "Optimasi", Icons.Outlined.Speed)
    )

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    private fun getInitialFeatures(): List<ServerFeature> {
        return listOf(
            // Server
            ServerFeature("srv_rest", "REST API", "server", "Antarmuka pemrograman aplikasi web.", Icons.Outlined.Api),
            ServerFeature("srv_https", "HTTPS & SSL/TLS", "server", "Koneksi terenkripsi aman.", Icons.Outlined.Lock),
            ServerFeature("srv_cdn", "CDN Support", "server", "Content Delivery Network global.", Icons.Outlined.Public),
            ServerFeature("srv_lb", "Load Balancer", "server", "Distribusi lalu lintas jaringan.", Icons.Outlined.AltRoute),
            ServerFeature("srv_scale", "Auto Scaling", "server", "Skala sumber daya otomatis.", Icons.Outlined.SettingsOverscan),
            ServerFeature("srv_proxy", "Reverse Proxy", "server", "Server perantara lalu lintas.", Icons.Outlined.Router),
            ServerFeature("srv_health", "Health Check", "server", "Pemeriksaan kesehatan server berkala.", Icons.Outlined.Favorite),
            ServerFeature("srv_mon", "Monitoring", "server", "Pemantauan server real-time.", Icons.Outlined.Monitor),

            // Database
            ServerFeature("db_fire", "Firebase Firestore", "database", "Database NoSQL real-time.", Icons.Outlined.LocalFireDepartment),
            ServerFeature("db_supa", "Supabase PostgreSQL", "database", "Database relasional open source.", Icons.Outlined.Dataset),
            ServerFeature("db_mysql", "MySQL", "database", "Manajemen database relasional.", Icons.Outlined.TableChart),
            ServerFeature("db_sqlite", "SQLite (Offline Cache)", "database", "Penyimpanan lokal untuk offline mode.", Icons.Outlined.Storage),

            // Data Pengguna
            ServerFeature("usr_prof", "Profil Pengguna", "users", "Manajemen data profil pengguna.", Icons.Outlined.Person),
            ServerFeature("usr_log", "Login", "users", "Sistem autentikasi pengguna.", Icons.Outlined.Login),
            ServerFeature("usr_prem", "Premium", "users", "Manajemen status langganan premium.", Icons.Outlined.WorkspacePremium),
            ServerFeature("usr_set", "Pengaturan", "users", "Pengaturan preferensi pengguna.", Icons.Outlined.Settings),
            ServerFeature("usr_hist_act", "Riwayat Aktivitas", "users", "Log aktivitas harian pengguna.", Icons.Outlined.History),
            ServerFeature("usr_hist_dl", "Riwayat Download", "users", "Log unduhan pengguna.", Icons.Outlined.FileDownload),
            ServerFeature("usr_hist_ai", "Riwayat AI", "users", "Log penggunaan fitur AI.", Icons.Outlined.AutoAwesome),
            ServerFeature("usr_hist_pay", "Riwayat Pembayaran", "users", "Log transaksi dan pembayaran.", Icons.Outlined.Payment),

            // Data Aplikasi
            ServerFeature("app_api", "API Configuration", "appdata", "Konfigurasi API eksternal.", Icons.Outlined.SettingsInputComponent),
            ServerFeature("app_plug", "Plugin", "appdata", "Manajemen ekstensi aplikasi.", Icons.Outlined.Extension),
            ServerFeature("app_temp", "Template", "appdata", "Manajemen template UI.", Icons.Outlined.DashboardCustomize),
            ServerFeature("app_ban", "Banner", "appdata", "Pengaturan banner promosi.", Icons.Outlined.ViewCarousel),
            ServerFeature("app_ann", "Pengumuman", "appdata", "Sistem pengumuman aplikasi.", Icons.Outlined.Campaign),
            ServerFeature("app_upd", "Update", "appdata", "Manajemen pembaruan aplikasi.", Icons.Outlined.Update),
            ServerFeature("app_stat", "Statistik", "appdata", "Statistik penggunaan aplikasi.", Icons.Outlined.BarChart),

            // Sinkronisasi
            ServerFeature("sync_auto", "Auto Sync", "sync", "Sinkronisasi otomatis di latar.", Icons.Outlined.Sync),
            ServerFeature("sync_man", "Manual Sync", "sync", "Sinkronisasi manual oleh pengguna.", Icons.Outlined.SyncAlt),
            ServerFeature("sync_bg", "Background Sync", "sync", "Sinkronisasi saat aplikasi tertutup.", Icons.Outlined.PendingActions),
            ServerFeature("sync_conf", "Conflict Resolution", "sync", "Penanganan konflik data.", Icons.Outlined.MergeType),
            ServerFeature("sync_delta", "Delta Sync", "sync", "Sinkronisasi hanya data berubah.", Icons.Outlined.CompareArrows),
            ServerFeature("sync_real", "Real-Time Update", "sync", "Pembaruan data instan.", Icons.Outlined.FlashOn),

            // Backup
            ServerFeature("bak_auto", "Auto Backup", "backup", "Backup data terjadwal.", Icons.Outlined.Backup),
            ServerFeature("bak_man", "Manual Backup", "backup", "Backup data manual.", Icons.Outlined.Save),
            ServerFeature("bak_cld", "Cloud Backup", "backup", "Penyimpanan backup di cloud.", Icons.Outlined.CloudUpload),
            ServerFeature("bak_res", "Restore Backup", "backup", "Pemulihan data dari backup.", Icons.Outlined.SettingsBackupRestore),
            ServerFeature("bak_db", "Backup Database", "backup", "Pencadangan seluruh database.", Icons.Outlined.Storage),
            ServerFeature("bak_set", "Backup Pengaturan", "backup", "Pencadangan pengaturan sistem.", Icons.Outlined.DisplaySettings),

            // Keamanan
            ServerFeature("sec_jwt", "JWT Authentication", "security", "Autentikasi token aman.", Icons.Outlined.Key),
            ServerFeature("sec_https", "HTTPS Only", "security", "Hanya izinkan lalu lintas HTTPS.", Icons.Outlined.Https),
            ServerFeature("sec_aes", "AES-256 Encryption", "security", "Enkripsi data tingkat militer.", Icons.Outlined.EnhancedEncryption),
            ServerFeature("sec_store", "Secure Storage", "security", "Penyimpanan kredensial aman.", Icons.Outlined.Security),
            ServerFeature("sec_apikey", "API Key Encryption", "security", "Enkripsi kunci API.", Icons.Outlined.VpnKey),
            ServerFeature("sec_rbac", "Role Based Access Control", "security", "Kontrol akses berbasis peran.", Icons.Outlined.ManageAccounts),
            ServerFeature("sec_val", "Request Validation", "security", "Validasi input permintaan.", Icons.Outlined.Rule),
            ServerFeature("sec_rate", "Rate Limiting", "security", "Pembatasan laju permintaan API.", Icons.Outlined.Speed),
            ServerFeature("sec_audit", "Audit Log", "security", "Pencatatan log keamanan.", Icons.Outlined.ListAlt),

            // Monitoring
            ServerFeature("mon_cpu", "CPU Usage", "monitoring", "Pemantauan penggunaan CPU.", Icons.Outlined.Memory),
            ServerFeature("mon_ram", "RAM Usage", "monitoring", "Pemantauan memori RAM.", Icons.Outlined.Memory),
            ServerFeature("mon_store", "Storage Usage", "monitoring", "Pemantauan penyimpanan disk.", Icons.Outlined.SdStorage),
            ServerFeature("mon_net", "Network Usage", "monitoring", "Pemantauan lalu lintas jaringan.", Icons.Outlined.NetworkCheck),
            ServerFeature("mon_usr", "Active Users", "monitoring", "Jumlah pengguna aktif saat ini.", Icons.Outlined.PeopleOutline),
            ServerFeature("mon_ses", "Active Sessions", "monitoring", "Sesi aktif aplikasi.", Icons.Outlined.Timer),
            ServerFeature("mon_api", "API Status", "monitoring", "Status uptime API.", Icons.Outlined.Api),
            ServerFeature("mon_err", "Error Log", "monitoring", "Catatan kesalahan sistem.", Icons.Outlined.ErrorOutline),
            ServerFeature("mon_hlth", "Server Health", "monitoring", "Kesehatan server keseluruhan.", Icons.Outlined.HealthAndSafety),

            // Admin Dashboard
            ServerFeature("adm_usr", "Kelola Pengguna", "admin", "Manajemen akun pengguna.", Icons.Outlined.ManageAccounts),
            ServerFeature("adm_prem", "Kelola Premium", "admin", "Manajemen langganan premium.", Icons.Outlined.WorkspacePremium),
            ServerFeature("adm_api", "Kelola API", "admin", "Pengaturan API keys & limits.", Icons.Outlined.Api),
            ServerFeature("adm_plug", "Kelola Plugin", "admin", "Instalasi & update plugin.", Icons.Outlined.Extension),
            ServerFeature("adm_pay", "Kelola Pembayaran", "admin", "Manajemen transaksi.", Icons.Outlined.Payments),
            ServerFeature("adm_don", "Kelola Donasi", "admin", "Manajemen dukungan dana.", Icons.Outlined.FavoriteBorder),
            ServerFeature("adm_notif", "Kelola Notifikasi", "admin", "Pengiriman push notification.", Icons.Outlined.NotificationsActive),
            ServerFeature("adm_srv", "Kelola Server", "admin", "Pengaturan server inti.", Icons.Outlined.Dns),
            ServerFeature("adm_bak", "Kelola Backup", "admin", "Manajemen sistem cadangan.", Icons.Outlined.Backup),
            ServerFeature("adm_log", "Kelola Log", "admin", "Peninjauan log sistem.", Icons.Outlined.ReceiptLong),

            // Notifikasi
            ServerFeature("ntf_push", "Push Notification", "notif", "Notifikasi push ke perangkat.", Icons.Outlined.Notifications),
            ServerFeature("ntf_bcst", "Broadcast", "notif", "Pesan siaran ke semua pengguna.", Icons.Outlined.Campaign),
            ServerFeature("ntf_maint", "Maintenance Notice", "notif", "Pemberitahuan pemeliharaan.", Icons.Outlined.Construction),
            ServerFeature("ntf_upd", "Update Notification", "notif", "Pemberitahuan pembaruan versi.", Icons.Outlined.SystemUpdate),
            ServerFeature("ntf_sec", "Security Alert", "notif", "Peringatan keamanan sistem.", Icons.Outlined.WarningAmber),

            // Optimasi
            ServerFeature("opt_cache", "Cache System", "optimasi", "Sistem caching memori.", Icons.Outlined.Cached),
            ServerFeature("opt_lazy", "Lazy Loading", "optimasi", "Pemuatan data bertahap.", Icons.Outlined.HourglassEmpty),
            ServerFeature("opt_comp", "Compression", "optimasi", "Kompresi respons data.", Icons.Outlined.Compress),
            ServerFeature("opt_cdn", "CDN Cache", "optimasi", "Caching pada tingkat CDN.", Icons.Outlined.Public),
            ServerFeature("opt_img", "Image Optimization", "optimasi", "Optimasi ukuran gambar.", Icons.Outlined.Image),
            ServerFeature("opt_qry", "Query Optimization", "optimasi", "Optimasi kueri database.", Icons.Outlined.Speed)
        )
    }
}

// --- Main UI Component ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDataScreen(onBack: () -> Unit) {
    val viewModel: ServerDataViewModel = viewModel()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val allFeatures by viewModel.features.collectAsState()
    val serverUptime by viewModel.serverUptime.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }

    val filteredFeatures = remember(selectedCategory, allFeatures) {
        allFeatures.filter { it.categoryId == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sistem Server Data", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "v1.0.0",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            "Created By : Meydi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            isRefreshing = true
                            delay(1500)
                            isRefreshing = false
                        }
                    }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh Data", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Status Server: AKTIF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Semua sistem berjalan normal.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Uptime: $serverUptime | Latency: 42ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            // Categories Selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category.id,
                            onClick = { viewModel.setCategory(category.id) },
                            label = { Text(category.name) },
                            leadingIcon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Features List
            item {
                Text(
                    "Modul & Konfigurasi (${filteredFeatures.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(filteredFeatures) { feature ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = feature.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = feature.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = feature.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Status Badge
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = feature.status,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
