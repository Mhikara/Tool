package com.example.util

import android.content.Context
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.File
import java.security.MessageDigest

// Model Data untuk Manifest
@JsonClass(generateAdapter = true)
data class SyncManifest(
    val version: Int,
    val files: List<ManifestFileItem>
)

@JsonClass(generateAdapter = true)
data class ManifestFileItem(
    val path: String,       // path lokal relatif, misal: "config/settings.json"
    val hash: String,       // SHA-256 hash dari isi file
    val downloadUrl: String // URL raw github, misal: "https://raw.githubusercontent.com/user/repo/main/config/settings.json"
)

data class SyncResult(
    val downloaded: List<String> = emptyList(),
    val deleted: List<String> = emptyList(),
    val failed: List<String> = emptyList()
)

interface GitHubRawApi {
    @GET
    suspend fun getManifest(@Url url: String): SyncManifest

    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody
}

interface RemoteDataSyncManager {
    suspend fun checkForUpdates(): Pair<List<ManifestFileItem>, List<String>>
    suspend fun syncData(): SyncResult
}

class RemoteDataSyncManagerImpl(
    private val context: Context,
    private val manifestUrl: String // URL ke manifest.json di raw.githubusercontent.com
) : RemoteDataSyncManager {

    private val localDataDir = File(context.filesDir, "remote_data").apply { mkdirs() }
    private val localManifestFile = File(localDataDir, "manifest.json")

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val manifestAdapter = moshi.adapter(SyncManifest::class.java)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/") // Base URL is required by Retrofit, actual URL passed via @Url
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api = retrofit.create(GitHubRawApi::class.java)

    override suspend fun checkForUpdates(): Pair<List<ManifestFileItem>, List<String>> = withContext(Dispatchers.IO) {
        try {
            // 1. Ambil manifest remote
            val remoteManifest = api.getManifest(manifestUrl)

            // 2. Baca manifest lokal (jika ada)
            val localManifest = if (localManifestFile.exists()) {
                manifestAdapter.fromJson(localManifestFile.readText()) ?: SyncManifest(0, emptyList())
            } else {
                SyncManifest(0, emptyList())
            }

            // 3. Bandingkan
            val toDownload = mutableListOf<ManifestFileItem>()
            val toDelete = mutableListOf<String>()

            val remoteFilesMap = remoteManifest.files.associateBy { it.path }
            val localFilesMap = localManifest.files.associateBy { it.path }

            // Cek file baru atau berubah
            for (remoteFile in remoteManifest.files) {
                val localFile = localFilesMap[remoteFile.path]
                if (localFile == null || localFile.hash != remoteFile.hash || !File(localDataDir, remoteFile.path).exists()) {
                    toDownload.add(remoteFile)
                }
            }

            // Cek file yang dihapus di remote
            for (localFile in localManifest.files) {
                if (!remoteFilesMap.containsKey(localFile.path)) {
                    toDelete.add(localFile.path)
                }
            }

            return@withContext Pair(toDownload, toDelete)
        } catch (e: Exception) {
            // Offline atau error network
            return@withContext Pair(emptyList(), emptyList())
        }
    }

    override suspend fun syncData(): SyncResult = withContext(Dispatchers.IO) {
        val (toDownload, toDelete) = checkForUpdates()
        val downloaded = mutableListOf<String>()
        val deleted = mutableListOf<String>()
        val failed = mutableListOf<String>()

        if (toDownload.isEmpty() && toDelete.isEmpty()) {
            return@withContext SyncResult() // Tidak ada update
        }

        // Hapus file yang sudah tidak ada di remote
        for (path in toDelete) {
            val file = File(localDataDir, path)
            if (file.exists() && file.delete()) {
                deleted.add(path)
            }
        }

        // Download file baru/update
        for (item in toDownload) {
            try {
                val response = api.downloadFile(item.downloadUrl)
                val fileContent = response.bytes()
                
                // Validasi Hash sebelum menyimpan
                val downloadedHash = calculateHash(fileContent)
                if (downloadedHash == item.hash) {
                    val targetFile = File(localDataDir, item.path)
                    targetFile.parentFile?.mkdirs() // Buat folder jika belum ada
                    targetFile.writeBytes(fileContent)
                    downloaded.add(item.path)
                } else {
                    failed.add(item.path) // Hash mismatch (mungkin file korup atau MiTM)
                }
            } catch (e: Exception) {
                failed.add(item.path)
            }
        }

        // Jika ada perubahan sukses, update manifest lokal
        if (downloaded.isNotEmpty() || deleted.isNotEmpty()) {
            try {
                // Fetch the latest manifest again to save it locally
                val latestManifest = api.getManifest(manifestUrl)
                localManifestFile.writeText(manifestAdapter.toJson(latestManifest))
            } catch (e: Exception) {
                // Handle error
            }
        }

        return@withContext SyncResult(downloaded, deleted, failed)
    }

    // --- Helper Functions ---
    private fun calculateHash(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
