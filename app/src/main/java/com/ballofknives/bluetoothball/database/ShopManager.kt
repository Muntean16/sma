package com.ballofknives.bluetoothball.database

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ShopManager(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.shopItemDao()
    private val purchaseDao = database.userPurchaseDao()
    private val playerManager = PlayerManager(context)
    private val userManager = UserManager(context)
    private var isInitialized = false
    
    private fun getUserId(): Long {
        return userManager.getCurrentUserId() ?: throw IllegalStateException("No user logged in")
    }
    
    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            val items = dao.getAllItems()
            if (items.isEmpty()) {
                initializeShopItems()
            }
            isInitialized = true
        }
    }
    
    private suspend fun initializeShopItems() {
        val items = listOf(
            ShopItemEntity(
                id = "ball_red",
                name = "Minge Roșie",
                description = "Minge de culoare roșie",
                price = 50,
                itemType = "ball_color",
                value = "red"
            ),
            ShopItemEntity(
                id = "ball_blue",
                name = "Minge Albastră",
                description = "Minge de culoare albastră",
                price = 50,
                itemType = "ball_color",
                value = "blue"
            ),
            ShopItemEntity(
                id = "ball_green",
                name = "Minge Verde",
                description = "Minge de culoare verde",
                price = 50,
                itemType = "ball_color",
                value = "green"
            ),
            ShopItemEntity(
                id = "ball_yellow",
                name = "Minge Galbenă",
                description = "Minge de culoare galbenă",
                price = 50,
                itemType = "ball_color",
                value = "yellow"
            ),
            ShopItemEntity(
                id = "ball_purple",
                name = "Minge Mov",
                description = "Minge de culoare mov",
                price = 75,
                itemType = "ball_color",
                value = "purple"
            ),
            ShopItemEntity(
                id = "ball_orange",
                name = "Minge Portocalie",
                description = "Minge de culoare portocalie",
                price = 75,
                itemType = "ball_color",
                value = "orange"
            ),
            ShopItemEntity(
                id = "time_10",
                name = "+10 Secunde",
                description = "Adaugă 10 secunde la durata jocului",
                price = 100,
                itemType = "extra_time",
                value = "10"
            ),
            ShopItemEntity(
                id = "time_20",
                name = "+20 Secunde",
                description = "Adaugă 20 secunde la durata jocului",
                price = 180,
                itemType = "extra_time",
                value = "20"
            ),
            ShopItemEntity(
                id = "time_30",
                name = "+30 Secunde",
                description = "Adaugă 30 secunde la durata jocului",
                price = 250,
                itemType = "extra_time",
                value = "30"
            )
        )
        dao.insertItems(items)
    }
    
    fun getAllItems(): List<ShopItemEntity> {
        return runBlocking(Dispatchers.IO) {
            ensureInitialized()
            dao.getAllItems()
        }
    }
    
    fun isItemPurchased(itemId: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            val userId = getUserId()
            purchaseDao.getPurchase(userId, itemId) != null
        }
    }
    
    fun getItemsByType(type: String): List<ShopItemEntity> {
        return runBlocking(Dispatchers.IO) {
            ensureInitialized()
            dao.getItemsByType(type)
        }
    }
    
    fun purchaseItem(itemId: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            ensureInitialized()
            val userId = getUserId()
            val item = dao.getItemById(itemId) ?: return@runBlocking false
            
            // Check if already purchased
            if (isItemPurchased(itemId)) {
                return@runBlocking false
            }
            
            if (playerManager.subtractPoints(item.price)) {
                // Record purchase
                purchaseDao.insertPurchase(UserPurchaseEntity(userId = userId, shopItemId = itemId))
                
                when (item.itemType) {
                    "ball_color" -> {
                        playerManager.updateBallColor(item.value)
                    }
                    "extra_time" -> {
                        val currentTime = playerManager.getPlayer().extraTimeSeconds
                        val additionalTime = item.value.toIntOrNull() ?: 0
                        playerManager.updateExtraTime(currentTime + additionalTime)
                    }
                }
                true
            } else {
                false
            }
        }
    }
}


