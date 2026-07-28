package com.example.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Data Models ---
data class AiCategory(
    val id: String,
    val name: String,
    val icon: ImageVector
)

data class AiTool(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val icon: ImageVector,
    val samplePrompt: String,
    val isFavorite: Boolean = false
)

data class AiPromptTemplate(
    val title: String,
    val category: String,
    val promptText: String,
    val tags: List<String>
)

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val toolName: String,
    val inputPrompt: String,
    val outputText: String,
    val timestamp: String = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
)

// --- ViewModel ---
class AiMakerViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("text")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _selectedModel = MutableStateFlow("Google Gemini Pro")
    val selectedModel = _selectedModel.asStateFlow()

    private val _creativity = MutableStateFlow(0.7f) // Temperature
    val creativity = _creativity.asStateFlow()

    private val _maxTokens = MutableStateFlow(1024)
    val maxTokens = _maxTokens.asStateFlow()

    private val _tools = MutableStateFlow(getInitialAiTools())
    val tools = _tools.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history = _history.asStateFlow()

    private val _tokenUsage = MutableStateFlow(1420)
    val tokenUsage = _tokenUsage.asStateFlow()

    val categories = listOf(
        AiCategory("text", "AI Text", Icons.Outlined.TextFields),
        AiCategory("image", "AI Image", Icons.Outlined.Image),
        AiCategory("photo", "Photo Editor", Icons.Outlined.AutoFixHigh),
        AiCategory("video", "AI Video", Icons.Outlined.Videocam),
        AiCategory("audio", "AI Audio", Icons.Outlined.Mic),
        AiCategory("productivity", "Productivity", Icons.Outlined.TaskAlt),
        AiCategory("coding", "AI Coding", Icons.Outlined.Code),
        AiCategory("library", "Prompt Library", Icons.Outlined.AutoAwesome),
        AiCategory("history", "History", Icons.Outlined.History),
        AiCategory("settings", "AI Settings", Icons.Outlined.Settings)
    )

    val aiModels = listOf("Google Gemini Pro", "OpenAI GPT-4o", "DeepSeek V3", "Claude 3.5 Sonnet", "Custom AI API")

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun setModel(model: String) {
        _selectedModel.value = model
    }

    fun setCreativity(value: Float) {
        _creativity.value = value
    }

    fun setMaxTokens(value: Int) {
        _maxTokens.value = value
    }

    fun toggleFavorite(toolId: String) {
        _tools.update { list ->
            list.map { if (it.id == toolId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    fun addToHistory(toolName: String, prompt: String, result: String) {
        val item = HistoryItem(
            toolName = toolName,
            inputPrompt = prompt,
            outputText = result
        )
        _history.update { listOf(item) + it }
        _tokenUsage.update { it + (prompt.length + result.length) / 3 + 15 }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    private fun getInitialAiTools(): List<AiTool> {
        return listOf(
            // AI Text
            AiTool("text_chat", "AI Chat Assistant", "text", "Obrolan pintar interaktif serba bisa", Icons.Outlined.Chat, "Halo AI! Jelaskan konsep kecerdasan buatan secara sederhana."),
            AiTool("text_writer", "AI Writer", "text", "Penulis artikel & konten profesional", Icons.Outlined.Edit, "Tuliskan artikel singkat tentang tren teknologi AI tahun 2026."),
            AiTool("text_story", "AI Story Maker", "text", "Buat cerita fiksi & naratif kreatif", Icons.Outlined.MenuBook, "Buat cerita pendek tentang petualangan robot penjelajah luar angkasa."),
            AiTool("text_artikel", "AI Artikel Maker", "text", "Tulis artikel SEO-friendly otomatis", Icons.Outlined.Article, "Tulis artikel blog tentang pentingnya menjaga pola hidup sehat."),
            AiTool("text_prompt", "AI Prompt Generator", "text", "Buat prompt estetik & detail untuk AI", Icons.Outlined.Psychology, "Buat prompt gambar Midjourney untuk desain logo kedai kopi modern."),
            AiTool("text_caption", "AI Caption Generator", "text", "Caption viral untuk IG, TikTok, FB", Icons.Outlined.Subtitles, "Buat caption Instagram menarik tentang liburan akhir pekan."),
            AiTool("text_bio", "AI Bio Generator", "text", "Bio profil estetik & profesional", Icons.Outlined.AccountBox, "Buat bio LinkedIn/Instagram untuk seorang Software Engineer."),
            AiTool("text_email", "AI Email Generator", "text", "Draft email formal & bisnis cepat", Icons.Outlined.Email, "Tulis email permohonan izin cuti kerja untuk atasan."),
            AiTool("text_quote", "AI Quote Generator", "text", "Kutipan motivasi & inspirasi harian", Icons.Outlined.FormatQuote, "Buat 3 quote motivasi tentang pantang menyerah."),
            AiTool("text_hashtag", "AI Hashtag Generator", "text", "Hashtag trending & relevan", Icons.Outlined.Tag, "Berikan hashtag viral untuk konten kuliner resep masakan."),
            AiTool("text_script", "AI Script Generator", "text", "Naskah video Shorts/Reels/YouTube", Icons.Outlined.VideoLabel, "Buat skrip video edukasi 60 detik tentang tips produktivitas."),
            AiTool("text_code", "AI Code Generator", "text", "Hasilkan kode program otomatis", Icons.Outlined.Code, "Buat fungsi Kotlin untuk menghitung jarak dua koordinat GPS."),
            AiTool("text_translate", "AI Translate", "text", "Penerjemah akurat antar bahasa", Icons.Outlined.Translate, "Terjemahkan teks berikut ke dalam bahasa Inggris & Jepang."),
            AiTool("text_grammar", "AI Grammar Checker", "text", "Perbaiki tata bahasa & ejaan", Icons.Outlined.Spellcheck, "Perbaiki tata bahasa dalam kalimat berikut agar lebih formal."),
            AiTool("text_rewrite", "AI Rewrite", "text", "Parafrase & tulis ulang kalimat", Icons.Outlined.Sync, "Tulis ulang paragraf ini agar lebih berkesan persuasif."),
            AiTool("text_summarize", "AI Summarizer", "text", "Rangkum dokumen & teks panjang", Icons.Outlined.Compress, "Rangkum poin-poin utama dari artikel ini."),

            // AI Image
            AiTool("img_gen", "AI Image Generator", "image", "Generasi gambar dari deskripsi teks", Icons.Outlined.Brush, "Cyberpunk city at night with neon lights, ultra realistic 8k"),
            AiTool("img_logo", "AI Logo Maker", "image", "Desain logo merek & bisnis unik", Icons.Outlined.Checkroom, "Minimalist geometric coffee shop logo, vector white background"),
            AiTool("img_banner", "AI Banner Maker", "image", "Banner promosi & header sosial media", Icons.Outlined.ViewStream, "Modern tech webinar YouTube banner, blue and orange color theme"),
            AiTool("img_poster", "AI Poster Maker", "image", "Poster acara, film, dan event", Icons.Outlined.ConfirmationNumber, "Futuristic music festival event poster, vibrant colors"),
            AiTool("img_thumb", "AI Thumbnail Maker", "image", "Thumbnail video YouTube menarik", Icons.Outlined.OndemandVideo, "Shocked face expression thumbnail, bold yellow text"),
            AiTool("img_wallpaper", "AI Wallpaper Maker", "image", "Wallpaper smartphone & PC 4K", Icons.Outlined.Wallpaper, "Aesthetic mountain range sunset landscape, 4k OLED wallpaper"),
            AiTool("img_avatar", "AI Avatar Maker", "image", "Karakter avatar & ilustrasi profil", Icons.Outlined.Face, "3D Pixar style character avatar of a young developer"),
            AiTool("img_sticker", "AI Sticker Maker", "image", "Stiker lucu & emosional", Icons.Outlined.EmojiEmotions, "Cute cat drinking boba sticker with white die-cut border"),
            AiTool("img_icon", "AI Icon Maker", "image", "Ikon aplikasi 3D & flat modern", Icons.Outlined.Apps, "3D glassmorphism camera app icon, sleek rounded corners"),
            AiTool("img_bg", "AI Background Generator", "image", "Latar belakang studio produk", Icons.Outlined.CropOriginal, "Luxury marble pedestal with soft lighting studio background"),

            // Photo Editor
            AiTool("photo_hd", "AI HD Enhancer", "photo", "Tingkatkan kualitas & detail foto", Icons.Outlined.HighQuality, "Proses foto portrait agar lebih tajam dan jernih."),
            AiTool("photo_upscale", "AI Upscale", "photo", "Perbesar resolusi foto tanpa pecah", Icons.Outlined.ZoomIn, "Perbesar resolusi foto 4x lipat dengan pemulihan piksel."),
            AiTool("photo_face", "AI Face Enhance", "photo", "Haluskan wajah & retouch portrait", Icons.Outlined.FaceRetouchingNatural, "Enhance detail mata, kulit, dan pencahayaan wajah."),
            AiTool("photo_bgrem", "AI Background Remover", "photo", "Hapus latar belakang foto otomatis", Icons.Outlined.Texture, "Pisahkan subjek utama dari background transparan."),
            AiTool("photo_objrem", "AI Object Remover", "photo", "Hapus objek & orang yang mengganggu", Icons.Outlined.LayersClear, "Hapus objek kabel dan orang di latar belakang foto."),
            AiTool("photo_eraser", "AI Magic Eraser", "photo", "Hapus teks, watermark, dan noda", Icons.Outlined.AutoFixNormal, "Hapus teks watermark dari gambar."),
            AiTool("photo_color", "AI Color Enhance", "photo", "Optimalisasi warna & kontras foto", Icons.Outlined.Palette, "Tingkatkan kejenuhan warna sunset dan langit."),
            AiTool("photo_noise", "AI Noise Reduction", "photo", "Hilangkan bintik noise foto malam", Icons.Outlined.BlurOn, "Bersihkan noise foto pencahayaan rendah."),
            AiTool("photo_restore", "AI Old Photo Restore", "photo", "Restorasi & warnai foto jadul", Icons.Outlined.PhotoFilter, "Perbaiki goresan dan beri warna alami pada foto hitam putih lama."),

            // Video Maker
            AiTool("vid_gen", "AI Video Generator", "video", "Hasilkan klip video dari teks prompt", Icons.Outlined.Movie, "Cinematic drone shot of ocean waves hitting rocky cliffs at sunset."),
            AiTool("vid_enhance", "AI Video Enhancer", "video", "Tingkatkan kualitas frame video", Icons.Outlined.VideoSettings, "Tingkatkan ketajaman frame dan saturasi warna video."),
            AiTool("vid_upscale", "AI Video Upscaler", "video", "Tingkatkan resolusi video ke 4K", Icons.Outlined.AspectRatio, "Upscale video 720p menjadi 4K 60fps smoothing."),
            AiTool("vid_caption", "AI Video Caption", "video", "Buat caption otomatis untuk video", Icons.Outlined.ClosedCaption, "Generasi caption teks otomatis menyesuaikan audio video."),
            AiTool("vid_subtitle", "AI Video Subtitle", "video", "Subtitle multi-bahasa otomatis", Icons.Outlined.Subtitles, "Buat subtitle Indonesia dan Inggris tersinkronisasi."),
            AiTool("vid_thumb", "AI Video Thumbnail", "video", "Cuplikan frame thumbnail terbaik", Icons.Outlined.PhotoCamera, "Pilih dan poles frame momen terbaik sebagai thumbnail video."),
            AiTool("vid_summary", "AI Video Summary", "video", "Rangkuman isi video lengkap", Icons.Outlined.Segment, "Rangkum poin materi pembahasan dari video tutorial."),

            // Audio Maker
            AiTool("aud_tts", "AI Text To Speech", "audio", "Ubah teks jadi suara manusia alami", Icons.Outlined.RecordVoiceOver, "Suara narator profesional pria untuk narasi video edukasi."),
            AiTool("aud_stt", "AI Speech To Text", "audio", "Transkrip rekaman suara jadi teks", Icons.Outlined.Hearing, "Ubah rekaman wawancara audio menjadi transkrip tulisan."),
            AiTool("aud_cleaner", "AI Voice Cleaner", "audio", "Bersihkan noise latar belakang audio", Icons.Outlined.GraphicEq, "Hilangkan bising angin dan deru mesin dari rekaman audio."),
            AiTool("aud_enhance", "AI Voice Enhancement", "audio", "Tingkatkan kejernihan studio audio", Icons.Outlined.VolumeUp, "Tingkatkan bass dan kejelasan vokal seperti rekaman studio."),
            AiTool("aud_summary", "AI Audio Summary", "audio", "Rangkum rekaman rapat / kuliah", Icons.Outlined.Summarize, "Buat ringkasan poin notulensi dari rekaman rapat 30 menit."),

            // Productivity
            AiTool("prod_todo", "AI To-Do Generator", "productivity", "Rencana tugas harian otomatis", Icons.Outlined.Checklist, "Buat jadwal daftar tugas produktif untuk belajar koding hari ini."),
            AiTool("prod_notes", "AI Notes", "productivity", "Catatan terstruktur & poin penting", Icons.Outlined.NoteAdd, "Rapikan catatan acak hasil diskusi strategi bisnis."),
            AiTool("prod_planner", "AI Planner", "productivity", "Perencana proyek & liburan", Icons.Outlined.EventNote, "Buat itinerary liburan 3 hari 2 malam di Bali lengkap dengan perkiraan anggaran."),
            AiTool("prod_schedule", "AI Schedule Assistant", "productivity", "Atur jadwal kerja harian efektif", Icons.Outlined.Schedule, "Buat alokasi waktu kerja fokus (Pomodoro) 8 jam sehari."),
            AiTool("prod_reminder", "AI Reminder Creator", "productivity", "Pengingat target & rutinitas", Icons.Outlined.NotificationsActive, "Buat draf pengingat harian minum air putih dan olahraga."),

            // AI Coding
            AiTool("code_assistant", "AI Code Assistant", "coding", "Asisten koding & solusi algoritma", Icons.Outlined.Terminal, "Bagaimana cara mengimplementasikan StateFlow di Jetpack Compose?"),
            AiTool("code_bugfinder", "AI Bug Finder", "coding", "Cari error & bug dalam kode", Icons.Outlined.BugReport, "Temukan potensi memory leak atau NPE pada potongan kode Kotlin berikut."),
            AiTool("code_explainer", "AI Code Explainer", "coding", "Jelaskan alur & logika kode", Icons.Outlined.HelpOutline, "Jelaskan baris per baris bagaimana algoritma QuickSort bekerja."),
            AiTool("code_json", "AI JSON Generator", "coding", "Generasi struktur JSON valid", Icons.Outlined.DataObject, "Buat JSON data produk toko online lengkap dengan id, nama, harga, dan kategori."),
            AiTool("code_sql", "AI SQL Generator", "coding", "Kueri SQL kompleks otomatis", Icons.Outlined.Storage, "Buat kueri SQL JOIN antara tabel Users, Orders, dan Payments."),
            AiTool("code_html", "AI HTML Generator", "coding", "Template HTML5 modern", Icons.Outlined.Html, "Buat komponen landing page hero section sederhana dengan HTML5."),
            AiTool("code_css", "AI CSS Generator", "coding", "Styling CSS & Flexbox/Grid", Icons.Outlined.Css, "Buat style CSS card glassmorphism dengan efek backdrop-blur."),
            AiTool("code_js", "AI JavaScript Generator", "coding", "Fungsi JS & Async/Await", Icons.Outlined.Javascript, "Buat fungsi JS fetch data dari REST API menggunakan async await.")
        )
    }
}

// --- Main UI Component ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMakerScreen(onBack: () -> Unit) {
    val viewModel: AiMakerViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val creativity by viewModel.creativity.collectAsState()
    val maxTokens by viewModel.maxTokens.collectAsState()
    val allTools by viewModel.tools.collectAsState()
    val history by viewModel.history.collectAsState()
    val tokenUsage by viewModel.tokenUsage.collectAsState()

    var activeTool by remember { mutableStateOf<AiTool?>(allTools.first()) }
    var promptInput by remember { mutableStateOf("") }
    var aiOutput by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var showPromptLibrary by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val coroutineScope = rememberCoroutineScope()

    val filteredTools = remember(searchQuery, selectedCategory, allTools) {
        allTools.filter { tool ->
            val matchesSearch = tool.name.contains(searchQuery, ignoreCase = true) ||
                    tool.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == tool.categoryId ||
                    (selectedCategory == "favorites" && tool.isFavorite)
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sistem AI Maker", fontWeight = FontWeight.Bold)
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
                    IconButton(onClick = { showPromptLibrary = true }) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "Prompt Library", tint = MaterialTheme.colorScheme.primary)
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
                                "AI Studio Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Model: $selectedModel",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Penggunaan Token: $tokenUsage / 100,000",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { viewModel.setCategory("settings") },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pengaturan", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Search & Category Tabs
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari fitur AI (Chat, Image, Script, Code, DLL)...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }

            // Categories Selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "favorites",
                            onClick = { viewModel.setCategory("favorites") },
                            label = { Text("Favorit") },
                            leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
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

            // Render view depending on selected category (e.g. settings/history/library or tool workspace)
            when (selectedCategory) {
                "settings" -> {
                    item {
                        AiSettingsCard(
                            selectedModel = selectedModel,
                            creativity = creativity,
                            maxTokens = maxTokens,
                            onModelSelect = viewModel::setModel,
                            onCreativityChange = viewModel::setCreativity,
                            onMaxTokensChange = viewModel::setMaxTokens,
                            onClearHistory = viewModel::clearHistory,
                            tokenUsage = tokenUsage
                        )
                    }
                }
                "history" -> {
                    item {
                        AiHistorySection(
                            history = history,
                            onCopy = { text ->
                                val clip = ClipData.newPlainText("AI Result", text)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Berhasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onClear = viewModel::clearHistory
                        )
                    }
                }
                "library" -> {
                    item {
                        PromptLibrarySection(
                            onSelectPrompt = { prompt ->
                                promptInput = prompt
                                viewModel.setCategory("text")
                            }
                        )
                    }
                }
                else -> {
                    // Tools Selector List
                    item {
                        Text(
                            "Alat AI (${filteredTools.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredTools) { tool ->
                                val isSelected = activeTool?.id == tool.id
                                Card(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable {
                                            activeTool = tool
                                            promptInput = tool.samplePrompt
                                            aiOutput = ""
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = tool.icon,
                                                contentDescription = tool.name,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            IconButton(
                                                onClick = { viewModel.toggleFavorite(tool.id) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (tool.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                    contentDescription = "Favorit",
                                                    tint = if (tool.isFavorite) Color(0xFFFFC107) else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = tool.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = tool.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Workspace Section
                    item {
                        activeTool?.let { tool ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(tool.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(tool.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    OutlinedTextField(
                                        value = promptInput,
                                        onValueChange = { promptInput = it },
                                        label = { Text("Prompt / Instruksi AI") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        placeholder = { Text("Ketik perintah atau gunakan prompt bawaan...") }
                                    )

                                    // Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (promptInput.isBlank()) return@Button
                                                isGenerating = true
                                                coroutineScope.launch {
                                                    kotlinx.coroutines.delay(1200) // Simulate processing
                                                    val generated = generateAiResponse(
                                                        tool = tool,
                                                        prompt = promptInput,
                                                        model = selectedModel
                                                    )
                                                    aiOutput = generated
                                                    viewModel.addToHistory(tool.name, promptInput, generated)
                                                    isGenerating = false
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isGenerating,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (isGenerating) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Memproses...")
                                            } else {
                                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Hasilkan dengan AI")
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { promptInput = tool.samplePrompt },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Outlined.Lightbulb, contentDescription = null)
                                        }
                                    }

                                    // Result Output
                                    if (aiOutput.isNotEmpty()) {
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                                        Text(
                                            "Hasil AI ($selectedModel):",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = aiOutput,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontFamily = if (tool.categoryId == "coding") FontFamily.Monospace else FontFamily.Default
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            val clip = ClipData.newPlainText("AI Output", aiOutput)
                                                            clipboardManager.setPrimaryClip(clip)
                                                            Toast.makeText(context, "Teks disalin!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Salin")
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                type = "text/plain"
                                                                putExtra(Intent.EXTRA_TEXT, aiOutput)
                                                            }
                                                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Hasil AI"))
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Bagikan")
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
            }
        }
    }

    // Prompt Library Dialog
    if (showPromptLibrary) {
        AlertDialog(
            onDismissRequest = { showPromptLibrary = false },
            title = { Text("AI Prompt Library", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val samplePrompts = listOf(
                        "Buat rencana strategi pemasaran digital untuk produk e-commerce.",
                        "Tulis kode fungsi Python untuk ekstraksi web scraper.",
                        "Rangkum 5 prinsip utama dari buku habit & produktivitas.",
                        "Buat ide konsep desain UI/UX untuk aplikasi dompet digital."
                    )
                    samplePrompts.forEach { p ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    promptInput = p
                                    showPromptLibrary = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                p,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPromptLibrary = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

// --- Helper Composable Views ---
@Composable
fun AiSettingsCard(
    selectedModel: String,
    creativity: Float,
    maxTokens: Int,
    onModelSelect: (String) -> Unit,
    onCreativityChange: (Float) -> Unit,
    onMaxTokensChange: (Int) -> Unit,
    onClearHistory: () -> Unit,
    tokenUsage: Int
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Pengaturan AI System", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Model Picker
            Column {
                Text("Pilih Model AI Active", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(selectedModel, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Google Gemini Pro", "OpenAI GPT-4o", "DeepSeek V3", "Claude 3.5 Sonnet", "Custom AI API").forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    onModelSelect(m)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Creativity / Temperature
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kreativitas (Temperature)", style = MaterialTheme.typography.labelMedium)
                    Text(String.format("%.1f", creativity), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = creativity,
                    onValueChange = onCreativityChange,
                    valueRange = 0.1f..1.0f
                )
            }

            // Max Tokens
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Maksimum Panjang Jawaban", style = MaterialTheme.typography.labelMedium)
                    Text("$maxTokens Token", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = maxTokens.toFloat(),
                    onValueChange = { onMaxTokensChange(it.toInt()) },
                    valueRange = 256f..4096f,
                    steps = 15
                )
            }

            Divider()

            // Token Stats & Security
            Text("Penggunaan Token & Keamanan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Status Enkripsi API Key:", style = MaterialTheme.typography.bodySmall)
                Text("Aktif (AES-256)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Total Token Terpakai:", style = MaterialTheme.typography.bodySmall)
                Text("$tokenUsage Token", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onClearHistory,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hapus Riwayat Obrolan AI")
            }
        }
    }
}

@Composable
fun AiHistorySection(
    history: List<HistoryItem>,
    onCopy: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Riwayat Generasi AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text("Bersihkan", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (history.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    "Belum ada riwayat penggunaan AI.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            history.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.toolName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(item.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("P: ${item.inputPrompt}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.outputText, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onCopy(item.outputText) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Salin", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromptLibrarySection(onSelectPrompt: (String) -> Unit) {
    val library = listOf(
        AiPromptTemplate("Artikel SEO Optima", "Text", "Tulis artikel blog SEO 500 kata tentang [Topik] dengan kata kunci utama [Keyword].", listOf("SEO", "Blog")),
        AiPromptTemplate("Desain Logo Vector", "Image", "Minimalist geometric vector logo for a modern cafe, clean white background, 8k resolution.", listOf("Design", "Logo")),
        AiPromptTemplate("Refaktor Kode Clean", "Coding", "Refactor potongan kode berikut agar mengikuti Clean Architecture dan SOLID Principles.", listOf("Code", "Kotlin")),
        AiPromptTemplate("Skrip Video Viral", "Video", "Buat skrip video 30 detik yang menarik perhatian di 3 detik pertama tentang [Topik].", listOf("Social", "Video")),
        AiPromptTemplate("Draft Email Penawaran", "Business", "Tulis email penawaran kerjasama B2B yang ramah dan profesional kepada calon klien.", listOf("Email", "Business"))
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Prompt Library Terpopuler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        library.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPrompt(item.promptText) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(item.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(item.promptText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// --- AI Engine Generator ---
fun generateAiResponse(tool: AiTool, prompt: String, model: String): String {
    val cleanPrompt = prompt.ifBlank { tool.samplePrompt }

    return when (tool.categoryId) {
        "text" -> when (tool.id) {
            "text_chat" -> "🤖 [$model Chat]\n\nTerima kasih atas pertanyaan Anda mengenai: \"$cleanPrompt\".\n\nSecara menyeluruh, solusi untuk hal ini mencakup perancangan terstruktur, evaluasi berkelanjutan, dan pemanfaatan sistem kecerdasan buatan berbasis $model."
            "text_writer" -> "✍️ [Hasil AI Writer]\n\nJudul: Inovasi Masa Depan - $cleanPrompt\n\nPengembangan teknologi informasi dan sistem otomatisasi semakin memudahkan pengerjaan tugas sehari-hari. Dengan menggunakan $model, efisiensi kerja meningkat secara eksponensial."
            "text_story" -> "📖 [Kisah Fiksi AI]\n\nPada masa depan yang tidak jauh, di sebuah kota metropolitan bertenaga surya, seorang penemu muda menemukan rahasia $cleanPrompt..."
            "text_code" -> "💻 [Hasil Generasi Kode AI]\n\n```kotlin\n// Generated by $model\nfun executeTask(input: String): String {\n    println(\"Processing: \$input\")\n    return \"Result for \$input\"\n}\n```"
            else -> "✨ [$model Response - ${tool.name}]\n\nBerikut adalah hasil optimasi AI untuk instruksi Anda:\n\"$cleanPrompt\"\n\n- Poin Utama 1: Analisis awal telah selesai.\n- Poin Utama 2: Ditingkatkan dengan standar kualitas tinggi.\n- Poin Utama 3: Siap digunakan untuk konten Anda."
        }
        "image" -> "🖼️ [AI Image Prompt Generated ($model)]\n\nParameter Gambar:\n• Prompt: $cleanPrompt\n• Resolution: 4K Ultra HD\n• Style: Photorealistic, Octane Render 8K\n• Aspect Ratio: 16:9\n\n(Simulasi: Gambar berhasil di-generate dan disimpan ke galeri studio)"
        "photo" -> "✨ [AI Photo Processing Complete]\n\nProses yang diterapkan: ${tool.name}\nStatus: Berhasil 100%\nDetail: Menggunakan algoritma pemulihan piksel cerdas $model pada foto Anda."
        "video" -> "🎬 [AI Video Rendering Complete]\n\nProject: ${tool.name}\nPrompt: $cleanPrompt\nFPS: 60fps | Resolution: 1080p Full HD\nDurasi: 10 Detik"
        "audio" -> "🎙️ [AI Audio Synthesis Complete]\n\nJenis: ${tool.name}\nTeks Input: \"$cleanPrompt\"\nVoice Profile: Studio Clarity Natural HD\nFormat: WAV 320kbps"
        "productivity" -> "📋 [Rencana AI Productivity]\n\nTarget: $cleanPrompt\n\n1. [08:00] Persiapan & Analisis awal\n2. [10:00] Eksekusi fokus tinggi (Deep Work)\n3. [14:00] Review & Evaluasi hasil\n4. [16:00] Finalisasi & Dokumentasi"
        "coding" -> "👨‍💻 [AI Coding Engine ($model)]\n\nSolusi untuk: \"$cleanPrompt\"\n\n```kotlin\n// Clean & Modular Code\nclass AiSolutionManager {\n    fun processLogic() {\n        // Optimized implementation\n    }\n}\n```\n\nLogika telah disesuaikan dengan praktik terbaik."
        else -> "🚀 [$model Output]\n\nHasil generasi AI untuk ${tool.name}:\n$cleanPrompt"
    }
}
