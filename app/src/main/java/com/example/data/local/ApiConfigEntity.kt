package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_configs")
data class ApiConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val provider: String,
    val baseUrl: String,
    val apiKey: String,
    val secretKey: String = "",
    val bearerToken: String = "",
    val isActive: Boolean = true,
    val timeoutMs: Long = 30000,
    val retryLimit: Int = 3,
    val successCount: Int = 0,
    val errorCount: Int = 0,
    val lastPingMs: Long = 0,
    val status: String = "Active"
)
