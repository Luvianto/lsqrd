package com.example.lsqrd.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteName: String,
    val username : String,
    val encryptedPassword: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)