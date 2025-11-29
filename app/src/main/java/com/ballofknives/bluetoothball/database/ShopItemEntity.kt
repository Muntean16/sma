package com.ballofknives.bluetoothball.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_items")
data class ShopItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val itemType: String,
    val value: String,
    val isPurchased: Boolean = false
)

