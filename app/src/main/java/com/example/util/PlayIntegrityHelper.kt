package com.example.util

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import kotlinx.coroutines.tasks.await

class PlayIntegrityHelper(private val context: Context) {
    
    private val standardIntegrityManager: StandardIntegrityManager = 
        IntegrityManagerFactory.createStandard(context)
    
    private var tokenProvider: StandardIntegrityTokenProvider? = null

    /**
     * Initializes the Standard Integrity API by requesting a token provider.
     * Call this early in the app lifecycle (e.g., Application class or Main Activity).
     */
    suspend fun initializeProvider(cloudProjectNumber: Long) {
        try {
            val request = StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(cloudProjectNumber)
                .build()
                
            tokenProvider = standardIntegrityManager.prepareIntegrityToken(request).await()
            Log.d("PlayIntegrity", "Standard Integrity Provider initialized.")
        } catch (e: Exception) {
            Log.e("PlayIntegrity", "Failed to initialize provider", e)
            // Handle gracefully - don't crash
        }
    }

    /**
     * Requests an integrity token for a specific action.
     * The action is hashed and bound to the token to prevent replay attacks.
     */
    suspend fun requestToken(requestHash: String): String? {
        if (tokenProvider == null) {
            Log.w("PlayIntegrity", "Token provider not initialized.")
            return null
        }

        return try {
            val request = StandardIntegrityTokenRequest.builder()
                .setRequestHash(requestHash)
                .build()
                
            val result = tokenProvider!!.request(request).await()
            result.token()
        } catch (e: Exception) {
            Log.e("PlayIntegrity", "Failed to request token", e)
            null
        }
    }
}
