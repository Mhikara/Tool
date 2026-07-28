package com.example.util

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.text.DecimalFormat

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

class StorageManager(private val context: Context) {
    
    // Create base application folder
    private val appBaseFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ATBKZ_Tools")
    
    init {
        createFolders()
    }

    private fun createFolders() {
        val folders = listOf(
            "Video", "Audio", "Images", "Documents", "Cache", "Backup", "Temp"
        )
        if (!appBaseFolder.exists()) {
            appBaseFolder.mkdirs()
        }
        folders.forEach { folderName ->
            val folder = File(appBaseFolder, folderName)
            if (!folder.exists()) folder.mkdirs()
        }
    }

    fun getDownloadFolder(type: String): File {
        return File(appBaseFolder, type).apply { if (!exists()) mkdirs() }
    }

    fun getStorageInfo(): StorageInfo {
        val statFs = StatFs(Environment.getExternalStorageDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong

        val totalSpace = totalBlocks * blockSize
        val freeSpace = availableBlocks * blockSize
        val usedSpace = totalSpace - freeSpace

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
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
