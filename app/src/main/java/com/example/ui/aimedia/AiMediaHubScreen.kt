package com.example.ui.aimedia

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMediaHubScreen(
    onBack: () -> Unit,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }

    val tabs = listOf(
        Triple("Text to Image", Icons.Filled.Brush, 0),
        Triple("Edit Foto AI", Icons.Filled.AutoFixHigh, 1),
        Triple("Video AI (1-10s)", Icons.Filled.Videocam, 2),
        Triple("Prompt Studio", Icons.Filled.AutoAwesome, 3),
        Triple("Project Cache", Icons.Filled.FolderZip, 4)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 12.dp
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTabIndex == tab.third,
                    onClick = { selectedTabIndex = tab.third },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(tab.second, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tab.first, fontSize = 12.sp, fontWeight = if (selectedTabIndex == tab.third) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> AiTextToImageScreen(
                    onBack = onBack
                )
                1 -> AiImageEditScreen(
                    onBack = onBack,
                    onNavigateToVideoGen = { selectedTabIndex = 2 }
                )
                2 -> AiVideoGenerateScreen(
                    onBack = onBack,
                    onNavigateToImageEdit = { selectedTabIndex = 1 }
                )
                3 -> AiPromptStudioScreen(
                    onBack = onBack,
                    onUsePrompt = { prompt ->
                        Toast.makeText(context, "Prompt disalin ke Text to Image!", Toast.LENGTH_SHORT).show()
                        selectedTabIndex = 0
                    }
                )
                4 -> ProjectHistoryTab(onBack = onBack)
            }
        }
    }
}

@Composable
fun ProjectHistoryTab(onBack: () -> Unit) {
    val context = LocalContext.current
    var historyList by remember { mutableStateOf(AiGatewayEngine.getProjectsHistory()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Daftar Draft & Project AI", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Tersimpan di local cache & Cloud Sync", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = {
                historyList = AiGatewayEngine.getProjectsHistory()
                Toast.makeText(context, "Riwayat diperbarui", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada riwayat project AI.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (item.type == MediaType.EDITED_IMAGE) Icons.Filled.Image else Icons.Filled.Movie,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                }
                                Text(item.prompt, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(item.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                            }

                            Row {
                                IconButton(onClick = {
                                    Toast.makeText(context, "Ekspor ${item.title}...", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Filled.Download, contentDescription = null)
                                }
                                IconButton(onClick = {
                                    AiGatewayEngine.deleteProject(item.id)
                                    historyList = AiGatewayEngine.getProjectsHistory()
                                    Toast.makeText(context, "Project dihapus", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
