package com.example.lsqrd.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vault_default_credential_fields",
    foreignKeys = [ForeignKey(
        entity = Vault::class,
        parentColumns = ["id"],
        childColumns = ["vaultId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("vaultId")]
)
data class VaultDefaultCredentialField(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vaultId: Long,
    val label: String,
    val value: String = "",
    val isSecret: Boolean,
    val position: Int = 0
)