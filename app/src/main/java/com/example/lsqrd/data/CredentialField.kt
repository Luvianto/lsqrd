package com.example.lsqrd.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credential_fields",
    foreignKeys = [
        ForeignKey(
            entity = Credential::class,
            parentColumns = ["id"],
            childColumns = ["credentialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("credentialId")]
)
data class CredentialField(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val credentialId: Long,
    val label: String,
    val value: String,
    val isSecret: Boolean = true
)
