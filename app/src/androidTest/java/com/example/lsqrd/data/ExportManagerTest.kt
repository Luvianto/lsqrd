package com.example.lsqrd.data

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

@RunWith(AndroidJUnit4::class)
class ExportManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val tempFiles = mutableListOf<File>()

    private fun tempFile(name: String): File {
        return File(context.cacheDir, name).also { tempFiles.add(it) }
    }

    @After
    fun cleanup() {
        tempFiles.forEach { it.delete() }
        tempFiles.clear()
    }

    // ── Round trip ─────────────────────────────────────────

    @Test
    fun encrypt_then_decrypt_round_trip_preserves_all_data() {
        val file = tempFile("round-trip.lsqrd")
        val uri = Uri.fromFile(file)

        val original = BackupPayload(
            vaults = listOf(
                BackupVault(
                    name = "Work", credentials = listOf(
                        BackupCredential(
                            name = "Github",
                            fields = listOf(
                                BackupField(
                                    label = "Username",
                                    value = "my user",
                                    isSecret = false
                                ),
                                BackupField(label = "Password", value = "s3cr3t!", isSecret = true)
                            )
                        )
                    )
                )
            )
        )

        ExportManager.encrypt(original, "my-passphrase", uri, context)
        val restored = ExportManager.decrypt("my-passphrase", uri, context)

        assertEquals(1, restored.vaults.size)
        assertEquals("Work", restored.vaults[0].name)
        assertEquals(1, restored.vaults[0].credentials.size)
        assertEquals("Github", restored.vaults[0].credentials[0].name)
        assertEquals(2, restored.vaults[0].credentials[0].fields.size)

        val usernameField = restored.vaults[0].credentials[0].fields[0]
        assertEquals("Username", usernameField.label)
        assertEquals("my user", usernameField.value)
        assertFalse(usernameField.isSecret)

        val passwordField = restored.vaults[0].credentials[0].fields[1]
        assertEquals("s3cr3t!", passwordField.value)
        assertTrue(passwordField.isSecret)
    }

    @Test
    fun encrypt_then_decrypt_empty_payload() {
        val file = tempFile("empty.lsqrd")
        val payload = BackupPayload(vaults = emptyList())

        ExportManager.encrypt(payload, "my-passphrase", Uri.fromFile(file), context)
        val restored = ExportManager.decrypt("my-passphrase", Uri.fromFile(file), context)

        assertEquals(0, restored.vaults.size)
    }

    // ── Wrong passphrase ──────────────────────────────────

    @Test
    fun decrypt_with_empty_passphrase_throws_SecurityException() {
        val file = tempFile("empty_pass.lsqrd")
        val payload = BackupPayload(vaults = emptyList())

        ExportManager.encrypt(payload, "my-passphrase", Uri.fromFile(file), context)

        try {
            ExportManager.decrypt("", Uri.fromFile(file), context)
            fail("Expected SecurityException was not thrown")
        } catch (e: SecurityException) {
            // Expected
        }
    }

    // ── Invalid file ──────────────────────────────────────

    @Test
    fun decrypt_random_text_file_throws_exception() {
        val file = tempFile("random.txt")
        file.writeText("This is not a backup file")

        try {
            ExportManager.decrypt("my-passphrase", Uri.fromFile(file), context)
            fail("Expected an Exception")
        } catch (e: Exception) {
            // Expected
        }
    }

    @Test
    fun decrypt_file_with_wrong_magic_throws_IllegalArgumentException() {
        val file = tempFile("wrong_magic.lsqrd")
        file.writeText("""{"magic"="WRONGMAG","version"=1,"salt"="","iv"="","data"=""}""")
        try {
            ExportManager.decrypt("any-passphrase", Uri.fromFile(file), context)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    // ── Randomness ────────────────────────────────────────

    @Test
    fun two_exports_with_same_passphrase_produce_different_ciphertext() {
        val payload = BackupPayload(vaults = listOf(BackupVault("Work", emptyList())))
        val passphrase = "same-passphrase"

        val file1 = tempFile("export1.lsqrd")
        val file2 = tempFile("export2.lsqrd")

        ExportManager.encrypt(payload, passphrase, Uri.fromFile(file1), context)
        ExportManager.encrypt(payload, passphrase, Uri.fromFile(file2), context)

        // Different salt + IV each time -> different output, even with the same input
        assertFalse(
            "Two exports with same passphrase should not produce identical files",
            file1.readText() == file2.readText()
        )
    }
}