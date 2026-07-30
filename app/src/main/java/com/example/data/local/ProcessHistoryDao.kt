package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessHistoryDao {
    @Query("SELECT * FROM process_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ProcessHistoryEntity>>

    @Query("SELECT * FROM process_history WHERE processType = :processType ORDER BY timestamp DESC")
    fun getHistoryByType(processType: String): Flow<List<ProcessHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ProcessHistoryEntity): Long

    @Query("DELETE FROM process_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM process_history")
    suspend fun clearAllHistory()
}
