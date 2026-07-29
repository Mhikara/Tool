package com.example.util

import android.util.Log
// import com.google.firebase.crashlytics.FirebaseCrashlytics
// import com.google.firebase.analytics.FirebaseAnalytics
// import com.google.firebase.analytics.ktx.logEvent

interface CrashReportManager {
    fun logException(throwable: Throwable, tag: String)
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(key: String, value: String)
    fun logBreadcrumb(message: String)
    fun setCustomKey(key: String, value: String)
}

class CrashReportManagerImpl : CrashReportManager {

    // private val crashlytics = FirebaseCrashlytics.getInstance()
    // private val analytics = FirebaseAnalytics.getInstance(context)

    // Daftar kata kunci sensitif yang tidak boleh dikirim
    private val sensitiveKeys = listOf("password", "token", "api_key", "secret", "pin", "auth")

    override fun logException(throwable: Throwable, tag: String) {
        Log.e("CrashReport", "[$tag] Exception caught: ${throwable.message}")
        // crashlytics.setCustomKey("error_tag", tag)
        // crashlytics.recordException(throwable)
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        val sanitizedParams = sanitizeParams(params)
        Log.d("Analytics", "Event: $eventName, Params: $sanitizedParams")
        
        // analytics.logEvent(eventName) {
        //     sanitizedParams.forEach { (key, value) ->
        //         when (value) {
        //             is String -> param(key, value)
        //             is Long -> param(key, value)
        //             is Double -> param(key, value)
        //         }
        //     }
        // }
    }

    override fun setUserProperty(key: String, value: String) {
        if (isSensitive(key)) return
        Log.d("Analytics", "User Property: $key = $value")
        // analytics.setUserProperty(key, value)
    }

    override fun logBreadcrumb(message: String) {
        Log.d("CrashReport", "Breadcrumb: $message")
        // crashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        if (isSensitive(key)) {
            // crashlytics.setCustomKey(key, "[REDACTED]")
            return
        }
        // crashlytics.setCustomKey(key, value)
    }

    // Fungsi untuk menyaring parameter sensitif sebelum dikirim
    private fun sanitizeParams(params: Map<String, Any>): Map<String, Any> {
        return params.mapValues { (key, value) ->
            if (isSensitive(key)) "[REDACTED]" else value
        }
    }

    private fun isSensitive(key: String): Boolean {
        val lowerKey = key.lowercase()
        return sensitiveKeys.any { lowerKey.contains(it) }
    }
}
