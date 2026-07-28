package com.example.ui.donation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(onBack: () -> Unit) {
    var selectedMethod by remember { mutableStateOf("QRIS") }
    var showQrisDialog by remember { mutableStateOf(false) }
    var showThankYouDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dukung Pengembangan") },
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HeaderSection()
            }
            
            item {
                RoadmapSection()
            }

            item {
                Text(
                    "Metode Donasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("QRIS", "DANA", "GoPay").forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            item {
                AnimatedVisibility(visible = selectedMethod == "QRIS") {
                    QrisSection(
                        onEnlarge = { showQrisDialog = true },
                        onDone = { showThankYouDialog = true },
                        onSave = { Toast.makeText(context, "QR Disimpan ke Galeri", Toast.LENGTH_SHORT).show() },
                        onShare = { Toast.makeText(context, "Membagikan QR...", Toast.LENGTH_SHORT).show() }
                    )
                }
                
                AnimatedVisibility(visible = selectedMethod == "DANA" || selectedMethod == "GoPay") {
                    EwalletSection(
                        method = selectedMethod,
                        number = "082258371053",
                        onCopy = {
                            val clip = ClipData.newPlainText("E-Wallet Number", "082258371053")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Nomor disalin", Toast.LENGTH_SHORT).show()
                        },
                        onDone = { showThankYouDialog = true }
                    )
                }
            }
            
            item {
                NominalSection()
            }
            
            item {
                HistorySection()
            }
            
            item {
                SupportSection(
                    onContact = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:meydihikara@gmail.com")
                        }
                        context.startActivity(intent)
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    if (showQrisDialog) {
        Dialog(onDismissRequest = { showQrisDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "QRIS Memet store",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showQrisDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Tutup")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.qris_memet_store),
                        contentDescription = "QRIS Memet store - NMID: ID1026509242271",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
    
    if (showThankYouDialog) {
        AlertDialog(
            onDismissRequest = { showThankYouDialog = false },
            icon = {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Terima Kasih!", textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    "🎉 Terima kasih atas dukungan Anda.\n\nKontribusi Anda sangat berarti dan akan membantu pengembangan aplikasi agar menjadi lebih baik.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(onClick = { showThankYouDialog = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Kembali")
                }
            }
        )
    }
}

@Composable
fun HeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "❤️ Dukung Pengembangan Aplikasi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Terima kasih telah menggunakan aplikasi ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RoadmapSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Donasi akan digunakan untuk:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            val items = listOf(
                "Pengembangan fitur baru",
                "Biaya server & API",
                "Perbaikan bug",
                "Peningkatan keamanan & performa"
            )
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun QrisSection(
    onEnlarge: () -> Unit,
    onDone: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onEnlarge() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qris_memet_store),
                    contentDescription = "QRIS Memet store - NMID: ID1026509242271",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Memet store", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("NMID: ID1026509242271", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onEnlarge, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.ZoomIn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Perbesar", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Simpan", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bagikan", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Saya Sudah Donasi")
            }
        }
    }
}

@Composable
fun EwalletSection(
    method: String,
    number: String,
    onCopy: () -> Unit,
    onDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Transfer ke $method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    number,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Saya Sudah Donasi")
            }
        }
    }
}

@Composable
fun NominalSection() {
    Column {
        Text(
            "Nominal Cepat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        val nominals = listOf("Rp10.000", "Rp20.000", "Rp50.000", "Rp100.000")
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nominals.take(2).forEach { nominal ->
                OutlinedButton(
                    onClick = { /* Set nominal to copy/use */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(nominal)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nominals.drop(2).forEach { nominal ->
                OutlinedButton(
                    onClick = { /* Set nominal to copy/use */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(nominal)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Nominal Lainnya") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("Rp", modifier = Modifier.padding(start = 16.dp)) }
        )
    }
}

@Composable
fun HistorySection() {
    Column {
        Text(
            "Riwayat Donasi (Lokal)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mock History
        val history = listOf(
            HistoryItem("24 Jul 2026", "Rp50.000", "Berhasil", "Semangat terus developernya!"),
            HistoryItem("20 Jul 2026", "Rp20.000", "Berhasil", "-")
        )
        
        if (history.isEmpty()) {
            Text(
                "Belum ada riwayat donasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            history.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.amount, fontWeight = FontWeight.Bold)
                                Text(item.date, style = MaterialTheme.typography.bodySmall)
                            }
                            Badge(containerColor = Color(0xFF4CAF50)) {
                                Text(item.status, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                        if (item.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Catatan: ${item.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

data class HistoryItem(val date: String, val amount: String, val status: String, val note: String)

@Composable
fun SupportSection(onContact: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Bantuan & Dukungan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = onContact,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Hubungi Developer")
                }
            }
            TextButton(
                onClick = { /* FAQ Action */ },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Help, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("FAQ Donasi")
                }
            }
        }
    }
}
