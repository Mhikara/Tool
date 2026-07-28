package com.example.core.backupsync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val categories = inputData.getStringArray("categories") ?: return Result.failure()
        
        Log.d("BackupWorker", "Starting backup for categories: ${categories.joinToString()}")
        
        return try {
            // 1. Fetch data from Local Room DB (Simulated)
            val dataToBackup = fetchLocalData(categories.toSet())
            
            // 2. Encrypt data
            val cryptoManager = CryptoManager()
            val encryptedData = cryptoManager.encrypt(dataToBackup)
            
            // 3. Upload to Object Storage via SyncEngine
            val syncEngine = SyncEngine()
            syncEngine.uploadBackup(encryptedData)
            
            Log.d("BackupWorker", "Backup completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("BackupWorker", "Backup failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun fetchLocalData(categories: Set<String>): String {
        // Simulate database reading
        delay(1000)
        return "{ \"data\": \"Simulated JSON dump of selected categories: $categories\" }"
    }
}
