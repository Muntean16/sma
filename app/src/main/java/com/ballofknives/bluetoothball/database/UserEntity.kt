package com.ballofknives.bluetoothball.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String, // In production, this should be hashed
    val code: String = "" // Codul utilizatorului
)




