package com.example.data.api

enum class DownloaderProvider {
    COBALT,
    RAPID_API,
    APIFY,
    CUSTOM
}

data class DownloaderApiConfig(
    val provider: DownloaderProvider,
    val baseUrl: String,
    val apiKey: String? = null,
    val bearerToken: String? = null,
    val requestTimeoutMs: Long = 30000,
    val retryLimit: Int = 3,
    val maxConcurrentRequests: Int = 5,
    val isEnabled: Boolean = true
)
