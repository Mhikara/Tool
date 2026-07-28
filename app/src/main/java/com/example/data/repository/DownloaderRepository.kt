package com.example.data.repository

import com.example.data.api.DownloaderApiConfig
import com.example.data.api.DownloaderProvider
import kotlinx.coroutines.delay

class DownloaderRepository {

    // Store configurations for different API providers
    private val apiConfigs = mutableMapOf(
        DownloaderProvider.COBALT to DownloaderApiConfig(
            provider = DownloaderProvider.COBALT,
            baseUrl = "https://api.cobalt.tools",
            retryLimit = 3
        ),
        DownloaderProvider.RAPID_API to DownloaderApiConfig(
            provider = DownloaderProvider.RAPID_API,
            baseUrl = "https://rapidapi.com",
            apiKey = "YOUR_RAPID_API_KEY",
            retryLimit = 2
        ),
        DownloaderProvider.APIFY to DownloaderApiConfig(
            provider = DownloaderProvider.APIFY,
            baseUrl = "https://api.apify.com",
            bearerToken = "YOUR_APIFY_TOKEN",
            retryLimit = 2
        )
    )

    // Simulate an API call with Auto API Rotation, Failover, and Retry
    suspend fun getDownloadUrl(videoUrl: String, format: String, resolution: String): Result<String> {
        var lastException: Exception? = null
        val enabledProviders = apiConfigs.values.filter { it.isEnabled }.sortedByDescending { it.provider == DownloaderProvider.COBALT }

        for (config in enabledProviders) {
            try {
                // Simulate network request with the current provider
                return performRequest(config, videoUrl, format, resolution)
            } catch (e: Exception) {
                lastException = e
                // Failover: Loop continues to the next provider
                println("Provider ${config.provider} failed: ${e.message}. Trying next...")
            }
        }
        
        return Result.failure(lastException ?: Exception("All API providers failed or none are enabled."))
    }

    private suspend fun performRequest(
        config: DownloaderApiConfig, 
        videoUrl: String, 
        format: String,
        resolution: String
    ): Result<String> {
        // Auto Retry Mechanism
        val retries = config.retryLimit
        var requestException: Exception? = null

        for (attempt in 1..retries) {
            try {
                // Simulate Request Timeout and Rate Limiting processing
                delay(1000) // Simulate network delay
                
                if (videoUrl.isBlank()) throw Exception("Invalid URL")
                
                // Simulated validation for error handling
                if (!videoUrl.startsWith("http")) {
                    throw Exception("Link tidak valid")
                }
                
                // In a real implementation, we would use Retrofit or Ktor here.
                // Request would include:
                // Header("Authorization: Bearer ${config.bearerToken}")
                // Header("x-api-key: ${config.apiKey}")
                
                // Simulate successful response extraction
                val fakeDownloadLink = "https://example.com/download_media?url=${videoUrl}&format=$format&res=$resolution&provider=${config.provider}"
                return Result.success(fakeDownloadLink)
                
            } catch (e: Exception) {
                requestException = e
                if (attempt == retries) {
                    throw e // Exhausted retries
                }
                // Backoff before retry
                delay(1500) 
            }
        }
        throw requestException ?: Exception("Request failed")
    }
}
