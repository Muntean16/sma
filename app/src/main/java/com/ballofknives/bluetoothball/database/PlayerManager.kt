package com.ballofknives.bluetoothball.database

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PlayerManager(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.playerDao()
    private val userManager = UserManager(context)
    
    private fun getUserId(): Long {
        return userManager.getCurrentUserId() ?: throw IllegalStateException("No user logged in")
    }
    
    private suspend fun ensureInitialized(userId: Long) {
        val player = dao.getPlayer(userId)
        if (player == null) {
            dao.insertPlayer(PlayerEntity(id = userId, totalPoints = 0))
        }
    }
    
    fun getPlayer(): PlayerEntity {
        return runBlocking(Dispatchers.IO) {
            val userId = getUserId()
            ensureInitialized(userId)
            dao.getPlayer(userId) ?: PlayerEntity(id = userId, totalPoints = 0)
        }
    }
    
    fun addPoints(points: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = getUserId()
            ensureInitialized(userId)
            dao.addPoints(userId, points)
        }
    }
    
    fun subtractPoints(points: Int): Boolean {
        return runBlocking(Dispatchers.IO) {
            val userId = getUserId()
            ensureInitialized(userId)
            val player = dao.getPlayer(userId) ?: return@runBlocking false
            if (player.totalPoints >= points) {
                dao.subtractPoints(userId, points)
                true
            } else {
                false
            }
        }
    }
    
    fun updateBallColor(color: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = getUserId()
            ensureInitialized(userId)
            dao.updateBallColor(userId, color)
        }
    }
    
    fun updateExtraTime(seconds: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = getUserId()
            ensureInitialized(userId)
            dao.updateExtraTime(userId, seconds)
        }
    }
    
    fun getTotalPoints(): Int {
        return runBlocking(Dispatchers.IO) {
            val userId = getUserId()
            ensureInitialized(userId)
            dao.getPlayer(userId)?.totalPoints ?: 0
        }
    }
}


