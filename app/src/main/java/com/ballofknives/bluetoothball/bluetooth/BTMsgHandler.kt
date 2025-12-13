package com.ballofknives.bluetoothball.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import com.ballofknives.bluetoothball.game.GameSurface
import com.ballofknives.bluetoothball.ui.SelectAballFragmentDirections
import com.ballofknives.bluetoothball.utils.Constants
import com.ballofknives.bluetoothball.utils.GameGlobals
import java.nio.ByteBuffer

class BTMsgHandler(looper: Looper, private var surface: GameSurface?, private val viewForNavigation: View? = null): Handler(looper) {
    
    companion object {
        private const val TAG = "BTMsgHandler"
    }

    @SuppressLint("MissingPermission")
    override fun handleMessage(msg: Message) {
        when( msg.what ){
            GameGlobals.CONNECTED ->{
                val device: BluetoothDevice = msg.obj as BluetoothDevice
                val action = SelectAballFragmentDirections.actionSelectAballFragmentToDriverConnectedFragment( device.name )
                viewForNavigation?.findNavController()?.navigate(action)
            }
            GameGlobals.MESSAGE_WRITE-> {
            }
            GameGlobals.MESSAGE_TIMER -> {
                val timeText = msg.obj as? String ?: ""
                surface?.updateTimer(timeText)
            }
            GameGlobals.MESSAGE_READ -> {
                val data = msg.obj as ByteArray
                // Timer message is 6 bytes (1 byte type + 5 bytes for "mm:ss" string)
                // Point collection message is 5 bytes (1 byte type + 1 byte index + 1 byte isServer + 2 bytes points)
                // Joystick data is 2 floats = 8 bytes
                if (data.size == 6 && data.isNotEmpty() && data[0].toInt() == GameGlobals.MESSAGE_TIMER) {
                    // This is a timer message from client (shouldn't happen, but handle it)
                    val timeText = String(data, 1, data.size - 1, java.nio.charset.Charset.defaultCharset())
                    surface?.updateTimer(timeText)
                } else if (data.size == 4 && data.isNotEmpty() && data[0].toInt() == GameGlobals.MESSAGE_POINT_COLLECTED) {
                    // Point collection message (type, index, score high, score low)
                    val pointIndex = data[1].toInt()
                    val score = ((data[2].toInt() and 0xFF) shl 8) or (data[3].toInt() and 0xFF)
                    surface?.collectPoint(pointIndex)
                    // Update score display
                    surface?.updateScore(score)
                } else if (data.size == 8) {
                    val xCoord = ByteBuffer.wrap(data).getFloat(0);
                    val yCoord = ByteBuffer.wrap(data).getFloat(4);
                    Log.i(Constants.TAG, "Read (x,y) from socket: ($xCoord,$yCoord)")
                    surface?.updateMe(xCoord, yCoord)
                }
            }
            else -> {
                val pass: Unit = Unit
            }
        }
    }
}


