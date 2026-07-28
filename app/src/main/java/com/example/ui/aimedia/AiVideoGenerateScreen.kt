package com.example.ui.aimedia

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVideoGenerateScreen(
    onBack: () -> Unit,
    onNavigateToImageEdit: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Form Inputs
    var selectedGenMode by remember { mutableStateOf(VideoGenMode.TEXT_TO_VIDEO) }
    var promptInput by remember { mutableStateOf("") }
    var videoDurationSec by remember { mutableIntStateOf(5) } // Default 5 seconds (1-10 range)
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }

    // Async State
    var isGenerating by remember { mutableStateOf(false) }
    var progressVal by remember { mutableFloatStateOf(0f) }
    var progressStatus by remember { mutableStateOf("") }
    var estTimeRemaining by remember { mutableIntStateOf(0) }
    var generatedVideoItem by remember { mutableStateOf<MediaProjectItem?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Video Player Control States
    var isPlaying by remember { mutableStateOf(true) }
    var currentPlaybackSec by remember { mutableFloatStateOf(0f) }

    val samplePhotos = listOf(
        Pair("Pemandangan Gunung", "sample_mountain"),
        Pair("Air Terjun Kota", "sample_waterfall"),
        Pair("Mobil Sport", "sample_car"),
        Pair("Karakter Anime", "sample_anime")
    )

    val sampleTextPrompts = listOf(
        "Kamera pan perlahan memperlihatkan pemandangan gunung dengan awan bergerak cepat",
        "Air terjun mengalir deras dengan cipratan embun dan efek pelangi",
        "Mobil sport melaju kencang di jalanan kota futuristik dengan sinar lampu neon",
        "Karakter melambaikan tangan dengan latar belakang bunga sakura berguguran"
    )

    // Player Loop Simulation
    LaunchedEffect(isPlaying, generatedVideoItem) {
        if (generatedVideoItem != null && isPlaying) {
            val total = generatedVideoItem!!.durationSec.toFloat()
            while (isPlaying) {
                delay(100)
                currentPlaybackSec += 0.1f
                if (currentPlaybackSec >= total) {
                    currentPlaybackSec = 0f
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Generate Video AI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "1–10 Detik",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text("Text/Image-to-Video AI Gateway • Meydi v1.0.0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (onNavigateToImageEdit != null) {
                        IconButton(onClick = onNavigateToImageEdit) {
                            Icon(Icons.Outlined.AutoFixHigh, contentDescription = "Ke Edit Foto", tint = MaterialTheme.colorScheme.primary)
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
            // Mode Selector (Text-To-Video vs Image-To-Video)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedGenMode == VideoGenMode.TEXT_TO_VIDEO,
                    onClick = {
                        selectedGenMode = VideoGenMode.TEXT_TO_VIDEO
                        errorMessage = null
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Teks ke Video", fontSize = 12.sp)
                    }
                }
                SegmentedButton(
                    selected = selectedGenMode == VideoGenMode.IMAGE_TO_VIDEO,
                    onClick = {
                        selectedGenMode = VideoGenMode.IMAGE_TO_VIDEO
                        errorMessage = null
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Foto + Teks ke Video", fontSize = 12.sp)
                    }
                }
            }

            // Photo Selection if Image-to-Video mode
            if (selectedGenMode == VideoGenMode.IMAGE_TO_VIDEO) {
                Text("1. Pilih Foto Sumber Gerakan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(samplePhotos.indices.toList()) { index ->
                        val isSelected = selectedPhotoIndex == index
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .height(85.dp)
                                .clickable { selectedPhotoIndex = index },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    samplePhotos[index].first,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Video Duration Slider (1 to 10 Seconds)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Durasi Video (Batas: 1 – 10 Detik)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ) {
                            Text("${videoDurationSec}s", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = videoDurationSec.toFloat(),
                        onValueChange = { videoDurationSec = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1s (Cepat)", fontSize = 11.sp, color = Color.Gray)
                        Text("5s (Standar)", fontSize = 11.sp, color = Color.Gray)
                        Text("10s (Maksimal)", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            // Prompt Input
            Text("2. Deskripsi Gerakan / Adegan (Prompt)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = promptInput,
                onValueChange = {
                    promptInput = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Deskripsikan pergerakan kamera, subjek, atau latar belakang...") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                isError = errorMessage != null
            )

            errorMessage?.let { err ->
                Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Prompt Suggestions
            Text("Contoh Prompt Video:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sampleTextPrompts) { prompt ->
                    SuggestionChip(
                        onClick = { promptInput = prompt },
                        label = { Text(prompt, fontSize = 11.sp) }
                    )
                }
            }

            // Progress & WorkManager Status Display
            if (isGenerating) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(progressStatus, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("${(progressVal * 100).toInt()}%", fontWeight = FontWeight.Bold)
                        }

                        LinearProgressIndicator(
                            progress = { progressVal },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Background Task (WorkManager)", fontSize = 11.sp, color = Color.Gray)
                            Text("Sisa Waktu: ~${estTimeRemaining}s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            // Generate Button
            Button(
                onClick = {
                    if (promptInput.isBlank()) {
                        errorMessage = "Prompt deskripsi video wajib diisi."
                        return@Button
                    }
                    isGenerating = true
                    generatedVideoItem = null
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            val request = VideoGenerateRequest(
                                mode = selectedGenMode,
                                prompt = promptInput,
                                durationSeconds = videoDurationSec,
                                sourceImageName = if (selectedGenMode == VideoGenMode.IMAGE_TO_VIDEO) samplePhotos[selectedPhotoIndex].second else null
                            )

                            val result = AiGatewayEngine.processVideoGeneration(request) { status, progress, estSec ->
                                progressStatus = status
                                progressVal = progress
                                estTimeRemaining = estSec
                            }

                            generatedVideoItem = result
                            currentPlaybackSec = 0f
                            isPlaying = true
                            Toast.makeText(context, "Video AI ${videoDurationSec}s berhasil digenerasi!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Gagal memproses Video AI."
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generasi Frame Video AI...")
                } else {
                    Icon(Icons.Filled.Videocam, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Video AI (${videoDurationSec} Detik)", fontWeight = FontWeight.Bold)
                }
            }

            // Built-in Video Player Preview
            generatedVideoItem?.let { item ->
                Text("3. Preview Hasil Video AI", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Simulated Animated Video Frame background
                        val gradientPhase = (currentPlaybackSec * 50).toInt() % 360
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF004D40),
                                            Color(0xFF006064),
                                            Color(0xFF311B92)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Movie,
                                    contentDescription = null,
                                    tint = Color.Cyan,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    item.prompt,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Format: MP4 • 1080p @ 30fps • ${item.durationSec}s",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Watermark Overlay
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Generated • Super Tools", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Playback Controls Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                                .padding(12.dp)
                        ) {
                            Column {
                                // Seekbar
                                LinearProgressIndicator(
                                    progress = { currentPlaybackSec / item.durationSec.toFloat() },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.Cyan,
                                    trackColor = Color.Gray.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { isPlaying = !isPlaying },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }

                                    Text(
                                        "${String.format("%.1f", currentPlaybackSec)}s / ${item.durationSec}.0s",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Video Result Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Video disalin ke Galeri & Project Maker Studio!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan MP4", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Bagikan video AI...", Toast.LENGTH_SHORT).show()
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

            // Safety Disclaimer Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Gavel, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Disclaimer: Hasil AI generatif dapat bervariasi. Pengguna bertanggung jawab atas kepatuhan hak cipta & hak subjek foto.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
