package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

enum class MediaType {
    IMAGE, VIDEO, AUDIO, DOCUMENT
}

interface FileAccessManager {
    /**
     * Meminta permission secara logic. Untuk implementasi UI,
     * gunakan ActivityResultContracts di layer UI (Activity/Composable).
     */
    fun getRequiredPermissions(mediaType: MediaType): List<String>

    suspend fun saveImageToPublicGallery(context: Context, bitmap: Bitmap, fileName: String, folderName: String): Uri?
    suspend fun saveVideoToPublicGallery(context: Context, videoBytes: ByteArray, fileName: String, folderName: String): Uri?
}

class FileAccessManagerImpl : FileAccessManager {

    override fun getRequiredPermissions(mediaType: MediaType): List<String> {
        val permissions = mutableListOf<String>()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> { // API 33+
                when (mediaType) {
                    MediaType.IMAGE -> permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                    MediaType.VIDEO -> permissions.add(android.Manifest.permission.READ_MEDIA_VIDEO)
                    MediaType.AUDIO -> permissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
                    MediaType.DOCUMENT -> permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE) // Ganti SAF jika memungkinkan
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> { // API 29-32
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                // MANAGE_EXTERNAL_STORAGE jika sangat diperlukan untuk all files
            }
            else -> { // API 28 ke bawah
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        return permissions
    }

    override suspend fun saveImageToPublicGallery(context: Context, bitmap: Bitmap, fileName: String, folderName: String): Uri? {
        return withContext(Dispatchers.IO) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/$folderName")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                try {
                    resolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(it, contentValues, null, null)
                    }
                    it
                } catch (e: Exception) {
                    resolver.delete(it, null, null)
                    null
                }
            }
        }
    }

    override suspend fun saveVideoToPublicGallery(context: Context, videoBytes: ByteArray, fileName: String, folderName: String): Uri? {
        return withContext(Dispatchers.IO) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/$folderName")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                try {
                    resolver.openOutputStream(it)?.use { stream ->
                        stream.write(videoBytes)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(it, contentValues, null, null)
                    }
                    it
                } catch (e: Exception) {
                    resolver.delete(it, null, null)
                    null
                }
            }
        }
    }
}
