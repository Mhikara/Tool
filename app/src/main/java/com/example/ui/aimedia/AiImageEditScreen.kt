package com.example.ui.aimedia

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.AnimatedCheckmark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiImageEditScreen(
    onBack: () -> Unit,
    onNavigateToVideoGen: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State Variables
    var selectedSamplePhotoIndex by remember { mutableIntStateOf(0) }
    var promptText by remember { mutableStateOf("") }
    var isBrushMode by remember { mutableStateOf(false) }
    var brushSize by remember { mutableFloatStateOf(30f) }
    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    var isProcessing by remember { mutableStateOf(false) }
    var progressVal by remember { mutableFloatStateOf(0f) }
    var progressStatus by remember { mutableStateOf("") }
    var generatedResult by remember { mutableStateOf<MediaProjectItem?>(null) }
    var showBeforeAfterSplit by remember { mutableStateOf(true) }
    var splitSliderPosition by remember { mutableFloatStateOf(0.5f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val samplePhotos = listOf(
        Pair("Langit Siang", "sample_sky_day"),
        Pair("Potret Model", "sample_model_portrait"),
        Pair("Kota Malam", "sample_city_night"),
        Pair("Pemandangan Alam", "sample_landscape")
    )

    val samplePrompts = listOf(
        "Ubah langit jadi malam berbintang dengan aurora",
        "Ganti baju model jadi warna merah marun",
        "Tambahkan efek salju dan pencahayaan neon",
        "Hapus orang & kendaraan di latar belakang",
        "Ubah pencahayaan jadi mode sunset emas (Golden Hour)"
    )

    val sampleBgGradients = listOf(
        listOf(Color(0xFF1E88E5), Color(0xFF42A5F5)),
        listOf(Color(0xFF8E24AA), Color(0xFFAB47BC)),
        listOf(Color(0xFF37474F), Color(0xFF78909C)),
        listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Edit Foto AI (Text-Guided)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "Inpainting",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text("Super AI Maker • Meydi v1.0.0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (onNavigateToVideoGen != null) {
                        IconButton(onClick = onNavigateToVideoGen) {
                            Icon(Icons.Outlined.Videocam, contentDescription = "Ke Video AI", tint = MaterialTheme.colorScheme.primary)
                        }
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
            // Header Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoFixHigh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Text-Guided Image Editing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Pilih foto, tandai area (opsional), dan ketik instruksi teks untuk mengubah foto secara presisi berbasis AI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Photo Selection & Masking Canvas
            Text("1. Pilih & Tandai Foto Sumber", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(samplePhotos.indices.toList()) { index ->
                    val photo = samplePhotos[index]
                    val isSelected = selectedSamplePhotoIndex == index
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .height(80.dp)
                            .clickable {
                                selectedSamplePhotoIndex = index
                                paths = emptyList()
                                generatedResult = null
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(sampleBgGradients[index])),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                photo.first,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            // Canvas Display / Mask Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (generatedResult != null && showBeforeAfterSplit) {
                        // Split view Before / After
                        Box(modifier = Modifier.fillMaxSize()) {
                            // After View
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF311B92), Color(0xFF006064))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("SESUDAH (Hasil AI)", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Watermark AI Applied • 1080p", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }

                            // Before View (Clipped by slider)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(splitSliderPosition)
                                    .background(Brush.linearGradient(sampleBgGradients[selectedSamplePhotoIndex])),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("SEBELUM", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Foto Asli", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }

                            // Split Divider Slider Line
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .align(Alignment.CenterStart)
                                    .offset(x = (260 * splitSliderPosition).dp)
                                    .background(Color.White)
                            )
                        }
                    } else {
                        // Original / Drawing Canvas Mode
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(sampleBgGradients[selectedSamplePhotoIndex]))
                        ) {
                            Text(
                                text = "Foto Sumber: ${samplePhotos[selectedSamplePhotoIndex].first}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            )

                            // Interactive Mask Canvas
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(isBrushMode) {
                                        if (isBrushMode) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath?.lineTo(change.position.x, change.position.y)
                                                },
                                                onDragEnd = {
                                                    currentPath?.let { paths = paths + it }
                                                    currentPath = null
                                                }
                                            )
                                        }
                                    }
                            ) {
                                paths.forEach { path ->
                                    drawPath(
                                        path = path,
                                        color = Color(0x80FF1744),
                                        style = Stroke(width = brushSize, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                                currentPath?.let { path ->
                                    drawPath(
                                        path = path,
                                        color = Color(0x80FF1744),
                                        style = Stroke(width = brushSize, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                        }
                    }

                    // Watermark & Mode Badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Gateway Watermark", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Brush & Mask Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = isBrushMode,
                    onClick = { isBrushMode = !isBrushMode },
                    label = { Text(if (isBrushMode) "Mode Kuas Area (Aktif)" else "Tandai Area Spesifik") },
                    leadingIcon = { Icon(Icons.Filled.Brush, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )

                if (paths.isNotEmpty()) {
                    TextButton(onClick = { paths = emptyList() }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Mask", fontSize = 12.sp)
                    }
                }
            }

            if (generatedResult != null) {
                // Slider for Before/After comparison
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Perbandingan Sebelum / Sesudah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${(splitSliderPosition * 100).toInt()}%", fontSize = 12.sp)
                    }
                    Slider(
                        value = splitSliderPosition,
                        onValueChange = { splitSliderPosition = it },
                        valueRange = 0f..1f
                    )
                }
            }

            // Prompt Input Section
            Text("2. Perintah Teks Perubahan (Text Prompt)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = promptText,
                onValueChange = {
                    promptText = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: Ubah warna baju jadi merah marun...") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                isError = errorMessage != null
            )

            errorMessage?.let { err ->
                Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Quick Prompt Suggestions
            Text("Rekomendasi Prompt:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(samplePrompts) { prompt ->
                    SuggestionChip(
                        onClick = { promptText = prompt },
                        label = { Text(prompt, fontSize = 11.sp) }
                    )
                }
            }

            // Progress Indicator
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
                            Text(progressStatus, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("${(progressVal * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        }
                        LinearProgressIndicator(
                            progress = { progressVal },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Generate Button
            Button(
                onClick = {
                    if (promptText.isBlank()) {
                        errorMessage = "Masukkan instruksi teks terlebih dahulu."
                        return@Button
                    }
                    isProcessing = true
                    generatedResult = null
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            val request = ImageEditRequest(
                                prompt = promptText,
                                sourceImageName = samplePhotos[selectedSamplePhotoIndex].second,
                                hasMask = paths.isNotEmpty()
                            )

                            val result = AiGatewayEngine.processImageEdit(request) { status, progress ->
                                progressStatus = status
                                progressVal = progress
                            }
                            generatedResult = result
                            Toast.makeText(context, "Edit foto AI berhasil diselesaikan!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Terjadi kesalahan pada AI Gateway."
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
                    Text("Memproses AI Inpainting...")
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hasilkan Edit Foto AI", fontWeight = FontWeight.Bold)
                }
            }

            // Result Actions
            if (generatedResult != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Hasil edit foto disimpan ke Galeri & Draft Project!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Membuka menu bagikan...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
