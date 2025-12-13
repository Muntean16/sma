package com.ballofknives.bluetoothball.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.database.HighScoreManager
import com.ballofknives.bluetoothball.database.PlayerManager

class GameResultsActivity : AppCompatActivity() {
    
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_results)
        
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        
        val sharedScore = intent.getIntExtra("sharedScore", 0)
        val isServer = intent.getBooleanExtra("isServer", false)
        val partnerName = intent.getStringExtra("partnerName") ?: "Unknown"
        
        val scoreText = findViewById<TextView>(R.id.scoreText)
        val winnerText = findViewById<TextView>(R.id.winnerText)
        val backButton = findViewById<Button>(R.id.backButton)
        val viewHighScoresButton = findViewById<Button>(R.id.viewHighScoresButton)
        
        scoreText.text = "Scor Final: $sharedScore"
        winnerText.text = "Excelentă colaborare!"
        
        val highScoreManager = HighScoreManager(this)
        val role = if (isServer) "Server" else "Client"
        highScoreManager.addHighScore(sharedScore, partnerName, role)
        
        val playerManager = PlayerManager(this)
        playerManager.addPoints(sharedScore)
        
        val titleCard = findViewById<CardView>(R.id.titleCard)
        val scoreCard = findViewById<CardView>(R.id.scoreCard)
        
        titleCard?.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_in))
        scoreCard?.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_up))
        winnerText.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in))
        
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        
        viewHighScoresButton?.setOnClickListener {
            val intent = android.content.Intent(this, HighScoresActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
    }
}


