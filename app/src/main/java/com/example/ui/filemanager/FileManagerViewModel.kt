package com.example.ui.filemanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileManagerViewModel : ViewModel() {

    // Storage Paths
    val defaultRootPath: String = Environment.getExternalStorageDirectory()?.absolutePath ?: "/storage/emulated/0"
    val appCustomFolderPath: String = "$defaultRootPath/ATBKZTools"

    // --- State Flows ---
    val currentPath = MutableStateFlow(defaultRootPath)
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow(FileTypeCategory.ALL)
    val viewMode = MutableStateFlow(ViewMode.LIST)
    val sortOption = MutableStateFlow(SortOption.NAME_ASC)
    
    val selectedFilePaths = MutableStateFlow<Set<String>>(emptySet())
    val clipboardSources = MutableStateFlow<List<FileItem>>(emptyList())
    val isCutOperation = MutableStateFlow(false)

    val isPermissionGranted = MutableStateFlow(checkStoragePermission())
    val isAnalyzing = MutableStateFlow(false)
    val storageAnalysis = MutableStateFlow<StorageAnalysis?>(null)

    // Raw files list in current folder
    private val rawFileList = MutableStateFlow<List<FileItem>>(emptyList())

    // --- Filtered and Sorted Files Flow ---
    val displayedFiles: StateFlow<List<FileItem>> = combine(
        rawFileList,
        searchQuery,
        selectedCategory,
        sortOption,
        selectedFilePaths
    ) { files, query, category, sort, selectedPaths ->
        var list = files

        // Filter by Category if not ALL and not searching
        if (category != FileTypeCategory.ALL) {
            list = filterFilesByCategory(list, category)
        }

        // Filter by Search Query
        if (query.isNotBlank()) {
            list = list.filter { it.name.contains(query, ignoreCase = true) }
        }

        // Apply Selection State
        list = list.map { item ->
            item.copy(isSelected = selectedPaths.contains(item.path))
        }

        // Apply Sorting
        sortFiles(list, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Create ATBKZTools folder if not exists
        try {
            val appFolder = File(appCustomFolderPath)
            if (!appFolder.exists()) appFolder.mkdirs()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        refreshCurrentDirectory()
    }

    fun checkPermissionState(): Boolean {
        val granted = checkStoragePermission()
        isPermissionGranted.value = granted
        return granted
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    fun openStoragePermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
            }
        }
    }

    // --- Directory Navigation ---
    fun refreshCurrentDirectory() {
        if (selectedCategory.value == FileTypeCategory.TRASH) {
            loadTrashDirectory()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val folder = File(currentPath.value)
            if (folder.exists() && folder.isDirectory) {
                val files = folder.listFiles()?.map { file ->
                    FileItem(file = file)
                } ?: emptyList()
                rawFileList.value = files
            } else {
                rawFileList.value = emptyList()
            }
        }
    }

    fun navigateToFolder(path: String) {
        val folder = File(path)
        if (folder.exists() && folder.isDirectory) {
            currentPath.value = path
            selectedFilePaths.value = emptySet()
            if (selectedCategory.value == FileTypeCategory.TRASH) {
                selectedCategory.value = FileTypeCategory.ALL
            }
            refreshCurrentDirectory()
        }
    }

    fun navigateUp() {
        val current = File(currentPath.value)
        val parent = current.parentFile
        if (parent != null && parent.canRead()) {
            navigateToFolder(parent.absolutePath)
        }
    }

    fun navigateToHome() {
        navigateToFolder(defaultRootPath)
    }

    fun selectCategory(category: FileTypeCategory, context: Context) {
        selectedCategory.value = category
        selectedFilePaths.value = emptySet()
        if (category == FileTypeCategory.TRASH) {
            loadTrashDirectory()
        } else if (category != FileTypeCategory.ALL) {
            loadCategoryFilesRecursively(category)
        } else {
            refreshCurrentDirectory()
        }
    }

    private fun loadCategoryFilesRecursively(category: FileTypeCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            val root = File(defaultRootPath)
            val allFiles = mutableListOf<FileItem>()
            
            fun scanDir(dir: File, maxDepth: Int = 3) {
                if (maxDepth <= 0 || !dir.exists() || !dir.isDirectory) return
                val list = dir.listFiles() ?: return
                for (file in list) {
                    if (file.name.startsWith(".")) continue
                    if (file.isDirectory) {
                        scanDir(file, maxDepth - 1)
                    } else {
                        val item = FileItem(file = file)
                        if (matchesCategory(item, category)) {
                            allFiles.add(item)
                        }
                    }
                }
            }

            scanDir(root)
            rawFileList.value = allFiles
        }
    }

    private fun matchesCategory(item: FileItem, category: FileTypeCategory): Boolean {
        if (item.isDirectory) return false
        return when (category) {
            FileTypeCategory.IMAGES -> item.fileTypeGroup == FileTypeGroup.IMAGE
            FileTypeCategory.VIDEOS -> item.fileTypeGroup == FileTypeGroup.VIDEO
            FileTypeCategory.AUDIO -> item.fileTypeGroup == FileTypeGroup.AUDIO
            FileTypeCategory.DOCUMENTS -> item.fileTypeGroup == FileTypeGroup.DOCUMENT
            FileTypeCategory.DOWNLOADS -> item.path.contains("/Download", ignoreCase = true)
            FileTypeCategory.RECENTS -> (System.currentTimeMillis() - item.lastModified) < 7 * 24 * 60 * 60 * 1000L // last 7 days
            FileTypeCategory.LARGE_FILES -> item.size > 50 * 1024 * 1024L // >50MB
            else -> true
        }
    }

    // --- File Operations ---
    fun createNewFolder(folderName: String, context: Context) {
        if (folderName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val newDir = File(currentPath.value, folderName.trim())
            if (!newDir.exists()) {
                val success = newDir.mkdirs()
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(context, "Folder '$folderName' berhasil dibuat", Toast.LENGTH_SHORT).show()
                        refreshCurrentDirectory()
                    } else {
                        Toast.makeText(context, "Gagal membuat folder", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Folder dengan nama tersebut sudah ada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun renameFile(fileItem: FileItem, newName: String, context: Context) {
        if (newName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val target = fileItem.file
            val newFile = File(target.parentFile, newName.trim())
            if (!newFile.exists()) {
                val success = target.renameTo(newFile)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(context, "Nama berhasil diubah", Toast.LENGTH_SHORT).show()
                        refreshCurrentDirectory()
                    } else {
                        Toast.makeText(context, "Gagal mengubah nama file", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "File dengan nama baru tersebut sudah ada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Trash/Sampah Management ---
    private fun getTrashDir(): File {
        val trashFolder = File(defaultRootPath, ".atbkz_trash")
        if (!trashFolder.exists()) trashFolder.mkdirs()
        return trashFolder
    }

    fun moveToTrash(fileItems: List<FileItem>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val trashDir = getTrashDir()
            var count = 0
            fileItems.forEach { item ->
                try {
                    val dest = File(trashDir, "${System.currentTimeMillis()}_${item.name}")
                    if (item.file.renameTo(dest)) {
                        count++
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "$count file dipindahkan ke Sampah", Toast.LENGTH_SHORT).show()
                selectedFilePaths.value = emptySet()
                refreshCurrentDirectory()
            }
        }
    }

    fun restoreFromTrash(fileItem: FileItem, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val originalName = fileItem.name.substringAfter("_", fileItem.name)
            val targetDir = File(defaultRootPath)
            val dest = File(targetDir, originalName)
            if (fileItem.file.renameTo(dest)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "File dipulihkan ke $defaultRootPath/$originalName", Toast.LENGTH_LONG).show()
                    loadTrashDirectory()
                }
            }
        }
    }

    fun deletePermanently(fileItems: List<FileItem>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            var count = 0
            fileItems.forEach { item ->
                if (item.file.deleteRecursively()) {
                    count++
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "$count file dihapus permanen", Toast.LENGTH_SHORT).show()
                selectedFilePaths.value = emptySet()
                if (selectedCategory.value == FileTypeCategory.TRASH) {
                    loadTrashDirectory()
                } else {
                    refreshCurrentDirectory()
                }
            }
        }
    }

    fun emptyTrash(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val trashDir = getTrashDir()
            val files = trashDir.listFiles() ?: emptyArray()
            var deletedCount = 0
            files.forEach { file ->
                if (file.deleteRecursively()) deletedCount++
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "$deletedCount file di folder Sampah berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                loadTrashDirectory()
            }
        }
    }

    private fun loadTrashDirectory() {
        viewModelScope.launch(Dispatchers.IO) {
            val trashDir = getTrashDir()
            val files = trashDir.listFiles()?.map { file ->
                FileItem(
                    file = file,
                    isTrash = true,
                    originalPath = file.name.substringAfter("_", file.name)
                )
            } ?: emptyList()
            rawFileList.value = files
        }
    }

    // --- Copy / Move Operations ---
    fun copySelectedFilesToClipboard(sources: List<FileItem>, isCut: Boolean) {
        clipboardSources.value = sources
        isCutOperation.value = isCut
        selectedFilePaths.value = emptySet()
    }

    fun pasteClipboardFiles(context: Context) {
        val sources = clipboardSources.value
        if (sources.isEmpty()) return
        val targetFolder = File(currentPath.value)

        viewModelScope.launch(Dispatchers.IO) {
            var successCount = 0
            sources.forEach { source ->
                val destFile = File(targetFolder, source.name)
                if (isCutOperation.value) {
                    if (source.file.renameTo(destFile)) successCount++
                } else {
                    try {
                        source.file.copyRecursively(destFile, overwrite = true)
                        successCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            val actionName = if (isCutOperation.value) "dipindahkan" else "disalin"
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "$successCount file berhasil $actionName", Toast.LENGTH_SHORT).show()
                clipboardSources.value = emptyList()
                refreshCurrentDirectory()
            }
        }
    }

    // --- Compression (ZIP / Unzip) ---
    fun compressSelectedToZip(sources: List<FileItem>, zipName: String, context: Context) {
        val cleanZipName = if (zipName.lowercase().endsWith(".zip")) zipName else "$zipName.zip"
        val zipFile = File(currentPath.value, cleanZipName)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                    sources.forEach { item ->
                        addFileToZip(item.file, "", zos)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "ZIP berhasil dibuat: ${zipFile.name}", Toast.LENGTH_LONG).show()
                    selectedFilePaths.value = emptySet()
                    refreshCurrentDirectory()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal membuat ZIP: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addFileToZip(file: File, parentPath: String, zos: ZipOutputStream) {
        val entryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
        if (file.isDirectory) {
            val children = file.listFiles() ?: return
            for (child in children) {
                addFileToZip(child, entryName, zos)
            }
        } else {
            val buffer = ByteArray(8192)
            FileInputStream(file).use { fis ->
                zos.putNextEntry(ZipEntry(entryName))
                var length: Int
                while (fis.read(buffer).also { length = it } > 0) {
                    zos.write(buffer, 0, length)
                }
                zos.closeEntry()
            }
        }
    }

    fun extractZip(zipFileItem: FileItem, context: Context) {
        val targetFolderName = zipFileItem.name.substringBeforeLast(".") + "_extracted"
        val destDir = File(currentPath.value, targetFolderName)
        if (!destDir.exists()) destDir.mkdirs()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                ZipInputStream(BufferedInputStream(FileInputStream(zipFileItem.file))).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    val buffer = ByteArray(8192)
                    while (entry != null) {
                        val newFile = File(destDir, entry.name)
                        if (entry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            newFile.parentFile?.mkdirs()
                            FileOutputStream(newFile).use { fos ->
                                var len: Int
                                while (zis.read(buffer).also { len = it } > 0) {
                                    fos.write(buffer, 0, len)
                                }
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ekstraksi berhasil ke '$targetFolderName'", Toast.LENGTH_LONG).show()
                    refreshCurrentDirectory()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal mengekstrak: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Share File via FileProvider ---
    fun shareFiles(fileItems: List<FileItem>, context: Context) {
        try {
            val uris = fileItems.map { item ->
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    item.file
                )
            }

            if (uris.isEmpty()) return

            val intent = Intent().apply {
                if (uris.size == 1) {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uris.first())
                    type = getMimeType(fileItems.first().file)
                } else {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                    type = "*/*"
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan File via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }

    // --- Selection State ---
    fun toggleSelect(path: String) {
        val current = selectedFilePaths.value.toMutableSet()
        if (current.contains(path)) current.remove(path) else current.add(path)
        selectedFilePaths.value = current
    }

    fun selectAll() {
        val allPaths = rawFileList.value.map { it.path }.toSet()
        selectedFilePaths.value = allPaths
    }

    fun clearSelection() {
        selectedFilePaths.value = emptySet()
    }

    // --- Storage Analyzer & Duplicate Scanner ---
    fun runStorageAnalysis() {
        isAnalyzing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val root = File(defaultRootPath)
            var totalSpace = 0L
            var freeSpace = 0L
            try {
                totalSpace = root.totalSpace
                freeSpace = root.freeSpace
            } catch (e: Exception) {
                e.printStackTrace()
            }

            var imgBytes = 0L
            var vidBytes = 0L
            var audBytes = 0L
            var docBytes = 0L
            var archiveBytes = 0L
            var otherBytes = 0L

            val largeFiles = mutableListOf<FileItem>()
            val candidatesForDuplicate = mutableMapOf<Long, MutableList<File>>()

            fun scanDir(dir: File, maxDepth: Int = 4) {
                if (maxDepth <= 0 || !dir.exists() || !dir.isDirectory) return
                val list = dir.listFiles() ?: return
                for (file in list) {
                    if (file.name.startsWith(".")) continue
                    if (file.isDirectory) {
                        scanDir(file, maxDepth - 1)
                    } else {
                        val length = file.length()
                        val group = determineFileTypeGroup(file)
                        when (group) {
                            FileTypeGroup.IMAGE -> imgBytes += length
                            FileTypeGroup.VIDEO -> vidBytes += length
                            FileTypeGroup.AUDIO -> audBytes += length
                            FileTypeGroup.DOCUMENT -> docBytes += length
                            FileTypeGroup.ARCHIVE -> archiveBytes += length
                            else -> otherBytes += length
                        }

                        if (length > 50 * 1024 * 1024L) {
                            largeFiles.add(FileItem(file = file))
                        }

                        // Collect for duplicate check if size > 100KB
                        if (length > 100 * 1024L) {
                            candidatesForDuplicate.getOrPut(length) { mutableListOf() }.add(file)
                        }
                    }
                }
            }

            scanDir(root)

            // Detect Duplicates via MD5 / SHA-256 Hash
            val duplicateGroups = mutableListOf<DuplicateGroup>()
            candidatesForDuplicate.filter { it.value.size > 1 }.forEach { (size, fileList) ->
                val hashGroups = mutableMapOf<String, MutableList<File>>()
                for (f in fileList) {
                    val hash = computeFileHash(f)
                    if (hash.isNotEmpty()) {
                        hashGroups.getOrPut(hash) { mutableListOf() }.add(f)
                    }
                }
                hashGroups.filter { it.value.size > 1 }.forEach { (hash, dupFiles) ->
                    duplicateGroups.add(
                        DuplicateGroup(
                            fileHash = hash,
                            fileSize = size,
                            files = dupFiles.map { FileItem(file = it) }
                        )
                    )
                }
            }

            val analysis = StorageAnalysis(
                totalBytes = totalSpace,
                freeBytes = freeSpace,
                usedBytes = totalSpace - freeSpace,
                imagesSize = imgBytes,
                videosSize = vidBytes,
                audioSize = audBytes,
                docsSize = docBytes,
                archivesSize = archiveBytes,
                otherSize = otherBytes,
                largeFiles = largeFiles.sortedByDescending { it.size },
                duplicateGroups = duplicateGroups.sortedByDescending { it.fileSize * it.files.size }
            )

            storageAnalysis.value = analysis
            isAnalyzing.value = false
        }
    }

    private fun computeFileHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var read = fis.read(buffer)
                // Hash first 1MB max for high speed scanning
                var bytesHashed = 0
                while (read != -1 && bytesHashed < 1024 * 1024) {
                    digest.update(buffer, 0, read)
                    bytesHashed += read
                    read = fis.read(buffer)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    private fun filterFilesByCategory(list: List<FileItem>, category: FileTypeCategory): List<FileItem> {
        return list.filter { matchesCategory(it, category) }
    }

    private fun sortFiles(list: List<FileItem>, sort: SortOption): List<FileItem> {
        val (dirs, files) = list.partition { it.isDirectory }
        val sortedDirs = dirs.sortedBy { it.name.lowercase() }
        val sortedFiles = when (sort) {
            SortOption.NAME_ASC -> files.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_DESC -> files.sortedByDescending { it.lastModified }
            SortOption.DATE_ASC -> files.sortedBy { it.lastModified }
            SortOption.SIZE_DESC -> files.sortedByDescending { it.size }
            SortOption.SIZE_ASC -> files.sortedBy { it.size }
        }
        return sortedDirs + sortedFiles
    }
}
