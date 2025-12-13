package com.ballofknives.bluetoothball.database

import androidx.room.*

@Dao
interface ShopItemDao {
    @Query("SELECT * FROM shop_items")
    suspend fun getAllItems(): List<ShopItemEntity>
    
    @Query("SELECT * FROM shop_items WHERE itemType = :type")
    suspend fun getItemsByType(type: String): List<ShopItemEntity>
    
    @Query("SELECT * FROM shop_items WHERE id = :id")
    suspend fun getItemById(id: String): ShopItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShopItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShopItemEntity>)
    
    @Update
    suspend fun updateItem(item: ShopItemEntity)
    
    @Query("UPDATE shop_items SET isPurchased = 1 WHERE id = :id")
    suspend fun markAsPurchased(id: String)
}


