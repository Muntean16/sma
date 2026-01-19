package com.ballofknives.bluetoothball.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.database.UserManager

class LoginActivity : AppCompatActivity() {
    
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var codeEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var userManager: UserManager
    
    private var isRegisterMode = false
    
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
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
        
        userManager = UserManager(this)
        
        // Check if user is already logged in
        if (userManager.getCurrentUserId() != null) {
            navigateToMain()
            return
        }
        
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        codeEditText = findViewById(R.id.codeEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        registerTextView = findViewById(R.id.registerTextView)
        
        // Initially hide code field and register button
        codeEditText.visibility = View.GONE
        registerButton.visibility = View.GONE
        
        loginButton.setOnClickListener {
            if (isRegisterMode) {
                switchToLoginMode()
            } else {
                attemptLogin()
            }
        }
        
        registerButton.setOnClickListener {
            attemptRegister()
        }
        
        registerTextView.setOnClickListener {
            switchToRegisterMode()
        }
    }
    
    private fun switchToLoginMode() {
        isRegisterMode = false
        codeEditText.visibility = View.GONE
        registerButton.visibility = View.GONE
        loginButton.text = "Conectează-te"
        registerTextView.text = "Nu ai cont? Înregistrează-te"
        registerTextView.visibility = View.VISIBLE
    }
    
    private fun switchToRegisterMode() {
        isRegisterMode = true
        codeEditText.visibility = View.VISIBLE
        registerButton.visibility = View.VISIBLE
        loginButton.text = "Anulează"
        registerTextView.visibility = View.GONE
    }
    
    private fun attemptLogin() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Te rugăm să completezi toate câmpurile", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = userManager.loginUser(username, password)
        if (userId != null) {
            userManager.setCurrentUserId(userId)
            Toast.makeText(this, "Bun venit, $username!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Eroare de autentificare")
                .setMessage("Numele de utilizator sau parola sunt incorecte.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    private fun attemptRegister() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val code = codeEditText.text.toString().trim()
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Te rugăm să completezi numele de utilizator și parola", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (username.length < 3) {
            Toast.makeText(this, "Numele de utilizator trebuie să aibă cel puțin 3 caractere", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password.length < 4) {
            Toast.makeText(this, "Parola trebuie să aibă cel puțin 4 caractere", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = userManager.registerUser(username, password, code)
        if (userId != null) {
            userManager.setCurrentUserId(userId)
            Toast.makeText(this, "Cont creat cu succes! Bun venit, $username!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Eroare la înregistrare")
                .setMessage("Numele de utilizator este deja folosit. Te rugăm să alegi altul.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}



