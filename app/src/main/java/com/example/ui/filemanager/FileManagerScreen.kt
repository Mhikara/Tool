package com.example.ui.filemanager

import android.content.Context
import android.text.format.Formatter
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.flow.StateFlow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    onBack: () -> Unit,
    onNavigateToTool: (String) -> Unit = {},
    viewModel: FileManagerViewModel = viewModel()
) {
    val context = LocalContext.current
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val displayedFiles by viewModel.displayedFiles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPaths by viewModel.selectedFilePaths.collectAsState()
    val clipboardSources by viewModel.clipboardSources.collectAsState()
    val storageAnalysis by viewModel.storageAnalysis.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var showSearchField by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<FileItem?>(null) }
    var showZipDialog by remember { mutableStateOf(false) }
    var showPreviewFile by remember { mutableStateOf<FileItem?>(null) }
    var showAnalyzerSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var actionFileItem by remember { mutableStateOf<FileItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.checkPermissionState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchField) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text("Cari file...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.searchQuery.value = ""
                                    showSearchField = false
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Tutup Cari")
                                }
                            }
                        )
                    } else {
                        Column {
                            Text(
                                text = "File Manager",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = if (selectedCategory == FileTypeCategory.TRASH) "Folder Sampah" else currentPath,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (!showSearchField) {
                        IconButton(onClick = { showSearchField = true }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Cari File")
                        }
                        IconButton(onClick = {
                            viewModel.runStorageAnalysis()
                            showAnalyzerSheet = true
                        }) {
                            Icon(Icons.Outlined.Analytics, contentDescription = "Analisis Penyimpanan")
                        }
                        IconButton(onClick = {
                            viewModel.viewMode.value = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                        }) {
                            Icon(
                                imageVector = if (viewMode == ViewMode.LIST) Icons.Outlined.GridView else Icons.Outlined.ViewList,
                                contentDescription = "Mode Tampilan"
                            )
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Outlined.Sort, contentDescription = "Urutkan")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        viewModel.sortOption.value = option
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (sortOption == option) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedCategory != FileTypeCategory.TRASH) {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.CreateNewFolder, contentDescription = "Folder Baru")
                }
            }
        },
        bottomBar = {
            if (selectedPaths.isNotEmpty()) {
                MultiSelectBottomBar(
                    selectedCount = selectedPaths.size,
                    isTrashCategory = selectedCategory == FileTypeCategory.TRASH,
                    onCopy = {
                        val items = displayedFiles.filter { selectedPaths.contains(it.path) }
                        viewModel.copySelectedFilesToClipboard(items, isCut = false)
                    },
                    onCut = {
                        val items = displayedFiles.filter { selectedPaths.contains(it.path) }
                        viewModel.copySelectedFilesToClipboard(items, isCut = true)
                    },
                    onZip = { showZipDialog = true },
                    onShare = {
                        val items = displayedFiles.filter { selectedPaths.contains(it.path) }
                        viewModel.shareFiles(items, context)
                    },
                    onDelete = {
                        val items = displayedFiles.filter { selectedPaths.contains(it.path) }
                        if (selectedCategory == FileTypeCategory.TRASH) {
                            viewModel.deletePermanently(items, context)
                        } else {
                            viewModel.moveToTrash(items, context)
                        }
                    },
                    onRestore = {
                        val items = displayedFiles.filter { selectedPaths.contains(it.path) }
                        items.forEach { viewModel.restoreFromTrash(it, context) }
                    },
                    onClear = { viewModel.clearSelection() }
                )
            } else if (clipboardSources.isNotEmpty()) {
                PasteBottomBar(
                    itemCount = clipboardSources.size,
                    isCut = viewModel.isCutOperation.value,
                    onPaste = { viewModel.pasteClipboardFiles(context) },
                    onCancel = { viewModel.clipboardSources.value = emptyList() }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Check storage permission banner if missing
            if (!isPermissionGranted) {
                PermissionRequestBanner(
                    onRequestPermission = { viewModel.openStoragePermissionSettings(context) }
                )
            }

            // Quick Category Filter Bar
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelect = { viewModel.selectCategory(it, context) }
            )

            // Breadcrumb Navigation Row (for file browsing)
            if (selectedCategory == FileTypeCategory.ALL) {
                BreadcrumbBar(
                    currentPath = currentPath,
                    onPathClick = { viewModel.navigateToFolder(it) },
                    onHomeClick = { viewModel.navigateToHome() }
                )
            }

            // Trash Banner if in Trash
            if (selectedCategory == FileTypeCategory.TRASH) {
                TrashHeaderBanner(
                    itemCount = displayedFiles.size,
                    onEmptyTrash = { viewModel.emptyTrash(context) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File Content List/Grid View
            if (displayedFiles.isEmpty()) {
                EmptyDirectoryView(category = selectedCategory)
            } else {
                if (viewMode == ViewMode.LIST) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedFiles, key = { it.path }) { item ->
                            FileListItem(
                                item = item,
                                onClick = {
                                    if (selectedPaths.isNotEmpty()) {
                                        viewModel.toggleSelect(item.path)
                                    } else if (item.isDirectory) {
                                        viewModel.navigateToFolder(item.path)
                                    } else {
                                        actionFileItem = item
                                    }
                                },
                                onLongClick = { viewModel.toggleSelect(item.path) },
                                onActionClick = { actionFileItem = item }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(110.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedFiles, key = { it.path }) { item ->
                            FileGridItem(
                                item = item,
                                onClick = {
                                    if (selectedPaths.isNotEmpty()) {
                                        viewModel.toggleSelect(item.path)
                                    } else if (item.isDirectory) {
                                        viewModel.navigateToFolder(item.path)
                                    } else {
                                        actionFileItem = item
                                    }
                                },
                                onLongClick = { viewModel.toggleSelect(item.path) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs & Sheets ---
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                viewModel.createNewFolder(name, context)
                showCreateFolderDialog = false
            }
        )
    }

    if (showRenameDialog != null) {
        RenameFileDialog(
            fileItem = showRenameDialog!!,
            onDismiss = { showRenameDialog = null },
            onRename = { newName ->
                viewModel.renameFile(showRenameDialog!!, newName, context)
                showRenameDialog = null
            }
        )
    }

    if (showZipDialog) {
        CompressZipDialog(
            onDismiss = { showZipDialog = false },
            onCompress = { zipName ->
                val selectedItems = displayedFiles.filter { selectedPaths.contains(it.path) }
                viewModel.compressSelectedToZip(selectedItems, zipName, context)
                showZipDialog = false
            }
        )
    }

    if (actionFileItem != null) {
        QuickFileActionModal(
            fileItem = actionFileItem!!,
            onDismiss = { actionFileItem = null },
            onPreview = {
                showPreviewFile = actionFileItem
                actionFileItem = null
            },
            onRename = {
                showRenameDialog = actionFileItem
                actionFileItem = null
            },
            onDelete = {
                viewModel.moveToTrash(listOf(actionFileItem!!), context)
                actionFileItem = null
            },
            onShare = {
                viewModel.shareFiles(listOf(actionFileItem!!), context)
                actionFileItem = null
            },
            onExtractZip = {
                viewModel.extractZip(actionFileItem!!, context)
                actionFileItem = null
            },
            onNavigateToTool = { route ->
                actionFileItem = null
                onNavigateToTool(route)
            }
        )
    }

    if (showPreviewFile != null) {
        FilePreviewDialog(
            fileItem = showPreviewFile!!,
            onDismiss = { showPreviewFile = null },
            onShare = { viewModel.shareFiles(listOf(showPreviewFile!!), context) }
        )
    }

    if (showAnalyzerSheet) {
        StorageAnalyzerDialog(
            isAnalyzing = isAnalyzing,
            analysis = storageAnalysis,
            onDismiss = { showAnalyzerSheet = false },
            onDeleteDuplicates = { duplicateFiles ->
                viewModel.deletePermanently(duplicateFiles, context)
                viewModel.runStorageAnalysis()
            }
        )
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: FileTypeCategory,
    onCategorySelect: (FileTypeCategory) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FileTypeCategory.values()) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(category) },
                label = { Text(category.displayName) },
                leadingIcon = {
                    val icon = when (category) {
                        FileTypeCategory.ALL -> Icons.Outlined.Folder
                        FileTypeCategory.IMAGES -> Icons.Outlined.Image
                        FileTypeCategory.VIDEOS -> Icons.Outlined.VideoFile
                        FileTypeCategory.AUDIO -> Icons.Outlined.AudioFile
                        FileTypeCategory.DOCUMENTS -> Icons.Outlined.Description
                        FileTypeCategory.DOWNLOADS -> Icons.Outlined.Download
                        FileTypeCategory.RECENTS -> Icons.Outlined.Schedule
                        FileTypeCategory.LARGE_FILES -> Icons.Outlined.Storage
                        FileTypeCategory.TRASH -> Icons.Outlined.Delete
                    }
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BreadcrumbBar(
    currentPath: String,
    onPathClick: (String) -> Unit,
    onHomeClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val segments = currentPath.split("/").filter { it.isNotEmpty() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onHomeClick,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Outlined.Home, contentDescription = "Home", modifier = Modifier.size(18.dp))
        }

        var accumulatedPath = ""
        segments.forEach { segment ->
            accumulatedPath += "/$segment"
            val thisPath = accumulatedPath
            Text("/", modifier = Modifier.padding(horizontal = 4.dp), color = Color.Gray, fontSize = 12.sp)
            Text(
                text = segment,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (thisPath == currentPath) FontWeight.Bold else FontWeight.Normal,
                color = if (thisPath == currentPath) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).combinedClickable(onClick = { onPathClick(thisPath) })
            )
        }
    }
}

@Composable
fun PermissionRequestBanner(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Izin Akses File Diperlukan",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Beri izin MANAGE_EXTERNAL_STORAGE agar File Manager dapat mengelola seluruh file di perangkat.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Izinkan", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TrashHeaderBanner(itemCount: Int, onEmptyTrash: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Sampah sementara ($itemCount file)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("File di sini dapat dipulihkan atau dihapus permanen.", style = MaterialTheme.typography.labelSmall)
            }
            if (itemCount > 0) {
                OutlinedButton(onClick = onEmptyTrash) {
                    Text("Kosongkan", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    item: FileItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val context = LocalContext.current
    val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.lastModified))
    val formattedSize = if (item.isDirectory) "" else Formatter.formatFileSize(context, item.size)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                FileTypeIcon(item.fileTypeGroup, size = 36)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = formattedDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    if (formattedSize.isNotEmpty()) {
                        Text(text = "• $formattedSize", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            IconButton(onClick = onActionClick) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Opsi", tint = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    item: FileItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val formattedSize = if (item.isDirectory) "" else Formatter.formatFileSize(context, item.size)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                FileTypeIcon(item.fileTypeGroup, size = 44)
                if (item.isSelected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
            if (formattedSize.isNotEmpty()) {
                Text(
                    text = formattedSize,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun FileTypeIcon(group: FileTypeGroup, size: Int = 36) {
    val (icon, color) = when (group) {
        FileTypeGroup.FOLDER -> Icons.Filled.Folder to Color(0xFFFFB300)
        FileTypeGroup.IMAGE -> Icons.Filled.Image to Color(0xFF4CAF50)
        FileTypeGroup.VIDEO -> Icons.Filled.VideoFile to Color(0xFFE91E63)
        FileTypeGroup.AUDIO -> Icons.Filled.AudioFile to Color(0xFF9C27B0)
        FileTypeGroup.DOCUMENT -> Icons.Filled.Description to Color(0xFF2196F3)
        FileTypeGroup.ARCHIVE -> Icons.Filled.FolderZip to Color(0xFFFF5722)
        FileTypeGroup.APK -> Icons.Filled.Android to Color(0xFF8BC34A)
        FileTypeGroup.OTHER -> Icons.Filled.InsertDriveFile to Color(0xFF607D8B)
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size((size * 0.6).dp))
    }
}

@Composable
fun EmptyDirectoryView(category: FileTypeCategory) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (category == FileTypeCategory.TRASH) "Tidak ada file di Sampah" else "Folder ini kosong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MultiSelectBottomBar(
    selectedCount: Int,
    isTrashCategory: Boolean,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onZip: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "Batal")
                }
                Text("$selectedCount dipilih", fontWeight = FontWeight.Bold)
            }

            Row {
                if (isTrashCategory) {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Outlined.Restore, contentDescription = "Pulihkan")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.DeleteForever, contentDescription = "Hapus Permanen", tint = Color.Red)
                    }
                } else {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Salin")
                    }
                    IconButton(onClick = onCut) {
                        Icon(Icons.Outlined.ContentCut, contentDescription = "Potong")
                    }
                    IconButton(onClick = onZip) {
                        Icon(Icons.Outlined.FolderZip, contentDescription = "Kompres ZIP")
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Outlined.Share, contentDescription = "Bagikan")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Ke Sampah", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun PasteBottomBar(
    itemCount: Int,
    isCut: Boolean,
    onPaste: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$itemCount file siap ${if (isCut) "dipindahkan" else "disalin"}", fontWeight = FontWeight.SemiBold)
            Row {
                TextButton(onClick = onCancel) {
                    Text("Batal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onPaste) {
                    Text("Tempel Di Sini")
                }
            }
        }
    }
}

@Composable
fun QuickFileActionModal(
    fileItem: FileItem,
    onDismiss: () -> Unit,
    onPreview: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExtractZip: () -> Unit,
    onNavigateToTool: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(fileItem.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onPreview,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Pratinjau / Preview Cepat")
                    }
                }

                // Tool Integration Shortcuts
                when (fileItem.fileTypeGroup) {
                    FileTypeGroup.IMAGE -> {
                        TextButton(onClick = { onNavigateToTool("imagemaker") }, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Buka di Image Tools")
                            }
                        }
                    }
                    FileTypeGroup.VIDEO -> {
                        TextButton(onClick = { onNavigateToTool("tools") }, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.VideoFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Buka di Video Tools")
                            }
                        }
                    }
                    FileTypeGroup.AUDIO -> {
                        TextButton(onClick = { onNavigateToTool("tools") }, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Buka di Audio Tools")
                            }
                        }
                    }
                    FileTypeGroup.ARCHIVE -> {
                        TextButton(onClick = onExtractZip, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Ekstrak ZIP Di Sini")
                            }
                        }
                    }
                    else -> {}
                }

                TextButton(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Bagikan")
                    }
                }

                TextButton(onClick = onRename, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Ubah Nama")
                    }
                }

                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Pindahkan ke Sampah", color = Color.Red)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun FilePreviewDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val formattedSize = Formatter.formatFileSize(context, fileItem.size)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pratinjau File", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FileTypeIcon(fileItem.fileTypeGroup, size = 64)
                Spacer(modifier = Modifier.height(12.dp))
                Text(fileItem.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ukuran: $formattedSize", style = MaterialTheme.typography.labelMedium)
                Text("Lokasi: ${fileItem.path}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                if (fileItem.fileTypeGroup == FileTypeGroup.DOCUMENT && fileItem.size < 100 * 1024L) {
                    val content = remember(fileItem) {
                        try {
                            fileItem.file.readText().take(1000)
                        } catch (e: Exception) {
                            "Tidak dapat membaca isi teks."
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Text(
                        "Gunakan aplikasi pendukung untuk melihat atau mengedit konten file ini secara penuh.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onShare) {
                Text("Bagikan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun StorageAnalyzerDialog(
    isAnalyzing: Boolean,
    analysis: StorageAnalysis?,
    onDismiss: () -> Unit,
    onDeleteDuplicates: (List<FileItem>) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analisis Penyimpanan")
            }
        },
        text = {
            if (isAnalyzing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Memindai penyimpanan & deteksi file duplikat...")
                }
            } else if (analysis != null) {
                val item = analysis
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val totalStr = Formatter.formatFileSize(context, item.totalBytes)
                    val freeStr = Formatter.formatFileSize(context, item.freeBytes)
                    val usedStr = Formatter.formatFileSize(context, item.usedBytes)

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Ruang Penyimpanan", style = MaterialTheme.typography.labelMedium)
                            Text("$usedStr / $totalStr Terpakai", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Sisa ruang bebas: $freeStr", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Text("Kategori Pemakaian", fontWeight = FontWeight.Bold)

                    StorageCategoryRow("Gambar", item.imagesSize, context)
                    StorageCategoryRow("Video", item.videosSize, context)
                    StorageCategoryRow("Audio", item.audioSize, context)
                    StorageCategoryRow("Dokumen", item.docsSize, context)
                    StorageCategoryRow("Arsip (ZIP)", item.archivesSize, context)

                    if (item.duplicateGroups.isNotEmpty()) {
                        Divider()
                        Text("Deteksi File Duplikat (${item.duplicateGroups.size} kelompok)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        item.duplicateGroups.take(5).forEach { group ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Hash: ${group.fileHash.take(8)}... (${group.files.size} file sama)", fontWeight = FontWeight.SemiBold)
                                    group.files.forEach { file ->
                                        Text("• ${file.name}", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            val toRemove = group.files.drop(1)
                                            onDeleteDuplicates(toRemove)
                                        }
                                    ) {
                                        Text("Hapus Duplikat Ekstra", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text("Gagal memuat data analisis.")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun StorageCategoryRow(label: String, bytes: Long, context: Context) {
    val sizeStr = Formatter.formatFileSize(context, bytes)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(sizeStr, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CreateFolderDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Folder Baru") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Folder") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onCreate(name) }) {
                Text("Buat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun RenameFileDialog(fileItem: FileItem, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    var newName by remember { mutableStateOf(fileItem.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Nama") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nama Baru") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onRename(newName) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun CompressZipDialog(onDismiss: () -> Unit, onCompress: (String) -> Unit) {
    var name by remember { mutableStateOf("Arsip_Baru") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kompres ke ZIP") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama File ZIP") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onCompress(name) }) {
                Text("Kompres")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
