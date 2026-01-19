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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.database.PlayerManager
import com.ballofknives.bluetoothball.database.ShopItemEntity
import com.ballofknives.bluetoothball.database.ShopManager
import kotlinx.coroutines.launch

class ShopActivity : AppCompatActivity() {
    
    private lateinit var shopManager: ShopManager
    private lateinit var playerManager: PlayerManager
    private lateinit var itemsContainer: LinearLayout
    private lateinit var pointsTextView: TextView
    private lateinit var backButton: Button
    
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)
        
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        
        try {
            shopManager = ShopManager(this)
            playerManager = PlayerManager(this)
            
            itemsContainer = findViewById(R.id.itemsContainer)
            pointsTextView = findViewById(R.id.pointsTextView)
            backButton = findViewById(R.id.backButton)
            
            backButton.setOnClickListener {
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            
            loadShopItems()
            updatePointsDisplay()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }
    
    private fun updatePointsDisplay() {
        val totalPoints = playerManager.getTotalPoints()
        pointsTextView.text = "Puncte: $totalPoints"
    }
    
    private fun loadShopItems() {
        lifecycleScope.launch {
            val items = shopManager.getAllItems()
            val selectedColor = shopManager.getCurrentBallColor()
            
            runOnUiThread {
                displayItems(items, selectedColor)
            }
        }
    }
    
    private fun displayItems(items: List<ShopItemEntity>, selectedBallColor: String) {
        itemsContainer.removeAllViews()
        
        if (items.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Nu există items disponibile"
                textSize = 18f
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
            }
            itemsContainer.addView(emptyText)
            return
        }
        
        val ballItems = items.filter { it.itemType == "ball_color" }
        val timeItems = items.filter { it.itemType == "extra_time" }
        
        if (ballItems.isNotEmpty()) {
            val sectionTitle = TextView(this).apply {
                text = "Bile Colorate"
                textSize = 20f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(16, 24, 16, 16)
            }
            itemsContainer.addView(sectionTitle)
            
            ballItems.forEach { item ->
                addItemView(item, selectedBallColor)
            }
        }
        
        if (timeItems.isNotEmpty()) {
            val sectionTitle = TextView(this).apply {
                text = "Timp Extra"
                textSize = 20f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(16, 24, 16, 16)
            }
            itemsContainer.addView(sectionTitle)
            
            timeItems.forEach { item ->
                addItemView(item, selectedBallColor)
            }
        }
    }
    
    private fun addItemView(item: ShopItemEntity, selectedBallColor: String) {
        val isPurchased = shopManager.isItemPurchased(item.id)
        val isSelectedBall =
            item.itemType == "ball_color" && item.value.equals(selectedBallColor, ignoreCase = true)
        
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 12, 16, 12)
            }
            radius = 16f
            elevation = 8f
            setCardBackgroundColor(ContextCompat.getColor(this@ShopActivity, android.R.color.white))
        }
        
        val itemLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val nameText = TextView(this).apply {
            text = item.name
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(8, 8, 8, 4)
        }
        
        val descText = TextView(this).apply {
            text = item.description
            textSize = 14f
            setPadding(8, 4, 8, 4)
        }
        
        val priceText = TextView(this).apply {
            text = "Preț: ${item.price} puncte"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@ShopActivity, android.R.color.holo_green_dark))
            setPadding(8, 4, 8, 8)
        }
        
        val buyButton = Button(this).apply {
            when (item.itemType) {
                "ball_color" -> {
                    text = when {
                        isSelectedBall -> "Selectată"
                        isPurchased -> "Folosește"
                        else -> "Cumpără"
                    }

                    isEnabled = when {
                        isSelectedBall -> false
                        isPurchased -> true
                        else -> playerManager.getTotalPoints() >= item.price
                    }

                    setOnClickListener {
                        if (isPurchased) {
                            selectBallColor(item)
                        } else {
                            purchaseItem(item)
                        }
                    }
                }

                else -> {
                    text = if (isPurchased) "Achiziționat" else "Cumpără"
                    isEnabled = !isPurchased && playerManager.getTotalPoints() >= item.price
                    setOnClickListener {
                        purchaseItem(item)
                    }
                }
            }
        }
        
        itemLayout.addView(nameText)
        itemLayout.addView(descText)
        itemLayout.addView(priceText)
        itemLayout.addView(buyButton)
        
        cardView.addView(itemLayout)
        itemsContainer.addView(cardView)
        
        cardView.alpha = 0f
        cardView.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay((itemsContainer.childCount * 100).toLong())
            .start()
    }
    
    private fun purchaseItem(item: ShopItemEntity) {
        val totalPoints = playerManager.getTotalPoints()
        
        if (totalPoints < item.price) {
            AlertDialog.Builder(this)
                .setTitle("Puncte Insuficiente")
                .setMessage("Ai nevoie de ${item.price} puncte pentru a cumpăra acest item. Ai doar $totalPoints puncte.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        AlertDialog.Builder(this)
            .setTitle("Confirmă Cumpărarea")
            .setMessage("Ești sigur că vrei să cumperi ${item.name} pentru ${item.price} puncte?")
            .setPositiveButton("Da") { _, _ ->
                lifecycleScope.launch {
                    val success = shopManager.purchaseItem(item.id)
                    runOnUiThread {
                        if (success) {
                            AlertDialog.Builder(this@ShopActivity)
                                .setTitle("Succes!")
                                .setMessage("Ai cumpărat cu succes ${item.name}!")
                                .setPositiveButton("OK") { _, _ ->
                                    loadShopItems()
                                    updatePointsDisplay()
                                }
                                .show()
                        } else {
                            AlertDialog.Builder(this@ShopActivity)
                                .setTitle("Eroare")
                                .setMessage("Nu s-a putut finaliza cumpărarea.")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                }
            }
            .setNegativeButton("Anulează", null)
            .show()
    }

    private fun selectBallColor(item: ShopItemEntity) {
        lifecycleScope.launch {
            val success = shopManager.selectBallColor(item.id)
            runOnUiThread {
                if (success) {
                    AlertDialog.Builder(this@ShopActivity)
                        .setTitle("Culoare selectată")
                        .setMessage("Folosești acum ${item.name}.")
                        .setPositiveButton("OK") { _, _ ->
                            loadShopItems()
                        }
                        .show()
                } else {
                    AlertDialog.Builder(this@ShopActivity)
                        .setTitle("Eroare")
                        .setMessage("Selectarea nu a putut fi finalizată.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}


