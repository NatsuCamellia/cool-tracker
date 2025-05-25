package net.natsucamellia.cooltracker.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreManager {
    private var keyStore: KeyStore

    init {
        // Load Android Keystore provider
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load KeyStore: ${e.message}")
            throw RuntimeException("Failed to load KeyStore", e)
        }
    }

    private fun getSecretKey(): SecretKey {
        return try {
            val key = keyStore.getKey(KEY_ALIAS, null)
            if (key == null) {
                Log.d(TAG, "Key not found, generating new key.")
                generateNewKey()
            } else {
                key as SecretKey
            }
        } catch (e: Exception) {
            Log.e(TAG, "getSecretKey: ${e.message}")
            generateNewKey()
        }
    }

    private fun generateNewKey(): SecretKey {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            val key = keyGenerator.generateKey()
            Log.d(TAG, "New key generated successfully.")
            return key
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate key: ${e.message}")
            throw RuntimeException("Failed to generate key", e)
        }
    }

    fun encrypt(data: String): Pair<String, String>? {
        return try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            // Base64 encode, for storing
            val encryptedDataString = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)

            Pair(encryptedDataString, ivString)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}")
            null
        }
    }

    // 解密資料
    fun decrypt(encryptedData: String, ivString: String): String? {
        return try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)

            val iv = Base64.decode(ivString, Base64.DEFAULT)
            val gcmParameterSpec = GCMParameterSpec(128, iv) // GCM 模式的參數規格 (tagLen = 128 bit)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "KeystoreManager"
        private const val KEY_ALIAS = "cool_tracker_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}