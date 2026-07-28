package com.example.ui.downloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DownloaderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DownloaderState(
    val url: String = "",
    val detectedPlatform: String = "",
    val isLoading: Boolean = false,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedFormat: String = "Video",
    val selectedResolution: String = "1080p"
)

data class SearchResultItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val platform: String,
    val durationOrArtist: String,
    val channelName: String
)

data class SearchState(
    val query: String = "",
    val selectedPlatforms: Set<String> = setOf("Semua"),
    val selectedContentType: String = "Semua",
    val results: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DownloaderViewModel : ViewModel() {
    private val _state = MutableStateFlow(DownloaderState())
    val state: StateFlow<DownloaderState> = _state.asStateFlow()
    
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val repository = DownloaderRepository()
    private var searchJob: Job? = null

    // --- Downloader Tab Methods ---
    fun onUrlChange(newUrl: String) {
        val platform = detectPlatform(newUrl)
        _state.value = _state.value.copy(
            url = newUrl, 
            detectedPlatform = platform,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onFormatChange(format: String) {
        _state.value = _state.value.copy(selectedFormat = format)
    }

    fun onResolutionChange(resolution: String) {
        _state.value = _state.value.copy(selectedResolution = resolution)
    }

    private fun detectPlatform(url: String): String {
        return when {
            url.contains("youtube.com") || url.contains("youtu.be") -> "YouTube"
            url.contains("tiktok.com") -> "TikTok"
            url.contains("instagram.com") -> "Instagram"
            url.contains("facebook.com") || url.contains("fb.watch") -> "Facebook"
            url.contains("twitter.com") || url.contains("x.com") -> "X (Twitter)"
            url.contains("pinterest.com") -> "Pinterest"
            url.contains("reddit.com") -> "Reddit"
            url.contains("vimeo.com") -> "Vimeo"
            url.contains("dailymotion.com") -> "Dailymotion"
            url.contains("soundcloud.com") -> "SoundCloud"
            url.isNotBlank() -> "Unknown Platform"
            else -> ""
        }
    }

    fun startDownload() {
        val currentUrl = _state.value.url
        if (currentUrl.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "URL cannot be empty")
            return
        }
        if (!currentUrl.startsWith("http://") && !currentUrl.startsWith("https://")) {
            _state.value = _state.value.copy(errorMessage = "Invalid URL format. Must start with http:// or https://")
            return
        }

        _state.value = _state.value.copy(isDownloading = true, downloadProgress = 0f, errorMessage = null, successMessage = null)
        
        viewModelScope.launch {
            val apiResult = repository.getDownloadUrl(
                videoUrl = currentUrl,
                format = _state.value.selectedFormat,
                resolution = _state.value.selectedResolution
            )
            
            apiResult.fold(
                onSuccess = { downloadLink ->
                    for (i in 1..100) {
                        delay(20)
                        _state.value = _state.value.copy(downloadProgress = i / 100f)
                    }
                    delay(500)
                    _state.value = _state.value.copy(
                        isDownloading = false, 
                        downloadProgress = 0f,
                        successMessage = "Download complete from API Link: $downloadLink"
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isDownloading = false,
                        downloadProgress = 0f,
                        errorMessage = "Download Failed: ${error.message}"
                    )
                }
            )
        }
    }

    // --- Search Tab Methods ---
    fun onSearchQueryChange(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
        performSearchDebounced()
    }

    fun toggleSearchPlatform(platform: String) {
        val current = _searchState.value.selectedPlatforms.toMutableSet()
        if (platform == "Semua") {
            current.clear()
            current.add("Semua")
        } else {
            current.remove("Semua")
            if (current.contains(platform)) {
                current.remove(platform)
                if (current.isEmpty()) current.add("Semua")
            } else {
                current.add(platform)
            }
        }
        _searchState.value = _searchState.value.copy(selectedPlatforms = current)
        performSearchDebounced()
    }

    fun onSearchContentTypeChange(type: String) {
        _searchState.value = _searchState.value.copy(selectedContentType = type)
        performSearchDebounced()
    }

    private fun performSearchDebounced() {
        searchJob?.cancel()
        val query = _searchState.value.query
        if (query.isBlank()) {
            _searchState.value = _searchState.value.copy(results = emptyList(), errorMessage = null, isLoading = false)
            return
        }

        searchJob = viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isLoading = true, errorMessage = null)
            delay(800) // Debounce

            // Simulate API search call (since we don't have real keys for YouTube Data API, etc.)
            // We use dummy data to represent what the real API would return.
            try {
                val mockResults = generateMockSearchResults(query)
                if (mockResults.isEmpty()) {
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        results = emptyList(),
                        errorMessage = "Tidak ditemukan. Coba kata kunci lain atau tempel link langsung."
                    )
                } else {
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        results = mockResults,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _searchState.value = _searchState.value.copy(
                    isLoading = false,
                    results = emptyList(),
                    errorMessage = "Gagal memuat hasil: ${e.message}"
                )
            }
        }
    }

    private fun generateMockSearchResults(query: String): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val platforms = _searchState.value.selectedPlatforms
        val hasAll = platforms.contains("Semua")

        if (hasAll || platforms.contains("YouTube")) {
            results.add(SearchResultItem("yt1", "$query - Official Video", "https://picsum.photos/seed/yt1/320/180", "YouTube", "4:32", "OfficialChannel"))
            results.add(SearchResultItem("yt2", "$query - Live Performance", "https://picsum.photos/seed/yt2/320/180", "YouTube", "5:12", "MusicLive"))
        }
        if (hasAll || platforms.contains("Instagram")) {
            results.add(SearchResultItem("ig1", "$query Reels", "https://picsum.photos/seed/ig1/320/320", "Instagram", "0:45", "user_ig_123"))
        }
        if (hasAll || platforms.contains("TikTok")) {
            results.add(SearchResultItem("tt1", "$query Dance Challenge", "https://picsum.photos/seed/tt1/320/480", "TikTok", "0:30", "tiktok_star"))
        }
        if (hasAll || platforms.contains("X (Twitter)")) {
            results.add(SearchResultItem("tw1", "$query Trending clip", "https://picsum.photos/seed/tw1/320/240", "X (Twitter)", "0:15", "@news_update"))
        }
        
        // Filter by content type if needed
        val type = _searchState.value.selectedContentType
        if (type == "Music") {
            return results.filter { it.title.contains("Music") || it.title.contains("Official") }
        } else if (type == "Video") {
            return results.filter { !it.title.contains("Music") }
        }

        return results
    }
}
