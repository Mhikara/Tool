package com.example.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.DeviceInfo
import com.example.util.getDeviceInfo

data class CategoryGroup(
    val name: String,
    val icon: ImageVector,
    val items: List<CategoryItem>
)

data class CategoryItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val badge: String? = null,
    val badgeColor: Color? = null,
    val categoryName: String = "Lainnya"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCategory: (String) -> Unit
) {
    val context = LocalContext.current
    var deviceInfo by remember { mutableStateOf(getDeviceInfo(context)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterGroup by remember { mutableStateOf("Semua") }

    val allCategories = remember {
        listOf(
            CategoryItem(
                title = "Autentikasi & Akun",
                description = "Login, Registrasi, MFA, OTP & Sesi",
                icon = Icons.Outlined.LockPerson,
                route = "login_system",
                badge = "NEW",
                badgeColor = Color(0xFF4CAF50),
                categoryName = "Keamanan & Akun"
            ),
            CategoryItem(
                title = "Super Security",
                description = "Enkripsi, Audit Log, IP & Sesi aktif",
                icon = Icons.Outlined.Security,
                route = "security_system",
                badge = "SECURE",
                badgeColor = Color(0xFF2196F3),
                categoryName = "Keamanan & Akun"
            ),
            CategoryItem(
                title = "Text-to-Media AI",
                description = "Generasi Gambar, Video & Prompt AI",
                icon = Icons.Outlined.AutoFixHigh,
                route = "ai_media_hub",
                badge = "AI",
                badgeColor = Color(0xFF9C27B0),
                categoryName = "AI & Intelligence"
            ),
            CategoryItem(
                title = "AI Features",
                description = "Fitur Cerdas AI Multi-Model",
                icon = Icons.Outlined.AutoAwesome,
                route = "ai",
                badge = "AI",
                badgeColor = Color(0xFF9C27B0),
                categoryName = "AI & Intelligence"
            ),
            CategoryItem(
                title = "AI Assistant",
                description = "Asisten Virtual Interaktif",
                icon = Icons.Outlined.SmartToy,
                route = "aiassistant",
                badge = "AI",
                badgeColor = Color(0xFF9C27B0),
                categoryName = "AI & Intelligence"
            ),
            CategoryItem(
                title = "AI Agent",
                description = "Otomasi Tugas & Perencanaan AI",
                icon = Icons.Outlined.Psychology,
                route = "aiagent",
                badge = "AI",
                badgeColor = Color(0xFF9C27B0),
                categoryName = "AI & Intelligence"
            ),
            CategoryItem(
                title = "API Image Maker",
                description = "Buat Gambar via API Key",
                icon = Icons.Outlined.Api,
                route = "apiimagemaker",
                categoryName = "AI & Intelligence"
            ),
            CategoryItem(
                title = "Support AI",
                description = "Layanan Bantuan Berbasis AI",
                icon = Icons.Outlined.Diversity3,
                route = "supportai",
                categoryName = "AI & Intelligence"
            ),
            CategoryItem(
                title = "File Manager",
                description = "Kelola Penyimpanan & Dokumen",
                icon = Icons.Outlined.Folder,
                route = "file_manager",
                categoryName = "Kreatif & Utilitas"
            ),
            CategoryItem(
                title = "Downloader",
                description = "Unduh Media & Berkas Web",
                icon = Icons.Outlined.Download,
                route = "downloader",
                categoryName = "Kreatif & Utilitas"
            ),
            CategoryItem(
                title = "Maker & QR",
                description = "Pembuat QR Code & Desain Kustom",
                icon = Icons.Outlined.Brush,
                route = "maker",
                categoryName = "Kreatif & Utilitas"
            ),
            CategoryItem(
                title = "Social Media Maker",
                description = "Template & Konten Media Sosial",
                icon = Icons.Outlined.Share,
                route = "social_maker",
                categoryName = "Kreatif & Utilitas"
            ),
            CategoryItem(
                title = "Spotify Hub",
                description = "Streaming & Informasi Musik",
                icon = Icons.Outlined.LibraryMusic,
                route = "spotify",
                categoryName = "Kreatif & Utilitas"
            ),
            CategoryItem(
                title = "Tools Kit",
                description = "Kalkulator, Hitung Kata & Konverter",
                icon = Icons.Outlined.Build,
                route = "tools",
                categoryName = "Kreatif & Utilitas"
            ),
            CategoryItem(
                title = "Device Tools",
                description = "Informasi & Diagnostik Perangkat",
                icon = Icons.Outlined.PermDeviceInformation,
                route = "device_tools",
                categoryName = "Kreatif & Utilitas"
            ),
            CategoryItem(
                title = "System Server",
                description = "Monitoring Server & Database",
                icon = Icons.Outlined.Dns,
                route = "server",
                badge = "SYS",
                badgeColor = Color(0xFFFF9800),
                categoryName = "Sistem & Layanan"
            ),
            CategoryItem(
                title = "API Manager",
                description = "Konfigurasi & Manajemen Key",
                icon = Icons.Outlined.Key,
                route = "apimanager",
                categoryName = "Sistem & Layanan"
            ),
            CategoryItem(
                title = "Backup & Sync",
                description = "Cadangkan & Sinkronisasi Cloud",
                icon = Icons.Outlined.CloudSync,
                route = "backupsync",
                categoryName = "Sistem & Layanan"
            ),
            CategoryItem(
                title = "Keamanan & Policy",
                description = "Syarat Ketentuan & Kebijakan",
                icon = Icons.Outlined.Gavel,
                route = "compliance",
                categoryName = "Sistem & Layanan"
            ),
            CategoryItem(
                title = "Settings",
                description = "Pengaturan Aplikasi & Tema",
                icon = Icons.Outlined.Settings,
                route = "settings",
                categoryName = "Sistem & Layanan"
            ),
            CategoryItem(
                title = "Donasi QRIS",
                description = "Dukungan Pengembangan & QRIS",
                icon = Icons.Outlined.FavoriteBorder,
                route = "donation",
                badge = "QRIS",
                badgeColor = Color(0xFFE91E63),
                categoryName = "Lainnya"
            ),
            CategoryItem(
                title = "Premium Plan",
                description = "Fitur Pro & Akses Tanpa Batas",
                icon = Icons.Outlined.Star,
                route = "premium",
                badge = "PRO",
                badgeColor = Color(0xFFFFC107),
                categoryName = "Lainnya"
            )
        )
    }

    val filterGroups = listOf("Semua", "AI & Intelligence", "Keamanan & Akun", "Kreatif & Utilitas", "Sistem & Layanan", "Lainnya")

    val filteredCategories = remember(searchQuery, selectedFilterGroup) {
        allCategories.filter { item ->
            val matchesSearch = searchQuery.isEmpty() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            val matchesGroup = selectedFilterGroup == "Semua" || item.categoryName == selectedFilterGroup
            matchesSearch && matchesGroup
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "ATBKZ Super Tools",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "v1.0.0 • By Meydi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToCategory("login_system") }) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profil",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HeroHeaderCard(deviceInfo = deviceInfo, onNavigate = onNavigateToCategory)
            }

            item {
                SearchBarSection(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            item {
                CategoryFilterChips(
                    groups = filterGroups,
                    selectedGroup = selectedFilterGroup,
                    onGroupSelected = { selectedFilterGroup = it }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Hasil Pencarian (${filteredCategories.size})" else "$selectedFilterGroup (${filteredCategories.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (filteredCategories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Fitur tidak ditemukan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Coba gunakan kata kunci pencarian atau kategori lain",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                val chunkedItems = filteredCategories.chunked(2)
                items(chunkedItems) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in rowItems) {
                            CategoryGridCard(
                                modifier = Modifier.weight(1f),
                                item = item,
                                onClick = { onNavigateToCategory(item.route) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HeroHeaderCard(deviceInfo: DeviceInfo, onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Developer Lead",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Meydi Hikara",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (deviceInfo.isOnline) Color(0xFF4CAF50).copy(alpha = 0.25f) else Color.Red.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (deviceInfo.isOnline) Color(0xFF4CAF50) else Color.Red)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (deviceInfo.isOnline) "ONLINE" else "OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniInfoPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.BatteryFull,
                        label = "Baterai",
                        value = "${deviceInfo.batteryPercent}%"
                    )
                    MiniInfoPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Wifi,
                        label = "Jaringan",
                        value = deviceInfo.connectionType
                    )
                    MiniInfoPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.PhoneAndroid,
                        label = "Perangkat",
                        value = deviceInfo.device
                    )
                }
            }
        }
    }
}

@Composable
fun MiniInfoPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Cari fitur, alat, atau layanan...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun CategoryFilterChips(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(groups) { group ->
            val isSelected = group == selectedGroup
            FilterChip(
                selected = isSelected,
                onClick = { onGroupSelected(group) },
                label = {
                    Text(
                        text = group,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGridCard(
    modifier: Modifier = Modifier,
    item: CategoryItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (item.badge != null && item.badgeColor != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = item.badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = item.badge,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = item.badgeColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

