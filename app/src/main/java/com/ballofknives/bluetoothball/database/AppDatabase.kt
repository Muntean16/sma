package com.ballofknives.bluetoothball.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HighScoreEntity::class, PlayerEntity::class, ShopItemEntity::class, UserEntity::class, UserPurchaseEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun highScoreDao(): HighScoreDao
    abstract fun playerDao(): PlayerDao
    abstract fun shopItemDao(): ShopItemDao
    abstract fun userDao(): UserDao
    abstract fun userPurchaseDao(): UserPurchaseDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bluetooth_ball_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


