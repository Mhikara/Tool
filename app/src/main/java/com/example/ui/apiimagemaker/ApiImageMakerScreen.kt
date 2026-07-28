package com.example.ui.apiimagemaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
data class ApiCategory(
    val id: String,
    val name: String,
    val icon: ImageVector
)

data class ApiFeature(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val features: List<String>,
    val icon: ImageVector,
    val status: String = "Active"
)

// --- ViewModel ---
class ApiImageMakerViewModel : ViewModel() {
    private val _selectedCategoryId = MutableStateFlow("image_api")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _apis = MutableStateFlow(getInitialApis())
    val apis = _apis.asStateFlow()

    val categories = listOf(
        ApiCategory("image_api", "AI Image API", Icons.Outlined.Image),
        ApiCategory("smart_manager", "Smart Manager", Icons.Outlined.Memory),
        ApiCategory("security", "Security", Icons.Outlined.Security)
    )

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    private fun getInitialApis(): List<ApiFeature> {
        return listOf(
            // AI Image API
            ApiFeature(
                id = "api_openai",
                name = "OpenAI Images API",
                categoryId = "image_api",
                description = "Primary AI Image API",
                features = listOf("Text to Image", "Image Editing", "Image Variations"),
                icon = Icons.Outlined.AutoAwesome,
                status = "Primary AI Image API"
            ),
            ApiFeature(
                id = "api_gemini",
                name = "Google Gemini API",
                categoryId = "image_api",
                description = "AI Vision",
                features = listOf("Analisis Gambar", "Caption Gambar", "OCR", "Visual Understanding"),
                icon = Icons.Outlined.Visibility,
                status = "AI Vision"
            ),
            ApiFeature(
                id = "api_stability",
                name = "Stability AI API",
                categoryId = "image_api",
                description = "AI Art",
                features = listOf("AI Image Generation", "Image Editing", "Inpainting", "Outpainting"),
                icon = Icons.Outlined.Brush,
                status = "AI Art"
            ),
            ApiFeature(
                id = "api_flux",
                name = "FLUX API",
                categoryId = "image_api",
                description = "Premium",
                features = listOf("AI Image Generator", "Realistic Image", "Anime", "Illustration"),
                icon = Icons.Outlined.Stars,
                status = "Premium"
            ),
            ApiFeature(
                id = "api_replicate",
                name = "Replicate API",
                categoryId = "image_api",
                description = "Multi AI Models",
                features = listOf("Stable Diffusion", "FLUX", "SDXL", "ESRGAN", "Background Removal"),
                icon = Icons.Outlined.AccountTree,
                status = "Multi AI Models"
            ),
            ApiFeature(
                id = "api_removebg",
                name = "Remove.bg API",
                categoryId = "image_api",
                description = "AI Background Removal",
                features = listOf("AI Background Removal"),
                icon = Icons.Outlined.LayersClear,
                status = "Active"
            ),
            ApiFeature(
                id = "api_clipdrop",
                name = "ClipDrop API",
                categoryId = "image_api",
                description = "Image Processing",
                features = listOf("Background Removal", "Cleanup", "Relight", "Upscale"),
                icon = Icons.Outlined.AutoFixHigh,
                status = "Active"
            ),
            ApiFeature(
                id = "api_imgbb",
                name = "ImgBB API",
                categoryId = "image_api",
                description = "Image Hosting",
                features = listOf("Upload Image", "Image Hosting"),
                icon = Icons.Outlined.CloudUpload,
                status = "Active"
            ),
            ApiFeature(
                id = "api_cloudinary",
                name = "Cloudinary API",
                categoryId = "image_api",
                description = "Image Storage & CDN",
                features = listOf("Image Storage", "Resize", "Crop", "Compress", "Watermark", "CDN"),
                icon = Icons.Outlined.CloudQueue,
                status = "Active"
            ),
            ApiFeature(
                id = "api_imgur",
                name = "Imgur API",
                categoryId = "image_api",
                description = "Image Gallery",
                features = listOf("Upload", "Share Image", "Gallery"),
                icon = Icons.Outlined.PhotoLibrary,
                status = "Active"
            ),

            // Smart Manager
            ApiFeature("mgr_health", "API Health Check", "smart_manager", "Pemeriksaan kesehatan API otomatis", emptyList(), Icons.Outlined.HealthAndSafety),
            ApiFeature("mgr_rotate", "Auto API Rotation", "smart_manager", "Rotasi otomatis API key", emptyList(), Icons.Outlined.Sync),
            ApiFeature("mgr_retry", "Auto Retry", "smart_manager", "Coba ulang otomatis saat gagal", emptyList(), Icons.Outlined.Replay),
            ApiFeature("mgr_failover", "Auto Failover", "smart_manager", "Peralihan otomatis ke API cadangan", emptyList(), Icons.Outlined.AltRoute),
            ApiFeature("mgr_cache", "API Cache", "smart_manager", "Sistem caching respon API", emptyList(), Icons.Outlined.Cached),
            ApiFeature("mgr_usage", "Usage Monitor", "smart_manager", "Pemantauan penggunaan API", emptyList(), Icons.Outlined.DataUsage),
            ApiFeature("mgr_error", "Error Log", "smart_manager", "Catatan kesalahan API", emptyList(), Icons.Outlined.ErrorOutline),
            ApiFeature("mgr_response", "Response Monitor", "smart_manager", "Pemantauan respon dan latensi", emptyList(), Icons.Outlined.Speed),

            // Security
            ApiFeature("sec_https", "HTTPS", "security", "Koneksi terenkripsi aman", emptyList(), Icons.Outlined.Https),
            ApiFeature("sec_key", "API Key Encryption", "security", "Enkripsi kunci API", emptyList(), Icons.Outlined.VpnKey),
            ApiFeature("sec_store", "Secure Storage", "security", "Penyimpanan data aman", emptyList(), Icons.Outlined.Security),
            ApiFeature("sec_rate", "Rate Limiting", "security", "Pembatasan laju permintaan API", emptyList(), Icons.Outlined.Speed),
            ApiFeature("sec_val", "Request Validation", "security", "Validasi input permintaan", emptyList(), Icons.Outlined.Rule)
        )
    }
}

// --- Main UI Component ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiImageMakerScreen(onBack: () -> Unit) {
    val viewModel: ApiImageMakerViewModel = viewModel()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val allApis by viewModel.apis.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }

    val filteredApis = remember(selectedCategory, allApis) {
        allApis.filter { it.categoryId == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sistem API Image Maker", fontWeight = FontWeight.Bold)
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
                            delay(1000)
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
                                "API Manager: AKTIF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Seluruh API integrasi siap beroperasi.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
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

            // List
            item {
                Text(
                    "Konfigurasi & Modul (${filteredApis.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(filteredApis) { feature ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                        
                        if (feature.features.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Fungsi Tersedia:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                feature.features.forEach { func ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = func,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
