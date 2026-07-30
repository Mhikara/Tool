package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "process_history")
data class ProcessHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val processType: String,
    val featureName: String,
    val status: String,
    val details: String = "",
    val inputPath: String = "",
    val outputPath: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
