package com.ballofknives.bluetoothball.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Bundle
import android.util.Log
import com.ballofknives.bluetoothball.game.GameSurface
import com.ballofknives.bluetoothball.utils.Constants
import com.ballofknives.bluetoothball.utils.GameGlobals
import com.ballofknives.bluetoothball.database.PlayerManager
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.*

class BluetoothGameServer(
    private var adapter: BluetoothAdapter?,
    private var handler: Handler?,
    private val onGameEnd: ((Int) -> Unit)? = null,
    private val context: Context? = null
) {

    companion object {
        val GameUUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
        const val BLUETOOTH_SERVICE_NAME = "Bluetooth ball"
        const val STATE_NONE: Int = 0
        const val STATE_LISTEN: Int = 1
        const val STATE_CONNECTING: Int = 2
        const val STATE_CONNECTED: Int = 3
    }

    var connectedThread: ConnectedThread? = null
    private var acceptThread: AcceptThread? = null
    private var mState: Int = STATE_NONE
    var newState: Int = STATE_NONE
    private var connectedDevice: BluetoothDevice? = null
    private var timerThread: TimerThread? = null

    init {
        mState = STATE_NONE
        newState = mState
    }

    @Synchronized fun start(){
        if( connectedThread != null ){
            connectedThread?.cancel()
            connectedThread = null
        }

        if( acceptThread == null ){
            acceptThread = AcceptThread()
            acceptThread?.start()
        }
    }

    @SuppressLint("MissingPermission")
    @Synchronized fun connected(socket: BluetoothSocket, device: BluetoothDevice){
        if( connectedThread != null){
            connectedThread?.cancel()
            connectedThread = null
        }

        if( acceptThread != null ){
            acceptThread?.cancel()
            acceptThread = null
        }

        connectedDevice = device
        connectedThread = ConnectedThread( socket )
        connectedThread?.start()

        val msg = handler?.obtainMessage( GameGlobals.MESSAGE_DEVICE_NAME )
        val bundle = Bundle()
        bundle.putString(GameGlobals.DEVICE_NAME, getDeviceName(device))
        msg?.data = bundle
        if(msg!=null)
            handler?.sendMessage(msg)
        
        // Start game timer
        startGameTimer()
    }
    
    private fun startGameTimer() {
        timerThread?.cancel()
        timerThread = null
        
        val playerManager = context?.let { PlayerManager(it) }
        val extraTimeSeconds = playerManager?.getPlayer()?.extraTimeSeconds ?: 0
        val totalGameDuration = GameGlobals.GAME_DURATION_SECONDS + extraTimeSeconds
        
        timerThread = TimerThread(totalGameDuration) { remainingSeconds ->
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeText = String.format("%02d:%02d", minutes, seconds)
            
            // Send timer update to client
            sendTimerUpdate(timeText)
            
            // Update server UI
            val msg = handler?.obtainMessage(GameGlobals.MESSAGE_TIMER)
            msg?.let {
                it.obj = timeText
                handler?.sendMessage(it)
            }
            
            if (remainingSeconds <= 0) {
                // Game ended - get final score from GameSurface via handler
                onGameEnd?.invoke(0) // Will be updated with actual score
            }
        }
        timerThread?.start()
    }
    
    private fun sendTimerUpdate(timeText: String) {
        val message = ByteArray(6) // 1 byte type + 5 bytes for "mm:ss"
        message[0] = GameGlobals.MESSAGE_TIMER.toByte()
        val timeBytes = timeText.toByteArray(Charsets.UTF_8)
        System.arraycopy(timeBytes, 0, message, 1, minOf(timeBytes.size, 5))
        connectedThread?.write(message)
    }
    
    fun sendPointCollectedWithScore(pointIndex: Int, score: Int) {
        val message = ByteArray(5) // 1 byte type + 1 byte index + 2 bytes score
        message[0] = GameGlobals.MESSAGE_POINT_COLLECTED.toByte()
        message[1] = pointIndex.toByte()
        message[2] = ((score shr 8) and 0xFF).toByte() // High byte
        message[3] = (score and 0xFF).toByte() // Low byte
        connectedThread?.write(message)
    }
    
    fun sendGameEnd(score: Int) {
        val message = ByteArray(3) // 1 byte type + 2 bytes score
        message[0] = GameGlobals.MESSAGE_GAME_END.toByte()
        message[1] = ((score shr 8) and 0xFF).toByte() // High byte
        message[2] = (score and 0xFF).toByte() // Low byte
        android.util.Log.d(Constants.TAG, "Sending game end message with score: $score, message type: ${message[0].toInt()}")
        if (connectedThread != null) {
            connectedThread?.write(message)
        } else {
            android.util.Log.e(Constants.TAG, "Cannot send game end: connectedThread is null")
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun getDeviceName(device: BluetoothDevice?): String {
        return try {
            device?.name ?: "Unknown"
        } catch (e: SecurityException) {
            "Unknown"
        }
    }
    
    @SuppressLint("MissingPermission")
    fun getConnectedDeviceName(): String? {
        return getDeviceName(connectedDevice)
    }
    
    private inner class TimerThread(
        private val durationSeconds: Int,
        private val onTick: (Int) -> Unit
    ) : Thread() {
        private var running = true
        
        override fun run() {
            var remaining = durationSeconds
            while (running && remaining >= 0) {
                onTick(remaining)
                if (remaining > 0) {
                    try {
                        sleep(1000)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
                remaining--
            }
        }
        
        fun cancel() {
            running = false
            interrupt()
        }
    }

    @Synchronized fun stop(){
        timerThread?.cancel()
        timerThread = null
        if ( connectedThread != null ){
            connectedThread?.cancel()
            connectedThread = null
        }
        if ( acceptThread != null ) {
            acceptThread?.cancel()
            acceptThread = null
        }
        mState = STATE_NONE
    }

    fun write( out : ByteArray ){
        var r : ConnectedThread?
        synchronized(this){
            if( mState != STATE_CONNECTED ) {
                return
            }
            r = connectedThread
        }

        r?.write(out)
        val msg = handler?.obtainMessage( GameGlobals.MESSAGE_WRITE )
        val bundle = Bundle()
        bundle.putByteArray(GameGlobals.BLUETOOTH_DATA, out)
        msg?.data = bundle
        if(msg != null)
            handler?.sendMessage(msg)
    }

    private fun connectionLost(){
        val msg = handler?.obtainMessage(GameGlobals.MESSAGE_TOAST)
        val bundle = Bundle()
        mState = STATE_NONE

        this.start()
    }

    @SuppressLint("MissingPermission")
    inner class AcceptThread : Thread() {
        private var serverSocket : BluetoothServerSocket? = null
        init{
            var tmp: BluetoothServerSocket? = null

            try{
                tmp = adapter!!.listenUsingRfcommWithServiceRecord(BLUETOOTH_SERVICE_NAME, GameUUID)
            }
            catch( e: IOException ){
            }
            serverSocket = tmp
            mState = STATE_LISTEN
        }

        override fun run(){
            name = "Accept Thread"
            var localSocket: BluetoothSocket? = null
            while ( mState != STATE_CONNECTED ){
                try{
                    localSocket = serverSocket?.accept()
                }
                catch( e: IOException){
                    break
                }

                if ( localSocket != null){
                    synchronized(this){
                        when(mState){
                            STATE_LISTEN, STATE_CONNECTING -> connected( localSocket, localSocket.remoteDevice)
                            else ->try {
                                    localSocket.close()
                            }
                                catch( e: IOException ){
                            }
                        }

                    }
                }
            }
        }

        fun cancel(){
            try{
                serverSocket?.close()
            }
            catch(e: IOException){
            }
        }
    }

    inner class ConnectedThread(socket: BluetoothSocket? ): Thread(){
        var localSocket: BluetoothSocket? = null
        var localInStream: InputStream? = null
        var localOutStream: OutputStream? = null

        init{
            localSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try{
                tmpIn = socket?.getInputStream()
                tmpOut = socket?.getOutputStream()
            }
            catch( e: IOException ){
            }

            localInStream = tmpIn
            localOutStream = tmpOut
            mState = STATE_CONNECTED

        }

        override fun run(){
            var buffer = ByteArray(java.lang.Float.BYTES * 2)
            var bytes = 0

            while(mState == STATE_CONNECTED){
                try{
                    bytes = localInStream?.read(buffer)!!
                    Log.i(Constants.TAG, "read ${bytes} bytes from socket")
                    val xCoord = ByteBuffer.wrap(buffer).getFloat(0);
                    val yCoord = ByteBuffer.wrap(buffer).getFloat(4);
                    Log.i(Constants.TAG, "Read (x,y) from socket: ($xCoord,$yCoord)")
                    handler?.obtainMessage(GameGlobals.MESSAGE_READ, -1, -1, buffer)?.sendToTarget()
                }
                catch( e: IOException){
                    connectionLost()
                    break;
                }
            }
        }

        fun write(buffer: ByteArray){
            try{
                if (localOutStream != null) {
                    localOutStream?.write(buffer)
                    localOutStream?.flush() // Ensure message is sent immediately
                    android.util.Log.d(Constants.TAG, "Sent ${buffer.size} bytes, first byte: ${buffer[0]}")
                    handler?.obtainMessage(GameGlobals.MESSAGE_WRITE, -1, -1, buffer)?.sendToTarget()
                } else {
                    android.util.Log.e(Constants.TAG, "Cannot write: output stream is null")
                }
            }
            catch( e: IOException){
                android.util.Log.e(Constants.TAG, "Error writing to output stream", e)
            }
        }

        fun cancel(){
            try {
                localSocket?.close()
            }
            catch( e: IOException){
            }
        }
    }

}

