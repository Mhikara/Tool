package com.example.ui.tools

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaToolInteractiveProcessor(
    tool: Tool,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Common Processing States
    var isProcessing by remember { mutableStateOf(false) }
    var progressVal by remember { mutableFloatStateOf(0f) }
    var stepText by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dynamic Parameter States
    var textInput by remember { mutableStateOf("") }
    var secTextInput by remember { mutableStateOf("") }
    var qualityFactor by remember { mutableFloatStateOf(85f) }
    var resolutionSelect by remember { mutableStateOf("1080p") }
    var outputFormat by remember { mutableStateOf("PNG") }
    var scaleFactor by remember { mutableStateOf("4x") }
    var speedVal by remember { mutableFloatStateOf(1.0f) }
    var selectedColor by remember { mutableStateOf(Color(0xFF6200EE)) }
    var subtitleLanguage by remember { mutableStateOf("Bahasa Indonesia") }
    var volumeLevel by remember { mutableFloatStateOf(150f) }
    var equalizerPreset by remember { mutableStateOf("Pop") }
    var isChecked by remember { mutableStateOf(true) }

    // Waveform simulation state for Audio tools
    var audioPlayProgress by remember { mutableFloatStateOf(0f) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    // QR Configuration
    var qrType by remember { mutableStateOf("URL") }

    // Generate output attributes when finished
    var outputFileName by remember { mutableStateOf("") }
    var outputSize by remember { mutableStateOf("") }

    val formatsList = when (tool.categoryId) {
        "image" -> listOf("PNG", "JPG", "WEBP", "PDF")
        "video" -> listOf("MP4", "MKV", "AVI", "WEBM")
        "audio" -> listOf("MP3", "WAV", "M4A", "FLAC")
        "pdf" -> listOf("PDF")
        "qr" -> listOf("PNG", "SVG", "JPG")
        "ai" -> listOf("PNG", "MP4", "MP3", "WEBP")
        else -> listOf("PNG", "JPG", "WEBP", "PDF", "MP4", "MP3", "ZIP")
    }

    LaunchedEffect(tool) {
        // Set up initial values based on tool
        outputFormat = formatsList.firstOrNull() ?: "PNG"
        if (tool.id.contains("qr")) {
            textInput = "https://meidi.studio"
        } else if (tool.id.contains("caption")) {
            textInput = "Tulis topik konten Anda di sini untuk caption"
        } else if (tool.id.contains("dl") || tool.id.contains("download")) {
            textInput = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tool.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Meydi Media Tools Suite v1.0.0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Tool ditambahkan ke Favorit!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Star, contentDescription = "Favorit", tint = Color(0xFFFFC107))
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
            // Live Interactive Preview Canvas Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151522))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        tool.categoryId == "image" || tool.id.contains("img") || tool.id.contains("photo") -> {
                            // Image Tool Interactive Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            listOf(selectedColor, Color(0xFF0F0F1A))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        tool.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        "Interactive Image Editor",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Filter/Color: ${selectedColor.value.toString(16).take(8).uppercase()}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }

                        tool.categoryId == "video" || tool.id.contains("vid") -> {
                            // Video Tool Live Player Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(8.dp)
                                    ) {
                                        Text("Preview: ${tool.name} ($resolutionSelect)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { audioPlayProgress },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = Color.Red,
                                            trackColor = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        tool.categoryId == "audio" || tool.id.contains("aud") -> {
                            // Audio Tool Live Waveform Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0D0D16)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        val width = size.width
                                        val height = size.height
                                        val points = 30
                                        val step = width / points
                                        val path = Path()
                                        path.moveTo(0f, height / 2)
                                        for (i in 0..points) {
                                            val x = i * step
                                            val amp = if (isAudioPlaying) Random.nextFloat() * 40f + 10f else 15f
                                            val y = (height / 2) + Math.sin(i * 0.5 + audioPlayProgress * 20) * amp
                                            path.lineTo(x, y.toFloat())
                                        }
                                        drawPath(path, color = Color.Cyan, style = Stroke(width = 3.dp.toPx()))
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        IconButton(onClick = {
                                            isAudioPlaying = !isAudioPlaying
                                            scope.launch {
                                                while (isAudioPlaying) {
                                                    audioPlayProgress = (audioPlayProgress + 0.05f) % 1.0f
                                                    delay(50)
                                                }
                                            }
                                        }) {
                                            Icon(
                                                if (isAudioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.Cyan,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                        Text("Codec: ${outputFormat} • Booster: ${volumeLevel.toInt()}%", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        tool.categoryId == "qr" || tool.id.contains("qr") -> {
                            // QR Tool Live QR Vector Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Canvas(modifier = Modifier.size(100.dp)) {
                                        // Draw simulated beautiful QR Code pattern
                                        val boxSize = size.width / 3.5f
                                        drawRect(Color.Black, Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(boxSize, boxSize))
                                        drawRect(Color.White, Offset(5f, 5f), size = androidx.compose.ui.geometry.Size(boxSize - 10f, boxSize - 10f))
                                        drawRect(Color.Black, Offset(10f, 10f), size = androidx.compose.ui.geometry.Size(boxSize - 20f, boxSize - 20f))

                                        drawRect(Color.Black, Offset(size.width - boxSize, 0f), size = androidx.compose.ui.geometry.Size(boxSize, boxSize))
                                        drawRect(Color.White, Offset(size.width - boxSize + 5f, 5f), size = androidx.compose.ui.geometry.Size(boxSize - 10f, boxSize - 10f))
                                        drawRect(Color.Black, Offset(size.width - boxSize + 10f, 10f), size = androidx.compose.ui.geometry.Size(boxSize - 20f, boxSize - 20f))

                                        drawRect(Color.Black, Offset(0f, size.height - boxSize), size = androidx.compose.ui.geometry.Size(boxSize, boxSize))
                                        drawRect(Color.White, Offset(5f, size.height - boxSize + 5f), size = androidx.compose.ui.geometry.Size(boxSize - 10f, boxSize - 10f))
                                        drawRect(Color.Black, Offset(10f, size.height - boxSize + 10f), size = androidx.compose.ui.geometry.Size(boxSize - 20f, boxSize - 20f))

                                        // Random code pixels inside
                                        for (x in 2..8) {
                                            for (y in 2..8) {
                                                if (x > 5 && y > 5) continue
                                                if (Random.nextBoolean()) {
                                                    drawRect(
                                                        Color.Black,
                                                        Offset(x * size.width / 10f, y * size.height / 10f),
                                                        size = size / 10f
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(textInput.take(30) + if (textInput.length > 30) "..." else "", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        else -> {
                            // Default beautiful layout
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1E1E2F), Color(0xFF11111E))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                    Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(tool.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Status: Siap Memproses Data", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Watermark & Quality badge overlay
                    Surface(
                        color = Color.Black.copy(alpha = 0.62f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            "High Definition Engine v1.0.0",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.Cyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive Forms Based On Category
            Text("Konfigurasi Parameter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            when {
                tool.categoryId == "qr" || tool.id.contains("qr") -> {
                    // QR Custom Field Group
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("URL", "WiFi", "WhatsApp", "Email", "Text").forEach { type ->
                            FilterChip(
                                selected = qrType == type,
                                onClick = {
                                    qrType = type
                                    textInput = when (type) {
                                        "URL" -> "https://meidi.studio"
                                        "WiFi" -> "WIFI:S:MeydiOffice;T:WPA;P:MeydiSecure2026;;"
                                        "WhatsApp" -> "https://wa.me/6281234567890"
                                        "Email" -> "mailto:meydihikara@gmail.com"
                                        else -> "Halo, salam dari Meydi Studio!"
                                    }
                                },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Isi / Data QR Code") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                tool.categoryId == "image" || tool.id.contains("img") -> {
                    // Image Parameters
                    if (tool.id.contains("compress") || tool.id.contains("resize")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kualitas Kompresi (Quality)", fontSize = 13.sp)
                            Text("${qualityFactor.toInt()}%", fontWeight = FontWeight.Bold)
                        }
                        Slider(value = qualityFactor, onValueChange = { qualityFactor = it }, valueRange = 10f..100f)
                    }

                    if (tool.id.contains("upscale") || tool.id.contains("hd")) {
                        Text("Skala Upscale:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("2x", "4x", "8x").forEach { factor ->
                                FilterChip(
                                    selected = scaleFactor == factor,
                                    onClick = { scaleFactor = factor },
                                    label = { Text(factor) }
                                )
                            }
                        }
                    }

                    Text("Warna Aksen / Filter:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(Color(0xFF6200EE), Color(0xFFE91E63), Color(0xFF009688), Color(0xFF03A9F4), Color(0xFFFF9800)).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = if (selectedColor == color) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }

                tool.categoryId == "video" || tool.id.contains("vid") -> {
                    // Video Parameters
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("480p", "720p", "1080p", "4K").forEach { res ->
                            FilterChip(
                                selected = resolutionSelect == res,
                                onClick = { resolutionSelect = res },
                                label = { Text(res) }
                            )
                        }
                    }

                    if (tool.id.contains("speed")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kecepatan Putar (Speed)", fontSize = 13.sp)
                            Text("${String.format("%.2f", speedVal)}x", fontWeight = FontWeight.Bold)
                        }
                        Slider(value = speedVal, onValueChange = { speedVal = it }, valueRange = 0.25f..4.0f)
                    }

                    if (tool.id.contains("sub")) {
                        OutlinedTextField(
                            value = subtitleLanguage,
                            onValueChange = { subtitleLanguage = it },
                            label = { Text("Bahasa Subtitle") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                tool.categoryId == "audio" || tool.id.contains("aud") -> {
                    // Audio Parameters
                    if (tool.id.contains("volume")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amplifikasi Volume (Booster)", fontSize = 13.sp)
                            Text("${volumeLevel.toInt()}%", fontWeight = FontWeight.Bold)
                        }
                        Slider(value = volumeLevel, onValueChange = { volumeLevel = it }, valueRange = 100f..500f)
                    }

                    if (tool.id.contains("eq")) {
                        Text("Equalizer Preset:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Flat", "Pop", "Rock", "Jazz", "Classical", "Bass Boost").forEach { preset ->
                                FilterChip(
                                    selected = equalizerPreset == preset,
                                    onClick = { equalizerPreset = preset },
                                    label = { Text(preset) }
                                )
                            }
                        }
                    }
                }

                tool.id.contains("dl") || tool.id.contains("download") -> {
                    // Downloader Parameters
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Tautan Link Video / Audio") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                textInput = "https://www.tiktok.com/@meidi/video/12345"
                            }) {
                                Icon(Icons.Filled.ContentPaste, contentDescription = "Paste")
                            }
                        }
                    )
                }

                tool.categoryId == "pdf" || tool.id.contains("pdf") -> {
                    // PDF Custom Fields
                    if (tool.id.contains("encrypt") || tool.id.contains("lock")) {
                        OutlinedTextField(
                            value = secTextInput,
                            onValueChange = { secTextInput = it },
                            label = { Text("Kata Sandi Enkripsi (Password)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text("Judul Dokumen PDF Baru") },
                            placeholder = { Text("Meydi_Studio_Doc") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                tool.categoryId == "ai" || tool.id.contains("ai") -> {
                    // AI content creation inputs
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Tulis instruksi / prompt untuk model AI") },
                        placeholder = { Text("Seni futuristik, cyberpunk, 8k, ultra-realistic...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Export Options Selector Group
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Opsi Format Ekspor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Format Output:", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            formatsList.forEach { fmt ->
                                FilterChip(
                                    selected = outputFormat == fmt,
                                    onClick = { outputFormat = fmt },
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
                        Text("Watermark Hak Cipta:", fontSize = 12.sp)
                        Switch(checked = isChecked, onCheckedChange = { isChecked = it })
                    }
                }
            }

            // Processing Progress Indicator
            if (isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
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
                            Text(stepText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("${(progressVal * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        }
                        LinearProgressIndicator(
                            progress = { progressVal },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Output Results Block
            if (isCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Proses Selesai Berhasil!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Divider()
                        Text("Nama File: $outputFileName", fontSize = 12.sp)
                        Text("Ukuran File: $outputSize", fontSize = 12.sp)
                        Text("Format Target: $outputFormat", fontSize = 12.sp)
                        if (tool.id.contains("ocr")) {
                            Text("Hasil Teks OCR:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("MEIDI STUDIO TECHNOLOGY INDONESIA\nVersion: 1.0.0\nStatus: Terverifikasi", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            // Execute processing action
            Button(
                onClick = {
                    isProcessing = true
                    isCompleted = false
                    errorMessage = null

                    scope.launch {
                        try {
                            stepText = "Membuka media source streams..."
                            progressVal = 0.15f
                            delay(500)

                            stepText = "Melakukan decoding & parsing metadata..."
                            progressVal = 0.4f
                            delay(500)

                            stepText = "Menerapkan engine pemrosesan ${tool.name}..."
                            progressVal = 0.75f
                            delay(600)

                            stepText = "Melakukan encoding & rendering ke $outputFormat..."
                            progressVal = 0.92f
                            delay(400)

                            progressVal = 1.0f
                            isCompleted = true

                            // Calculate mock file sizes
                            outputFileName = "meydi_studio_${tool.id}_${System.currentTimeMillis() % 100000}.$outputFormat".lowercase()
                            outputSize = when (tool.categoryId) {
                                "image" -> "${(Random.nextInt(500, 3500) / 100f)} MB"
                                "video" -> "${(Random.nextInt(15, 120))} MB"
                                "audio" -> "${(Random.nextInt(2, 12))} MB"
                                else -> "452 KB"
                            }

                            Toast.makeText(context, "${tool.name} berhasil dieksekusi!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Kesalahan pemrosesan sistem."
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
                    Text("Memproses Media...")
                } else {
                    Icon(Icons.Filled.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jalankan ${tool.name}", fontWeight = FontWeight.Bold)
                }
            }

            // Actions Block (Save, Share, Copy) when done
            if (isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Selesai menyimpan $outputFileName ke Direktori!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Hasil Media Tool: $outputFileName ($outputSize). Diproses secara instan di Meydi Media Tools Suite!")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan hasil"))
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
