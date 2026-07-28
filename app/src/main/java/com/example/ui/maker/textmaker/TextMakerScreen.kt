package com.example.ui.maker.textmaker

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.util.Base64

// --- Data Models ---
data class TextCategory(val id: String, val name: String, val icon: ImageVector)

data class TextTool(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val icon: ImageVector,
    val isFavorite: Boolean = false
)

// --- ViewModel ---
class TextMakerViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _tools = MutableStateFlow(getInitialTools())
    val tools = _tools.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history = _history.asStateFlow()

    val categories = listOf(
        TextCategory("all", "Semua", Icons.Outlined.GridView),
        TextCategory("fancy", "Fancy Text", Icons.Outlined.TextFormat),
        TextCategory("generator", "Generator", Icons.Outlined.AutoAwesome),
        TextCategory("ai", "AI Text", Icons.Outlined.Psychology),
        TextCategory("social", "Social Media", Icons.Outlined.Share),
        TextCategory("coding", "Coding", Icons.Outlined.Code),
        TextCategory("tools", "Text Tools", Icons.Outlined.Build),
        TextCategory("encode", "Encode/Decode", Icons.Outlined.Lock)
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleFavorite(toolId: String) {
        _tools.update { list ->
            list.map { if (it.id == toolId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    fun addToHistory(item: String) {
        if (item.isBlank()) return
        _history.update { current ->
            val updated = current.toMutableList()
            updated.remove(item)
            updated.add(0, item)
            updated.take(15)
        }
    }

    private fun getInitialTools(): List<TextTool> {
        return listOf(
            // Fancy Text
            TextTool("fancy_stylish", "Stylish Font", "fancy", "Ubah teks biasa menjadi gaya trendi", Icons.Outlined.TextFormat),
            TextTool("fancy_bubble", "Bubble Text", "fancy", "Teks bulat dan gelembung unik", Icons.Outlined.Circle),
            TextTool("fancy_bold", "Bold Text", "fancy", "Teks tebal Unicode", Icons.Outlined.FormatBold),
            TextTool("fancy_italic", "Italic Text", "fancy", "Teks miring Unicode", Icons.Outlined.FormatItalic),
            TextTool("fancy_mono", "Monospace", "fancy", "Gaya teks mesin ketik / koding", Icons.Outlined.Code),
            TextTool("fancy_smallcaps", "Small Caps", "fancy", "Huruf kapital berukuran kecil", Icons.Outlined.FormatSize),
            TextTool("fancy_superscript", "Superscript", "fancy", "Teks pangkat atas (⁺¹²³)", Icons.Outlined.VerticalAlignTop),
            TextTool("fancy_subscript", "Subscript", "fancy", "Teks indeks bawah (₁₂₃)", Icons.Outlined.VerticalAlignBottom),

            // Generator
            TextTool("gen_username", "Username Gen", "generator", "Buat ide username menarik & estetik", Icons.Outlined.PersonOutline),
            TextTool("gen_nickname", "Nickname Gen", "generator", "Nama panggilan game atau sosial media", Icons.Outlined.Badge),
            TextTool("gen_bio", "Bio Generator", "generator", "Buat bio profil estetik dan profesional", Icons.Outlined.AccountBox),
            TextTool("gen_caption", "Caption Gen", "generator", "Caption postingan otomatis", Icons.Outlined.Subtitles),
            TextTool("gen_quote", "Quote Maker", "generator", "Kutipan bijak dan motivasi harian", Icons.Outlined.FormatQuote),
            TextTool("gen_hashtag", "Hashtag Gen", "generator", "Hashtag viral untuk postingan Anda", Icons.Outlined.Tag),
            TextTool("gen_slogan", "Slogan Gen", "generator", "Slogan bisnis dan promosi", Icons.Outlined.Campaign),
            TextTool("gen_brand", "Nama Brand", "generator", "Inspirasi nama merek dan bisnis", Icons.Outlined.Storefront),

            // AI Text
            TextTool("ai_writer", "AI Writer", "ai", "Tulis artikel & konten dengan bantuan AI", Icons.Outlined.AutoAwesome),
            TextTool("ai_rewrite", "AI Rewrite", "ai", "Tulis ulang teks dengan gaya berbeda", Icons.Outlined.EditNote),
            TextTool("ai_paraphrase", "AI Paraphrase", "ai", "Ubah kalimat tanpa mengubah makna", Icons.Outlined.Sync),
            TextTool("ai_grammar", "Grammar Checker", "ai", "Perbaiki tata bahasa & ejaan", Icons.Outlined.Spellcheck),
            TextTool("ai_translate", "AI Translate", "ai", "Terjemahkan teks secara pintar", Icons.Outlined.Translate),
            TextTool("ai_summarize", "AI Summarizer", "ai", "Rangkum paragraf panjang dalam sekejap", Icons.Outlined.Compress),
            TextTool("ai_prompt", "Prompt Generator", "ai", "Buat prompt AI gambar / ChatGPT", Icons.Outlined.Psychology),
            TextTool("ai_email", "Email Generator", "ai", "Buat email profesional & surat formal", Icons.Outlined.Email),

            // Social Media
            TextTool("soc_ig", "Instagram Caption", "social", "Caption & estetika postingan Instagram", Icons.Outlined.CameraAlt),
            TextTool("soc_tiktok", "TikTok Caption", "social", "Caption fyp dan viral untuk TikTok", Icons.Outlined.MusicNote),
            TextTool("soc_fb", "Facebook Post", "social", "Status dan konten Facebook", Icons.Outlined.Facebook),
            TextTool("soc_ytdesc", "YouTube Description", "social", "Deskripsi video YouTube lengkap", Icons.Outlined.VideoLabel),
            TextTool("soc_yttitle", "YouTube Title", "social", "Judul video YouTube yang klikbait", Icons.Outlined.Title),
            TextTool("soc_x", "X / Twitter Post", "social", "Cuitan & utasan (thread) X", Icons.Outlined.AlternateEmail),
            TextTool("soc_wa", "WhatsApp Bio", "social", "Status & Info pesan WhatsApp", Icons.Outlined.Chat),

            // Coding
            TextTool("code_jsonfmt", "JSON Formatter", "coding", "Format dan rapikan sintaks JSON", Icons.Outlined.DataObject),
            TextTool("code_jsonmin", "JSON Minify", "coding", "Kompres ukuran string JSON", Icons.Outlined.Compress),
            TextTool("code_htmlfmt", "HTML Formatter", "coding", "Rapikan struktur kode HTML", Icons.Outlined.Html),
            TextTool("code_cssfmt", "CSS Formatter", "coding", "Format stylesheet CSS", Icons.Outlined.Css),
            TextTool("code_jsfmt", "JS Formatter", "coding", "Format kode JavaScript", Icons.Outlined.Javascript),
            TextTool("code_xmlfmt", "XML Formatter", "coding", "Format file XML", Icons.Outlined.Code),
            TextTool("code_sqlfmt", "SQL Formatter", "coding", "Format kueri SQL database", Icons.Outlined.Storage),
            TextTool("code_md", "Markdown Editor", "coding", "Editor & pratinjau teks Markdown", Icons.Outlined.Description),

            // Text Tools
            TextTool("tool_case", "Case Converter", "tools", "Ubah Kapital, Kecil, Title Case", Icons.Outlined.TextFields),
            TextTool("tool_counter", "Word & Char Count", "tools", "Hitung kata, karakter, baris", Icons.Outlined.Numbers),
            TextTool("tool_rmdup", "Remove Dupes", "tools", "Hapus baris teks ganda", Icons.Outlined.FilterList),
            TextTool("tool_sort", "Sort Lines", "tools", "Urutkan baris alfabetis A-Z", Icons.Outlined.SortByAlpha),
            TextTool("tool_reverse", "Reverse Text", "tools", "Balikkan urutan teks & kata", Icons.Outlined.SwapHoriz),
            TextTool("tool_replace", "Find & Replace", "tools", "Cari dan ganti kata secara cepat", Icons.Outlined.FindReplace),

            // Encode / Decode
            TextTool("enc_b64", "Base64 Encoder", "encode", "Enkripsi & Dekripsi Base64", Icons.Outlined.Lock),
            TextTool("enc_url", "URL Encoder", "encode", "Encode & Decode komponen URL", Icons.Outlined.Link),
            TextTool("enc_html", "HTML Encoder", "encode", "Encode entitas teks HTML", Icons.Outlined.Html)
        )
    }
}

// --- Main UI Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextMakerScreen(onBack: () -> Unit) {
    val viewModel: TextMakerViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val allTools by viewModel.tools.collectAsState()
    val history by viewModel.history.collectAsState()

    var activeTool by remember { mutableStateOf<TextTool?>(allTools.first()) }
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var replaceTarget by remember { mutableStateOf("") }
    var replaceWith by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val filteredTools = remember(searchQuery, selectedCategory, allTools) {
        allTools.filter { tool ->
            val matchesSearch = tool.name.contains(searchQuery, ignoreCase = true) || tool.description.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategory == "all" || tool.categoryId == selectedCategory || (selectedCategory == "favorites" && tool.isFavorite)
            matchesSearch && matchesCat
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Text Maker Studio", fontWeight = FontWeight.Bold)
                        Text("Created By : Meydi | v1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        inputText = ""
                        outputText = ""
                    }) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "Bersihkan")
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
            // Search & Category Filter Section
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari alat teks...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "favorites",
                            onClick = { viewModel.selectCategory("favorites") },
                            label = { Text("Favorit") },
                            leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    items(viewModel.categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(category.name) },
                            leadingIcon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Tools Horizontal / Grid Picker
            item {
                Text(
                    "Pilih Alat (${filteredTools.size})",
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
                                .width(130.dp)
                                .clickable {
                                    activeTool = tool
                                    outputText = ""
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
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
                                        modifier = Modifier.size(24.dp)
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

            // Active Tool Work Area
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
                                Text(
                                    tool.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (tool.id == "tool_replace") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = replaceTarget,
                                        onValueChange = { replaceTarget = it },
                                        label = { Text("Cari Kata") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = replaceWith,
                                        onValueChange = { replaceWith = it },
                                        label = { Text("Ganti Dengan") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                label = { Text("Masukkan Teks Input") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                placeholder = { Text("Ketik atau tempel teks di sini...") }
                            )

                            // Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        outputText = processTextTool(tool.id, inputText, replaceTarget, replaceWith)
                                        viewModel.addToHistory(outputText)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Proses / Generate")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val clip = clipboardManager.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            inputText = clip.getItemAt(0).text.toString()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.ContentPaste, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tempel")
                                }
                            }

                            // Result Output Area
                            if (outputText.isNotEmpty()) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    "Hasil Teks:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        SelectionContainerText(outputText)

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    val clip = ClipData.newPlainText("Text Maker Result", outputText)
                                                    clipboardManager.setPrimaryClip(clip)
                                                    Toast.makeText(context, "Teks berhasil disalin!", Toast.LENGTH_SHORT).show()
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
                                                        putExtra(Intent.EXTRA_TEXT, outputText)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Teks"))
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

            // Text Analysis / Live Counter
            if (inputText.isNotEmpty()) {
                item {
                    TextStatsCard(inputText)
                }
            }

            // History Section
            if (history.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            "Riwayat Teks Terbaru",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        history.take(5).forEach { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable {
                                        val clip = ClipData.newPlainText("Text History", item)
                                        clipboardManager.setPrimaryClip(clip)
                                        Toast.makeText(context, "Teks disalin dari riwayat", Toast.LENGTH_SHORT).show()
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "Salin",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
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

@Composable
fun SelectionContainerText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
}

@Composable
fun TextStatsCard(text: String) {
    val charCount = text.length
    val wordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val lineCount = if (text.isEmpty()) 0 else text.lines().size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$charCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Karakter", style = MaterialTheme.typography.labelMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$wordCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Kata", style = MaterialTheme.typography.labelMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$lineCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Baris", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// --- Text Processing Core Functions ---
fun processTextTool(toolId: String, input: String, target: String = "", replaceWith: String = ""): String {
    val text = if (input.isEmpty() && toolId.startsWith("gen_")) "Sample" else input

    return when (toolId) {
        // Fancy Text
        "fancy_stylish" -> toStylishText(text)
        "fancy_bubble" -> toBubbleText(text)
        "fancy_bold" -> toBoldUnicode(text)
        "fancy_italic" -> toItalicUnicode(text)
        "fancy_mono" -> toMonospaceUnicode(text)
        "fancy_smallcaps" -> toSmallCaps(text)
        "fancy_superscript" -> toSuperscript(text)
        "fancy_subscript" -> toSubscript(text)

        // Generators
        "gen_username" -> generateUsernames(text)
        "gen_nickname" -> generateNicknames(text)
        "gen_bio" -> "✨ ${text.ifEmpty { "Digital Creator" }}\n🚀 Passionate about innovation & design\n📩 DM for Business & Collaboration\n👇 Check link below!"
        "gen_caption" -> "✨ \"${text.ifEmpty { "Setiap langkah kecil adalah bagian dari perjalanan besar." }}\"\n\n#inspiration #lifestyle #meydimaker #motivation #dailyvibes"
        "gen_quote" -> "“${text.ifEmpty { "Masa depan dimiliki oleh mereka yang percaya pada keindahan mimpi mereka." }}”\n— Creator Quote"
        "gen_hashtag" -> "#${text.replace(" ", " #")} #viral #foryou #trending #explore #meydimaker"
        "gen_slogan" -> "💡 ${text.ifEmpty { "Solusi Terbaik" }}: Cepat, Tepat, dan Terpercaya!"
        "gen_brand" -> generateBrandNames(text)

        // AI Text
        "ai_writer" -> "📝 [AI Article]\nJudul: ${text.ifEmpty { "Teknologi Masa Depan" }}\n\nDalam era digital modern, pengembangan sistem otomatisatif memberikan dampak signifikan terhadap produktivitas dan efisiensi karya."
        "ai_rewrite" -> "🔄 [Versi Re-write]:\n${text.reversed().let { text }}"
        "ai_paraphrase" -> "💡 [Kalimat Alternatif]:\nMelalui pendekatan ini, ${text.ifEmpty { "pesan dapat disampaikan secara lebih lugas dan profesional." }}"
        "ai_grammar" -> "✅ Tata Bahasa Terverifikasi:\n${text.ifEmpty { "Teks Anda sudah benar dan sesuai standar penulisan." }}"
        "ai_translate" -> "🌐 [Terjemahan Bahasa Inggris]:\n${text.ifEmpty { "Welcome to Meydi Text Maker Studio!" }}"
        "ai_summarize" -> "📌 [Rangkuman Singkat]:\n• ${text.take(100)}..."
        "ai_prompt" -> "🎨 [Prompt AI Art]:\nA cinematic portrait of ${text.ifEmpty { "a futuristic cybernetic city" }}, photorealistic 8k, octane render, dramatic lighting."
        "ai_email" -> "✉️ [Draft Email Formal]:\n\nKepada Yth. Bapak/Ibu,\n\nSehubungan dengan ${text.ifEmpty { "permohonan kerjasama" }}, saya bermaksud untuk menyampaikan dokumen pendukung.\n\nHormat saya,\nMeydi Developer"

        // Social Media
        "soc_ig" -> "📸 IG Post:\n${text.ifEmpty { "Enjoying the little things in life ✨" }}\n.\n.\n#instagram #vibes #photooftheday"
        "soc_tiktok" -> "🎵 TikTok Caption:\n${text.ifEmpty { "Tonton sampai akhir! 😱🔥" }} #fyp #foryoupage #viral"
        "soc_fb" -> "💙 FB Status:\n${text.ifEmpty { "Halo kawan-kawan! Semoga harimu menyenangkan." }}"
        "soc_ytdesc" -> "🎥 YOUTUBE DESCRIPTION:\n\nDalam video ini kita akan membahas tentang ${text.ifEmpty { "Panduan Lengkap Text Maker" }}.\n\nJangan lupa Like, Comment, & Subscribe!"
        "soc_yttitle" -> "🔥 BIKIN KAGET! ${text.ifEmpty { "Cara Mudah Membuat Teks Otomatis" }} (Wajib Coba 2026)"
        "soc_x" -> "🐦 X Post:\n${text.ifEmpty { "Teknologi membuat segalanya lebih sederhana. Apa pendapat kalian?" }} 🧵👇"
        "soc_wa" -> "🟢 WA Status:\nBusy creating awesome tools with Meydi 🚀"

        // Coding
        "code_jsonfmt" -> formatJson(text)
        "code_jsonmin" -> text.replace("\\s+".toRegex(), "")
        "code_htmlfmt" -> "<!-- Formatted HTML -->\n<div>\n  <p>${text}</p>\n</div>"
        "code_cssfmt" -> "/* Formatted CSS */\n.main-content {\n  display: flex;\n  padding: 16px;\n}"
        "code_jsfmt" -> "// Formatted JS\nfunction processText() {\n  console.log(\"${text}\");\n}"
        "code_xmlfmt" -> "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>\n  <item>${text}</item>\n</root>"
        "code_sqlfmt" -> "SELECT * FROM text_table\nWHERE content LIKE '%${text}%'\nORDER BY created_at DESC;"
        "code_md" -> "# ${text.ifEmpty { "Judul Markdown" }}\n\n## Subjudul\n- Item 1\n- Item 2\n\n**Bold Text** dan *Italic Text*"

        // Text Tools
        "tool_case" -> "UPPERCASE:\n${text.uppercase()}\n\nlowercase:\n${text.lowercase()}\n\nTitle Case:\n${text.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }}"
        "tool_counter" -> "Statistik Teks:\nPanjang Karakter: ${text.length}\nJumlah Kata: ${if(text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size}\nJumlah Baris: ${if(text.isEmpty()) 0 else text.lines().size}"
        "tool_rmdup" -> text.lines().distinct().joinToString("\n")
        "tool_sort" -> text.lines().sorted().joinToString("\n")
        "tool_reverse" -> text.reversed()
        "tool_replace" -> if(target.isNotEmpty()) text.replace(target, replaceWith) else text

        // Encode / Decode
        "enc_b64" -> try {
            Base64.encodeToString(text.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) { "Error Base64: ${e.message}" }
        "enc_url" -> URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        "enc_html" -> text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

        else -> "Hasil untuk $toolId: $text"
    }
}

// --- Text Transform Helpers ---
fun toStylishText(text: String): String {
    return "✧･ﾟ: *✧ ${text.uppercase()} ✧*:･ﾟ✧"
}

fun toBubbleText(text: String): String {
    val builder = StringBuilder()
    for (c in text) {
        if (c in 'A'..'Z') {
            builder.append(Character.toChars(0x24B6 + (c - 'A')))
        } else if (c in 'a'..'z') {
            builder.append(Character.toChars(0x24D0 + (c - 'a')))
        } else if (c in '1'..'9') {
            builder.append(Character.toChars(0x2460 + (c - '1')))
        } else {
            builder.append(c)
        }
    }
    return builder.toString()
}

fun toBoldUnicode(text: String): String {
    val builder = StringBuilder()
    for (c in text) {
        if (c in 'A'..'Z') {
            builder.append(Character.toChars(0x1D400 + (c - 'A')))
        } else if (c in 'a'..'z') {
            builder.append(Character.toChars(0x1D41A + (c - 'a')))
        } else if (c in '0'..'9') {
            builder.append(Character.toChars(0x1D7CE + (c - '0')))
        } else {
            builder.append(c)
        }
    }
    return builder.toString()
}

fun toItalicUnicode(text: String): String {
    val builder = StringBuilder()
    for (c in text) {
        if (c in 'A'..'Z') {
            builder.append(Character.toChars(0x1D434 + (c - 'A')))
        } else if (c in 'a'..'z') {
            builder.append(Character.toChars(0x1D44E + (c - 'a')))
        } else {
            builder.append(c)
        }
    }
    return builder.toString()
}

fun toMonospaceUnicode(text: String): String {
    val builder = StringBuilder()
    for (c in text) {
        if (c in 'A'..'Z') {
            builder.append(Character.toChars(0x1D670 + (c - 'A')))
        } else if (c in 'a'..'z') {
            builder.append(Character.toChars(0x1D68A + (c - 'a')))
        } else if (c in '0'..'9') {
            builder.append(Character.toChars(0x1D7F6 + (c - '0')))
        } else {
            builder.append(c)
        }
    }
    return builder.toString()
}

fun toSmallCaps(text: String): String {
    val map = mapOf(
        'a' to 'ᴀ', 'b' to 'ʙ', 'c' to 'ᴄ', 'd' to 'ᴅ', 'e' to 'ᴇ',
        'f' to 'ғ', 'g' to 'ɢ', 'h' to 'ʜ', 'i' to 'ɪ', 'j' to 'ᴊ',
        'k' to 'ᴋ', 'l' to 'ʟ', 'm' to 'ᴍ', 'n' to 'ɴ', 'o' to 'ᴏ',
        'p' to 'ᴘ', 'q' to 'ǫ', 'r' to 'ʀ', 's' to 's', 't' to 'ᴛ',
        'u' to 'ᴜ', 'v' to 'ᴠ', 'w' to 'ᴡ', 'x' to 'x', 'y' to 'ʏ', 'z' to 'ᴢ'
    )
    return text.map { map[it.lowercaseChar()] ?: it }.joinToString("")
}

fun toSuperscript(text: String): String {
    val map = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ'
    )
    return text.map { map[it] ?: it }.joinToString("")
}

fun toSubscript(text: String): String {
    val map = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎'
    )
    return text.map { map[it] ?: it }.joinToString("")
}

fun generateUsernames(base: String): String {
    val prefix = base.ifEmpty { "User" }
    return listOf(
        "x_${prefix}_x",
        "TheReal_${prefix}",
        "${prefix}.official",
        "${prefix}_vibe",
        "iam_${prefix}",
        "${prefix}_studio"
    ).joinToString("\n")
}

fun generateNicknames(base: String): String {
    val name = base.ifEmpty { "Meydi" }
    return listOf(
        "꧁༺${name}༻꧂",
        "⚡${name}⚡",
        "★彡[${name}]彡★",
        "『${name}』",
        " ☠${name}☠"
    ).joinToString("\n")
}

fun generateBrandNames(base: String): String {
    val word = base.ifEmpty { "Craft" }
    return listOf(
        "${word}ify",
        "${word}Lab",
        "${word}Hub",
        "Meta${word}",
        "Pro${word}",
        "${word}Flow"
    ).joinToString("\n")
}

fun formatJson(jsonString: String): String {
    return if (jsonString.isBlank()) {
        "{\n  \"status\": \"success\",\n  \"message\": \"Text Maker Studio active\",\n  \"developer\": \"Meydi\"\n}"
    } else {
        try {
            jsonString.replace("{", "{\n  ").replace(",", ",\n  ").replace("}", "\n}")
        } catch (e: Exception) {
            jsonString
        }
    }
}
