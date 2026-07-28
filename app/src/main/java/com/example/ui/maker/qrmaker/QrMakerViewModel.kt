package com.example.ui.maker.qrmaker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.QrHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed class QrValidationResult {
    data class Valid(val payload: String, val title: String) : QrValidationResult()
    data class Invalid(val reason: String) : QrValidationResult()
}

class QrMakerViewModel : ViewModel() {

    // --- State Variables ---
    val selectedType = MutableStateFlow("Text")

    // WiFi Form
    val wifiSsid = MutableStateFlow("")
    val wifiPassword = MutableStateFlow("")
    val wifiEncryption = MutableStateFlow("WPA/WPA2")
    val hidePassword = MutableStateFlow(true)

    // WhatsApp Form
    val waPhone = MutableStateFlow("")
    val waMessage = MutableStateFlow("")

    // Email Form
    val emailAddress = MutableStateFlow("")
    val emailSubject = MutableStateFlow("")
    val emailBody = MutableStateFlow("")

    // Contact Form
    val contactName = MutableStateFlow("")
    val contactPhone = MutableStateFlow("")
    val contactEmail = MutableStateFlow("")
    val contactCompany = MutableStateFlow("")

    // Location Form
    val locationLat = MutableStateFlow("")
    val locationLng = MutableStateFlow("")

    // Text & URL Form
    val textData = MutableStateFlow("")
    val urlData = MutableStateFlow("")

    // Customization State
    val fgColor = MutableStateFlow(Color.BLACK)
    val bgColor = MutableStateFlow(Color.WHITE)
    val moduleShape = MutableStateFlow(QrModuleShape.SQUARE)
    val selectedLogoPreset = MutableStateFlow("None") // None, WhatsApp, WiFi, Mail, User, Location, Custom
    val customLogoBitmap = MutableStateFlow<Bitmap?>(null)

    // Clipboard Auto-Detect State
    val detectedClipboardText = MutableStateFlow<String?>(null)
    val detectedClipboardType = MutableStateFlow<String?>(null)
    val showClipboardBanner = MutableStateFlow(false)

    // History Flow from Room DB
    private val _historyList = MutableStateFlow<List<QrHistoryEntity>>(emptyList())
    val historyList = _historyList.asStateFlow()

    // --- Derived Validation Flow ---
    val validationState: StateFlow<QrValidationResult> = combine(
        listOf(
            selectedType,
            wifiSsid, wifiPassword, wifiEncryption,
            waPhone, waMessage,
            emailAddress, emailSubject, emailBody,
            contactName, contactPhone, contactEmail, contactCompany,
            locationLat, locationLng,
            textData, urlData
        )
    ) { array ->
        val type = array[0]
        when (type) {
            "WiFi" -> {
                val ssid = array[1]
                val pass = array[2]
                val enc = array[3]
                if (ssid.isBlank()) {
                    QrValidationResult.Invalid("Lengkapi Nama WiFi (SSID) terlebih dahulu.")
                } else if (enc != "None" && pass.isBlank()) {
                    QrValidationResult.Invalid("Jaringan $enc membutuhkan password.")
                } else {
                    val encType = if (enc == "None") "nopass" else enc.replace("/", "")
                    val payload = "WIFI:T:$encType;S:$ssid;P:$pass;;"
                    QrValidationResult.Valid(payload, "WiFi: $ssid")
                }
            }
            "WhatsApp" -> {
                val phone = array[4].trim()
                val msg = array[5]
                val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
                if (cleanPhone.length < 7) {
                    QrValidationResult.Invalid("Masukkan nomor telepon WhatsApp yang valid (min. 7 digit).")
                } else {
                    val encodedMsg = Uri.encode(msg)
                    val payload = if (msg.isNotBlank()) "https://wa.me/$cleanPhone?text=$encodedMsg" else "https://wa.me/$cleanPhone"
                    QrValidationResult.Valid(payload, "WA: $cleanPhone")
                }
            }
            "Email" -> {
                val email = array[6].trim()
                val subj = array[7]
                val body = array[8]
                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    QrValidationResult.Invalid("Masukkan alamat email yang valid.")
                } else {
                    val encodedSubj = Uri.encode(subj)
                    val encodedBody = Uri.encode(body)
                    var payload = "mailto:$email"
                    val params = mutableListOf<String>()
                    if (subj.isNotBlank()) params.add("subject=$encodedSubj")
                    if (body.isNotBlank()) params.add("body=$encodedBody")
                    if (params.isNotEmpty()) {
                        payload += "?" + params.joinToString("&")
                    }
                    QrValidationResult.Valid(payload, "Email: $email")
                }
            }
            "Contact" -> {
                val name = array[9]
                val phone = array[10]
                val email = array[11]
                val company = array[12]
                if (name.isBlank() && phone.isBlank() && email.isBlank()) {
                    QrValidationResult.Invalid("Lengkapi minimal Nama atau Nomor Telepon kontak.")
                } else {
                    val vcard = StringBuilder()
                        .appendLine("BEGIN:VCARD")
                        .appendLine("VERSION:3.0")
                        .appendLine("N:$name")
                        .appendLine("FN:$name")
                    if (phone.isNotBlank()) vcard.appendLine("TEL:$phone")
                    if (email.isNotBlank()) vcard.appendLine("EMAIL:$email")
                    if (company.isNotBlank()) vcard.appendLine("ORG:$company")
                    vcard.append("END:VCARD")
                    QrValidationResult.Valid(vcard.toString(), "Kontak: ${name.ifBlank { phone }}")
                }
            }
            "Location" -> {
                val lat = array[13].trim()
                val lng = array[14].trim()
                if (lat.isBlank() || lng.isBlank()) {
                    QrValidationResult.Invalid("Masukkan Latitude dan Longitude koordinat lokasi.")
                } else {
                    val payload = "https://maps.google.com/?q=$lat,$lng"
                    QrValidationResult.Valid(payload, "Lokasi: $lat, $lng")
                }
            }
            "URL" -> {
                val url = array[16].trim()
                if (url.isBlank()) {
                    QrValidationResult.Invalid("Masukkan alamat URL web.")
                } else {
                    val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                    QrValidationResult.Valid(formattedUrl, "URL: $formattedUrl")
                }
            }
            else -> { // Text
                val text = array[15]
                if (text.isBlank()) {
                    QrValidationResult.Invalid("Ketik teks untuk menghasilkan QR code.")
                } else {
                    QrValidationResult.Valid(text, "Teks: ${text.take(20)}")
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QrValidationResult.Invalid("Ketik data terlebih dahulu."))

    // Combined Style Config
    val styleConfig: StateFlow<QrStyleConfig> = combine(
        fgColor, bgColor, moduleShape, customLogoBitmap
    ) { fg, bg, shape, logo ->
        QrStyleConfig(fgColor = fg, bgColor = bg, shape = shape, logoBitmap = logo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QrStyleConfig())

    // --- Debounced Real-time Generated QR Bitmap Flow ---
    @OptIn(FlowPreview::class)
    val generatedBitmap: StateFlow<Bitmap?> = combine(validationState, styleConfig) { valState, style ->
        Pair(valState, style)
    }
        .debounce(300) // 300ms debounce per specs
        .map { (valState, style) ->
            if (valState is QrValidationResult.Valid) {
                withContext(Dispatchers.Default) {
                    QrGenerator.generateQrBitmap(valState.payload, size = 800, qrStyle = style)
                }
            } else {
                null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadHistory(context: Context) {
        viewModelScope.launch {
            try {
                val dao = AppDatabase.getDatabase(context).qrHistoryDao()
                dao.getAllQrHistory().collect { list ->
                    _historyList.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveCurrentQrToHistory(context: Context) {
        val valState = validationState.value
        if (valState is QrValidationResult.Valid) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val dao = AppDatabase.getDatabase(context).qrHistoryDao()
                    dao.insertQrHistory(
                        QrHistoryEntity(
                            title = valState.title,
                            qrType = selectedType.value,
                            rawData = valState.payload
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteHistoryItem(context: Context, id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dao = AppDatabase.getDatabase(context).qrHistoryDao()
                dao.deleteQrHistory(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Clipboard Auto-Detect ---
    fun checkClipboard(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val clipData: ClipData? = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString()?.trim()
                    if (!text.isNullOrBlank()) {
                        analyzeAndSetClipboardText(text)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun analyzeAndSetClipboardText(text: String) {
        var type = "Text"
        when {
            text.startsWith("WIFI:") -> type = "WiFi"
            text.contains("wa.me") || text.contains("api.whatsapp.com") -> type = "WhatsApp"
            text.startsWith("mailto:") || Patterns.EMAIL_ADDRESS.matcher(text).matches() -> type = "Email"
            text.startsWith("BEGIN:VCARD") -> type = "Contact"
            text.startsWith("geo:") || (text.contains("maps.google.com") && text.contains("q=")) -> type = "Location"
            text.startsWith("http://") || text.startsWith("https://") || Patterns.WEB_URL.matcher(text).matches() -> type = "URL"
            text.startsWith("+") || (text.all { it.isDigit() || it == '-' || it == ' ' } && text.length in 8..15) -> type = "WhatsApp"
        }

        detectedClipboardText.value = text
        detectedClipboardType.value = type
        showClipboardBanner.value = true
    }

    fun applyClipboardData() {
        val text = detectedClipboardText.value ?: return
        val type = detectedClipboardType.value ?: "Text"
        selectedType.value = type

        when (type) {
            "URL" -> urlData.value = text
            "WhatsApp" -> waPhone.value = text
            "Email" -> emailAddress.value = text
            "Text" -> textData.value = text
            else -> textData.value = text
        }
        showClipboardBanner.value = false
    }

    fun dismissClipboardBanner() {
        showClipboardBanner.value = false
    }

    // --- On Text Data Change (Auto detect URL) ---
    fun onTextDataChange(newText: String) {
        textData.value = newText
        if (newText.startsWith("http://") || newText.startsWith("https://") || (newText.contains(".") && Patterns.WEB_URL.matcher(newText).matches())) {
            selectedType.value = "URL"
            urlData.value = newText
        }
    }

    // --- Share & Save Image Functions ---
    fun saveImageToGallery(context: Context, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val qrFolder = File(picturesDir, "QR_Maker")
                if (!qrFolder.exists()) qrFolder.mkdirs()

                val fileName = "QR_${System.currentTimeMillis()}.png"
                val file = File(qrFolder, fileName)
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                saveCurrentQrToHistory(context)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "QR Code tersimpan di Pictures/QR_Maker/$fileName", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal menyimpan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareQrImage(context: Context, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "shared_qr.png")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                if (contentUri != null) {
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                        putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                        type = "image/png"
                    }

                    saveCurrentQrToHistory(context)

                    withContext(Dispatchers.Main) {
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan QR Code"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal membagikan QR Code: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun printQrCode(context: Context, bitmap: Bitmap) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "QR Code Print"
            printManager.print(
                jobName,
                object : android.print.PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: android.os.Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = android.print.PrintDocumentInfo.Builder("qr_code.pdf")
                            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out android.print.PageRange>?,
                        destination: android.os.ParcelFileDescriptor?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            val pdfDocument = android.graphics.pdf.PdfDocument()
                            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
                            val page = pdfDocument.startPage(pageInfo)
                            val canvas = page.canvas

                            val paint = Paint()
                            val scaled = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
                            canvas.drawBitmap(scaled, 97f, 200f, paint)

                            pdfDocument.writeTo(FileOutputStream(destination?.fileDescriptor))
                            pdfDocument.close()
                            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                },
                null
            )
            saveCurrentQrToHistory(context)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun String?.isNullOfBlank(): Boolean = this == null || this.isBlank()
