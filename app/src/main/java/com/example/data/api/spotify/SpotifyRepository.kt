package com.example.data.api.spotify

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SpotifyRepository {
    
    private val authRetrofit = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()
        )
        .build()

    private val apiRetrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()
        )
        .build()

    private val authService = authRetrofit.create(SpotifyAuthService::class.java)
    private val apiService = apiRetrofit.create(SpotifyApiService::class.java)

    private var currentToken: String? = null
    private var tokenExpiry: Long = 0

    suspend fun getAccessToken(clientId: String, clientSecret: String): String {
        if (currentToken != null && System.currentTimeMillis() < tokenExpiry) {
            return currentToken!!
        }

        val authHeader = "Basic " + Base64.encodeToString(
            "$clientId:$clientSecret".toByteArray(),
            Base64.NO_WRAP
        )
        
        val response = authService.getClientCredentialsToken(authHeader)
        currentToken = response.accessToken
        tokenExpiry = System.currentTimeMillis() + (response.expiresIn * 1000) - 60000 // 1 min buffer
        
        return currentToken!!
    }

    suspend fun getTrackDetails(trackId: String, clientId: String, clientSecret: String): SpotifyTrack {
        val token = getAccessToken(clientId, clientSecret)
        return apiService.getTrack("Bearer $token", trackId)
    }

    suspend fun getAlbumDetails(albumId: String, clientId: String, clientSecret: String): SpotifyAlbum {
        val token = getAccessToken(clientId, clientSecret)
        return apiService.getAlbum("Bearer $token", albumId)
    }

    suspend fun getArtistDetails(artistId: String, clientId: String, clientSecret: String): SpotifyArtist {
        val token = getAccessToken(clientId, clientSecret)
        return apiService.getArtist("Bearer $token", artistId)
    }

    suspend fun getPlaylistDetails(playlistId: String, clientId: String, clientSecret: String): SpotifyPlaylist {
        val token = getAccessToken(clientId, clientSecret)
        return apiService.getPlaylist("Bearer $token", playlistId)
    }
}
