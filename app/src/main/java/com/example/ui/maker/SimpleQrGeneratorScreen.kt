package com.example.ui.maker

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.ui.maker.qrmaker.QrGenerator
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleQrGeneratorScreen(
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    // Automatically generate QR whenever textInput changes
    LaunchedEffect(textInput) {
        if (textInput.isNotBlank()) {
            qrBitmap = QrGenerator.generateQrBitmap(textInput, size = 600)
        } else {
            qrBitmap = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate QR Code", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Enter text or URL to generate a QR code instantly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Enter text or URL") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code Preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.QrCode2,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Preview will appear here",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    qrBitmap?.let { bitmap ->
                        try {
                            val cachePath = File(context.cacheDir, "images")
                            cachePath.mkdirs() // don't forget to make the directory
                            val file = File(cachePath, "shared_qr_code.png")
                            val stream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            stream.close()

                            val uri: Uri = FileProvider.getUriForFile(
                                context,
                                context.packageName + ".fileprovider",
                                file
                            )

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_TEXT, "Generated QR Code")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                enabled = qrBitmap != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share QR Code", fontWeight = FontWeight.Bold)
            }
        }
    }
}
