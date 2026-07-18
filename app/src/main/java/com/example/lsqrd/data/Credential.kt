package com.example.lsqrd.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credentials",
    foreignKeys = [
        ForeignKey(
            entity = Vault::class,
            parentColumns = ["id"],
            childColumns = ["vaultId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vaultId")]
)
data class Credential (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vaultId: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)