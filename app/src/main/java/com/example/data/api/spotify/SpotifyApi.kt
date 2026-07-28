package com.example.data.api.spotify

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class SpotifyTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int
)

@JsonClass(generateAdapter = true)
data class SpotifyTrack(
    val id: String,
    val name: String,
    val duration_ms: Long,
    val popularity: Int?,
    val preview_url: String?,
    val album: SpotifyAlbum,
    val artists: List<SpotifyArtist>
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbum(
    val id: String,
    val name: String,
    val release_date: String?,
    val images: List<SpotifyImage>
)

@JsonClass(generateAdapter = true)
data class SpotifyArtist(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>? = null,
    val genres: List<String>? = null,
    val followers: SpotifyFollowers? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyFollowers(
    val total: Int
)

@JsonClass(generateAdapter = true)
data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylist(
    val id: String,
    val name: String,
    val description: String?,
    val images: List<SpotifyImage>? = null,
    val owner: SpotifyOwner? = null,
    val tracks: SpotifyPlaylistTracks? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyOwner(
    val display_name: String?
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistTracks(
    val total: Int
)

interface SpotifyAuthService {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getClientCredentialsToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse
}

interface SpotifyApiService {
    @GET("v1/tracks/{id}")
    suspend fun getTrack(
        @Header("Authorization") authorization: String,
        @Path("id") trackId: String
    ): SpotifyTrack

    @GET("v1/albums/{id}")
    suspend fun getAlbum(
        @Header("Authorization") authorization: String,
        @Path("id") albumId: String
    ): SpotifyAlbum

    @GET("v1/artists/{id}")
    suspend fun getArtist(
        @Header("Authorization") authorization: String,
        @Path("id") artistId: String
    ): SpotifyArtist

    @GET("v1/playlists/{id}")
    suspend fun getPlaylist(
        @Header("Authorization") authorization: String,
        @Path("id") playlistId: String
    ): SpotifyPlaylist
}
