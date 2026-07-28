package com.example.ui.spotify

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyScreen(onBack: () -> Unit) {
    val viewModel: SpotifyViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spotify Viewer") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopPreview()
                        onBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.urlInput,
                    onValueChange = viewModel::onUrlChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Paste Spotify Link") },
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
            }
            
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (state.track != null) {
                item {
                    TrackDetailsCard(
                        track = state.track!!,
                        isPlaying = state.isPlayingPreview,
                        onPlayToggle = viewModel::togglePreviewPlay,
                        onOpenSpotify = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.urlInput))
                            context.startActivity(intent)
                        }
                    )
                }
            } else if (state.album != null) {
                item {
                    AlbumDetailsCard(
                        album = state.album!!,
                        onOpenSpotify = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.urlInput))
                            context.startActivity(intent)
                        }
                    )
                }
            } else if (state.artist != null) {
                item {
                    ArtistDetailsCard(
                        artist = state.artist!!,
                        onOpenSpotify = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.urlInput))
                            context.startActivity(intent)
                        }
                    )
                }
            } else if (state.playlist != null) {
                item {
                    PlaylistDetailsCard(
                        playlist = state.playlist!!,
                        onOpenSpotify = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.urlInput))
                            context.startActivity(intent)
                        }
                    )
                }
            } else {
                item {
                    EmptySpotifyState()
                }
            }
        }
    }
    
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Spotify API Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your Spotify Developer Credentials to fetch data.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = state.clientId,
                        onValueChange = viewModel::onClientIdChange,
                        label = { Text("Client ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.clientSecret,
                        onValueChange = viewModel::onClientSecretChange,
                        label = { Text("Client Secret") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showSettings = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun EmptySpotifyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF1DB954)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Smart Link Detection",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Paste any Spotify Track link above to view metadata, listen to a 30s preview, and more.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun TrackDetailsCard(
    track: com.example.data.api.spotify.SpotifyTrack,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onOpenSpotify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            val imageUrl = track.album.images.firstOrNull()?.url
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Album Cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artists.joinToString { it.name },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Album", style = MaterialTheme.typography.labelMedium)
                        Text(track.album.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Duration", style = MaterialTheme.typography.labelMedium)
                        val seconds = track.duration_ms / 1000
                        val minutes = seconds / 60
                        val remSeconds = seconds % 60
                        Text(String.format("%d:%02d", minutes, remSeconds), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (track.preview_url != null) {
                        FilledTonalButton(
                            onClick = onPlayToggle,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = "Play Preview"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPlaying) "Stop" else "Preview")
                        }
                    }
                    
                    Button(
                        onClick = onOpenSpotify,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = "Open")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Spotify")
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumDetailsCard(
    album: com.example.data.api.spotify.SpotifyAlbum,
    onOpenSpotify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            val imageUrl = album.images.firstOrNull()?.url
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Album Cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Release Date: ${album.release_date ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOpenSpotify,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = "Open")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in Spotify")
                }
            }
        }
    }
}

@Composable
fun ArtistDetailsCard(
    artist: com.example.data.api.spotify.SpotifyArtist,
    onOpenSpotify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            val imageUrl = artist.images?.firstOrNull()?.url
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Artist Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Followers", style = MaterialTheme.typography.labelMedium)
                        Text("${artist.followers?.total ?: 0}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    if (artist.genres?.isNotEmpty() == true) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Top Genre", style = MaterialTheme.typography.labelMedium)
                            Text(artist.genres.first(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOpenSpotify,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = "Open")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in Spotify")
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailsCard(
    playlist: com.example.data.api.spotify.SpotifyPlaylist,
    onOpenSpotify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            val imageUrl = playlist.images?.firstOrNull()?.url
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Playlist Cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!playlist.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = playlist.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Created by", style = MaterialTheme.typography.labelMedium)
                        Text(playlist.owner?.display_name ?: "Unknown", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Tracks", style = MaterialTheme.typography.labelMedium)
                        Text("${playlist.tracks?.total ?: 0}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOpenSpotify,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = "Open")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in Spotify")
                }
            }
        }
    }
}
