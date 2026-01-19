package com.ballofknives.bluetoothball.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_purchases")
data class UserPurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val shopItemId: String
)




