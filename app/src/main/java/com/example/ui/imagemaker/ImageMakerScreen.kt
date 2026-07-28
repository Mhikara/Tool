package com.example.ui.imagemaker

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- Data Models ---
data class ImageCategory(
    val id: String,
    val name: String,
    val icon: ImageVector
)

data class ImageTool(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val icon: ImageVector,
    val isFavorite: Boolean = false
)

// --- ViewModel ---
class ImageMakerViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("generator")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _tools = MutableStateFlow(getInitialTools())
    val tools = _tools.asStateFlow()

    val categories = listOf(
        ImageCategory("generator", "AI Generator", Icons.Outlined.AutoAwesome),
        ImageCategory("logo", "Logo Maker", Icons.Outlined.Checkroom),
        ImageCategory("thumbnail", "Thumbnail Maker", Icons.Outlined.OndemandVideo),
        ImageCategory("poster", "Poster Maker", Icons.Outlined.ConfirmationNumber),
        ImageCategory("banner", "Banner Maker", Icons.Outlined.ViewStream),
        ImageCategory("avatar", "Avatar Maker", Icons.Outlined.Face),
        ImageCategory("sticker", "Sticker Maker", Icons.Outlined.EmojiEmotions),
        ImageCategory("qr", "QR Maker", Icons.Outlined.QrCode),
        ImageCategory("editor_ai", "AI Editor", Icons.Outlined.AutoFixHigh),
        ImageCategory("editor_basic", "Basic Editor", Icons.Outlined.Edit),
        ImageCategory("template", "Templates", Icons.Outlined.DashboardCustomize)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleFavorite(toolId: String) {
        _tools.update { list ->
            list.map { if (it.id == toolId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    private fun getInitialTools(): List<ImageTool> {
        return listOf(
            // AI Image Generator
            ImageTool("gen_text", "Text to Image", "generator", "Ubah deskripsi teks menjadi gambar", Icons.Outlined.TextFields),
            ImageTool("gen_art", "AI Art Generator", "generator", "Ciptakan karya seni abstrak & artistik", Icons.Outlined.Palette),
            ImageTool("gen_real", "AI Realistic Image", "generator", "Gambar fotorealistik beresolusi tinggi", Icons.Outlined.CameraAlt),
            ImageTool("gen_anime", "AI Anime Image", "generator", "Buat karakter bergaya anime", Icons.Outlined.Brush),
            ImageTool("gen_cartoon", "AI Cartoon Image", "generator", "Ubah foto atau teks menjadi kartun", Icons.Outlined.FaceRetouchingNatural),
            ImageTool("gen_chibi", "AI Chibi Maker", "generator", "Karakter Chibi yang lucu & imut", Icons.Outlined.ChildCare),
            ImageTool("gen_pixel", "AI Pixel Art", "generator", "Gambar gaya pixel retro 8-bit", Icons.Outlined.GridOn),
            ImageTool("gen_sketch", "AI Sketch", "generator", "Ubah ide jadi sketsa pensil", Icons.Outlined.Edit),
            ImageTool("gen_paint", "AI Painting", "generator", "Gaya lukisan cat minyak/air", Icons.Outlined.FormatPaint),
            ImageTool("gen_wall", "AI Wallpaper", "generator", "Wallpaper 4K untuk PC & HP", Icons.Outlined.Wallpaper),

            // Logo Maker
            ImageTool("logo_ai", "AI Logo Generator", "logo", "Buat logo otomatis dengan AI", Icons.Outlined.AutoAwesome),
            ImageTool("logo_game", "Gaming Logo", "logo", "Desain logo tim eSports & Gaming", Icons.Outlined.SportsEsports),
            ImageTool("logo_esport", "Esports Logo", "logo", "Logo maskot eSports agresif", Icons.Outlined.SportsScore),
            ImageTool("logo_biz", "Business Logo", "logo", "Logo profesional untuk perusahaan", Icons.Outlined.BusinessCenter),
            ImageTool("logo_min", "Minimalist Logo", "logo", "Desain logo simpel dan elegan", Icons.Outlined.HorizontalRule),
            ImageTool("logo_3d", "3D Logo", "logo", "Efek 3D pop-out pada logo", Icons.Outlined.ViewInAr),
            ImageTool("logo_mascot", "Mascot Logo", "logo", "Karakter maskot untuk brand", Icons.Outlined.Pets),
            ImageTool("logo_temp", "Logo Template", "logo", "Kumpulan template logo siap pakai", Icons.Outlined.DesignServices),

            // Thumbnail Maker
            ImageTool("thumb_yt", "YouTube Thumbnail", "thumbnail", "Thumbnail video YouTube klikbait", Icons.Outlined.PlayCircleOutline),
            ImageTool("thumb_tiktok", "TikTok Cover", "thumbnail", "Cover video vertikal untuk TikTok", Icons.Outlined.MusicNote),
            ImageTool("thumb_fb", "Facebook Thumbnail", "thumbnail", "Gambar pratinjau video Facebook", Icons.Outlined.Facebook),
            ImageTool("thumb_ig", "Instagram Post", "thumbnail", "Desain feed & reels Instagram", Icons.Outlined.CameraAlt),
            ImageTool("thumb_twitch", "Twitch Thumbnail", "thumbnail", "Thumbnail stream & VOD Twitch", Icons.Outlined.LiveTv),
            ImageTool("thumb_gaming", "Gaming Thumbnail", "thumbnail", "Desain khusus thumbnail gaming", Icons.Outlined.VideogameAsset),

            // Poster Maker
            ImageTool("post_event", "Event Poster", "poster", "Poster acara dan kegiatan", Icons.Outlined.Event),
            ImageTool("post_promo", "Promotion Poster", "poster", "Poster diskon & marketing", Icons.Outlined.Campaign),
            ImageTool("post_biz", "Business Poster", "poster", "Poster profil & layanan bisnis", Icons.Outlined.Business),
            ImageTool("post_movie", "Movie Poster", "poster", "Poster film bergaya sinematik", Icons.Outlined.Movie),
            ImageTool("post_music", "Music Poster", "poster", "Poster konser & festival musik", Icons.Outlined.LibraryMusic),
            ImageTool("post_fest", "Festival Poster", "poster", "Poster perayaan & festival", Icons.Outlined.Festival),

            // Banner Maker
            ImageTool("ban_yt", "YouTube Banner", "banner", "Header channel YouTube (16:9)", Icons.Outlined.Tv),
            ImageTool("ban_fb", "Facebook Cover", "banner", "Foto sampul halaman Facebook", Icons.Outlined.ImageAspectRatio),
            ImageTool("ban_x", "X (Twitter) Banner", "banner", "Header profil Twitter/X", Icons.Outlined.AlternateEmail),
            ImageTool("ban_in", "LinkedIn Banner", "banner", "Header profesional LinkedIn", Icons.Outlined.WorkOutline),
            ImageTool("ban_web", "Website Banner", "banner", "Banner slider website", Icons.Outlined.Web),
            ImageTool("ban_dc", "Discord Banner", "banner", "Banner server & profil Discord", Icons.Outlined.ChatBubbleOutline),

            // Avatar Maker
            ImageTool("ava_ai", "AI Avatar", "avatar", "Avatar profil unik buatan AI", Icons.Outlined.AutoAwesomeMosaic),
            ImageTool("ava_anime", "Anime Avatar", "avatar", "Karakter avatar bergaya anime", Icons.Outlined.CatchingPokemon),
            ImageTool("ava_cartoon", "Cartoon Avatar", "avatar", "Avatar vektor kartun", Icons.Outlined.Face),
            ImageTool("ava_gaming", "Gaming Avatar", "avatar", "Avatar sangar untuk akun game", Icons.Outlined.SportsEsports),
            ImageTool("ava_biz", "Business Avatar", "avatar", "Foto profil profesional/LinkedIn", Icons.Outlined.PersonOutline),
            ImageTool("ava_pfp", "Profile Picture", "avatar", "Desain bingkai & foto profil", Icons.Outlined.AccountCircle),

            // Sticker Maker
            ImageTool("stk_wa", "WhatsApp Sticker", "sticker", "Buat stiker WA statis/animasi", Icons.Outlined.Chat),
            ImageTool("stk_tg", "Telegram Sticker", "sticker", "Pack stiker untuk Telegram", Icons.Outlined.Send),
            ImageTool("stk_png", "PNG Sticker", "sticker", "Stiker transparan berformat PNG", Icons.Outlined.Layers),
            ImageTool("stk_ai", "AI Sticker", "sticker", "Generasi stiker dari teks", Icons.Outlined.SmartToy),
            ImageTool("stk_pack", "Sticker Pack", "sticker", "Kelola bundel koleksi stiker", Icons.Outlined.Collections),

            // QR Image Maker
            ImageTool("qr_logo", "Tambahkan Logo", "qr", "Sisipkan logo di tengah QR Code", Icons.Outlined.QrCodeScanner),
            ImageTool("qr_color", "QR Berwarna", "qr", "Ubah warna QR Code", Icons.Outlined.ColorLens),
            ImageTool("qr_grad", "QR Gradient", "qr", "Efek gradasi pada QR Code", Icons.Outlined.Gradient),
            ImageTool("qr_trans", "QR Transparan", "qr", "Background transparan untuk QR", Icons.Outlined.FormatColorFill),
            ImageTool("qr_temp", "QR Template", "qr", "Desain frame QR siap pakai", Icons.Outlined.CropFree),

            // Image Editor (AI)
            ImageTool("ed_bg", "AI Background Remover", "editor_ai", "Hapus latar belakang secara instan", Icons.Outlined.LayersClear),
            ImageTool("ed_obj", "AI Object Remover", "editor_ai", "Hapus objek / orang mengganggu", Icons.Outlined.BlurOff),
            ImageTool("ed_magic", "AI Magic Eraser", "editor_ai", "Hilangkan teks & noda watermark", Icons.Outlined.AutoFixNormal),
            ImageTool("ed_hd", "AI HD Enhance", "editor_ai", "Perjelas foto buram & resolusi rendah", Icons.Outlined.HighQuality),
            ImageTool("ed_up", "AI Upscale (2x, 4x)", "editor_ai", "Perbesar ukuran tanpa pecah", Icons.Outlined.ZoomIn),
            ImageTool("ed_face", "AI Face Enhance", "editor_ai", "Retouch & perjelas detail wajah", Icons.Outlined.FaceRetouchingNatural),
            ImageTool("ed_noise", "AI Noise Reduction", "editor_ai", "Bersihkan bintik noise dari foto malam", Icons.Outlined.LensBlur),
            ImageTool("ed_color", "AI Color Correction", "editor_ai", "Auto perbaiki kontras & warna", Icons.Outlined.Palette),
            ImageTool("ed_sharp", "AI Sharpen", "editor_ai", "Tajamkan tepian gambar", Icons.Outlined.ChangeHistory),
            ImageTool("ed_blur", "AI Blur Background", "editor_ai", "Efek bokeh / potret profesional", Icons.Outlined.CameraFront),

            // Basic Editor
            ImageTool("bs_crop", "Crop", "editor_basic", "Potong gambar (1:1, 16:9, dll)", Icons.Outlined.Crop),
            ImageTool("bs_rot", "Rotate", "editor_basic", "Putar gambar 90/180 derajat", Icons.Outlined.RotateRight),
            ImageTool("bs_flip", "Flip", "editor_basic", "Balik gambar horizontal/vertikal", Icons.Outlined.Flip),
            ImageTool("bs_res", "Resize", "editor_basic", "Ubah resolusi lebar & tinggi", Icons.Outlined.PhotoSizeSelectLarge),
            ImageTool("bs_comp", "Compress", "editor_basic", "Kompres ukuran file (KB/MB)", Icons.Outlined.Compress),
            ImageTool("bs_water", "Watermark", "editor_basic", "Tambahkan teks/logo watermark", Icons.Outlined.BrandingWatermark),
            ImageTool("bs_bord", "Border", "editor_basic", "Beri bingkai pada gambar", Icons.Outlined.BorderOuter),
            ImageTool("bs_shad", "Shadow", "editor_basic", "Tambahkan efek bayangan", Icons.Outlined.FlipToBack),
            ImageTool("bs_round", "Rounded Corner", "editor_basic", "Buat sudut gambar membulat", Icons.Outlined.RoundedCorner),
            ImageTool("bs_filt", "Filter", "editor_basic", "Filter warna dasar (B&W, Sepia)", Icons.Outlined.FilterBAndW),

            // Template
            ImageTool("tpl_biz", "Business", "template", "Template profesional", Icons.Outlined.BusinessCenter),
            ImageTool("tpl_game", "Gaming", "template", "Template konten gaming", Icons.Outlined.SportsEsports),
            ImageTool("tpl_tech", "Technology", "template", "Template futuristik & tekno", Icons.Outlined.Memory),
            ImageTool("tpl_edu", "Education", "template", "Template sekolah & edukasi", Icons.Outlined.School),
            ImageTool("tpl_soc", "Social Media", "template", "Template feed sosial media", Icons.Outlined.Share),
            ImageTool("tpl_pers", "Personal", "template", "Template profil pribadi", Icons.Outlined.Person),
            ImageTool("tpl_prem", "Premium Template", "template", "Template eksklusif berkualitas tinggi", Icons.Outlined.Diamond)
        )
    }
}

// --- Main UI Component ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageMakerScreen(
    onBack: () -> Unit,
    onNavigate: ((String) -> Unit)? = null
) {
    val viewModel: ImageMakerViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val allTools by viewModel.tools.collectAsState()

    var selectedToolForEdit by remember { mutableStateOf<ImageTool?>(null) }

    if (selectedToolForEdit != null) {
        ImageToolInteractiveEditor(
            tool = selectedToolForEdit!!,
            onBack = { selectedToolForEdit = null }
        )
        return
    }

    val filteredTools = remember(searchQuery, selectedCategory, allTools) {
        allTools.filter { tool ->
            val matchesSearch = tool.name.contains(searchQuery, ignoreCase = true) ||
                    tool.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == tool.categoryId ||
                    (selectedCategory == "favorites" && tool.isFavorite) ||
                    (selectedCategory == "all")
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sistem Image Maker", fontWeight = FontWeight.Bold)
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
                    IconButton(onClick = { /* TODO: Settings / Cloud Backup */ }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
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
            // Header Promo / Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
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
                                "AI Image Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Buat, edit, dan poles gambar berkualitas tinggi dengan dukungan teknologi AI generasi terbaru.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Outlined.AutoFixHigh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(start = 16.dp)
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari alat (contoh: Logo, Background Remover)...") },
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
                            selected = selectedCategory == "all",
                            onClick = { viewModel.setCategory("all") },
                            label = { Text("Semua") }
                        )
                    }
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

            // Tools Listing
            item {
                Text(
                    "Alat Tersedia (${filteredTools.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Grid-like layout using row items chunked (2 per row)
            val chunkedTools = filteredTools.chunked(2)
            items(chunkedTools) { rowTools ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (tool in rowTools) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (tool.categoryId == "qr" && onNavigate != null) {
                                        onNavigate("qr_maker")
                                    } else {
                                        selectedToolForEdit = tool
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = tool.name,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(tool.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (tool.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Favorit",
                                            tint = if (tool.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = tool.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tool.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    // Add empty spacer if row is not full
                    if (rowTools.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
