package com.example.ui.aimedia

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
fun AiTextToImageScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var promptText by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Realistic") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedQuality by remember { mutableStateOf("Full HD") }
    var selectedExportFormat by remember { mutableStateOf("PNG") }

    var isGenerating by remember { mutableStateOf(false) }
    var progressVal by remember { mutableFloatStateOf(0f) }
    var progressStatus by remember { mutableStateOf("") }
    var generatedImageItem by remember { mutableStateOf<MediaProjectItem?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val styleOptions = listOf("Realistic", "Anime", "3D", "Cartoon", "Chibi", "Sketch", "Pixel Art", "Painting", "Cyberpunk", "Fantasy")
    val aspectRatios = listOf("1:1", "3:4", "4:3", "9:16", "16:9")
    val qualityOptions = listOf("HD", "Full HD", "Ultra HD")
    val formatOptions = listOf("PNG", "JPG", "WEBP")

    val samplePrompts = listOf(
        "Pemandangan hutan ajaib berpendar neon di malam hari, resolusi ultra tinggi",
        "Karakter samurai cybernetic berdiri di atas gedung pencakar langit bernuansa hujan hujan neon",
        "Kucing astronom menjelajahi galaksi dengan baju ruang angkasa fiksi ilmiah",
        "Studio portrait profesional seorang astronot berlatar belakang planet mars"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Text-to-Image Generator", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        Text("AI Image Studio • Meydi", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
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
            // Prompt Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Deskripsi Gambar (Prompt)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = {
                            if (promptText.isNotBlank()) {
                                promptText = "Highly detailed, cinematic lighting, 8k resolution, masterpiece, " + promptText
                                Toast.makeText(context, "Prompt Auto-Enhanced!", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enhance AI", fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tuliskan deskripsi gambar yang ingin dibuat...") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Rekomendasi Prompt:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(samplePrompts) { prompt ->
                            SuggestionChip(
                                onClick = { promptText = prompt },
                                label = { Text(prompt.take(30) + "...", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Style Preset Selection
            Text("1. Pilih Gaya Gambar (Style Preset)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(styleOptions) { style ->
                    val isSelected = selectedStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStyle = style },
                        label = { Text(style, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Aspect Ratio & Quality Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("2. Rasio & Kualitas Output", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rasio Aspek:", fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            aspectRatios.forEach { ratio ->
                                FilterChip(
                                    selected = selectedAspectRatio == ratio,
                                    onClick = { selectedAspectRatio = ratio },
                                    label = { Text(ratio, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kualitas:", fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            qualityOptions.forEach { q ->
                                FilterChip(
                                    selected = selectedQuality == q,
                                    onClick = { selectedQuality = q },
                                    label = { Text(q, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Format Ekspor:", fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            formatOptions.forEach { fmt ->
                                FilterChip(
                                    selected = selectedExportFormat == fmt,
                                    onClick = { selectedExportFormat = fmt },
                                    label = { Text(fmt, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Progress Bar
            if (isGenerating) {
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
                        errorMessage = "Prompt deskripsi gambar tidak boleh kosong."
                        return@Button
                    }
                    isGenerating = true
                    generatedImageItem = null
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            progressStatus = "Memulai generasi ($selectedStyle)..."
                            progressVal = 0.2f
                            delay(600)

                            progressStatus = "Memproses Diffusion Model ($selectedAspectRatio)..."
                            progressVal = 0.6f
                            delay(800)

                            progressStatus = "Menerapkan Upscale & Watermark ($selectedQuality)..."
                            progressVal = 0.9f
                            delay(600)

                            val project = MediaProjectItem(
                                title = "T2I: " + promptText.take(20) + "...",
                                type = MediaType.EDITED_IMAGE,
                                prompt = "[$selectedStyle, $selectedAspectRatio, $selectedQuality] $promptText",
                                mediaUrl = "generated_t2i_" + System.currentTimeMillis()
                            )
                            generatedImageItem = project
                            progressStatus = "Selesai!"
                            progressVal = 1.0f
                            Toast.makeText(context, "Gambar AI berhasil dibuat!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Gagal membuat gambar."
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isGenerating,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generasi Gambar...")
                } else {
                    Icon(Icons.Filled.Brush, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Gambar AI ($selectedStyle)", fontWeight = FontWeight.Bold)
                }
            }

            // Result Preview
            generatedImageItem?.let { item ->
                Text("3. Hasil Gambar AI", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4A148C), Color(0xFF006064), Color(0xFF1B5E20))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Gaya: $selectedStyle • $selectedAspectRatio • $selectedQuality", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.prompt, color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Watermark • $selectedExportFormat", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, "Disimpan sebagai $selectedExportFormat ke Galeri!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan $selectedExportFormat", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Membuka dialog bagikan...", Toast.LENGTH_SHORT).show() },
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
