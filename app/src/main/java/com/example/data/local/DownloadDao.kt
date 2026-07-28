package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_history ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadHistory)

    @Query("DELETE FROM download_history WHERE id = :id")
    suspend fun deleteDownloadById(id: Int)

    @Query("DELETE FROM download_history")
    suspend fun clearHistory()
}
