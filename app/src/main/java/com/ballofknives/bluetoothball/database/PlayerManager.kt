package com.ballofknives.bluetoothball.database

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PlayerManager(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.playerDao()
    private var isInitialized = false
    
    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            val player = dao.getPlayer()
            if (player == null) {
                dao.insertPlayer(PlayerEntity(id = 1, totalPoints = 0))
            }
            isInitialized = true
        }
    }
    
    fun getPlayer(): PlayerEntity {
        return runBlocking(Dispatchers.IO) {
            ensureInitialized()
            dao.getPlayer() ?: PlayerEntity(id = 1, totalPoints = 0)
        }
    }
    
    fun addPoints(points: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.addPoints(points)
        }
    }
    
    fun subtractPoints(points: Int): Boolean {
        return runBlocking(Dispatchers.IO) {
            ensureInitialized()
            val player = dao.getPlayer() ?: return@runBlocking false
            if (player.totalPoints >= points) {
                dao.subtractPoints(points)
                true
            } else {
                false
            }
        }
    }
    
    fun updateBallColor(color: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.updateBallColor(color)
        }
    }
    
    fun updateExtraTime(seconds: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.updateExtraTime(seconds)
        }
    }
    
    fun getTotalPoints(): Int {
        return runBlocking(Dispatchers.IO) {
            ensureInitialized()
            dao.getPlayer()?.totalPoints ?: 0
        }
    }
}

