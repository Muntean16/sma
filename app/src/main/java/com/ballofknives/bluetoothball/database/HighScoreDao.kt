package com.ballofknives.bluetoothball.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighScore(highScore: HighScoreEntity): Long
    
    @Query("SELECT * FROM high_scores ORDER BY score DESC, timestamp DESC")
    fun getAllHighScores(): Flow<List<HighScoreEntity>>
    
    @Query("SELECT * FROM high_scores ORDER BY score DESC, timestamp DESC")
    suspend fun getAllHighScoresList(): List<HighScoreEntity>
    
    @Query("SELECT * FROM high_scores ORDER BY score DESC, timestamp DESC LIMIT :limit")
    suspend fun getTopHighScores(limit: Int): List<HighScoreEntity>
    
    @Query("SELECT * FROM high_scores WHERE id = :id")
    suspend fun getHighScoreById(id: Long): HighScoreEntity?
    
    @Update
    suspend fun updateHighScore(highScore: HighScoreEntity)
    
    @Delete
    suspend fun deleteHighScore(highScore: HighScoreEntity)
    
    @Query("DELETE FROM high_scores")
    suspend fun deleteAllHighScores()
    
    @Query("DELETE FROM high_scores WHERE id = :id")
    suspend fun deleteHighScoreById(id: Long)
    
    @Query("SELECT COUNT(*) FROM high_scores")
    suspend fun getHighScoreCount(): Int
}


