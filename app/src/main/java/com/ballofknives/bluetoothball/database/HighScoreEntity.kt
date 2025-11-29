package com.ballofknives.bluetoothball.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val partnerName: String,
    val role: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toHighScore(): HighScore {
        return HighScore(score, partnerName, role, timestamp)
    }
    
    companion object {
        fun fromHighScore(highScore: HighScore): HighScoreEntity {
            return HighScoreEntity(
                score = highScore.score,
                partnerName = highScore.partnerName,
                role = highScore.role,
                timestamp = highScore.timestamp
            )
        }
    }
}

