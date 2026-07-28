package com.example.data.repository

import android.util.Base64

// Placeholder untuk implementasi enkripsi sungguhan
// Di produksi, gunakan Android Keystore System
class CryptoManager {
    fun encrypt(plainText: String): String {
        // TODO: Ganti dengan enkripsi simetris (AES) menggunakan kunci dari Android Keystore
        return Base64.encodeToString(plainText.toByteArray(), Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        // TODO: Ganti dengan dekripsi AES menggunakan kunci dari Android Keystore
        return String(Base64.decode(encryptedText, Base64.DEFAULT))
    }
}
