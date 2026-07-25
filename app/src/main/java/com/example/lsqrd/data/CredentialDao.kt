package com.example.lsqrd.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface CredentialDao {

    @Query("SELECT * FROM credentials WHERE vaultId = :vaultId ORDER BY name ASC")
    fun getAllCredentials(vaultId: Long): Flow<List<Credential>>

    @Transaction
    @Query("SELECT * FROM credentials WHERE id = :credentialId")
    fun getCredentialWithFields(credentialId: Long): Flow<CredentialWithFields>

    @Transaction
    @Query("SELECT * FROM credentials WHERE vaultId = :vaultId ORDER BY name ASC")
    fun getCredetialsWithFieldsByVault(vaultId: Long): Flow<List<CredentialWithFields>>

    @Insert
    suspend fun insert(credential: Credential): Long

    @Update
    suspend fun update(credential: Credential)

    @Delete
    suspend fun delete(credential: Credential)
}