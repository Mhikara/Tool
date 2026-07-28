package com.example.ui.socialmaker

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SocialPlatform(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val category: String,
    val dimensions: String,
    val ratio: String,
    val accentColor: Color
)

data class SocialTemplate(
    val id: String,
    val title: String,
    val platformName: String,
    val category: String,
    val previewGradient: List<Color>,
    val defaultText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaMakerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var activeTab by remember { mutableIntStateOf(0) }

    // Platform Selection State
    var selectedPlatformId by remember { mutableStateOf("ig_post") }
    var customPostTitle by remember { mutableStateOf("Promo Hari Ini! Discount 50%") }
    var customPostSubtitle by remember { mutableStateOf("Dapatkan penawaran terbatas di bio link!") }
    var selectedBrandColor by remember { mutableStateOf(Color(0xFFE91E63)) }

    // Export & Filter state
    var selectedExportFormat by remember { mutableStateOf("PNG") }
    var selectedQuality by remember { mutableStateOf("Full HD") }
    var watermarkEnabled by remember { mutableStateOf(true) }
    var watermarkText by remember { mutableStateOf("@MeydiStudio") }

    // AI Content State
    var aiTopicInput by remember { mutableStateOf("") }
    var aiContentType by remember { mutableStateOf("Caption & Hashtag") }
    var aiTone by remember { mutableStateOf("Professional & Friendly") }
    var generatedAiText by remember { mutableStateOf("") }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var aiProgressVal by remember { mutableFloatStateOf(0f) }

    // Draft / Favorites
    val favoritesList = remember {
        mutableStateListOf(
            "Promo Spesial Akhir Pekan! #DiskonHebat #PromoMurah #MeydiStudio",
            "Tips Produktivitas Kerja Remote 2026 #WorkLifeBalance #TipsSukses"
        )
    }

    val platforms = listOf(
        SocialPlatform("ig_post", "Instagram Post", Icons.Filled.CameraAlt, "Instagram", "1080x1080", "1:1", Color(0xFFE1306C)),
        SocialPlatform("ig_story", "Instagram Story", Icons.Filled.HistoryToggleOff, "Instagram", "1080x1920", "9:16", Color(0xFFC13584)),
        SocialPlatform("ig_reels", "IG Reels Cover", Icons.Filled.Movie, "Instagram", "1080x1920", "9:16", Color(0xFF833AB4)),
        SocialPlatform("fb_post", "Facebook Post", Icons.Filled.ThumbUp, "Facebook", "1200x630", "1.91:1", Color(0xFF1877F2)),
        SocialPlatform("fb_cover", "Facebook Cover", Icons.Filled.ViewCarousel, "Facebook", "820x312", "16:9", Color(0xFF1877F2)),
        SocialPlatform("x_post", "X (Twitter) Post", Icons.Filled.Tag, "Twitter", "1200x675", "16:9", Color(0xFF1DA1F2)),
        SocialPlatform("threads_post", "Threads Post", Icons.Filled.AlternateEmail, "Threads", "1080x1080", "1:1", Color(0xFF000000)),
        SocialPlatform("tiktok_cover", "TikTok Cover", Icons.Filled.MusicVideo, "TikTok", "1080x1920", "9:16", Color(0xFF00F2FE)),
        SocialPlatform("yt_thumb", "YouTube Thumbnail", Icons.Filled.SmartDisplay, "YouTube", "1280x720", "16:9", Color(0xFFFF0000)),
        SocialPlatform("yt_banner", "YouTube Banner", Icons.Filled.VideoLabel, "YouTube", "2560x1440", "16:9", Color(0xFFFF0000)),
        SocialPlatform("pinterest_pin", "Pinterest Pin", Icons.Filled.PushPin, "Pinterest", "1000x1500", "2:3", Color(0xFFBD081C)),
        SocialPlatform("linkedin_post", "LinkedIn Post", Icons.Filled.BusinessCenter, "LinkedIn", "1200x627", "1.91:1", Color(0xFF0A66C2)),
        SocialPlatform("wa_status", "WhatsApp Status", Icons.Filled.Chat, "WhatsApp", "1080x1920", "9:16", Color(0xFF25D366)),
        SocialPlatform("telegram_post", "Telegram Post", Icons.Filled.Send, "Telegram", "1200x800", "3:2", Color(0xFF0088CC)),
        SocialPlatform("discord_banner", "Discord Banner", Icons.Filled.Forum, "Discord", "600x240", "5:2", Color(0xFF5865F2))
    )

    val templates = listOf(
        SocialTemplate("tpl_1", "Flash Sale Diskon 50%", "Instagram Post", "Promo", listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)), "FLASH SALE\nDiskon Hingga 50%"),
        SocialTemplate("tpl_2", "New Video Release", "YouTube Thumbnail", "Gaming", listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)), "EPISODE BARU!\nRahasia Tersembunyi"),
        SocialTemplate("tpl_3", "Quote Inspiratif Hari Ini", "Instagram Story", "Motivation", listOf(Color(0xFF11998E), Color(0xFF38EF7D)), "Fokus Pada Proses,\nBukan Hasil Akhir."),
        SocialTemplate("tpl_4", "Tips & Trik Teknologi", "LinkedIn Post", "Business", listOf(Color(0xFF1F1C2C), Color(0xFF928DAB)), "5 Tools AI Terbaik\nUntuk Produktivitas 2026")
    )

    val currentPlatform = platforms.find { it.id == selectedPlatformId } ?: platforms.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Social Media Maker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
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
                        Text("Meydi Studio • Cross-Platform Content Suite", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Menebak hashtag & caption tren terkini...", Toast.LENGTH_SHORT).show()
                        aiTopicInput = "Tren Konten Media Sosial Terbaru 2026"
                        activeTab = 1
                    }) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
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
            // Main Tabs Navigation Bar
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Maker & Templates", fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Content & Captions", fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Design Canvas Studio", fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Video & Shorts Cover", fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Brand Kit & History", fontSize = 12.sp)
                        }
                    }
                )
            }

            // Tab Content Switcher
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> PostMakerTab(
                        platforms = platforms,
                        templates = templates,
                        selectedPlatform = currentPlatform,
                        onSelectPlatform = { selectedPlatformId = it.id },
                        onSelectTemplate = { tpl ->
                            customPostTitle = tpl.defaultText
                            activeTab = 2
                        },
                        onOpenStudio = { activeTab = 2 }
                    )

                    1 -> AiContentStudioTab(
                        topicInput = aiTopicInput,
                        onTopicChange = { aiTopicInput = it },
                        contentType = aiContentType,
                        onContentTypeChange = { aiContentType = it },
                        tone = aiTone,
                        onToneChange = { aiTone = it },
                        generatedText = generatedAiText,
                        isGenerating = isGeneratingAi,
                        progressVal = aiProgressVal,
                        onGenerate = {
                            if (aiTopicInput.isBlank()) {
                                Toast.makeText(context, "Masukkan topik atau ide konten dahulu.", Toast.LENGTH_SHORT).show()
                                return@AiContentStudioTab
                            }
                            isGeneratingAi = true
                            coroutineScope.launch {
                                aiProgressVal = 0.3f
                                delay(400)
                                aiProgressVal = 0.7f
                                delay(500)
                                generatedAiText = when (aiContentType) {
                                    "Caption & Hashtag" -> "🔥 ${aiTopicInput.uppercase()} 🔥\n\nSolusi terbaik untuk meningkatkan produktivitas dan hasil visual Anda hari ini! Jangan lewatkan kesempatan khusus ini.\n\n👉 Klik link di bio untuk informasi selengkapnya!\n\n#MeydiStudio #SocialMediaMaker #${aiTopicInput.replace(" ", "")} #Trending2026 #KontenKreatif"
                                    "Bio Generator" -> "✨ Creator & Strategis Digital | $aiTopicInput\n🚀 Membantu Brand Tumbuh Lebih Cepat\n👇 Dapatkan Resource Gratis Di Sini:"
                                    "Script Generator" -> "[HOOK 0-3s]: Tahukah Anda rahasia terbesar dari $aiTopicInput?\n[BODY 3-15s]: Dalam video ini, saya akan tunjukkan 3 langkah praktis yang bisa Anda terapkan sekarang.\n[CTA]: Tap Follow untuk tips harian lainnya!"
                                    else -> " Quote Inspiratif: \"Keberhasilan dari $aiTopicInput berakar dari konsistensi dan keberanian mencoba hal baru.\"\n\n#Inspirasi #MeydiStudio"
                                }
                                aiProgressVal = 1.0f
                                isGeneratingAi = false
                                Toast.makeText(context, "Konten AI Berhasil Dibuat!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCopy = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Teks disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onSaveFavorite = { text ->
                            favoritesList.add(text)
                            Toast.makeText(context, "Disimpan ke Favorit!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    2 -> DesignCanvasStudioTab(
                        platform = currentPlatform,
                        titleText = customPostTitle,
                        onTitleChange = { customPostTitle = it },
                        subtitleText = customPostSubtitle,
                        onSubtitleChange = { customPostSubtitle = it },
                        exportFormat = selectedExportFormat,
                        onFormatChange = { selectedExportFormat = it },
                        quality = selectedQuality,
                        onQualityChange = { selectedQuality = it },
                        watermarkEnabled = watermarkEnabled,
                        onWatermarkToggle = { watermarkEnabled = it },
                        watermarkText = watermarkText,
                        onWatermarkChange = { watermarkText = it },
                        brandColor = selectedBrandColor,
                        onBrandColorSelect = { selectedBrandColor = it }
                    )

                    3 -> VideoCoverStudioTab(
                        platforms = platforms.filter { it.category in listOf("Instagram", "YouTube", "TikTok") },
                        onSelectVideoPlatform = { selectedPlatformId = it.id; activeTab = 2 }
                    )

                    4 -> BrandKitHistoryTab(
                        favorites = favoritesList,
                        watermarkText = watermarkText,
                        onWatermarkChange = { watermarkText = it },
                        onUseFavorite = { fav ->
                            customPostTitle = fav
                            activeTab = 2
                        }
                    )
                }
            }
        }
    }
}

// --- Tab 1: Post Maker & Templates ---
@Composable
fun PostMakerTab(
    platforms: List<SocialPlatform>,
    templates: List<SocialTemplate>,
    selectedPlatform: SocialPlatform,
    onSelectPlatform: (SocialPlatform) -> Unit,
    onSelectTemplate: (SocialTemplate) -> Unit,
    onOpenStudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("1. Pilih Platform Social Media", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(platforms) { platform ->
                val isSelected = selectedPlatform.id == platform.id
                Card(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onSelectPlatform(platform) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) platform.accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, platform.accentColor) else null
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(platform.accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(platform.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(platform.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 1)
                        Text("${platform.dimensions} (${platform.ratio})", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Active Platform Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = selectedPlatform.accentColor.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(selectedPlatform.icon, contentDescription = null, tint = selectedPlatform.accentColor, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Terpilih: ${selectedPlatform.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Ukuran: ${selectedPlatform.dimensions} • Rasio Aspect: ${selectedPlatform.ratio}", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Button(
                    onClick = onOpenStudio,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = selectedPlatform.accentColor)
                ) {
                    Text("Buka Editor", fontSize = 12.sp)
                }
            }
        }

        // Template Presets
        Text("2. Pustaka Template Siap Pakai", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            templates.forEach { tpl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTemplate(tpl) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(Brush.linearGradient(tpl.previewGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tpl.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(tpl.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                                }
                            }
                            Text(tpl.defaultText, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

// --- Tab 2: AI Content Studio ---
@Composable
fun AiContentStudioTab(
    topicInput: String,
    onTopicChange: (String) -> Unit,
    contentType: String,
    onContentTypeChange: (String) -> Unit,
    tone: String,
    onToneChange: (String) -> Unit,
    generatedText: String,
    isGenerating: Boolean,
    progressVal: Float,
    onGenerate: () -> Unit,
    onCopy: (String) -> Unit,
    onSaveFavorite: (String) -> Unit
) {
    val contentTypes = listOf("Caption & Hashtag", "Bio Generator", "Script Generator", "Inspirational Quote")
    val tones = listOf("Professional & Friendly", "Energetic & Hype", "Humorous & Casual", "Minimalist & Clean")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("AI Content & Caption Generator", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = topicInput,
            onValueChange = onTopicChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Topik Utama / Kata Kunci / Produk") },
            placeholder = { Text("Contoh: Diskon Kopi Kekinian, Tips Belajar Coding, Outfit Pantai") },
            minLines = 2,
            shape = RoundedCornerShape(12.dp)
        )

        Text("Jenis Konten AI:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(contentTypes) { type ->
                FilterChip(
                    selected = contentType == type,
                    onClick = { onContentTypeChange(type) },
                    label = { Text(type, fontSize = 12.sp) }
                )
            }
        }

        Text("Gaya Bahasa (Tone of Voice):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tones) { t ->
                FilterChip(
                    selected = tone == t,
                    onClick = { onToneChange(t) },
                    label = { Text(t, fontSize = 12.sp) }
                )
            }
        }

        if (isGenerating) {
            LinearProgressIndicator(progress = { progressVal }, modifier = Modifier.fillMaxWidth())
        }

        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isGenerating,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Konten dengan AI", fontWeight = FontWeight.Bold)
        }

        if (generatedText.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Hasil Generasi AI:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(generatedText, style = MaterialTheme.typography.bodyMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onCopy(generatedText) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin Teks", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onSaveFavorite(generatedText) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- Tab 3: Design Canvas Studio ---
@Composable
fun DesignCanvasStudioTab(
    platform: SocialPlatform,
    titleText: String,
    onTitleChange: (String) -> Unit,
    subtitleText: String,
    onSubtitleChange: (String) -> Unit,
    exportFormat: String,
    onFormatChange: (String) -> Unit,
    quality: String,
    onQualityChange: (String) -> Unit,
    watermarkEnabled: Boolean,
    onWatermarkToggle: (Boolean) -> Unit,
    watermarkText: String,
    onWatermarkChange: (String) -> Unit,
    brandColor: Color,
    onBrandColorSelect: (Color) -> Unit
) {
    val context = LocalContext.current
    val formats = listOf("PNG", "JPG", "WEBP", "MP4", "PDF")
    val qualities = listOf("HD", "Full HD", "4K")
    val colorPresets = listOf(Color(0xFFE91E63), Color(0xFF673AB7), Color(0xFF2196F3), Color(0xFF009688), Color(0xFFFF9800), Color(0xFF3F51B5))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Studio Editor & Canvas Interactive", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        // Live Preview Canvas Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(brandColor, Color(0xFF1A1A2E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(platform.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (titleText.isNotBlank()) titleText else "Judul Konten Anda",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (subtitleText.isNotBlank()) subtitleText else "Sub-judul deskripsi postingan",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Watermark overlay
                if (watermarkEnabled) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            watermarkText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Badge Dimension
                Surface(
                    color = platform.accentColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        "${platform.name} • ${platform.dimensions}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Editing Inputs
        OutlinedTextField(
            value = titleText,
            onValueChange = onTitleChange,
            label = { Text("Teks Judul Utama") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = subtitleText,
            onValueChange = onSubtitleChange,
            label = { Text("Teks Sub-Judul / Keterangan") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Text("Pilih Warna Utama Canvas:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            colorPresets.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (brandColor == color) 3.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { onBrandColorSelect(color) }
                )
            }
        }

        // Export Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pengaturan Ekspor & Quality", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Format File:", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        formats.forEach { fmt ->
                            FilterChip(
                                selected = exportFormat == fmt,
                                onClick = { onFormatChange(fmt) },
                                label = { Text(fmt, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kualitas Output:", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        qualities.forEach { q ->
                            FilterChip(
                                selected = quality == q,
                                onClick = { onQualityChange(q) },
                                label = { Text(q, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    Toast.makeText(context, "Mengekspor gambar $exportFormat ($quality) ke Galeri!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan $exportFormat", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "$titleText\n$subtitleText\n\nDibuat dengan Social Media Maker App")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan ke Social Media"))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bagikan Direct", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Tab 4: Video Cover Studio ---
@Composable
fun VideoCoverStudioTab(
    platforms: List<SocialPlatform>,
    onSelectVideoPlatform: (SocialPlatform) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Video & Shorts Cover Tool", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Text("Pilih format video pendek Anda:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        platforms.forEach { platform ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectVideoPlatform(platform) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(platform.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(platform.icon, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(platform.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Resolusi Video: ${platform.dimensions}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Subtitles, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Subtitle & Auto Caption Video", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                Text("Ekstrak dan hasilkan subtitle otomatis beranimasi untuk video TikTok dan IG Reels Anda secara presisi.", fontSize = 12.sp, color = Color.Gray)
                Button(
                    onClick = {
                        Toast.makeText(context, "Fitur Subtitle AI siap digunakan!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gunakan Generator Subtitle AI")
                }
            }
        }
    }
}

// --- Tab 5: Brand Kit & History ---
@Composable
fun BrandKitHistoryTab(
    favorites: List<String>,
    watermarkText: String,
    onWatermarkChange: (String) -> Unit,
    onUseFavorite: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Brand Kit & Preferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pengaturan Watermark Brand", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = onWatermarkChange,
                    label = { Text("Teks Watermark / Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Text("Daftar Favorit Teks & Captions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        if (favorites.isEmpty()) {
            Text("Belum ada favorit tersimpan.", color = Color.Gray, fontSize = 12.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                favorites.forEach { fav ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(fav, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { onUseFavorite(fav) }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Gunakan", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
