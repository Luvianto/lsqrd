package com.example.lsqrd.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface CredentialFieldDao {

    @Query("SELECT * FROM credential_fields WHERE credentialId = :credentialId ORDER BY id ASC")
    fun getFieldsForCredential(credentialId: Long): Flow<List<CredentialField>>

    @Insert
    suspend fun insert(field: CredentialField)

    @Insert
    suspend fun insertAll(fields: List<CredentialField>)

    @Update
    suspend fun update(field: CredentialField)

    @Delete
    suspend fun delete(field: CredentialField)
}