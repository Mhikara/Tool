package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedItemDao {
    @Query("SELECT * FROM saved_items ORDER BY timestamp DESC")
    fun getAllSavedItems(): Flow<List<SavedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SavedItem)

    @Query("DELETE FROM saved_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("DELETE FROM saved_items")
    suspend fun clearAllItems()
}
