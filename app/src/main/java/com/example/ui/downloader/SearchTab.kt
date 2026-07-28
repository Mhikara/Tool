package com.example.ui.downloader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTab(viewModel: DownloaderViewModel) {
    val searchState by viewModel.searchState.collectAsState()
    var selectedResultForDownload by remember { mutableStateOf<SearchResultItem?>(null) }
    var showDownloadSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchState.query,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ketik judul video/lagu...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        // Platform Filter
        ScrollableTabRow(
            selectedTabIndex = 0,
            edgePadding = 0.dp,
            indicator = {},
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            val platforms = listOf("Semua", "YouTube", "Instagram", "TikTok", "X (Twitter)")
            platforms.forEach { platform ->
                FilterChip(
                    selected = searchState.selectedPlatforms.contains(platform),
                    onClick = { viewModel.toggleSearchPlatform(platform) },
                    label = { Text(platform) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // Content Type Filter
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val types = listOf("Semua", "Video", "Music")
            types.forEach { type ->
                FilterChip(
                    selected = searchState.selectedContentType == type,
                    onClick = { viewModel.onSearchContentTypeChange(type) },
                    label = { Text(type) }
                )
            }
        }

        // Results List
        if (searchState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (searchState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(searchState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }
        } else if (searchState.results.isEmpty() && searchState.query.isNotBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ditemukan. Coba kata kunci lain atau tempel link langsung.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchState.results) { result ->
                    SearchResultCard(result) {
                        selectedResultForDownload = result
                        showDownloadSheet = true
                    }
                }
            }
        }
    }

    if (showDownloadSheet && selectedResultForDownload != null) {
        ModalBottomSheet(onDismissRequest = { showDownloadSheet = false }) {
            DownloadOptionsSheet(
                item = selectedResultForDownload!!,
                onDownload = { type, quality ->
                    showDownloadSheet = false
                    // Simulate setting the URL in the downloader tab and starting download
                    val dummyUrl = "https://${selectedResultForDownload!!.platform.lowercase()}.com/watch?v=${selectedResultForDownload!!.id}"
                    viewModel.onUrlChange(dummyUrl)
                    viewModel.onFormatChange(if (type == "Music") "Audio" else "Video")
                    viewModel.onResolutionChange(quality)
                    // In a real flow we might auto-switch tabs or just start downloading
                    viewModel.startDownload()
                }
            )
        }
    }
}

@Composable
fun SearchResultCard(item: SearchResultItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = "Thumbnail",
                modifier = Modifier
                    .size(120.dp, 80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.platform,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.durationOrArtist, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.channelName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DownloadOptionsSheet(item: SearchResultItem, onDownload: (type: String, quality: String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Download: ${item.title}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        
        Text("Pilih Jenis Unduhan:", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { onDownload("Video", "1080p") }, modifier = Modifier.weight(1f)) {
                Text("Video (1080p)")
            }
            Button(onClick = { onDownload("Video", "720p") }, modifier = Modifier.weight(1f)) {
                Text("Video (720p)")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onDownload("Music", "High") }, 
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Audio (MP3 320kbps)")
            }
            Button(
                onClick = { onDownload("Music", "Normal") }, 
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Audio (MP3 128kbps)")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
