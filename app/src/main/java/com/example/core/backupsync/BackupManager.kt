package com.example.core.backupsync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class BackupManager(private val context: Context) {

    fun schedulePeriodicBackup(categories: Set<String>, requiresWiFi: Boolean) {
        val constraintsBuilder = Constraints.Builder()
            .setRequiresBatteryNotLow(true)

        if (requiresWiFi) {
            constraintsBuilder.setRequiredNetworkType(NetworkType.UNMETERED)
        } else {
            constraintsBuilder.setRequiredNetworkType(NetworkType.CONNECTED)
        }

        val inputData = Data.Builder()
            .putStringArray("categories", categories.toTypedArray())
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraintsBuilder.build())
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "CloudBackupWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            backupRequest
        )
    }

    fun triggerManualBackup(categories: Set<String>) {
        val inputData = Data.Builder()
            .putStringArray("categories", categories.toTypedArray())
            .build()

        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(backupRequest)
    }

    fun cancelAllBackups() {
        WorkManager.getInstance(context).cancelUniqueWork("CloudBackupWork")
    }
}
