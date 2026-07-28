package com.example.ui.aimedia

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPromptStudioScreen(
    onBack: () -> Unit,
    onUsePrompt: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedCategory by remember { mutableStateOf("Enhancer") }
    var inputPrompt by remember { mutableStateOf("") }
    var enhancedPrompt by remember { mutableStateOf("") }

    val categories = listOf("Enhancer", "Generator", "Templates", "Favorites & History")

    val templates = listOf(
        Pair("Cinematic Photography", "A cinematic 8k portrait of [SUBJECT], soft studio lighting, shallow depth of field, 35mm lens, award-winning photography."),
        Pair("Cyberpunk Sci-Fi", "Futuristic cyberpunk cityscape with neon reflections on wet asphalt, [SUBJECT] in the center, volumetric fog, Octane render."),
        Pair("Anime Masterpiece", "Makoto Shinkai style anime illustration, sky filled with cumulus clouds and vibrant sunset colors, [SUBJECT], detailed anime artwork."),
        Pair("3D Cartoon Avatar", "Cute 3D Pixar-style character render of [SUBJECT], vibrant colors, smooth textures, studio backdrop, isometric view.")
    )

    val favorites = remember {
        mutableStateListOf(
            "Ultra detailed 8k photography of a golden retriever wearing a space helmet, mars background",
            "Oil painting on canvas of a serene mountain lake at sunrise, thick brush strokes"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Prompt AI Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Prompt Generator, Enhancer & Templates", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScrollableTabRow(selectedTabIndex = categories.indexOf(selectedCategory)) {
                categories.forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = { Text(cat, fontSize = 13.sp) }
                    )
                }
            }

            when (selectedCategory) {
                "Enhancer" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Sempurnakan Prompt Anda", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            placeholder = { Text("Ketik ide dasar prompt, misal: 'kucing di luar angkasa'") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (inputPrompt.isNotBlank()) {
                                    enhancedPrompt = "Masterpiece, ultra detailed, 8k resolution, cinematic lighting, dramatic atmospheric depth, intricate textures, masterpiece artwork: " + inputPrompt
                                    Toast.makeText(context, "Prompt berhasil ditingkatkan!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sempurnakan dengan AI Enhancer")
                        }

                        if (enhancedPrompt.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Hasil Prompt Ditingkatkan:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                    Text(enhancedPrompt, style = MaterialTheme.typography.bodyMedium)

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(enhancedPrompt))
                                                Toast.makeText(context, "Disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Salin")
                                        }

                                        Button(
                                            onClick = {
                                                onUsePrompt(enhancedPrompt)
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Gunakan")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Generator" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("AI Smart Prompt Generator", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Pilih elemen untuk membuat prompt otomatis:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        val subjects = listOf("Cyberpunk Warrior", "Cute Fantasy Dragon", "Futuristic Supercar", "Ancient Temple")
                        val styles = listOf("Unreal Engine 5 Render", "Studio Photography", "Impressionist Oil Painting", "Retro Pixel Art")

                        var selSub by remember { mutableStateOf(subjects[0]) }
                        var selStyle by remember { mutableStateOf(styles[0]) }

                        Text("Subjek Utama:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            subjects.forEach { s ->
                                FilterChip(selected = selSub == s, onClick = { selSub = s }, label = { Text(s, fontSize = 11.sp) })
                            }
                        }

                        Text("Gaya Visual:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            styles.forEach { st ->
                                FilterChip(selected = selStyle == st, onClick = { selStyle = st }, label = { Text(st, fontSize = 11.sp) })
                            }
                        }

                        val generated = "A high quality $selStyle depicting $selSub, cinematic volumetric lighting, 8k resolution, highly intricate details."

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Prompt Hasil Generasi:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                Text(generated, style = MaterialTheme.typography.bodyMedium)

                                Button(
                                    onClick = { onUsePrompt(generated) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gunakan Prompt Ini")
                                }
                            }
                        }
                    }
                }

                "Templates" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(templates) { template ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(template.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(template.second, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    TextButton(onClick = { onUsePrompt(template.second) }) {
                                        Text("Gunakan Template")
                                    }
                                }
                            }
                        }
                    }
                }

                "Favorites & History" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(favorites) { fav ->
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
                                    IconButton(onClick = { onUsePrompt(fav) }) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
