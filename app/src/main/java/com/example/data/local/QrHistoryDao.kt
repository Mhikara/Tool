package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QrHistoryDao {
    @Query("SELECT * FROM qr_history ORDER BY timestamp DESC")
    fun getAllQrHistory(): Flow<List<QrHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrHistory(qr: QrHistoryEntity)

    @Query("DELETE FROM qr_history WHERE id = :id")
    suspend fun deleteQrHistory(id: Long)

    @Query("DELETE FROM qr_history")
    suspend fun clearAllHistory()
}
