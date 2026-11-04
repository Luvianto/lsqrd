package com.example.lsqrd.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VaultDefaultCredentialFieldDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var vaultDao: VaultDao
    private lateinit var credentialDao: CredentialDao
    private lateinit var credentialFieldDao: CredentialFieldDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
            .build()
        vaultDao = db.vaultDao()
        credentialDao = db.credentialDao()
        credentialFieldDao = db.credentialFieldDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ── Helpers ──────────────────────────────────────────

    private suspend fun insertVault(name: String): Vault {
        vaultDao.insert(Vault(name = name))
        return vaultDao.getAllVaults().first().first {it.name == name}
    }

    // ── Tests ─────────────────────────────────────────────

    @Test
    fun insert_and_retrieve_default_fields() = runTest {
        val vault = insertVault("Work")

        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Email", isSecret = false, position = 0)
        )
        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Password", isSecret = true, position = 1)
        )

        val fields = vaultDao.getDefaultCredentialFields(vault.id).first()
        assertEquals(2, fields.size)
        assertEquals("Email", fields[0].label)
        assertFalse(fields[0].isSecret)
        assertEquals("Password", fields[1].label)
        assertTrue(fields[1].isSecret)
    }

    @Test
    fun fields_are_ordered_by_position() = runTest {
        val vault = insertVault("Social")

        // Insert in reverse order intentionally
        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Notes", isSecret = false, position = 2)
        )
        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Username", isSecret = false, position = 0)
        )
        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Password", isSecret = true, position = 1)
        )

        val fields = vaultDao.getDefaultCredentialFields(vault.id).first()
        assertEquals("Username", fields[0].label)
        assertEquals("Password", fields[1].label)
        assertEquals("Notes", fields[2].label)
    }

    @Test
    fun delete_specific_field() = runTest {
        val vault = insertVault("Banking")

        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Account Number", isSecret = false, position = 0)
        )
        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "PIN", isSecret = true, position = 1)
        )

        val toDelete = vaultDao.getDefaultCredentialFields(vault.id).first().first {it.label == "PIN"}
        vaultDao.deleteDefaultCredentialField(toDelete)

        val remaining = vaultDao.getDefaultCredentialFields(vault.id).first()
        assertEquals(1, remaining.size)
        assertEquals("Account Number", remaining[0].label)
    }

    @Test
    fun cascade_delete_when_vault_deleted() = runTest {
        val vault = insertVault("Temp")

        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Email", isSecret = false, position = 0)
        )
        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Password", isSecret = true, position = 1)
        )

        assertEquals(2, vaultDao.getDefaultCredentialFields(vault.id).first().size)

        vaultDao.delete(vault)

        assertEquals(0, vaultDao.getDefaultCredentialFields(vault.id).first().size)
    }

    @Test
    fun fields_are_vaults_scoped() = runTest {
        val work = insertVault("Work")
        val personal = insertVault("Personal")

        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = work.id, label = "Employee ID", isSecret = false, position = 0)
        )
        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = personal.id, label = "Phone", isSecret = false, position = 0)
        )

        val workFields = vaultDao.getDefaultCredentialFields(work.id).first()
        val personalFields = vaultDao.getDefaultCredentialFields(personal.id).first()

        assertEquals(1, workFields.size)
        assertEquals("Employee ID", workFields[0].label)
        assertEquals(1, workFields.size)
        assertEquals("Phone", personalFields[0].label)
    }

    @Test
    fun new_credentials_inherits_default_fields() = runTest {
        val vault = insertVault("Social")

        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Email", isSecret = false, position = 0)
        )
        vaultDao.insertDefaultCredentialField(
            VaultDefaultCredentialField(vaultId = vault.id, label = "Password", isSecret = true, position = 1)
        )

        val credId = credentialDao.insert(Credential(vaultId = vault.id, name ="Instagram"))
        val defaultCredentialFields = vaultDao.getDefaultCredentialFields(vault.id).first()
        defaultCredentialFields.forEach { defaultCredentialField ->
            credentialFieldDao.insert(
                CredentialField(
                    credentialId = credId,
                    label = defaultCredentialField.label,
                    value = "",
                    isSecret = defaultCredentialField.isSecret
                )
            )
        }

        val credWithFields = credentialDao.getCredentialWithFields(credId).first()
        assertEquals(2, credWithFields.fields.size)
        assertEquals("Email", credWithFields.fields[0].label)
        assertFalse(credWithFields.fields[0].isSecret)
        assertEquals("Password", credWithFields.fields[1].label)
        assertTrue(credWithFields.fields[1].isSecret)
    }
}