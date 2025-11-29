package com.ballofknives.bluetoothball.database

import java.io.Serializable

data class HighScore(
    val score: Int,
    val partnerName: String,
    val role: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

