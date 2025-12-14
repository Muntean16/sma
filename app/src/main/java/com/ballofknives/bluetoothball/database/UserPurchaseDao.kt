package com.ballofknives.bluetoothball.database

import androidx.room.*

@Dao
interface UserPurchaseDao {
    @Query("SELECT * FROM user_purchases WHERE userId = :userId")
    suspend fun getPurchasesByUserId(userId: Long): List<UserPurchaseEntity>
    
    @Query("SELECT * FROM user_purchases WHERE userId = :userId AND shopItemId = :shopItemId")
    suspend fun getPurchase(userId: Long, shopItemId: String): UserPurchaseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: UserPurchaseEntity)
    
    @Query("DELETE FROM user_purchases WHERE userId = :userId AND shopItemId = :shopItemId")
    suspend fun deletePurchase(userId: Long, shopItemId: String)
}

