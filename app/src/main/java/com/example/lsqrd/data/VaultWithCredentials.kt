package com.example.lsqrd.data

import androidx.room.Embedded
import androidx.room.Relation

data class VaultWithCredentials(
    @Embedded val vault: Vault,
    @Relation(
        parentColumn = "id",
        entityColumn = "vaultId"
    )
    val credentials: List<Credential>
)
