package com.ballofknives.bluetoothball.database

import androidx.room.*

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player WHERE id = :userId")
    suspend fun getPlayer(userId: Long): PlayerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)
    
    @Update
    suspend fun updatePlayer(player: PlayerEntity)
    
    @Query("UPDATE player SET totalPoints = totalPoints + :points WHERE id = :userId")
    suspend fun addPoints(userId: Long, points: Int)
    
    @Query("UPDATE player SET totalPoints = totalPoints - :points WHERE id = :userId")
    suspend fun subtractPoints(userId: Long, points: Int)
    
    @Query("UPDATE player SET selectedBallColor = :color WHERE id = :userId")
    suspend fun updateBallColor(userId: Long, color: String)
    
    @Query("UPDATE player SET extraTimeSeconds = :seconds WHERE id = :userId")
    suspend fun updateExtraTime(userId: Long, seconds: Int)
}


