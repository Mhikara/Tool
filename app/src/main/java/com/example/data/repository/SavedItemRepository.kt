package com.example.data.repository

import com.example.data.local.SavedItem
import com.example.data.local.SavedItemDao
import kotlinx.coroutines.flow.Flow

class SavedItemRepository(private val savedItemDao: SavedItemDao) {
    val allItems: Flow<List<SavedItem>> = savedItemDao.getAllSavedItems()

    suspend fun insert(item: SavedItem) {
        savedItemDao.insertItem(item)
    }

    suspend fun deleteById(id: Int) {
        savedItemDao.deleteItemById(id)
    }

    suspend fun clearAll() {
        savedItemDao.clearAllItems()
    }
}
