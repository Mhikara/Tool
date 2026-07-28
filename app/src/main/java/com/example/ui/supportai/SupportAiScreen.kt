package com.example.ui.supportai

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Data Models ---
data class SupportAiCategory(
    val id: String,
    val name: String,
    val icon: ImageVector
)

data class SupportAiFeature(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val icon: ImageVector,
    val status: String = "Active"
)

// --- ViewModel ---
class SupportAiViewModel : ViewModel() {
    private val _selectedCategoryId = MutableStateFlow("ai_providers")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _features = MutableStateFlow(getInitialFeatures())
    val features = _features.asStateFlow()

    val categories = listOf(
        SupportAiCategory("ai_providers", "AI Providers", Icons.Outlined.SmartToy),
        SupportAiCategory("ai_features", "AI Features", Icons.Outlined.AutoAwesome),
        SupportAiCategory("smart_ai", "Smart AI System", Icons.Outlined.Memory),
        SupportAiCategory("api_system", "API System", Icons.Outlined.Api),
        SupportAiCategory("security", "Security", Icons.Outlined.Security)
    )

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    private fun getInitialFeatures(): List<SupportAiFeature> {
        return listOf(
            // AI Providers
            SupportAiFeature("prov_openai", "OpenAI", "ai_providers", "GPT models, DALL-E, Whisper", Icons.Outlined.Code),
            SupportAiFeature("prov_gemini", "Google Gemini", "ai_providers", "Gemini Pro, Ultra, Vision", Icons.Outlined.Visibility),
            SupportAiFeature("prov_claude", "Anthropic Claude", "ai_providers", "Claude 3 Opus, Sonnet, Haiku", Icons.Outlined.Chat),
            SupportAiFeature("prov_deepseek", "DeepSeek", "ai_providers", "DeepSeek Coder & Chat", Icons.Outlined.Psychology),
            SupportAiFeature("prov_grok", "Grok", "ai_providers", "Real-time AI by xAI", Icons.Outlined.Language),
            SupportAiFeature("prov_mistral", "Mistral AI", "ai_providers", "Open weights AI models", Icons.Outlined.Cloud),
            SupportAiFeature("prov_cohere", "Cohere", "ai_providers", "Enterprise AI & NLP", Icons.Outlined.Business),
            SupportAiFeature("prov_perplexity", "Perplexity AI", "ai_providers", "AI Search Engine", Icons.Outlined.Search),
            SupportAiFeature("prov_together", "Together AI", "ai_providers", "Fast inference for open models", Icons.Outlined.Speed),
            SupportAiFeature("prov_hf", "Hugging Face", "ai_providers", "Machine learning hub API", Icons.Outlined.Hub),
            SupportAiFeature("prov_replicate", "Replicate", "ai_providers", "Run open-source models", Icons.Outlined.SettingsSuggest),
            SupportAiFeature("prov_stability", "Stability AI", "ai_providers", "Stable Diffusion & Video", Icons.Outlined.Brush),
            SupportAiFeature("prov_flux", "FLUX", "ai_providers", "Advanced image generation", Icons.Outlined.Image),
            SupportAiFeature("prov_eleven", "ElevenLabs", "ai_providers", "Realistic AI Voice", Icons.Outlined.RecordVoiceOver),
            SupportAiFeature("prov_whisper", "Whisper", "ai_providers", "Speech recognition", Icons.Outlined.Mic),
            SupportAiFeature("prov_groq", "Groq", "ai_providers", "LPU Inference Engine", Icons.Outlined.Bolt),
            SupportAiFeature("prov_openrouter", "OpenRouter", "ai_providers", "Unified AI API endpoint", Icons.Outlined.Router),
            SupportAiFeature("prov_ollama", "Ollama (Local AI)", "ai_providers", "Run LLMs locally", Icons.Outlined.Computer),
            SupportAiFeature("prov_lmstudio", "LM Studio (Local AI)", "ai_providers", "Local LLM desktop app", Icons.Outlined.LaptopMac),
            SupportAiFeature("prov_custom", "Custom AI API", "ai_providers", "Integrate your own endpoint", Icons.Outlined.DashboardCustomize),

            // AI Features
            SupportAiFeature("feat_chat", "AI Chat", "ai_features", "Percakapan AI natural", Icons.Outlined.ChatBubbleOutline),
            SupportAiFeature("feat_vision", "AI Vision", "ai_features", "Analisis & pemahaman gambar", Icons.Outlined.ImageSearch),
            SupportAiFeature("feat_img_gen", "AI Image Generation", "ai_features", "Pembuatan gambar dari teks", Icons.Outlined.AddPhotoAlternate),
            SupportAiFeature("feat_img_edit", "AI Image Editing", "ai_features", "Manipulasi gambar cerdas", Icons.Outlined.AutoFixHigh),
            SupportAiFeature("feat_video", "AI Video", "ai_features", "Pembuatan & pengeditan video", Icons.Outlined.Movie),
            SupportAiFeature("feat_audio", "AI Audio", "ai_features", "Pemrosesan audio AI", Icons.Outlined.Audiotrack),
            SupportAiFeature("feat_stt", "AI Speech to Text", "ai_features", "Transkripsi suara ke teks", Icons.Outlined.SettingsVoice),
            SupportAiFeature("feat_tts", "AI Text to Speech", "ai_features", "Sintesis suara manusia", Icons.Outlined.RecordVoiceOver),
            SupportAiFeature("feat_code", "AI Coding", "ai_features", "Bantuan penulisan kode", Icons.Outlined.Code),
            SupportAiFeature("feat_write", "AI Writing", "ai_features", "Generasi teks & copywriting", Icons.Outlined.EditNote),
            SupportAiFeature("feat_trans", "AI Translation", "ai_features", "Terjemahan multi-bahasa akurat", Icons.Outlined.Translate),
            SupportAiFeature("feat_ocr", "AI OCR", "ai_features", "Ekstraksi teks dari gambar", Icons.Outlined.DocumentScanner),
            SupportAiFeature("feat_search", "AI Search", "ai_features", "Pencarian pintar AI", Icons.Outlined.YoutubeSearchedFor),
            SupportAiFeature("feat_reason", "AI Reasoning", "ai_features", "Logika & pemecahan masalah", Icons.Outlined.Psychology),
            SupportAiFeature("feat_sum", "AI Summarization", "ai_features", "Ringkasan dokumen otomatis", Icons.Outlined.Summarize),
            SupportAiFeature("feat_embed", "AI Embedding", "ai_features", "Representasi vektor data", Icons.Outlined.DataObject),

            // Smart AI System
            SupportAiFeature("sys_multi", "Multi AI Support", "smart_ai", "Dukungan berbagai provider", Icons.Outlined.Diversity3),
            SupportAiFeature("sys_sel", "AI Model Selector", "smart_ai", "Pemilihan model dinamis", Icons.Outlined.ListAlt),
            SupportAiFeature("sys_auto_sel", "Auto AI Selection", "smart_ai", "Pemilihan model otomatis", Icons.Outlined.AutoAwesome),
            SupportAiFeature("sys_man_sel", "Manual AI Selection", "smart_ai", "Pemilihan model manual", Icons.Outlined.TouchApp),
            SupportAiFeature("sys_route", "AI Routing", "smart_ai", "Perutean request cerdas", Icons.Outlined.Route),
            SupportAiFeature("sys_fail", "AI Fallback", "smart_ai", "Cadangan saat model gagal", Icons.Outlined.Restore),
            SupportAiFeature("sys_health", "AI Health Check", "smart_ai", "Monitor status penyedia AI", Icons.Outlined.FavoriteBorder),
            SupportAiFeature("sys_load", "AI Load Balancing", "smart_ai", "Penyeimbang beban request", Icons.Outlined.Balance),
            SupportAiFeature("sys_cache", "AI Response Cache", "smart_ai", "Penyimpanan hasil cache", Icons.Outlined.Memory),
            SupportAiFeature("sys_stat", "AI Usage Statistics", "smart_ai", "Statistik penggunaan token", Icons.Outlined.QueryStats),

            // API System
            SupportAiFeature("api_key", "API Key Manager", "api_system", "Manajemen kunci API", Icons.Outlined.VpnKey),
            SupportAiFeature("api_enc", "API Encryption", "api_system", "Enkripsi kunci aman", Icons.Outlined.Lock),
            SupportAiFeature("api_val", "API Validation", "api_system", "Validasi integritas API", Icons.Outlined.FactCheck),
            SupportAiFeature("api_mon", "API Monitoring", "api_system", "Pemantauan lalu lintas", Icons.Outlined.MonitorHeart),
            SupportAiFeature("api_retry", "Auto Retry", "api_system", "Coba ulang otomatis", Icons.Outlined.Replay),
            SupportAiFeature("api_rot", "Auto Rotation", "api_system", "Rotasi kunci API", Icons.Outlined.Sync),
            SupportAiFeature("api_fail", "Auto Failover", "api_system", "Pengalihan saat gagal", Icons.Outlined.AltRoute),
            SupportAiFeature("api_queue", "Request Queue", "api_system", "Antrian request aman", Icons.Outlined.Queue),
            SupportAiFeature("api_err", "Error Handling", "api_system", "Penanganan error", Icons.Outlined.ErrorOutline),

            // Security
            SupportAiFeature("sec_https", "HTTPS Only", "security", "Wajib gunakan HTTPS", Icons.Outlined.Https),
            SupportAiFeature("sec_store", "Secure Storage", "security", "Penyimpanan lokal aman", Icons.Outlined.Security),
            SupportAiFeature("sec_key_enc", "API Key Encryption", "security", "Enkripsi AES untuk kunci", Icons.Outlined.EnhancedEncryption),
            SupportAiFeature("sec_jwt", "JWT Authentication", "security", "Autentikasi token JWT", Icons.Outlined.Key),
            SupportAiFeature("sec_req_val", "Request Validation", "security", "Validasi request body", Icons.Outlined.Rule),
            SupportAiFeature("sec_rate", "Rate Limiting", "security", "Pembatasan rate limit", Icons.Outlined.Speed)
        )
    }
}

// --- Main UI Component ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportAiScreen(onBack: () -> Unit) {
    val viewModel: SupportAiViewModel = viewModel()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val allFeatures by viewModel.features.collectAsState()
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
                            Text("Support AI System", fontWeight = FontWeight.Bold)
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
                                "Modul Integrasi AI: AKTIF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Mendukung berbagai penyedia AI dengan arsitektur modular yang siap dikembangkan.",
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

            // Features List
            item {
                Text(
                    "Sistem & Penyedia (${filteredFeatures.size})",
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
