package com.example.ui.apimanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.ApiConfigDao

class ApiManagerViewModelFactory(private val apiConfigDao: ApiConfigDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApiManagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ApiManagerViewModel(apiConfigDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
