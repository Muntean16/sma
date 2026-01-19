package com.ballofknives.bluetoothball.database

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HighScoreManager(private val context: Context) {
    
    companion object {
        private const val MAX_SCORES = 10
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.highScoreDao()
    
    fun addHighScore(score: Int, partnerName: String, role: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val highScoreEntity = HighScoreEntity(
                score = score,
                partnerName = partnerName,
                role = role,
                timestamp = System.currentTimeMillis()
            )
            dao.insertHighScore(highScoreEntity)
            
            val allScores = dao.getAllHighScoresList()
            if (allScores.size > MAX_SCORES) {
                val scoresToDelete = allScores.drop(MAX_SCORES)
                scoresToDelete.forEach { dao.deleteHighScore(it) }
            }
        }
    }
    
    fun getHighScores(): List<HighScore> {
        return runBlocking(Dispatchers.IO) {
            dao.getAllHighScoresList().map { it.toHighScore() }
        }
    }
    
    fun getHighScoresFlow(): Flow<List<HighScore>> {
        return dao.getAllHighScores().map { entities ->
            entities.map { it.toHighScore() }
        }
    }
    
    fun getTopHighScores(limit: Int = MAX_SCORES): List<HighScore> {
        return runBlocking(Dispatchers.IO) {
            dao.getTopHighScores(limit).map { it.toHighScore() }
        }
    }
    
    fun getHighScoreById(id: Long): HighScore? {
        return runBlocking(Dispatchers.IO) {
            dao.getHighScoreById(id)?.toHighScore()
        }
    }
    
    fun updateHighScore(id: Long, score: Int, partnerName: String, role: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = dao.getHighScoreById(id)
            if (existing != null) {
                val updated = existing.copy(
                    score = score,
                    partnerName = partnerName,
                    role = role
                )
                dao.updateHighScore(updated)
            }
        }
    }
    
    fun updateHighScore(highScoreEntity: HighScoreEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.updateHighScore(highScoreEntity)
        }
    }
    
    fun deleteHighScore(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteHighScoreById(id)
        }
    }
    
    fun clearHighScores() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteAllHighScores()
        }
    }
    
    fun getHighScoreCount(): Int {
        return runBlocking(Dispatchers.IO) {
            dao.getHighScoreCount()
        }
    }
}





