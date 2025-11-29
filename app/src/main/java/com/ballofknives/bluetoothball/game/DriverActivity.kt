package com.ballofknives.bluetoothball.game

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.bluetooth.BluetoothSharedViewModel
import com.ballofknives.bluetoothball.databinding.ActivityDriverBinding

class DriverActivity : AppCompatActivity(){

    private lateinit var navController: NavController

    lateinit var bluetoothSharedViewModel: BluetoothSharedViewModel

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel before fragments try to access it
        // Use ViewModelProvider to ensure fragments can access the same instance
        // For AndroidViewModel, we need to use the application context
        bluetoothSharedViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[BluetoothSharedViewModel::class.java]

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navController = navHostFragment?.navController ?: run {
            finish()
            return
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}

