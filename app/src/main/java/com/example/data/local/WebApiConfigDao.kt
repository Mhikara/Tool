package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WebApiConfigDao {
    @Query("SELECT * FROM web_api_configs")
    fun getAllWebServices(): Flow<List<WebApiConfigEntity>>

    @Query("SELECT * FROM web_api_configs WHERE serviceName = :serviceName LIMIT 1")
    suspend fun getWebServiceByName(serviceName: String): WebApiConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWebService(config: WebApiConfigEntity)

    @Query("DELETE FROM web_api_configs WHERE serviceName = :serviceName")
    suspend fun deleteWebService(serviceName: String)
}
