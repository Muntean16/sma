package com.ballofknives.bluetoothball.database

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers

class UserManager(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.userDao()
    private val playerDao = database.playerDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    private val CURRENT_USER_ID_KEY = "current_user_id"
    
    fun getCurrentUserId(): Long? {
        val userId = prefs.getLong(CURRENT_USER_ID_KEY, -1L)
        return if (userId == -1L) null else userId
    }
    
    fun setCurrentUserId(userId: Long) {
        prefs.edit().putLong(CURRENT_USER_ID_KEY, userId).apply()
    }
    
    fun logout() {
        prefs.edit().remove(CURRENT_USER_ID_KEY).apply()
    }
    
    fun registerUser(username: String, password: String, code: String = ""): Long? {
        return runBlocking(Dispatchers.IO) {
            // Check if username already exists
            val existingUser = dao.getUserByUsername(username)
            if (existingUser != null) {
                return@runBlocking null
            }
            
            // Create new user
            val userId = dao.insertUser(UserEntity(username = username, password = password, code = code))
            
            // Create player entity for this user
            playerDao.insertPlayer(PlayerEntity(id = userId, totalPoints = 0))
            
            userId
        }
    }
    
    fun loginUser(username: String, password: String): Long? {
        return runBlocking(Dispatchers.IO) {
            val user = dao.getUserByUsername(username)
            if (user != null && user.password == password) {
                // Ensure player entity exists
                val player = playerDao.getPlayer(user.id)
                if (player == null) {
                    playerDao.insertPlayer(PlayerEntity(id = user.id, totalPoints = 0))
                }
                user.id
            } else {
                null
            }
        }
    }
    
    fun getUserById(userId: Long): UserEntity? {
        return runBlocking(Dispatchers.IO) {
            dao.getUserById(userId)
        }
    }
    
    fun updateUserCode(userId: Long, code: String) {
        runBlocking(Dispatchers.IO) {
            dao.updateCode(userId, code)
        }
    }
    
    fun getCurrentUser(): UserEntity? {
        val userId = getCurrentUserId() ?: return null
        return getUserById(userId)
    }
}




