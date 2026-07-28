package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_history")
data class DownloadHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val platform: String,
    val filePath: String,
    val fileType: String,
    val fileSize: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true
)
