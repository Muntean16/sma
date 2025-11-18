package com.ballofknives.bluetoothmeatball

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.core.app.ActivityCompat

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.abs

class BluetoothGameServer(private var adapter: BluetoothAdapter?, private var handler: Handler?) {

    companion object {
        val GameUUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
        const val BLUETOOTH_SERVICE_NAME = "Bluetooth Meatball"
        const val STATE_NONE: Int = 0
        const val STATE_LISTEN: Int = 1
        const val STATE_CONNECTING: Int = 2
        const val STATE_CONNECTED: Int = 3
    }

    var connectedThread: ConnectedThread? = null
    private var acceptThread: AcceptThread? = null
    private var mState: Int = STATE_NONE
    var newState: Int = STATE_NONE

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

        connectedThread = ConnectedThread( socket )
        connectedThread?.start()

        val msg = handler?.obtainMessage( GameGlobals.MESSAGE_DEVICE_NAME )
        val bundle = Bundle()

        bundle.putString(GameGlobals.DEVICE_NAME, device.name)
        msg?.data = bundle
        if(msg!=null)
            handler?.sendMessage(msg)
    }

    @Synchronized fun stop(){
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
                    Log.i(TAG, "read ${bytes} bytes from socket")
                    val xCoord = ByteBuffer.wrap(buffer).getFloat(0);
                    val yCoord = ByteBuffer.wrap(buffer).getFloat(4);
                    Log.i(TAG, "Read (x,y) from socket: ($xCoord,$yCoord)")
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
                localOutStream?.write(buffer)
                handler?.obtainMessage(GameGlobals.MESSAGE_WRITE, -1, -1, buffer)?.sendToTarget()
            }
            catch( e: IOException){
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