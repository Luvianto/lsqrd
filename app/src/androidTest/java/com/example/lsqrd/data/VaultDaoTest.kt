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
class VaultDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: VaultDao

    @Before
    fun setup(){
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.vaultDao()
    }

    @After
    fun teardown(){
        db.close()
    }

    @Test
    fun insertAndGetAllValues() = runBlocking {
        dao.insert(Vault(name = "Work"))
        dao.insert(Vault(name = "Personal"))

        val vaults = dao.getAllVaults().first()
        Assert.assertEquals(2, vaults.size)
    }

    @Test
    fun getVaultById_returnsCorrectVault() = runBlocking {
        dao.insert(Vault(name = "Banking"))
        val inserted = dao.getAllVaults().first().first()

        val retrieved = dao.getVaultById(inserted.id).first()
        Assert.assertEquals("Banking", retrieved!!.name)
    }

    @Test
    fun getVaultById_returnsNull_whenNotFound() = runBlocking {
        val result = dao.getVaultById(999L).first()
        Assert.assertNull(result)
    }

    @Test
    fun updateVault_changesName() = runBlocking {
        dao.insert(Vault(name = "Old Name"))
        val vault = dao.getAllVaults().first().first()

        dao.update(vault.copy(name = "New Name"))

        val updated = dao.getVaultById(vault.id).first()
        Assert.assertEquals("New Name", updated!!.name)
    }

    @Test
    fun deleteVault_removesIt() = runBlocking {
        dao.insert(Vault(name = "Temp"))
        val vault = dao.getAllVaults().first().first()

        dao.delete(vault)

        val vaults = dao.getAllVaults().first()
        Assert.assertEquals(0, vaults.size)
    }
}