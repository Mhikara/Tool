package com.example.ui.downloader

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(onBack: () -> Unit, viewModel: DownloaderViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Download", "Search", "Manager", "History", "Convert")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Super Media Downloader") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTabIndex) {
                0 -> DownloaderTab(state, viewModel, clipboard)
                1 -> SearchTab(viewModel)
                2 -> ManagerTab()
                3 -> HistoryTab()
                4 -> ConvertTab()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderTab(state: DownloaderState, viewModel: DownloaderViewModel, clipboard: ClipboardManager) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.url,
            onValueChange = viewModel::onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Paste Video/Image URL here") },
            trailingIcon = {
                IconButton(onClick = {
                    val clipData = clipboard.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val text = clipData.getItemAt(0).text?.toString() ?: ""
                        viewModel.onUrlChange(text)
                    }
                }) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = "Paste from Clipboard")
                }
            },
            singleLine = true
        )

        if (state.detectedPlatform.isNotEmpty()) {
            Text(
                text = "Platform: ${state.detectedPlatform}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text("Select Format", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Video", "Audio", "Photo", "GIF").forEach { format ->
                FilterChip(
                    selected = state.selectedFormat == format,
                    onClick = { viewModel.onFormatChange(format) },
                    label = { Text(format) }
                )
            }
        }

        Text("Select Resolution", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("480p", "720p", "1080p", "4K").forEach { res ->
                FilterChip(
                    selected = state.selectedResolution == res,
                    onClick = { viewModel.onResolutionChange(res) },
                    label = { Text(res) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        
        if (state.successMessage != null) {
            Text(
                text = state.successMessage ?: "",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (state.isDownloading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Downloading... ${(state.downloadProgress * 100).toInt()}%", 
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.downloadProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Button(
            onClick = viewModel::startDownload,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !state.isDownloading
        ) {
            Icon(Icons.Filled.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download ${state.selectedFormat}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ManagerTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Download Manager & Queue")
    }
}

@Composable
fun HistoryTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Download History")
    }
}

@Composable
fun ConvertTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Media Convert & Trim")
    }
}
