package com.example.ui.apimanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ApiConfigDao
import com.example.data.local.ApiConfigEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApiManagerViewModel(private val apiConfigDao: ApiConfigDao) : ViewModel() {

    val apiConfigs: StateFlow<List<ApiConfigEntity>> = apiConfigDao.getAllApiConfigs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addApiConfig(config: ApiConfigEntity) {
        viewModelScope.launch {
            apiConfigDao.insertApiConfig(config)
        }
    }

    fun updateApiConfig(config: ApiConfigEntity) {
        viewModelScope.launch {
            apiConfigDao.updateApiConfig(config)
        }
    }

    fun deleteApiConfig(id: Int) {
        viewModelScope.launch {
            apiConfigDao.deleteApiConfigById(id)
        }
    }
    
    fun toggleApiStatus(config: ApiConfigEntity) {
        updateApiConfig(config.copy(isActive = !config.isActive))
    }
    
    fun testConnection(config: ApiConfigEntity) {
        viewModelScope.launch {
            // Simulate network test
            kotlinx.coroutines.delay(1000)
            val isSuccess = Math.random() > 0.2
            updateApiConfig(
                config.copy(
                    status = if (isSuccess) "Active" else "Error",
                    successCount = if (isSuccess) config.successCount + 1 else config.successCount,
                    errorCount = if (!isSuccess) config.errorCount + 1 else config.errorCount,
                    lastPingMs = System.currentTimeMillis()
                )
            )
        }
    }
}
