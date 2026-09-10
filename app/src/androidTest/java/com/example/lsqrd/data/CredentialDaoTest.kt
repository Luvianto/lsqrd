package com.example.lsqrd.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CredentialDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var vaultDao: VaultDao
    private lateinit var credentialDao: CredentialDao
    private lateinit var fieldDao: CredentialFieldDao

    // A vault id shared across tests
    private var testVaultId = 0L

    @Before
    fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        vaultDao = db.vaultDao()
        credentialDao = db.credentialDao()
        fieldDao = db.credentialFieldDao()

        vaultDao.insert(Vault(name = "Test Vault"))
        testVaultId = vaultDao.getAllVaults().first().first().id
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetCredentials() = runBlocking {
        credentialDao.insert(Credential(vaultId = testVaultId, name = "Github"))
        credentialDao.insert(Credential(vaultId = testVaultId, name = "Gmail"))

        val credentials = credentialDao.getAllCredentials(testVaultId).first()
        Assert.assertEquals(2, credentials.size)
    }

    @Test
    fun updateCredential_changeName() = runBlocking {
        credentialDao.insert(Credential(vaultId = testVaultId, name = "Old"))
        val credential = credentialDao.getAllCredentials(testVaultId).first().first()

        credentialDao.update(credential.copy(name = "New"))

        val updated = credentialDao.getAllCredentials(testVaultId).first().first()
        Assert.assertEquals("New", updated.name)
    }

    @Test
    fun deleteCredential_removesIt() = runBlocking {
        credentialDao.insert(Credential(vaultId = testVaultId, name = "Temp"))
        val credential = credentialDao.getAllCredentials(testVaultId).first().first()

        credentialDao.delete(credential)

        val credentials = credentialDao.getAllCredentials(testVaultId).first()
        Assert.assertEquals(0, credentials.size)
    }

    @Test
    fun getCredentialWithFields_returnFieldsCorrectly() = runBlocking {
        credentialDao.insert(Credential(vaultId = testVaultId, name = "Github"))
        val credential = credentialDao.getAllCredentials(testVaultId).first().first()

        fieldDao.insert(
            CredentialField(
                credentialId = credential.id,
                label = "Password",
                value = "secret",
                isSecret = true
            )
        )

        fieldDao.insert(
            CredentialField(
                credentialId = credential.id,
                label = "Email",
                value = "me@test.com",
                isSecret = false
            )
        )

        val result = credentialDao.getCredentialWithFields(credential.id).first()
        Assert.assertEquals("Github", result.credential.name)
        Assert.assertEquals(2, result.fields.size)
    }

    @Test
    fun deletingVault_cascadesCredentials() = runBlocking {
        credentialDao.insert(Credential(vaultId = testVaultId, name = "Github"))
        val vault = vaultDao.getAllVaults().first().first()

        vaultDao.delete(vault)

        val credentials = credentialDao.getAllCredentials(testVaultId).first()
        Assert.assertEquals(0, credentials.size)
    }
}