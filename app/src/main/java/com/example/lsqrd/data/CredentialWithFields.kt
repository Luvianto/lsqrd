package com.example.lsqrd.data

import androidx.room.Embedded
import androidx.room.Relation

data class CredentialWithFields(
    @Embedded val credential: Credential,
    @Relation (
        parentColumn = "id",
        entityColumn = "credentialId"
    )
    val fields: List<CredentialField>
)
