package com.example.ui.maker.qrmaker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.QrHistoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrMakerScreen(
    onBack: () -> Unit,
    viewModel: QrMakerViewModel = viewModel()
) {
    val context = LocalContext.current

    val selectedType by viewModel.selectedType.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val generatedBitmap by viewModel.generatedBitmap.collectAsState()
    val styleConfig by viewModel.styleConfig.collectAsState()

    val showClipboardBanner by viewModel.showClipboardBanner.collectAsState()
    val detectedClipboardText by viewModel.detectedClipboardText.collectAsState()
    val detectedClipboardType by viewModel.detectedClipboardType.collectAsState()

    val historyList by viewModel.historyList.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Maker, 1: History
    var showCustomizationSection by remember { mutableStateOf(false) }

    // Check clipboard on launch
    LaunchedEffect(Unit) {
        viewModel.checkClipboard(context)
        viewModel.loadHistory(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Code Maker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.checkClipboard(context) }) {
                        Icon(Icons.Outlined.ContentPaste, contentDescription = "Cek Clipboard")
                    }
                    IconButton(onClick = { showCustomizationSection = !showCustomizationSection }) {
                        Icon(Icons.Outlined.Palette, contentDescription = "Kustomisasi Tampilan")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Buat QR Live", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Outlined.QrCodeScanner, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Riwayat QR (${historyList.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Outlined.History, contentDescription = null) }
                )
            }

            if (activeTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // --- Clipboard Auto Detect Banner ---
                    if (showClipboardBanner && !detectedClipboardText.isNullOrBlank()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Terisi otomatis dari clipboard ($detectedClipboardType)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            detectedClipboardText!!.take(40) + if (detectedClipboardText!!.length > 40) "..." else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.applyClipboardData() },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Gunakan", fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = { viewModel.dismissClipboardBanner() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Tutup", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // --- Real-time Live Preview Card ---
                    item {
                        LiveQrPreviewCard(
                            validationState = validationState,
                            bitmap = generatedBitmap,
                            fgColor = styleConfig.fgColor,
                            bgColor = styleConfig.bgColor,
                            onSave = { generatedBitmap?.let { viewModel.saveImageToGallery(context, it) } },
                            onShare = { generatedBitmap?.let { viewModel.shareQrImage(context, it) } },
                            onPrint = { generatedBitmap?.let { viewModel.printQrCode(context, it) } }
                        )
                    }

                    // --- QR Type Selector ---
                    item {
                        Text("Tipe QR Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        val qrTypes = listOf("Text", "URL", "WiFi", "WhatsApp", "Email", "Contact", "Location")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(qrTypes) { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { viewModel.selectedType.value = type },
                                    label = { Text(type) },
                                    leadingIcon = {
                                        val icon = when (type) {
                                            "WiFi" -> Icons.Outlined.Wifi
                                            "WhatsApp" -> Icons.Outlined.Chat
                                            "Email" -> Icons.Outlined.Email
                                            "Contact" -> Icons.Outlined.ContactPage
                                            "Location" -> Icons.Outlined.LocationOn
                                            "URL" -> Icons.Outlined.Link
                                            else -> Icons.Outlined.TextFields
                                        }
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                )
                            }
                        }
                    }

                    // --- Customization Toggle Header & Section ---
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCustomizationSection = !showCustomizationSection },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Kustomisasi Desain QR", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("Warna, bentuk modul & logo di tengah", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(
                                    if (showCustomizationSection) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    if (showCustomizationSection) {
                        item {
                            CustomizationPanel(viewModel = viewModel)
                        }
                    }

                    // --- Dynamic Inputs per Type ---
                    item {
                        Text("Input Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        DynamicQrInputForm(type = selectedType, viewModel = viewModel)
                    }
                }
            } else {
                // --- History View ---
                QrHistoryView(
                    historyList = historyList,
                    onDelete = { id -> viewModel.deleteHistoryItem(context, id) }
                )
            }
        }
    }
}

@Composable
fun LiveQrPreviewCard(
    validationState: QrValidationResult,
    bitmap: Bitmap?,
    fgColor: Int,
    bgColor: Int,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit
) {
    val contrastRatio = remember(fgColor, bgColor) {
        QrGenerator.checkContrastRatio(fgColor, bgColor)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(bgColor)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                if (validationState is QrValidationResult.Valid && bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Live Preview QR Code",
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(90.dp),
                            tint = Color.Gray.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = (validationState as? QrValidationResult.Invalid)?.reason ?: "Masukkan data valid",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (contrastRatio < 3.0f) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Peringatan: Kontras warna rendah, QR mungkin sulit dipindai.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onSave,
                    enabled = validationState is QrValidationResult.Valid && bitmap != null
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan")
                }

                OutlinedButton(
                    onClick = onShare,
                    enabled = validationState is QrValidationResult.Valid && bitmap != null
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan")
                }

                OutlinedButton(
                    onClick = onPrint,
                    enabled = validationState is QrValidationResult.Valid && bitmap != null
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak")
                }
            }
        }
    }
}

@Composable
fun CustomizationPanel(viewModel: QrMakerViewModel) {
    val context = LocalContext.current
    val fgColor by viewModel.fgColor.collectAsState()
    val bgColor by viewModel.bgColor.collectAsState()
    val shape by viewModel.moduleShape.collectAsState()
    val logoPreset by viewModel.selectedLogoPreset.collectAsState()

    val logoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.customLogoBitmap.value = bitmap
                viewModel.selectedLogoPreset.value = "Custom"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Colors
            Text("Warna Modul QR", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            val fgColors = listOf(
                Pair("Hitam", AndroidColor.BLACK),
                Pair("Navy", AndroidColor.parseColor("#0F172A")),
                Pair("Emerald", AndroidColor.parseColor("#065F46")),
                Pair("Biru", AndroidColor.parseColor("#1D4ED8")),
                Pair("Merah", AndroidColor.parseColor("#991B1B")),
                Pair("Ungu", AndroidColor.parseColor("#581C87"))
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fgColors) { (name, colorInt) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(if (fgColor == colorInt) 3.dp else 1.dp, if (fgColor == colorInt) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                            .clickable { viewModel.fgColor.value = colorInt }
                    )
                }
            }

            Text("Warna Latar Belakang", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            val bgColors = listOf(
                Pair("Putih", AndroidColor.WHITE),
                Pair("Krem", AndroidColor.parseColor("#FEF3C7")),
                Pair("Abu", AndroidColor.parseColor("#F1F5F9")),
                Pair("Kuning", AndroidColor.parseColor("#FEF08A"))
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bgColors) { (name, colorInt) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(if (bgColor == colorInt) 3.dp else 1.dp, if (bgColor == colorInt) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                            .clickable { viewModel.bgColor.value = colorInt }
                    )
                }
            }

            // Shapes
            Text("Bentuk Modul", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = shape == QrModuleShape.SQUARE,
                    onClick = { viewModel.moduleShape.value = QrModuleShape.SQUARE },
                    label = { Text("Kotak Standar") }
                )
                FilterChip(
                    selected = shape == QrModuleShape.ROUNDED,
                    onClick = { viewModel.moduleShape.value = QrModuleShape.ROUNDED },
                    label = { Text("Rounded Soft") }
                )
                FilterChip(
                    selected = shape == QrModuleShape.DOTS,
                    onClick = { viewModel.moduleShape.value = QrModuleShape.DOTS },
                    label = { Text("Dots Lingkaran") }
                )
            }

            // Logo
            Text("Logo di Tengah (Opsional)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = logoPreset == "None",
                    onClick = {
                        viewModel.selectedLogoPreset.value = "None"
                        viewModel.customLogoBitmap.value = null
                    },
                    label = { Text("Tanpa Logo") }
                )
                FilterChip(
                    selected = logoPreset == "Custom",
                    onClick = { logoLauncher.launch("image/*") },
                    label = { Text("Pilih Foto Galeri") },
                    leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
fun DynamicQrInputForm(type: String, viewModel: QrMakerViewModel) {
    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            val name = it.getString(nameIndex)
                            viewModel.contactName.value = name ?: ""
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (type) {
                "WiFi" -> {
                    val ssid by viewModel.wifiSsid.collectAsState()
                    val pass by viewModel.wifiPassword.collectAsState()
                    val enc by viewModel.wifiEncryption.collectAsState()
                    val hidePass by viewModel.hidePassword.collectAsState()

                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { viewModel.wifiSsid.value = it },
                        label = { Text("Nama Jaringan (SSID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (enc != "None") {
                        OutlinedTextField(
                            value = pass,
                            onValueChange = { viewModel.wifiPassword.value = it },
                            label = { Text("Password WiFi") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (hidePass) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(onClick = { viewModel.hidePassword.value = !hidePass }) {
                                    Icon(
                                        if (hidePass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Sembunyikan/Tampilkan Password"
                                    )
                                }
                            }
                        )
                    }

                    Text("Jenis Enkripsi:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("WPA/WPA2", "WEP", "None").forEach { option ->
                            FilterChip(
                                selected = enc == option,
                                onClick = { viewModel.wifiEncryption.value = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }

                "WhatsApp" -> {
                    val phone by viewModel.waPhone.collectAsState()
                    val msg by viewModel.waMessage.collectAsState()

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { viewModel.waPhone.value = it },
                        label = { Text("Nomor Telepon WA (cth: 628123456789)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = msg,
                        onValueChange = { viewModel.waMessage.value = it },
                        label = { Text("Pesan Default (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                "Email" -> {
                    val email by viewModel.emailAddress.collectAsState()
                    val subj by viewModel.emailSubject.collectAsState()
                    val body by viewModel.emailBody.collectAsState()

                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.emailAddress.value = it },
                        label = { Text("Alamat Email Penerima") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = subj,
                        onValueChange = { viewModel.emailSubject.value = it },
                        label = { Text("Subjek Email (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = body,
                        onValueChange = { viewModel.emailBody.value = it },
                        label = { Text("Isi Pesan Email (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                "Contact" -> {
                    val name by viewModel.contactName.collectAsState()
                    val phone by viewModel.contactPhone.collectAsState()
                    val email by viewModel.contactEmail.collectAsState()
                    val company by viewModel.contactCompany.collectAsState()

                    OutlinedButton(
                        onClick = { contactPickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Contacts, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih dari Kontak Telepon")
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.contactName.value = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { viewModel.contactPhone.value = it },
                        label = { Text("Nomor HP / Telepon") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.contactEmail.value = it },
                        label = { Text("Alamat Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = company,
                        onValueChange = { viewModel.contactCompany.value = it },
                        label = { Text("Perusahaan / Jabatan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                "Location" -> {
                    val lat by viewModel.locationLat.collectAsState()
                    val lng by viewModel.locationLng.collectAsState()

                    OutlinedTextField(
                        value = lat,
                        onValueChange = { viewModel.locationLat.value = it },
                        label = { Text("Latitude (cth: -6.2088)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lng,
                        onValueChange = { viewModel.locationLng.value = it },
                        label = { Text("Longitude (cth: 106.8456)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.locationLat.value = "-6.1754"
                            viewModel.locationLng.value = "106.8272"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gunakan Contoh Koordinat (Monas, Jakarta)")
                    }
                }

                "URL" -> {
                    val url by viewModel.urlData.collectAsState()
                    OutlinedTextField(
                        value = url,
                        onValueChange = { viewModel.urlData.value = it },
                        label = { Text("https://domain.com/halaman") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                else -> { // Text
                    val text by viewModel.textData.collectAsState()
                    OutlinedTextField(
                        value = text,
                        onValueChange = { viewModel.onTextDataChange(it) },
                        label = { Text("Masukkan Teks Bebas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            }
        }
    }
}

@Composable
fun QrHistoryView(
    historyList: List<QrHistoryEntity>,
    onDelete: (Long) -> Unit
) {
    val context = LocalContext.current

    if (historyList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Belum Ada Riwayat QR Code", fontWeight = FontWeight.Bold)
                Text("QR Code yang Anda buat akan tersimpan di sini secara otomatis.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyList, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (item.qrType) {
                                "WiFi" -> Icons.Outlined.Wifi
                                "WhatsApp" -> Icons.Outlined.Chat
                                "Email" -> Icons.Outlined.Email
                                "Contact" -> Icons.Outlined.ContactPage
                                "Location" -> Icons.Outlined.LocationOn
                                "URL" -> Icons.Outlined.Link
                                else -> Icons.Outlined.QrCode
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Tipe: ${item.qrType}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(item.rawData, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        IconButton(onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, item.rawData)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Teks QR"))
                        }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Bagikan")
                        }
                        IconButton(onClick = { onDelete(item.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
