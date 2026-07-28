package com.example.ui.backupsync

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.backupsync.BackupManager
import com.example.core.backupsync.RestoreManager
import com.example.core.backupsync.SyncEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SyncCategory(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

class BackupSyncViewModel : ViewModel() {
    private val _isCloudSyncEnabled = MutableStateFlow(false)
    val isCloudSyncEnabled = _isCloudSyncEnabled.asStateFlow()

    private val _isWifiOnly = MutableStateFlow(true)
    val isWifiOnly = _isWifiOnly.asStateFlow()

    private val _categories = MutableStateFlow(listOf(
        SyncCategory("favorites", "Favorite Tools", "Alat yang ditandai favorit", true, Icons.Outlined.Favorite),
        SyncCategory("secure_notes", "Secure Notes", "Catatan terenkripsi E2EE", true, Icons.Outlined.Lock),
        SyncCategory("maker_projects", "Project Maker Studio", "Draft dan project tersimpan", true, Icons.Outlined.Brush),
        SyncCategory("preferences", "Pengaturan Aplikasi", "Tema, bahasa, dan konfigurasi", true, Icons.Outlined.Settings),
        SyncCategory("qr_history", "Riwayat QR", "Data hasil scan QR", false, Icons.Outlined.QrCode),
        SyncCategory("tool_history", "Riwayat Penggunaan", "Histori alat yang sering dipakai", false, Icons.Outlined.History)
    ))
    val categories = _categories.asStateFlow()

    private val _syncStatus = MutableStateFlow("Terakhir sinkronisasi: Belum pernah")
    val syncStatus = _syncStatus.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()
    
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()
    
    private val _showRestoreDialog = MutableStateFlow(false)
    val showRestoreDialog = _showRestoreDialog.asStateFlow()

    fun toggleCloudSync(enabled: Boolean) {
        _isCloudSyncEnabled.value = enabled
        if (!enabled) {
            // Cancel background jobs
        }
    }

    fun toggleWifiOnly(enabled: Boolean) {
        _isWifiOnly.value = enabled
    }

    fun toggleCategory(id: String) {
        _categories.value = _categories.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun triggerManualBackup(context: android.content.Context) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Menyiapkan data untuk enkripsi..."
            
            val manager = BackupManager(context)
            val activeCategories = _categories.value.filter { it.isEnabled }.map { it.id }.toSet()
            
            // Note: In real implementation, we wait for Worker to finish. 
            // For UI feedback simulation:
            kotlinx.coroutines.delay(1000)
            _syncStatus.value = "Mengenkripsi dan mengunggah ke Cloud..."
            
            manager.triggerManualBackup(activeCategories)
            
            kotlinx.coroutines.delay(2000)
            
            _isSyncing.value = false
            _syncStatus.value = "Terakhir sinkronisasi: Baru saja"
        }
    }
    
    fun deleteCloudData() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Menghapus data di Cloud..."
            
            val syncEngine = SyncEngine()
            syncEngine.deleteCloudData()
            
            _isSyncing.value = false
            _syncStatus.value = "Data Cloud telah dihapus."
            _showDeleteDialog.value = false
        }
    }
    
    fun setDeleteDialogVisible(visible: Boolean) {
        _showDeleteDialog.value = visible
    }

    fun setRestoreDialogVisible(visible: Boolean) {
        _showRestoreDialog.value = visible
    }
    
    fun restoreData() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Mengunduh dan menerapkan data dari Cloud..."
            _showRestoreDialog.value = false
            
            val restoreManager = RestoreManager()
            val result = restoreManager.restoreBackup("latest", emptySet())
            
            _isSyncing.value = false
            if (result.isSuccess) {
                _syncStatus.value = "Restore berhasil."
            } else {
                _syncStatus.value = "Restore gagal: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSyncScreen(onBack: () -> Unit) {
    val viewModel: BackupSyncViewModel = viewModel()
    val context = LocalContext.current
    
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()
    val isWifiOnly by viewModel.isWifiOnly.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showRestoreDialog by viewModel.showRestoreDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Sync", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
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
            // Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Cloud Sync Otomatis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Aman terenkripsi E2EE", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = isCloudSyncEnabled,
                                onCheckedChange = viewModel::toggleCloudSync
                            )
                        }
                        
                        if (isCloudSyncEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(syncStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row {
                                Button(
                                    onClick = { viewModel.triggerManualBackup(context) },
                                    enabled = !isSyncing
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Sync Sekarang")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.setRestoreDialogVisible(true) },
                                    enabled = !isSyncing
                                ) {
                                    Text("Pulihkan")
                                }
                            }
                        }
                    }
                }
            }

            if (isCloudSyncEnabled) {
                // Preferences
                item {
                    Text("Pengaturan Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hanya gunakan Wi-Fi", fontWeight = FontWeight.SemiBold)
                            Text("Menghemat kuota data seluler", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isWifiOnly,
                            onCheckedChange = viewModel::toggleWifiOnly
                        )
                    }
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Data yang Disinkronkan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(categories.size) { index ->
                    val category = categories[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(category.name, fontWeight = FontWeight.SemiBold)
                            Text(category.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Checkbox(
                            checked = category.isEnabled,
                            onCheckedChange = { viewModel.toggleCategory(category.id) }
                        )
                    }
                }
                
                item {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    TextButton(
                        onClick = { viewModel.setDeleteDialogVisible(true) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hapus Semua Data Cloud")
                    }
                }
            }
        }

        // Dialogs
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setDeleteDialogVisible(false) },
                title = { Text("Hapus Data Cloud?") },
                text = { Text("Semua data cadangan Anda di cloud akan dihapus secara permanen. Data di perangkat ini tidak akan terpengaruh.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteCloudData() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setDeleteDialogVisible(false) }) {
                        Text("Batal")
                    }
                }
            )
        }

        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setRestoreDialogVisible(false) },
                title = { Text("Pulihkan dari Cloud?") },
                text = { Text("Tindakan ini akan mengunduh data cadangan terakhir dan menimpa data lokal saat ini. Jika terjadi konflik pada Secure Notes, Anda akan diminta memilih. Lanjutkan?") },
                confirmButton = {
                    Button(onClick = { viewModel.restoreData() }) {
                        Text("Pulihkan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setRestoreDialogVisible(false) }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
