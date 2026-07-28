package com.example.data.repository

import com.example.data.local.WebApiConfigDao
import com.example.data.local.WebApiConfigEntity
import kotlinx.coroutines.flow.Flow

class WebApiKeyRepository(
    private val dao: WebApiConfigDao,
    private val cryptoManager: CryptoManager // Asumsi Anda memiliki utility enkripsi
) {

    fun listWebServices(): Flow<List<WebApiConfigEntity>> {
        return dao.getAllWebServices()
    }

    suspend fun getWebApiKey(serviceName: String): String? {
        val entity = dao.getWebServiceByName(serviceName) ?: return null
        return try {
            cryptoManager.decrypt(entity.encryptedApiKey)
        } catch (e: Exception) {
            // Log error, mungkin key corrupt
            null
        }
    }
    
    suspend fun getWebServiceConfig(serviceName: String): WebApiConfigEntity? {
        return dao.getWebServiceByName(serviceName)
    }

    suspend fun registerOrUpdateWebService(
        serviceName: String,
        providerName: String,
        baseUrl: String,
        plainApiKey: String,
        authHeaderName: String? = null,
        authHeaderPrefix: String? = null,
        status: String = "ACTIVE"
    ) {
        val encryptedKey = cryptoManager.encrypt(plainApiKey)
        val entity = WebApiConfigEntity(
            serviceName = serviceName,
            providerName = providerName,
            baseUrl = baseUrl,
            encryptedApiKey = encryptedKey,
            authHeaderName = authHeaderName,
            authHeaderPrefix = authHeaderPrefix,
            status = status
        )
        dao.insertOrUpdateWebService(entity)
    }

    suspend fun deleteWebService(serviceName: String) {
        dao.deleteWebService(serviceName)
    }
}
