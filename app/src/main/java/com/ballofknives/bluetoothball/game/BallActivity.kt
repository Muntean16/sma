package com.ballofknives.bluetoothball.game

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.ballofknives.bluetoothball.bluetooth.BTMsgHandler
import com.ballofknives.bluetoothball.bluetooth.BluetoothGameServer
import com.ballofknives.bluetoothball.ui.GameResultsActivity
import com.ballofknives.bluetoothball.utils.bluetoothAdapter
import java.lang.ref.WeakReference

class BallActivity : AppCompatActivity() {
    private var gameSurface : GameSurface ?= null
    private var handler: BTMsgHandler? = null
    private var btServer: BluetoothGameServer? = null
    private lateinit var surfaceReference : WeakReference<GameSurface>

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameSurface = GameSurface(this)
        setContentView(gameSurface)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

    }

     override fun onResume(){
         super.onResume()
         handler = null
         handler = BTMsgHandler(Looper.myLooper()!!, gameSurface)
         btServer = BluetoothGameServer(this.bluetoothAdapter(), handler, { sharedScore ->
             // Game end callback with shared score
             runOnUiThread {
                 // Get the actual score from GameSurface (source of truth)
                 val finalScore = gameSurface?.getSharedScore() ?: sharedScore
                 
                 // Send game end message to client with final score FIRST
                 // This must happen before opening the results activity
                 android.util.Log.d("BallActivity", "Game ended, sending message to client with score: $finalScore")
                 btServer?.sendGameEnd(finalScore)
                 
                 // Wait a bit to ensure message is sent before opening results
                 handler?.postDelayed({
                     val partnerName = btServer?.getConnectedDeviceName() ?: "Unknown"
                     val intent = android.content.Intent(this, GameResultsActivity::class.java).apply {
                         putExtra("sharedScore", finalScore)
                         putExtra("isServer", true)
                         putExtra("partnerName", partnerName)
                     }
                     startActivity(intent)
                     // Don't finish immediately - wait a bit more to ensure message is sent
                     handler?.postDelayed({
                         finish()
                     }, 500)
                 }, 200)
             }
         }, this)
         btServer?.start()
         
         // Set up point collection callback
         gameSurface?.setOnPointCollectedCallback { pointIndex ->
             val currentScore = gameSurface?.getSharedScore() ?: 0
             btServer?.sendPointCollectedWithScore(pointIndex, currentScore)
         }
         
         setContentView(gameSurface)

     }

     override fun onPause() {
         super.onPause()
         handler = null
         btServer?.stop()
         btServer = null
         finish()
     }

     override fun onDestroy() {
         super.onDestroy()
         finish()
     }


     @Deprecated("Deprecated in Java")
     override fun onBackPressed() {
         super.onBackPressed()
         try {
             gameSurface?.destroySurface()
             gameSurface = null
             finish()
         }
         catch(e: Exception){
         }
     }
}

