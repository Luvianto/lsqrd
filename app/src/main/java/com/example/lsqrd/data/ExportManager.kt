package com.example.lsqrd.data

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object ExportManager {

    private const val MAGIC = "LSQRD001"
    private const val PBKDF2_ITERATIONS = 200_000
    private const val KEY_LENGTH = 256
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private fun deriveKey(passPhrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passPhrase.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val secret = factory.generateSecret(spec)
        spec.clearPassword()
        return SecretKeySpec(secret.encoded, "AES")
    }

    fun encrypt(payload: BackupPayload, passPhrase: String, uri: Uri, context: Context) {
        val plainText = payload.toJson().toByteArray(Charsets.UTF_8)

        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(passPhrase, salt)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText)

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(
            Date()
        )

        val envelope = JSONObject().apply {
            put("magic", MAGIC)
            put("version", 1)
            put("created_at", timestamp)
            put("salt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            put("iv", android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
            put("data", android.util.Base64.encodeToString(cipherText, android.util.Base64.NO_WRAP))
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(envelope.toString().toByteArray(Charsets.UTF_8))
        }?: throw IOException("Could not open input stream for URI")
    }

    fun decrypt(passphrase: String, uri: Uri, context: Context): BackupPayload {
        val content = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: throw IOException("Could not open input stream for URI")

        val envelope = JSONObject(content)

        if(envelope.optString("magic") != MAGIC){
            throw IllegalArgumentException("Not a valid lsqrd backup file")
        }

        val salt = android.util.Base64.decode(envelope.getString("salt"), android.util.Base64.NO_WRAP)
        val iv = android.util.Base64.decode(envelope.getString("iv"), android.util.Base64.NO_WRAP)
        val cipherText = android.util.Base64.decode(envelope.getString("data"), android.util.Base64.NO_WRAP)

        val key = deriveKey(passphrase, salt)

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE, iv))
            val plainText = cipher.doFinal(cipherText)
            BackupPayload.fromJson(JSONObject(String(plainText, Charsets.UTF_8)))
        } catch (e: Exception){
            throw SecurityException("Wrong passphrase or corrupted file")
        }
    }
}