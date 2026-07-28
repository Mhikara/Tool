package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_api_configs")
data class WebApiConfigEntity(
    @PrimaryKey val serviceName: String, // Identifier unik (misal: "Weather Service XYZ")
    val providerName: String,            // Nama penyedia (misal: "OpenWeatherMap")
    val baseUrl: String,                 // URL dasar layanan
    val encryptedApiKey: String,         // API Key yang sudah dienkripsi
    val authHeaderName: String?,         // Opsi: Nama header custom (misal: "Authorization", "x-api-key")
    val authHeaderPrefix: String?,       // Opsi: Prefix token (misal: "Bearer ")
    val status: String = "ACTIVE"        // Status: ACTIVE, INACTIVE, ERROR
)
