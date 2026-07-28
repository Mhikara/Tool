package com.example.ui.spotify

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.spotify.SpotifyRepository
import com.example.data.api.spotify.SpotifyTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

import com.example.data.api.spotify.SpotifyAlbum
import com.example.data.api.spotify.SpotifyArtist
import com.example.data.api.spotify.SpotifyPlaylist

data class SpotifyState(
    val urlInput: String = "",
    val track: SpotifyTrack? = null,
    val album: SpotifyAlbum? = null,
    val artist: SpotifyArtist? = null,
    val playlist: SpotifyPlaylist? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val clientId: String = "",
    val clientSecret: String = "",
    val isPlayingPreview: Boolean = false
)

class SpotifyViewModel : ViewModel() {
    private val _state = MutableStateFlow(SpotifyState())
    val state: StateFlow<SpotifyState> = _state
    
    private val repository = SpotifyRepository()
    private var mediaPlayer: MediaPlayer? = null

    fun onUrlChange(url: String) {
        _state.update { it.copy(urlInput = url, errorMessage = null) }
        extractAndFetchTrack(url)
    }
    
    fun onClientIdChange(id: String) {
        _state.update { it.copy(clientId = id) }
    }
    
    fun onClientSecretChange(secret: String) {
        _state.update { it.copy(clientSecret = secret) }
    }

    private fun extractAndFetchTrack(url: String) {
        if (!url.contains("spotify.com")) return
        
        val currentState = _state.value
        if (currentState.clientId.isBlank() || currentState.clientSecret.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter Spotify Client ID and Secret in settings to fetch metadata.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, track = null, album = null, artist = null, playlist = null) }
            try {
                when {
                    url.contains("/track/") -> {
                        val id = extractId(url, "track")
                        if (id != null) {
                            val track = repository.getTrackDetails(id, currentState.clientId, currentState.clientSecret)
                            _state.update { it.copy(isLoading = false, track = track) }
                        }
                    }
                    url.contains("/album/") -> {
                        val id = extractId(url, "album")
                        if (id != null) {
                            val album = repository.getAlbumDetails(id, currentState.clientId, currentState.clientSecret)
                            _state.update { it.copy(isLoading = false, album = album) }
                        }
                    }
                    url.contains("/artist/") -> {
                        val id = extractId(url, "artist")
                        if (id != null) {
                            val artist = repository.getArtistDetails(id, currentState.clientId, currentState.clientSecret)
                            _state.update { it.copy(isLoading = false, artist = artist) }
                        }
                    }
                    url.contains("/playlist/") -> {
                        val id = extractId(url, "playlist")
                        if (id != null) {
                            val playlist = repository.getPlaylistDetails(id, currentState.clientId, currentState.clientSecret)
                            _state.update { it.copy(isLoading = false, playlist = playlist) }
                        }
                    }
                    else -> {
                        _state.update { it.copy(isLoading = false, errorMessage = "Unsupported Spotify link type.") }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Failed to fetch data") }
            }
        }
    }

    private fun extractId(url: String, type: String): String? {
        val regex = Regex("$type/([a-zA-Z0-9]+)")
        val matchResult = regex.find(url)
        return matchResult?.groupValues?.get(1)
    }

    fun togglePreviewPlay() {
        val previewUrl = _state.value.track?.preview_url
        if (previewUrl == null) {
            _state.update { it.copy(errorMessage = "No preview available for this track.") }
            return
        }

        if (_state.value.isPlayingPreview) {
            stopPreview()
        } else {
            playPreview(previewUrl)
        }
    }

    private fun playPreview(url: String) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                }
            }
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                it.start()
                _state.update { state -> state.copy(isPlayingPreview = true) }
            }
            mediaPlayer?.setOnCompletionListener {
                _state.update { state -> state.copy(isPlayingPreview = false) }
            }
        } catch (e: IOException) {
            _state.update { it.copy(errorMessage = "Failed to play preview.") }
        }
    }

    fun stopPreview() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
        _state.update { it.copy(isPlayingPreview = false) }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
