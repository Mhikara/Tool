package com.example.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import java.util.concurrent.TimeUnit
import android.util.Log

class RemoteDataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val manifestUrl = inputData.getString(KEY_MANIFEST_URL) ?: return Result.failure()
        
        val syncManager = RemoteDataSyncManagerImpl(applicationContext, manifestUrl)
        
        return try {
            val result = syncManager.syncData()
            Log.d("RemoteDataSyncWorker", "Sync completed. Downloaded: ${result.downloaded.size}, Deleted: ${result.deleted.size}, Failed: ${result.failed.size}")
            
            if (result.failed.isEmpty()) {
                Result.success()
            } else {
                Result.retry() // Retry if some files failed to download
            }
        } catch (e: Exception) {
            Log.e("RemoteDataSyncWorker", "Sync failed", e)
            Result.retry()
        }
    }

    companion object {
        const val KEY_MANIFEST_URL = "MANIFEST_URL"
        const val WORK_NAME = "RemoteDataSyncWorker"

        fun scheduleSync(context: Context, manifestUrl: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<RemoteDataSyncWorker>(
                repeatInterval = 12, // Schedule every 12 hours
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInputData(androidx.work.workDataOf(KEY_MANIFEST_URL to manifestUrl))
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Don't override if already scheduled
                workRequest
            )
        }
    }
}
