package com.example.ui.maker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakerScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maker Studio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                StudioHeroSection()
            }

            item {
                RecentDraftsSection()
            }

            val categories = getMakerCategories()
            categories.forEach { category ->
                item {
                    CategorySection(
                        title = category.title,
                        icon = category.icon,
                        items = category.items,
                        onItemClick = { itemRoute ->
                            onNavigate(itemRoute)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudioHeroSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "What will you make today?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unleash your creativity with Meydi's HD Maker Tools.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun RecentDraftsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        PaddingValues(horizontal = 16.dp).let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Drafts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { /* View All */ }) {
                    Text("View All")
                }
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) { index ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.InsertDriveFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Draft ${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySection(title: String, icon: ImageVector, items: List<MakerItem>, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                MakerItemCard(item = item, onClick = { onItemClick(item.route) })
            }
        }
    }
}

@Composable
fun MakerItemCard(item: MakerItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

data class MakerCategory(
    val title: String,
    val icon: ImageVector,
    val items: List<MakerItem>
)

data class MakerItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

fun getMakerCategories(): List<MakerCategory> {
    return listOf(
        MakerCategory(
            title = "AI Maker",
            icon = Icons.Outlined.AutoAwesome,
            items = listOf(
                MakerItem("AI Image", Icons.Filled.Image, "ai_image"),
                MakerItem("AI Logo", Icons.Filled.Brush, "ai_logo"),
                MakerItem("AI Avatar", Icons.Filled.Face, "ai_avatar"),
                MakerItem("AI Prompt", Icons.Filled.Chat, "ai_prompt")
            )
        ),
        MakerCategory(
            title = "AI Maker Studio",
            icon = Icons.Outlined.AutoAwesome,
            items = listOf(
                MakerItem("Edit Foto AI", Icons.Filled.AutoFixHigh, "ai_image_edit"),
                MakerItem("Video AI (1-10s)", Icons.Filled.Videocam, "ai_video_generate"),
                MakerItem("AI Studio Hub", Icons.Filled.AutoAwesome, "ai_media_hub"),
                MakerItem("AI Coding", Icons.Filled.Code, "ai")
            )
        ),
        MakerCategory(
            title = "Image Maker",
            icon = Icons.Outlined.Image,
            items = listOf(
                MakerItem("Image Studio", Icons.Filled.Image, "imagemaker"),
                MakerItem("Logo", Icons.Filled.Brush, "imagemaker"),
                MakerItem("Banner", Icons.Filled.ViewCarousel, "imagemaker"),
                MakerItem("Thumbnail", Icons.Filled.ImageAspectRatio, "imagemaker")
            )
        ),
        MakerCategory(
            title = "Social Media",
            icon = Icons.Outlined.Share,
            items = listOf(
                MakerItem("Social Studio", Icons.Filled.Share, "social_maker"),
                MakerItem("IG Post", Icons.Filled.CameraAlt, "social_maker"),
                MakerItem("IG Story", Icons.Filled.HistoryToggleOff, "social_maker"),
                MakerItem("YT Banner", Icons.Filled.VideoLabel, "social_maker")
            )
        ),
        MakerCategory(
            title = "Text Maker",
            icon = Icons.Outlined.TextFields,
            items = listOf(
                MakerItem("Text Studio", Icons.Filled.TextFields, "text_maker"),
                MakerItem("Quote", Icons.Filled.FormatQuote, "text_maker"),
                MakerItem("Fancy Text", Icons.Filled.TextFormat, "text_maker"),
                MakerItem("Bio Gen", Icons.Filled.PersonOutline, "text_maker")
            )
        ),
        MakerCategory(
            title = "QR Maker",
            icon = Icons.Outlined.QrCode,
            items = listOf(
                MakerItem("QR Studio", Icons.Filled.QrCode2, "qr_maker"),
                MakerItem("QR WiFi", Icons.Filled.Wifi, "qr_maker"),
                MakerItem("QR Contact", Icons.Filled.Contacts, "qr_maker")
            )
        ),
        MakerCategory(
            title = "Document Maker",
            icon = Icons.Outlined.Description,
            items = listOf(
                MakerItem("PDF Creator", Icons.Filled.PictureAsPdf, "pdf_creator"),
                MakerItem("Invoice", Icons.Filled.Receipt, "invoice_maker"),
                MakerItem("Resume", Icons.Filled.WorkOutline, "resume_maker")
            )
        )
    )
}
