package com.example.ui.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- Data Models ---
data class ToolCategory(val id: String, val name: String, val icon: ImageVector)

data class Tool(
    val id: String,
    val name: String,
    val categoryId: String,
    val icon: ImageVector,
    val isFavorite: Boolean = false
)

// --- ViewModel ---
class ToolsViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _tools = MutableStateFlow(getInitialTools())
    val tools = _tools.asStateFlow()

    private val _recentToolIds = MutableStateFlow<List<String>>(emptyList())
    val recentToolIds = _recentToolIds.asStateFlow()

    val categories = listOf(
        ToolCategory("all", "All Tools", Icons.Outlined.GridView),
        ToolCategory("file", "File Tools", Icons.Outlined.Folder),
        ToolCategory("image", "Image Tools", Icons.Outlined.Image),
        ToolCategory("video", "Video Tools", Icons.Outlined.VideoFile),
        ToolCategory("audio", "Audio Tools", Icons.Outlined.AudioFile),
        ToolCategory("qr", "QR Tools", Icons.Outlined.QrCode),
        ToolCategory("text", "Text Tools", Icons.Outlined.TextFields),
        ToolCategory("developer", "Developer Tools", Icons.Outlined.Code),
        ToolCategory("network", "Network Tools", Icons.Outlined.Wifi),
        ToolCategory("security", "Security Tools", Icons.Outlined.Security),
        ToolCategory("ai", "AI Tools", Icons.Outlined.AutoAwesome),
        ToolCategory("download", "Download Tools", Icons.Outlined.Download),
        ToolCategory("media", "Media Support", Icons.Outlined.PermMedia),
        ToolCategory("storage", "Storage", Icons.Outlined.Storage),
        ToolCategory("export", "Export", Icons.Outlined.ImportExport),
        ToolCategory("smart", "Smart Features", Icons.Outlined.Memory)
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleFavorite(toolId: String) {
        _tools.update { currentTools ->
            currentTools.map { if (it.id == toolId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    fun addRecentTool(toolId: String) {
        _recentToolIds.update { recents ->
            val mutable = recents.toMutableList()
            mutable.remove(toolId)
            mutable.add(0, toolId)
            mutable.take(10)
        }
    }

    private fun getInitialTools(): List<Tool> {
        return listOf(
            // Image Tools
            Tool("img_viewer", "Image Viewer", "image", Icons.Outlined.Visibility),
            Tool("img_editor", "Image Editor", "image", Icons.Outlined.Edit),
            Tool("img_ai_enh", "AI Image Enhancer", "image", Icons.Outlined.AutoFixHigh),
            Tool("img_ai_upscale", "AI Upscale (2x, 4x, 8x)", "image", Icons.Outlined.HighQuality),
            Tool("img_ai_bg_rem", "AI Background Remover", "image", Icons.Outlined.ImageNotSupported),
            Tool("img_ai_obj_rem", "AI Object Remover", "image", Icons.Outlined.BlurOn),
            Tool("img_ai_face_enh", "AI Face Enhance", "image", Icons.Outlined.Face),
            Tool("img_ai_restore", "AI Restore Old Photo", "image", Icons.Outlined.SettingsBackupRestore),
            Tool("img_ai_colorize", "AI Colorize", "image", Icons.Outlined.Palette),
            Tool("img_crop", "Crop", "image", Icons.Outlined.Crop),
            Tool("img_resize", "Resize", "image", Icons.Outlined.PhotoSizeSelectLarge),
            Tool("img_rotate", "Rotate", "image", Icons.Outlined.RotateRight),
            Tool("img_flip", "Flip", "image", Icons.Outlined.Flip),
            Tool("img_compress", "Compress", "image", Icons.Outlined.Compress),
            Tool("img_blur", "Blur", "image", Icons.Outlined.BlurOn),
            Tool("img_sharpen", "Sharpen", "image", Icons.Outlined.FilterCenterFocus),
            Tool("img_watermark", "Watermark", "image", Icons.Outlined.BrandingWatermark),
            Tool("img_convert", "Image Converter", "image", Icons.Outlined.Transform),
            Tool("img_to_pdf", "Image to PDF", "image", Icons.Outlined.PictureAsPdf),
            Tool("img_ocr", "OCR (Image to Text)", "image", Icons.Outlined.Translate),
            Tool("img_metadata", "Metadata Viewer", "image", Icons.Outlined.Info),

            // Video Tools
            Tool("vid_player", "Video Player", "video", Icons.Outlined.PlayCircleOutline),
            Tool("vid_downloader", "Video Downloader", "video", Icons.Outlined.VideoFile),
            Tool("vid_editor", "Video Editor", "video", Icons.Outlined.Movie),
            Tool("vid_cutter", "Video Cutter", "video", Icons.Outlined.ContentCut),
            Tool("vid_merger", "Video Merger", "video", Icons.Outlined.MergeType),
            Tool("vid_compress", "Video Compressor", "video", Icons.Outlined.Compress),
            Tool("vid_convert", "Video Converter", "video", Icons.Outlined.Transform),
            Tool("vid_to_audio", "Video to Audio", "video", Icons.Outlined.AudioFile),
            Tool("vid_audio_to_vid", "Audio to Video", "video", Icons.Outlined.Audiotrack),
            Tool("vid_thumb", "Thumbnail Extractor", "video", Icons.Outlined.Image),
            Tool("vid_sub_gen", "Subtitle Generator", "video", Icons.Outlined.Subtitles),
            Tool("vid_sub_edit", "Subtitle Editor", "video", Icons.Outlined.SubtitlesOff),
            Tool("vid_ai_enh", "AI Video Enhance", "video", Icons.Outlined.AutoFixHigh),
            Tool("vid_ai_upscale", "AI Video Upscale", "video", Icons.Outlined.HighQuality),
            Tool("vid_speed", "Speed Controller", "video", Icons.Outlined.Speed),
            Tool("vid_reverse", "Reverse Video", "video", Icons.Outlined.SettingsBackupRestore),
            Tool("vid_rotate", "Rotate Video", "video", Icons.Outlined.RotateLeft),
            Tool("vid_metadata", "Video Metadata", "video", Icons.Outlined.Info),

            // Audio Tools
            Tool("aud_player", "Audio Player", "audio", Icons.Outlined.PlayArrow),
            Tool("aud_downloader", "Audio Downloader", "audio", Icons.Outlined.CloudDownload),
            Tool("aud_recorder", "Audio Recorder", "audio", Icons.Outlined.Mic),
            Tool("aud_editor", "Audio Editor", "audio", Icons.Outlined.Audiotrack),
            Tool("aud_cutter", "Audio Cutter", "audio", Icons.Outlined.ContentCut),
            Tool("aud_merger", "Audio Merger", "audio", Icons.Outlined.MergeType),
            Tool("aud_convert", "Audio Converter", "audio", Icons.Outlined.Transform),
            Tool("aud_compress", "Audio Compressor", "audio", Icons.Outlined.Compress),
            Tool("aud_voice_changer", "Voice Changer", "audio", Icons.Outlined.RecordVoiceOver),
            Tool("aud_noise_red", "Noise Reduction", "audio", Icons.Outlined.HearingDisabled),
            Tool("aud_volume", "Volume Booster", "audio", Icons.Outlined.VolumeUp),
            Tool("aud_eq", "Equalizer", "audio", Icons.Outlined.Equalizer),
            Tool("aud_metadata", "Audio Metadata", "audio", Icons.Outlined.Info),

            // PDF Tools
            Tool("pdf_viewer", "PDF Viewer", "file", Icons.Outlined.PictureAsPdf),
            Tool("pdf_creator", "PDF Creator", "file", Icons.Outlined.CreateNewFolder),
            Tool("pdf_merger", "PDF Merger", "file", Icons.Outlined.MergeType),
            Tool("pdf_splitter", "PDF Splitter", "file", Icons.Outlined.CallSplit),
            Tool("pdf_compress", "PDF Compressor", "file", Icons.Outlined.Compress),
            Tool("pdf_to_img", "PDF to Image", "file", Icons.Outlined.Image),
            Tool("pdf_img_to_pdf", "Image to PDF", "file", Icons.Outlined.PictureAsPdf),
            Tool("pdf_encrypt", "PDF Encrypt", "file", Icons.Outlined.Lock),
            Tool("pdf_unlock", "PDF Unlock", "file", Icons.Outlined.LockOpen),

            // File Tools
            Tool("file_manager", "File Manager", "file", Icons.Outlined.Folder),
            Tool("file_zip_create", "ZIP Creator", "file", Icons.Outlined.FolderZip),
            Tool("file_zip_extract", "ZIP Extractor", "file", Icons.Outlined.FolderZip),
            Tool("file_rar_extract", "RAR Extractor", "file", Icons.Outlined.Folder),
            Tool("file_7z_extract", "7Z Extractor", "file", Icons.Outlined.Folder),
            Tool("file_search", "File Search", "file", Icons.Outlined.Search),
            Tool("file_rename", "Rename File", "file", Icons.Outlined.Edit),
            Tool("file_copy", "Copy File", "file", Icons.Outlined.FileCopy),
            Tool("file_move", "Move File", "file", Icons.Outlined.DriveFileMove),
            Tool("file_delete", "Delete File", "file", Icons.Outlined.Delete),
            Tool("file_share", "Share File", "file", Icons.Outlined.Share),

            // QR Tools
            Tool("qr_gen", "QR Generator", "qr", Icons.Outlined.QrCode),
            Tool("qr_scan", "QR Scanner", "qr", Icons.Outlined.QrCodeScanner),
            Tool("qr_url", "QR URL", "qr", Icons.Outlined.Link),
            Tool("qr_wifi", "QR WiFi", "qr", Icons.Outlined.Wifi),
            Tool("qr_contact", "QR Contact", "qr", Icons.Outlined.ContactMail),
            Tool("qr_wa", "QR WhatsApp", "qr", Icons.Outlined.Message),
            Tool("qr_email", "QR Email", "qr", Icons.Outlined.Email),
            Tool("qr_text", "QR Text", "qr", Icons.Outlined.TextFields),
            Tool("qr_loc", "QR Location", "qr", Icons.Outlined.LocationOn),
            Tool("qr_history", "QR History", "qr", Icons.Outlined.History),

            // AI Media Tools
            Tool("ai_img_gen", "AI Image Generator", "ai", Icons.Outlined.AutoAwesome),
            Tool("ai_img_ed", "AI Image Editor", "ai", Icons.Outlined.Edit),
            Tool("ai_vid_gen", "AI Video Generator", "ai", Icons.Outlined.VideoSettings),
            Tool("ai_vid_ed", "AI Video Editor", "ai", Icons.Outlined.MovieFilter),
            Tool("ai_aud_gen", "AI Audio Generator", "ai", Icons.Outlined.Audiotrack),
            Tool("ai_stt", "AI Speech to Text", "ai", Icons.Outlined.KeyboardVoice),
            Tool("ai_tts", "AI Text to Speech", "ai", Icons.Outlined.RecordVoiceOver),
            Tool("ai_caption", "AI Caption Generator", "ai", Icons.Outlined.Notes),
            Tool("ai_thumb", "AI Thumbnail Maker", "ai", Icons.Outlined.Image),

            // Download Manager
            Tool("dl_smart_link", "Smart Link Detection", "download", Icons.Outlined.Link),
            Tool("dl_auto_paste", "Auto Paste Link", "download", Icons.Outlined.ContentPaste),
            Tool("dl_batch", "Batch Download", "download", Icons.Outlined.DownloadForOffline),
            Tool("dl_bg", "Background Download", "download", Icons.Outlined.Downloading),
            Tool("dl_controls", "Pause/Resume/Retry", "download", Icons.Outlined.SettingsBackupRestore),
            Tool("dl_queue", "Download Queue", "download", Icons.Outlined.QueuePlayNext),
            Tool("dl_history", "Download History", "download", Icons.Outlined.History),

            // Media Support
            Tool("ms_youtube", "YouTube Support", "media", Icons.Outlined.VideoLibrary),
            Tool("ms_tiktok", "TikTok Support", "media", Icons.Outlined.MusicNote),
            Tool("ms_instagram", "Instagram Support", "media", Icons.Outlined.CameraAlt),
            Tool("ms_facebook", "Facebook Support", "media", Icons.Outlined.ThumbUp),
            Tool("ms_x", "X Support", "media", Icons.Outlined.Tag),
            Tool("ms_pinterest", "Pinterest Support", "media", Icons.Outlined.PushPin),
            Tool("ms_reddit", "Reddit Support", "media", Icons.Outlined.Forum),
            Tool("ms_vimeo", "Vimeo Support", "media", Icons.Outlined.VideoLabel),
            Tool("ms_dailymotion", "Dailymotion Support", "media", Icons.Outlined.Movie),
            Tool("ms_twitch", "Twitch Support", "media", Icons.Outlined.LiveTv),
            Tool("ms_sound_cloud", "SoundCloud Support", "media", Icons.Outlined.CloudQueue)
        )
    }
}

// --- UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onBack: () -> Unit, onNavigateToTool: (String) -> Unit = {}) {
    val viewModel: ToolsViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val allTools by viewModel.tools.collectAsState()
    val recentToolIds by viewModel.recentToolIds.collectAsState()

    var selectedToolForEdit by remember { mutableStateOf<Tool?>(null) }

    if (selectedToolForEdit != null) {
        MediaToolInteractiveProcessor(
            tool = selectedToolForEdit!!,
            onBack = { selectedToolForEdit = null }
        )
        return
    }

    val filteredTools = remember(searchQuery, selectedCategoryId, allTools) {
        allTools.filter { tool ->
            val matchesSearch = tool.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryId == "all" || tool.categoryId == selectedCategoryId || 
                (selectedCategoryId == "favorites" && tool.isFavorite)
            matchesSearch && matchesCategory
        }
    }

    val favorites = allTools.filter { it.isFavorite }
    val recents = allTools.filter { it.id in recentToolIds }.sortedBy { recentToolIds.indexOf(it.id) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = { Text("Super Tools") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Cari alat...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // Categories Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == "favorites",
                            onClick = { viewModel.selectCategory("favorites") },
                            label = { Text("Favorit") },
                            leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    items(viewModel.categories) { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(category.name) },
                            leadingIcon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
                Divider()
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (searchQuery.isEmpty() && selectedCategoryId == "all" && recents.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader("Terakhir Digunakan")
                }
                
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recents) { tool ->
                            ToolCardMini(
                                tool = tool,
                                onClick = { 
                                    viewModel.addRecentTool(tool.id)
                                    if (tool.id == "file_manager") {
                                        onNavigateToTool("file_manager")
                                    } else if (tool.id == "text_wordcnt") {
                                        onNavigateToTool("tool_word_counter")
                                    } else if (tool.id == "qr_gen") {
                                        onNavigateToTool("qr_maker")
                                    } else {
                                        selectedToolForEdit = tool
                                    }
                                }
                            )
                        }
                    }
                }
                
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (filteredTools.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Alat tidak ditemukan.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredTools) { tool ->
                    ToolCard(
                        tool = tool,
                        onClick = {
                            viewModel.addRecentTool(tool.id)
                            if (tool.id == "file_manager") {
                                onNavigateToTool("file_manager")
                            } else if (tool.id == "text_wordcnt") {
                                onNavigateToTool("tool_word_counter")
                            } else if (tool.id == "qr_gen") {
                                onNavigateToTool("qr_maker")
                            } else {
                                selectedToolForEdit = tool
                            }
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(tool.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ToolCard(
    tool: Tool,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.name,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Favorite Button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
            ) {
                Icon(
                    imageVector = if (tool.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (tool.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun ToolCardMini(
    tool: Tool,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ToolActionDialog(tool: Tool, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(tool.name, textAlign = TextAlign.Center) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Fitur ${tool.name} akan segera hadir!",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Kami sedang mempersiapkan fitur ini agar Anda dapat menikmati pengalaman yang maksimal. Harap nantikan pembaruan berikutnya.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Tutup")
            }
        }
    )
}
