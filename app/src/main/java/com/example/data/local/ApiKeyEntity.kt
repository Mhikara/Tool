package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val serviceProvider: String,
    val category: String,
    val apiKey: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
