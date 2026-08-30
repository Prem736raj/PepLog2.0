package com.appvexis.peptidetracker.core.database.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the random SQLCipher database passphrase without storing it in plaintext.
 *
 * A 256-bit random database passphrase is generated once, encrypted with an AES key
 * held by Android Keystore, and the ciphertext/IV are stored in app-private
 * SharedPreferences. Android Auto Backup is disabled for the app so the wrapped
 * passphrase cannot be restored onto a different device without its Keystore key.
 *
 * If an existing wrapped passphrase can no longer be decrypted, this class fails
 * closed instead of silently generating a new passphrase (which would make the
 * existing encrypted database appear corrupt/unreadable).
 */
@Singleton
class DatabasePassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encrypted = preferences.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        val iv = preferences.getString(KEY_IV, null)

        if (encrypted != null || iv != null) {
            require(!encrypted.isNullOrBlank() && !iv.isNullOrBlank()) {
                "Database key metadata is incomplete; refusing to replace the existing key"
            }
            return decrypt(
                ciphertext = Base64.decode(encrypted, Base64.NO_WRAP),
                iv = Base64.decode(iv, Base64.NO_WRAP)
            )
        }

        val passphrase = ByteArray(PASSPHRASE_BYTES).also(SecureRandom()::nextBytes)
        val encryptedResult = encrypt(passphrase)

        val saved = preferences.edit()
            .putString(
                KEY_ENCRYPTED_PASSPHRASE,
                Base64.encodeToString(encryptedResult.ciphertext, Base64.NO_WRAP)
            )
            .putString(KEY_IV, Base64.encodeToString(encryptedResult.iv, Base64.NO_WRAP))
            .commit()

        check(saved) { "Failed to persist the encrypted database passphrase" }
        return passphrase
    }

    private fun encrypt(plaintext: ByteArray): EncryptedValue {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateMasterKey())
        return EncryptedValue(
            ciphertext = cipher.doFinal(plaintext),
            iv = cipher.iv
        )
    }

    private fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateMasterKey(),
            GCMParameterSpec(GCM_TAG_BITS, iv)
        )
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private data class EncryptedValue(
        val ciphertext: ByteArray,
        val iv: ByteArray
    )

    private companion object {
        const val PREFS_NAME = "peplog_database_key"
        const val KEY_ENCRYPTED_PASSPHRASE = "wrapped_passphrase"
        const val KEY_IV = "wrapped_passphrase_iv"
        const val KEYSTORE_ALIAS = "peplog_database_master_key_v1"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val PASSPHRASE_BYTES = 32
        const val GCM_TAG_BITS = 128
    }
}
