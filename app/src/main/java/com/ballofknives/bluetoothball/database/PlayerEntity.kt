package com.ballofknives.bluetoothball.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey
    val id: Int = 1,
    var totalPoints: Int = 0,
    var selectedBallColor: String = "default",
    var extraTimeSeconds: Int = 0
)


