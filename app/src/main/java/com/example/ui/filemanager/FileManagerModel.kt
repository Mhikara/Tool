package com.example.ui.filemanager

import java.io.File

enum class FileTypeCategory(val displayName: String) {
    ALL("Semua File"),
    IMAGES("Gambar"),
    VIDEOS("Video"),
    AUDIO("Audio"),
    DOCUMENTS("Dokumen"),
    DOWNLOADS("Download"),
    RECENTS("Terbaru"),
    LARGE_FILES("File Besar (>50MB)"),
    TRASH("Sampah")
}

enum class FileTypeGroup {
    FOLDER,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    ARCHIVE,
    APK,
    OTHER
}

enum class ViewMode {
    LIST,
    GRID
}

enum class SortOption(val displayName: String) {
    NAME_ASC("Nama (A-Z)"),
    NAME_DESC("Nama (Z-A)"),
    DATE_DESC("Terbaru Pertama"),
    DATE_ASC("Terlama Pertama"),
    SIZE_DESC("Terbesar Pertama"),
    SIZE_ASC("Terkecil Pertama")
}

data class FileItem(
    val file: File,
    val path: String = file.absolutePath,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isDirectory) 0L else file.length(),
    val lastModified: Long = file.lastModified(),
    val extension: String = file.extension.lowercase(),
    val fileTypeGroup: FileTypeGroup = determineFileTypeGroup(file),
    val isSelected: Boolean = false,
    val isTrash: Boolean = false,
    val originalPath: String? = null,
    val trashedTimestamp: Long? = null
)

fun determineFileTypeGroup(file: File): FileTypeGroup {
    if (file.isDirectory) return FileTypeGroup.FOLDER
    return when (file.extension.lowercase()) {
        "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "svg" -> FileTypeGroup.IMAGE
        "mp4", "mkv", "avi", "webm", "mov", "3gp", "flv" -> FileTypeGroup.VIDEO
        "mp3", "wav", "flac", "m4a", "aac", "ogg", "opus" -> FileTypeGroup.AUDIO
        "pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx", "ppt", "pptx", "csv" -> FileTypeGroup.DOCUMENT
        "zip", "rar", "7z", "tar", "gz", "bz2" -> FileTypeGroup.ARCHIVE
        "apk" -> FileTypeGroup.APK
        else -> FileTypeGroup.OTHER
    }
}

data class StorageAnalysis(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val imagesSize: Long = 0L,
    val videosSize: Long = 0L,
    val audioSize: Long = 0L,
    val docsSize: Long = 0L,
    val archivesSize: Long = 0L,
    val otherSize: Long = 0L,
    val largeFiles: List<FileItem> = emptyList(),
    val duplicateGroups: List<DuplicateGroup> = emptyList()
)

data class DuplicateGroup(
    val fileHash: String,
    val fileSize: Long,
    val files: List<FileItem>
)

data class TrashMetadata(
    val id: String,
    val originalPath: String,
    val trashPath: String,
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis()
)
