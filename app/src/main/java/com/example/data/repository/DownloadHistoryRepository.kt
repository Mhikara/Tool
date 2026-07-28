package com.example.data.repository

import com.example.data.local.DownloadDao
import com.example.data.local.DownloadHistory
import kotlinx.coroutines.flow.Flow

class DownloadHistoryRepository(private val downloadDao: DownloadDao) {
    val allHistory: Flow<List<DownloadHistory>> = downloadDao.getAllDownloads()

    suspend fun insert(history: DownloadHistory) {
        downloadDao.insertDownload(history)
    }

    suspend fun deleteById(id: Int) {
        downloadDao.deleteDownloadById(id)
    }

    suspend fun clearHistory() {
        downloadDao.clearHistory()
    }
}
