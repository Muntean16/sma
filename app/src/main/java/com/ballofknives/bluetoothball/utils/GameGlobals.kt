package com.ballofknives.bluetoothball.utils

class GameGlobals {
    companion object {
        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_DEVICE_NAME = 4
        const val MESSAGE_TOAST = 5
        const val MESSAGE_TIMER = 6
        const val MESSAGE_GAME_END = 8
        const val CONNECTED = 100
        const val DEVICE_NAME = "device_name"
        const val WRITE_DATA = "write_data"
        const val READ_DATA = "read_data"
        const val BLUETOOTH_DATA = "bt_data"
        const val TOAST = "toast"
        
        // Game constants
        const val GAME_DURATION_SECONDS = 60
        const val NUM_POINTS = 1
        const val POINT_RADIUS = 30f
        const val EXTRA_TIME_MESSAGE = 6
        const val MESSAGE_POINT_COLLECTED = 7
    }
}

