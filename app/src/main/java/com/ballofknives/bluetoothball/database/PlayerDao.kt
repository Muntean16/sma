package com.ballofknives.bluetoothball.database

import androidx.room.*

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player WHERE id = 1")
    suspend fun getPlayer(): PlayerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)
    
    @Update
    suspend fun updatePlayer(player: PlayerEntity)
    
    @Query("UPDATE player SET totalPoints = totalPoints + :points WHERE id = 1")
    suspend fun addPoints(points: Int)
    
    @Query("UPDATE player SET totalPoints = totalPoints - :points WHERE id = 1")
    suspend fun subtractPoints(points: Int)
    
    @Query("UPDATE player SET selectedBallColor = :color WHERE id = 1")
    suspend fun updateBallColor(color: String)
    
    @Query("UPDATE player SET extraTimeSeconds = :seconds WHERE id = 1")
    suspend fun updateExtraTime(seconds: Int)
}


