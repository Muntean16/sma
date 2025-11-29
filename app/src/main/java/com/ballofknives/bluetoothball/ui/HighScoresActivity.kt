package com.ballofknives.bluetoothball.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.database.AppDatabase
import com.ballofknives.bluetoothball.database.HighScoreEntity
import com.ballofknives.bluetoothball.database.HighScoreManager
import kotlinx.coroutines.launch

class HighScoresActivity : AppCompatActivity() {
    
    private lateinit var highScoreManager: HighScoreManager
    private lateinit var scoresContainer: LinearLayout
    private lateinit var backButton: Button
    
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)
        
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        
        highScoreManager = HighScoreManager(this)
        scoresContainer = findViewById(R.id.scoresContainer)
        backButton = findViewById(R.id.backButton)
        
        val titleCard = findViewById<CardView>(R.id.titleCard)
        titleCard?.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_in))
        
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        
        loadScoresWithEntities()
    }
    
    private fun loadScoresWithEntities() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(this@HighScoresActivity)
            val entities = database.highScoreDao().getAllHighScoresList()
            
            runOnUiThread {
                displayScoresWithEntities(entities)
            }
        }
    }
    
    private fun displayScoresWithEntities(entities: List<HighScoreEntity>) {
        scoresContainer.removeAllViews()
        
        if (entities.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Nu există scoruri înregistrate"
                textSize = 18f
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
            }
            scoresContainer.addView(emptyText)
            return
        }
        
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            background = ContextCompat.getDrawable(this@HighScoresActivity, android.R.drawable.divider_horizontal_dark)
        }
        
        val rankHeader = TextView(this).apply {
            text = "#"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.1f)
        }
        val scoreHeader = TextView(this).apply {
            text = "Scor"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f)
        }
        val partnerHeader = TextView(this).apply {
            text = "Partener"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
        }
        val roleHeader = TextView(this).apply {
            text = "Rol"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f)
        }
        
        headerLayout.addView(rankHeader)
        headerLayout.addView(scoreHeader)
        headerLayout.addView(partnerHeader)
        headerLayout.addView(roleHeader)
        scoresContainer.addView(headerLayout)
        
        entities.forEachIndexed { index, entity ->
            val score = entity.toHighScore()
            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                radius = 12f
                elevation = 6f
                setCardBackgroundColor(ContextCompat.getColor(this@HighScoresActivity, android.R.color.white))
            }
            
            val scoreLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            val rankText = TextView(this).apply {
                text = "${index + 1}"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.1f)
            }
            val scoreText = TextView(this).apply {
                text = "${score.score}"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f)
            }
            val partnerText = TextView(this).apply {
                text = score.partnerName
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
            }
            val roleText = TextView(this).apply {
                text = score.role
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f)
            }
            
            scoreLayout.addView(rankText)
            scoreLayout.addView(scoreText)
            scoreLayout.addView(partnerText)
            scoreLayout.addView(roleText)
            cardView.addView(scoreLayout)
            scoresContainer.addView(cardView)
            
            cardView.alpha = 0f
            cardView.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay((index * 100).toLong())
                .start()
        }
    }
}

