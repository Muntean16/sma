 package com.ballofknives.bluetoothmeatball

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build

import android.os.Bundle
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

 class MeatballActivity : AppCompatActivity() {
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
         btServer = BluetoothGameServer(this.bluetoothAdapter(), handler)
         btServer?.start()
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
