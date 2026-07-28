package com.example.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.URL

enum class MediaType {
    IMAGE, VIDEO, AUDIO, DOCUMENT
}

data class StorageInfo(
    val totalSpace: Long,
    val freeSpace: Long,
    val usedSpace: Long,
    val fileCount: Int,
    val totalSpaceFormatted: String,
    val freeSpaceFormatted: String,
    val usedSpaceFormatted: String,
    val usedPercentage: Float
)

interface StorageManager {
    /**
     * Mendapatkan daftar permission yang dibutuhkan berdasarkan versi Android dan tipe media.
     */
    fun getRequiredPermissions(mediaType: MediaType): List<String>

    /**
     * Download file dari URL dan simpan ke App-Specific Storage atau Shared Storage (MediaStore)
     */
    suspend fun downloadFile(
        context: Context,
        url: String,
        mediaType: MediaType,
        fileName: String,
        useSharedStorage: Boolean,
        onProgress: (Int) -> Unit
    ): Result<Uri>

    /**
     * Upload file (simulasi membaca file Uri dan mengirim ke tujuan)
     */
    suspend fun uploadFile(context: Context, uri: Uri, destinationUrl: String): Result<Boolean>

    /**
     * Mendapatkan informasi storage
     */
    fun getStorageInfo(context: Context): StorageInfo
}

class StorageManagerImpl : StorageManager {

    override fun getRequiredPermissions(mediaType: MediaType): List<String> {
        val permissions = mutableListOf<String>()
        when {
            // Android 14+ (API 34+) might need READ_MEDIA_VISUAL_USER_SELECTED
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                when (mediaType) {
                    MediaType.IMAGE -> {
                        permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                        permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    }
                    MediaType.VIDEO -> {
                        permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                        permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    }
                    MediaType.AUDIO -> permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                    MediaType.DOCUMENT -> {
                        // SAF is recommended for documents, no direct storage permission needed usually
                    }
                }
            }
            // Android 13 (API 33)
            Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU -> {
                when (mediaType) {
                    MediaType.IMAGE -> permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    MediaType.VIDEO -> permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    MediaType.AUDIO -> permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                    MediaType.DOCUMENT -> {}
                }
            }
            // Android 10-12 (API 29-32)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                // Write is not strictly needed for MediaStore insert, but useful for other operations
            }
            // Android 9 and below (API 28-)
            else -> {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        return permissions
    }

    override suspend fun downloadFile(
        context: Context,
        url: String,
        mediaType: MediaType,
        fileName: String,
        useSharedStorage: Boolean,
        onProgress: (Int) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val fileLength = connection.contentLength

            val inputStream = connection.getInputStream()
            val uri = if (useSharedStorage) {
                saveToSharedStorage(context, inputStream, mediaType, fileName, fileLength, onProgress)
            } else {
                saveToAppSpecificStorage(context, inputStream, mediaType, fileName, fileLength, onProgress)
            }
            
            if (uri != null) {
                Result.success(uri)
            } else {
                Result.failure(Exception("Failed to save file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveToSharedStorage(
        context: Context,
        inputStream: InputStream,
        mediaType: MediaType,
        fileName: String,
        fileLength: Int,
        onProgress: (Int) -> Unit
    ): Uri? {
        val resolver = context.contentResolver
        val collection = when (mediaType) {
            MediaType.IMAGE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaType.VIDEO -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            MediaType.AUDIO -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            MediaType.DOCUMENT -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else return null // Use SAF for older versions ideally
        }

        val relativePath = when (mediaType) {
            MediaType.IMAGE -> Environment.DIRECTORY_PICTURES
            MediaType.VIDEO -> Environment.DIRECTORY_MOVIES
            MediaType.AUDIO -> Environment.DIRECTORY_MUSIC
            MediaType.DOCUMENT -> Environment.DIRECTORY_DOWNLOADS
        } + "/ATBKZ_Tools"

        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(fileName).extension) ?: "*/*"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(collection, contentValues) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalRead: Long = 0
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (fileLength > 0) {
                        val progress = (totalRead * 100 / fileLength).toInt()
                        onProgress(progress)
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            return uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            return null
        } finally {
            inputStream.close()
        }
    }

    private fun saveToAppSpecificStorage(
        context: Context,
        inputStream: InputStream,
        mediaType: MediaType,
        fileName: String,
        fileLength: Int,
        onProgress: (Int) -> Unit
    ): Uri? {
        val directoryType = when (mediaType) {
            MediaType.IMAGE -> Environment.DIRECTORY_PICTURES
            MediaType.VIDEO -> Environment.DIRECTORY_MOVIES
            MediaType.AUDIO -> Environment.DIRECTORY_MUSIC
            MediaType.DOCUMENT -> Environment.DIRECTORY_DOCUMENTS
        }
        val directory = context.getExternalFilesDir(directoryType) ?: return null
        val file = File(directory, fileName)

        try {
            file.outputStream().use { outputStream ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalRead: Long = 0
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (fileLength > 0) {
                        val progress = (totalRead * 100 / fileLength).toInt()
                        onProgress(progress)
                    }
                }
            }
            return Uri.fromFile(file)
        } catch (e: Exception) {
            if (file.exists()) file.delete()
            return null
        } finally {
            inputStream.close()
        }
    }

    override suspend fun uploadFile(context: Context, uri: Uri, destinationUrl: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Simulasi membaca file dari URI menggunakan ContentResolver
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                // Di sini Anda akan mengimplementasikan logic upload yang sebenarnya 
                // menggunakan OkHttp atau Retrofit Multipart
                
                // Simulasi proses upload (membaca ukuran file)
                val bytes = inputStream.readBytes()
                inputStream.close()
                
                // Pretend successful upload
                Result.success(true)
            } else {
                Result.failure(Exception("Cannot open input stream for URI: $uri"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getStorageInfo(context: Context): StorageInfo {
        val statFs = android.os.StatFs(Environment.getExternalStorageDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong

        val totalSpace = totalBlocks * blockSize
        val freeSpace = availableBlocks * blockSize
        val usedSpace = totalSpace - freeSpace

        val appBaseFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ATBKZ_Tools")
        val fileCount = countFilesInDirectory(appBaseFolder)

        return StorageInfo(
            totalSpace = totalSpace,
            freeSpace = freeSpace,
            usedSpace = usedSpace,
            fileCount = fileCount,
            totalSpaceFormatted = formatSize(totalSpace),
            freeSpaceFormatted = formatSize(freeSpace),
            usedSpaceFormatted = formatSize(usedSpace),
            usedPercentage = if (totalSpace > 0) usedSpace.toFloat() / totalSpace.toFloat() else 0f
        )
    }

    private fun countFilesInDirectory(directory: File): Int {
        var count = 0
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        count++
                    } else if (file.isDirectory) {
                        count += countFilesInDirectory(file)
                    }
                }
            }
        }
        return count
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
