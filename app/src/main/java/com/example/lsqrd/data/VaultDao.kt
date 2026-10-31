package com.example.lsqrd.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vaults ORDER BY name ASC")
    fun getAllVaults(): Flow<List<Vault>>

    @Query("SELECT * FROM vaults WHERE id = :vaultId")
    fun getVaultById(vaultId: Long): Flow<Vault?>

    @Transaction
    @Query("SELECT * FROM vaults WHERE id = :vaultId")
    fun getVaultWithCredetials(vaultId: Long): Flow<VaultWithCredentials?>

    @Insert
    suspend fun insert(vault: Vault)

    @Update
    suspend fun update(vault: Vault)

    @Delete
    suspend fun delete(vault: Vault)

    @Query("SELECT * FROM vault_default_credential_fields WHERE vaultId = :vaultId ORDER BY position ASC")
    fun getDefaultCredentialFields(vaultId: Long): Flow<List<VaultDefaultCredentialField>>

    @Insert
    suspend fun insertDefaultCredentialField(field: VaultDefaultCredentialField): Long

    @Delete
    suspend fun deleteDefaultCredentialField(field: VaultDefaultCredentialField)

    @Query("DELETE FROM vault_default_credential_fields WHERE vaultId = :vaultId")
    suspend fun clearDefaultCredentialFields(vaultId: Long)
}
