package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.util.StorageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToOnboarding: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val storageManager = remember { com.example.util.StorageManagerImpl() }
    val storageInfo = remember { storageManager.getStorageInfo(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Storage") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Storage Information Section
            StorageInfoSection(storageInfo = storageInfo)
            
            // System Settings Section
            SettingsSection(
                title = "System & Intro",
                items = listOf(
                    SettingItem("Dark Mode", Icons.Filled.DarkMode, isSwitch = true),
                    SettingItem("Language", Icons.Filled.Language, value = "Indonesian")
                ),
                onOnboardingClick = if (onNavigateToOnboarding != null) {
                    {
                        com.example.ui.onboarding.OnboardingPreferences.setOnboardingCompleted(context, false)
                        onNavigateToOnboarding()
                    }
                } else null
            )

            // Smart Storage Section
            SettingsSection(
                title = "Smart Storage",
                items = listOf(
                    SettingItem("Auto Save", Icons.Filled.Save, isSwitch = true),
                    SettingItem("Auto Sync", Icons.Filled.Sync, isSwitch = true),
                    SettingItem("Auto Backup", Icons.Filled.Backup, isSwitch = true),
                    SettingItem("Auto Clean Cache", Icons.Filled.CleaningServices, isSwitch = true)
                )
            )

            // Backup & Restore Section
            SettingsSection(
                title = "Backup & Restore",
                items = listOf(
                    SettingItem("Backup to Cloud", Icons.Filled.CloudUpload),
                    SettingItem("Restore from Cloud", Icons.Filled.CloudDownload),
                    SettingItem("Backup Database", Icons.Filled.Storage)
                )
            )
            
            // Security Section
            SettingsSection(
                title = "Security",
                items = listOf(
                    SettingItem("AES Encryption", Icons.Filled.EnhancedEncryption, isSwitch = true),
                    SettingItem("Secure Storage", Icons.Filled.Security, isSwitch = true)
                )
            )

            // Developer Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ATBKZ Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Created By: Meydi", style = MaterialTheme.typography.labelMedium)
                    Text("Storage Engine: Hybrid Storage System", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StorageInfoSection(storageInfo: com.example.util.StorageInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Storage Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            LinearProgressIndicator(
                progress = { storageInfo.usedPercentage },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Used", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(storageInfo.usedSpaceFormatted, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Free", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(storageInfo.freeSpaceFormatted, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Files:", style = MaterialTheme.typography.bodyMedium)
                Text("${storageInfo.fileCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class SettingItem(
    val title: String, 
    val icon: androidx.compose.ui.graphics.vector.ImageVector, 
    val isSwitch: Boolean = false, 
    val value: String? = null
)

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingItem>,
    onOnboardingClick: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (item.isSwitch) {
                            var checked by remember { mutableStateOf(true) }
                            Switch(
                                checked = checked,
                                onCheckedChange = { checked = it }
                            )
                        } else if (item.value != null) {
                            Text(
                                text = item.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (index < items.size - 1 || onOnboardingClick != null) {
                        Divider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        )
                    }
                }

                if (onOnboardingClick != null) {
                    TextButton(
                        onClick = onOnboardingClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Lihat Tur Onboarding Lagi",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
