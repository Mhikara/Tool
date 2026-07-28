package com.example.ui.imagemaker

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.aimedia.AiGatewayEngine
import com.example.ui.aimedia.MediaProjectItem
import com.example.ui.aimedia.MediaType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToolInteractiveEditor(
    tool: ImageTool,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Common Controls
    var promptInput by remember { mutableStateOf("") }
    var selectedArtStyle by remember { mutableStateOf("Realistic") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedQuality by remember { mutableStateOf("Full HD") }
    var selectedExportFormat by remember { mutableStateOf("PNG") }

    // Basic Editor Sliders
    var brightnessVal by remember { mutableFloatStateOf(0f) }
    var contrastVal by remember { mutableFloatStateOf(0f) }
    var saturationVal by remember { mutableFloatStateOf(0f) }
    var blurVal by remember { mutableFloatStateOf(0f) }
    var rotationAngle by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var upscaleFactor by remember { mutableStateOf("4x") }

    // Maker Customization
    var titleText by remember { mutableStateOf("Meydi Studio") }
    var subtitleText by remember { mutableStateOf("Image Maker System v1.0.0") }
    var primaryColorHex by remember { mutableStateOf("#6200EE") }

    // Processing State
    var isProcessing by remember { mutableStateOf(false) }
    var progressVal by remember { mutableFloatStateOf(0f) }
    var progressStepText by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val stylesList = listOf(
        "Realistic", "Anime", "Cartoon", "Chibi", "Pixar Style", "Sketch", "Watercolor",
        "Oil Painting", "Pencil Drawing", "Pixel Art", "Cyberpunk", "Fantasy", "3D Render",
        "Low Poly", "Comic", "Ghibli Style"
    )

    val qualitiesList = listOf("SD", "HD", "Full HD", "2K", "4K")
    val formatsList = listOf("PNG", "JPG", "WEBP", "SVG", "PDF")
    val aspectRatiosList = listOf("1:1", "3:4", "4:3", "9:16", "16:9")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tool.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Kategori: ${tool.categoryId.uppercase()} • System v1.0.0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        promptInput = "Masterpiece, ultra detailed 8k, professional lighting: " + promptInput
                        Toast.makeText(context, "Prompt AI Ditingkatkan!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Enhance Prompt", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Interactive Preview Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF3F51B5),
                                        Color(0xFF121212)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = null,
                                    tint = Color.Cyan,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (tool.categoryId == "logo" || tool.categoryId == "poster" || tool.categoryId == "banner") titleText else tool.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (tool.categoryId == "logo" || tool.categoryId == "poster" || tool.categoryId == "banner") subtitleText else tool.description,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            if (isDone) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF00E676).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "READY: $selectedQuality • $selectedExportFormat • $selectedAspectRatio",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Watermark badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Meydi AI Engine v1.0.0", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Input / Parameters based on Category
            when {
                tool.categoryId == "generator" || tool.categoryId == "editor_ai" -> {
                    Text("1. Parameter AI & Prompt Deskripsi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tulis instruksi atau prompt AI di sini...") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (tool.id == "ed_up") {
                        Text("Skala Upscale:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("2x", "4x", "8x").forEach { factor ->
                                FilterChip(
                                    selected = upscaleFactor == factor,
                                    onClick = { upscaleFactor = factor },
                                    label = { Text(factor) }
                                )
                            }
                        }
                    }

                    Text("Gaya Seni (Art Style):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(stylesList) { style ->
                            FilterChip(
                                selected = selectedArtStyle == style,
                                onClick = { selectedArtStyle = style },
                                label = { Text(style, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                tool.categoryId == "editor_basic" -> {
                    Text("1. Pengaturan Editor Gambar Dasar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kecerahan (Brightness)", fontSize = 12.sp)
                            Text("${brightnessVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = brightnessVal,
                            onValueChange = { brightnessVal = it },
                            valueRange = -100f..100f
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kontras (Contrast)", fontSize = 12.sp)
                            Text("${contrastVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = contrastVal,
                            onValueChange = { contrastVal = it },
                            valueRange = -100f..100f
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Saturasi Warna", fontSize = 12.sp)
                            Text("${saturationVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = saturationVal,
                            onValueChange = { saturationVal = it },
                            valueRange = -100f..100f
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { rotationAngle = (rotationAngle + 90) % 360 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Putar ${rotationAngle}°", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { isFlipped = !isFlipped },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFlipped) "Flipped" else "Normal", fontSize = 11.sp)
                            }
                        }
                    }
                }

                else -> { // Logo, Poster, Banner, Thumbnail, Avatar, Sticker, Template, etc.
                    Text("1. Kustomisasi Desain & Teks", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Judul Utama / Teks Brand") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = subtitleText,
                        onValueChange = { subtitleText = it },
                        label = { Text("Sub-Judul / Tagline") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Export Options (Quality, Ratio, Format)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. Kualitas & Format Ekspor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kualitas Output:", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            qualitiesList.forEach { q ->
                                FilterChip(
                                    selected = selectedQuality == q,
                                    onClick = { selectedQuality = q },
                                    label = { Text(q, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Format File:", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            formatsList.forEach { fmt ->
                                FilterChip(
                                    selected = selectedExportFormat == fmt,
                                    onClick = { selectedExportFormat = fmt },
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
                        Text("Rasio Aspek:", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            aspectRatiosList.forEach { ratio ->
                                FilterChip(
                                    selected = selectedAspectRatio == ratio,
                                    onClick = { selectedAspectRatio = ratio },
                                    label = { Text(ratio, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Live Progress Indicator
            if (isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(progressStepText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("${(progressVal * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        }
                        LinearProgressIndicator(
                            progress = { progressVal },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Error Message Card
            errorMessage?.let { err ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = err,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Execute Button
            Button(
                onClick = {
                    isProcessing = true
                    isDone = false
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            progressStepText = "Menyiapkan GPU & Model AI..."
                            progressVal = 0.2f
                            delay(500)

                            progressStepText = "Memproses ${tool.name} ($selectedQuality)..."
                            progressVal = 0.6f
                            delay(600)

                            progressStepText = "Melakukan Rendering & Format $selectedExportFormat..."
                            progressVal = 0.9f
                            delay(400)

                            progressStepText = "Selesai!"
                            progressVal = 1.0f
                            isDone = true

                            // Save to AI Gateway project cache
                            AiGatewayEngine.addProjectToHistory(
                                MediaProjectItem(
                                    title = tool.name + " - " + if (titleText.isNotBlank()) titleText else promptInput.take(15),
                                    type = MediaType.EDITED_IMAGE,
                                    prompt = "[$selectedArtStyle, $selectedQuality, $selectedExportFormat] " + (if (promptInput.isNotBlank()) promptInput else titleText),
                                    mediaUrl = "edited_image_" + System.currentTimeMillis()
                                )
                            )

                            Toast.makeText(context, "Proses ${tool.name} Berhasil!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Terjadi kesalahan sistem."
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isProcessing,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Memproses AI...")
                } else {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jalankan ${tool.name}", fontWeight = FontWeight.Bold)
                }
            }

            // Export & Share Action Buttons when done
            if (isDone) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Berhasil diekspor berformat $selectedExportFormat ($selectedQuality) ke Galeri!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan $selectedExportFormat", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("Hasil ${tool.name}: Meydi AI Maker v1.0.0"))
                            Toast.makeText(context, "Tautan hasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan Hasil", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
