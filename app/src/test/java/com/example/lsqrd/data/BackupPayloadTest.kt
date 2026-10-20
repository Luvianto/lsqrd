package com.example.lsqrd.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupPayloadTest {

    // ── Round trip ─────────────────────────────────────────

    @Test
    fun full_payload_round_trip_preserves_all_data() {
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

        val restored = BackupPayload.fromJson(JSONObject(original.toJson()))

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
    fun empty_vaults_round_trip() {
        val original = BackupPayload(vaults = emptyList())
        val restored = BackupPayload.fromJson(JSONObject(original.toJson()))
        assertEquals(0, restored.vaults.size)
    }

    @Test
    fun version_is_preserved() {
        val original = BackupPayload(version = 1, vaults = emptyList())
        val restored = BackupPayload.fromJson(JSONObject(original.toJson()))
        assertEquals(1, restored.version)
    }

    // ── Special characters ────────────────────────────────

    @Test
    fun special_characters_in_field_values_are_preserved() {
        val special = $$$"""p@$$w0rd!"#%&'()*+,-./:;<=>?@[\]^_`{|}~"""
        val original = BackupPayload(
            vaults = listOf(
                BackupVault(
                    "Test", listOf(
                        BackupCredential(
                            "Site", listOf(
                                BackupField("Pass", special, true)
                            )
                        )
                    )
                )
            )
        )
        val restored = BackupPayload.fromJson(JSONObject(original.toJson()))
        assertEquals(special, restored.vaults[0].credentials[0].fields[0].value)
    }

    @Test
    fun unicode_characters_in_vault_name_are_preserved() {
        val unicodeName = "Kho 🔐 Mật Khẩu"
        val original = BackupPayload(
            vaults = listOf(
                BackupVault(unicodeName, emptyList())
            )
        )
        val restored = BackupPayload.fromJson(JSONObject(original.toJson()))
        assertEquals(unicodeName, restored.vaults[0].name)
    }

    // ── Multiple vaults ───────────────────────────────────

    @Test
    fun multiple_vaults_all_are_preserved(){
        val original = BackupPayload(
            vaults = listOf(
                BackupVault("Work", listOf(BackupCredential("Slack", emptyList()))),
                BackupVault("Personal", listOf(BackupCredential("Gmail", emptyList()))),
                BackupVault("Finance", emptyList())
            )
        )
        val restored = BackupPayload.fromJson(JSONObject(original.toJson()))
        assertEquals(3, restored.vaults.size)
        assertEquals("Work", restored.vaults[0].name)
        assertEquals("Personal", restored.vaults[1].name)
        assertEquals("Finance", restored.vaults[2].name)
    }
}