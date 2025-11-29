package com.ballofknives.bluetoothball.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.game.BallActivity
import com.ballofknives.bluetoothball.game.DriverActivity
import com.ballofknives.bluetoothball.utils.PersistentStorage

const val TAG = "Bluetoothball"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        @SuppressLint("SourceLockedOrientationActivity")
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        requestBluetoothPermission()
        
        window.decorView.post {
            val imageView = findViewById<android.widget.ImageView>(R.id.imageView)
            val titleText = findViewById<android.widget.TextView>(R.id.textView4)
            
            titleText?.let {
                it.alpha = 0f
                it.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .start()
            }
            imageView?.let {
                it.alpha = 0f
                it.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(200)
                    .start()
            }
            
            val buttons = listOf(
                findViewById<com.google.android.material.button.MaterialButton>(R.id.driver_button),
                findViewById<com.google.android.material.button.MaterialButton>(R.id.ball_button),
                findViewById<com.google.android.material.button.MaterialButton>(R.id.highScoresButton),
                findViewById<com.google.android.material.button.MaterialButton>(R.id.shopButton)
            )
            
            buttons.forEachIndexed { index, button ->
                button?.let {
                    it.alpha = 0f
                    it.translationY = 50f
                    it.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(600)
                        .setStartDelay((400 + index * 150).toLong())
                        .start()
                }
            }
        }
    }
    private val persistentStorage = PersistentStorage(this)

    private val bluetoothPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ map ->
            val userAcknowledgement = map.values.all{ it -> it }
            if(userAcknowledgement) {
                persistentStorage.userHasAcknowledgedBluetoothPermissionRationale = false
            }
        }

    private fun isBluetoothPermissionGranted() : Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT ) == PackageManager.PERMISSION_GRANTED
        } else{
            ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED ) &&
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ))
        }
    }

    private fun shouldShowBluetoothPermissionRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)
        } else{
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                Manifest.permission.BLUETOOTH)
        }
    }

    private fun userHasPreviouslyAcknowledgedBluetoothPermissionRationale(): Boolean {
        return persistentStorage.userHasAcknowledgedBluetoothPermissionRationale
    }

    private fun requestBluetoothPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT
            )
            bluetoothPermissionRequest.launch(permissions)
        } else{
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            bluetoothPermissionRequest.launch(permissions)
        }
    }

    private fun showRationaleDialog(){
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.bluetooth_rationale_1))
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                persistentStorage.userHasAcknowledgedBluetoothPermissionRationale = true
                requestBluetoothPermission()
            }
            .show()
    }

    private fun showPreviouslyAcknowledgedRationaleDialog(){
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.bluetooth_rationale_2))
            .setNegativeButton(getString(R.string.no_thanks), null)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ -> showApplicationDetailsSettingsScreen() }
            .show()
    }

    private fun showApplicationDetailsSettingsScreen() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .setData(Uri.fromParts("package", packageName, null))

        startActivity(intent)
    }

    override fun onResume(){
        super.onResume()
    }

    fun chooseDriver( view: View) {
        if(isBluetoothPermissionGranted()){
            val driverIntent = Intent( this, DriverActivity::class.java)
            startActivity( driverIntent )
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
        else{
            if(shouldShowBluetoothPermissionRationale()){
                showRationaleDialog()
            }
            else if(userHasPreviouslyAcknowledgedBluetoothPermissionRationale()){
                showPreviouslyAcknowledgedRationaleDialog()
            }
            else{
                requestBluetoothPermission()
            }
        }
    }

    fun chooseBall(view: View) {
        if(isBluetoothPermissionGranted()){
            val ballIntent = Intent( this, BallActivity::class.java)
            ballIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity( ballIntent )
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
        else{
            if(shouldShowBluetoothPermissionRationale()){
                showRationaleDialog()
            }
            else if(userHasPreviouslyAcknowledgedBluetoothPermissionRationale()){
                showPreviouslyAcknowledgedRationaleDialog()
            }
            else{
                requestBluetoothPermission()
            }
        }
    }

    fun viewHighScores(view: View) {
        val highScoresIntent = Intent(this, HighScoresActivity::class.java)
        startActivity(highScoresIntent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
    }

    fun openShop(view: View) {
        val shopIntent = Intent(this, ShopActivity::class.java)
        startActivity(shopIntent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
    }
}

