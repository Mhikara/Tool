package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiConfigDao {
    @Query("SELECT * FROM api_configs ORDER BY category, name")
    fun getAllApiConfigs(): Flow<List<ApiConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiConfig(config: ApiConfigEntity)

    @Update
    suspend fun updateApiConfig(config: ApiConfigEntity)

    @Query("DELETE FROM api_configs WHERE id = :id")
    suspend fun deleteApiConfigById(id: Int)
}
