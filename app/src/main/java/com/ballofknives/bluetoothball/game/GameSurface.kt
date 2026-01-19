package com.ballofknives.bluetoothball.game

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.Display
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.database.PlayerManager
import com.ballofknives.bluetoothball.utils.Constants
import com.ballofknives.bluetoothball.utils.GameGlobals
import kotlin.random.Random

class GameSurface(context: Context) : SurfaceView(context), SurfaceHolder.Callback{
    
    companion object {
        private const val TAG = "GameSurface"
    }
    
    private var cx : Float = 0.toFloat()
    var cy : Float = 0.toFloat()

    var picHeight: Int = 0
    var picWidth : Int = 0

    private var icon: Bitmap
    private var icons: List<Bitmap>
    private var ballColorFilter: ColorMatrixColorFilter? = null

    private var Windowwidth : Int = 0
    private var Windowheight : Int = 0
    private var gameWidth: Int = 0
    private var gameHeight: Int = 0

    private var onBorderX = false
    private var onBorderY = false

    private var drawThread : DrawThread?= null
    
    private var timerText: String = ""
    private var timerPaint: Paint = Paint().apply {
        color = Color.BLACK
        textSize = 48f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    
    private var scorePaint: Paint = Paint().apply {
        color = Color.BLACK
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    
    private var backgroundPaint: Paint = Paint().apply {
        color = Color.argb(180, 255, 255, 255) // Semi-transparent white
        style = Paint.Style.FILL
    }
    
    private var pointPaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    
    private var points: MutableList<GamePoint> = mutableListOf()
    private var sharedScore: Int = 0
    private var onPointCollected: ((Int) -> Unit)? = null // pointIndex
    
    data class GamePoint(val x: Float, val y: Float, var collected: Boolean = false)


    init {
        holder.addCallback(this)

        drawThread = DrawThread(holder, this)

        val display: Display = (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size: Point = Point()
        display.getSize(size)
        Windowwidth = size.x
        Windowheight = size.y
        icon = BitmapFactory.decodeResource(resources,R.drawable.ball)
        icons = listOf<Bitmap>(BitmapFactory.decodeResource(resources,R.drawable.ball),
            BitmapFactory.decodeResource(resources,R.drawable.ball),
            BitmapFactory.decodeResource(resources,R.drawable.ball),
            BitmapFactory.decodeResource(resources,R.drawable.ball))
        picHeight = icon!!.height
        picWidth = icon!!.width
        // Allow ball to reach full screen - ball position is top-left corner, so we need full screen bounds
        gameWidth = Windowwidth - picWidth
        gameHeight = Windowheight - picHeight
        
        // Initialize points
        sharedScore = 0
        initializePoints()
        
        // Load player's selected ball color
        loadBallColor()
    }
    
    private fun loadBallColor() {
        val playerManager = PlayerManager(context)
        val player = playerManager.getPlayer()
        val colorName = player.selectedBallColor
        
        ballColorFilter = when (colorName) {
            "red" -> createColorFilter(1f, 0f, 0f) // Red
            "blue" -> createColorFilter(0f, 0f, 1f) // Blue
            "green" -> createColorFilter(0f, 1f, 0f) // Green
            "yellow" -> createColorFilter(1f, 1f, 0f) // Yellow
            "purple" -> createColorFilter(0.5f, 0f, 0.5f) // Purple
            "orange" -> createColorFilter(1f, 0.5f, 0f) // Orange
            else -> null // Default - no filter
        }
    }
    
    private fun createColorFilter(r: Float, g: Float, b: Float): ColorMatrixColorFilter {
        val matrix = ColorMatrix(floatArrayOf(
            r, 0f, 0f, 0f, 0f,  // Red channel
            0f, g, 0f, 0f, 0f,  // Green channel
            0f, 0f, b, 0f, 0f,  // Blue channel
            0f, 0f, 0f, 1f, 0f  // Alpha channel
        ))
        return ColorMatrixColorFilter(matrix)
    }
    
    private fun initializePoints() {
        points.clear()
        val pointRadius = GameGlobals.POINT_RADIUS
        val ballRadius = maxOf(picWidth, picHeight) / 2f
        // Spawn one point initially in reachable area
        // Ball center can be from (ballRadius) to (Windowwidth - ballRadius)
        val minX = ballRadius + pointRadius
        val maxX = Windowwidth - ballRadius - pointRadius
        val minY = ballRadius + pointRadius
        val maxY = Windowheight - ballRadius - pointRadius
        
        val x = if (maxX > minX) Random.nextFloat() * (maxX - minX) + minX else Windowwidth / 2f
        val y = if (maxY > minY) Random.nextFloat() * (maxY - minY) + minY else Windowheight / 2f
        points.add(GamePoint(x, y))
    }
    
    fun setOnPointCollectedCallback(callback: (Int) -> Unit) {
        onPointCollected = callback
    }
    
    fun collectPoint(index: Int) {
        if (index in points.indices && !points[index].collected) {
            points[index].collected = true
            sharedScore++
            // Spawn a new point at a random location
            spawnNewPoint()
            invalidate()
        }
    }
    
    private fun spawnNewPoint() {
        val pointRadius = GameGlobals.POINT_RADIUS
        val ballRadius = maxOf(picWidth, picHeight) / 2f
        // Spawn point in area where ball center can reach it
        // Ball center can be from (ballRadius) to (Windowwidth - ballRadius)
        val minX = ballRadius + pointRadius
        val maxX = Windowwidth - ballRadius - pointRadius
        val minY = ballRadius + pointRadius
        val maxY = Windowheight - ballRadius - pointRadius
        
        val x = if (maxX > minX) Random.nextFloat() * (maxX - minX) + minX else Windowwidth / 2f
        val y = if (maxY > minY) Random.nextFloat() * (maxY - minY) + minY else Windowheight / 2f
        points.add(GamePoint(x, y))
    }
    
    fun getSharedScore(): Int = sharedScore

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
       destroySurface()
    }

    fun destroySurface()
    {
        Log.i(Constants.TAG, "surface destroyed!")
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.i(Constants.TAG,"surfaceCreated")
        drawThread!!.setRunning(true)
        drawThread!!.start()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (canvas != null){
            canvas.drawColor(Color.WHITE)
            
            // Draw points
            points.forEachIndexed { index, point ->
                if (!point.collected) {
                    canvas.drawCircle(point.x, point.y, GameGlobals.POINT_RADIUS, pointPaint)
                }
            }
            
            // Draw ball
            val ballPaint = Paint().apply {
                ballColorFilter?.let { colorFilter = it }
            }
            canvas.drawBitmap(icon, cx, cy, ballPaint)
            
            // Draw timer and score with background
            if (timerText.isNotEmpty()) {
                val timerY = 80f
                val scoreY = 130f
                val padding = 20f
                val cornerRadius = 15f
                
                // Calculate text widths for background
                val timerWidth = timerPaint.measureText(timerText)
                val scoreText = "Scor: $sharedScore"
                val scoreWidth = scorePaint.measureText(scoreText)
                val maxWidth = maxOf(timerWidth, scoreWidth) + padding * 2
                val backgroundHeight = 100f
                val backgroundX = (Windowwidth - maxWidth) / 2f
                val backgroundY = timerY - 50f
                
                // Draw rounded rectangle background
                val rect = android.graphics.RectF(
                    backgroundX,
                    backgroundY,
                    backgroundX + maxWidth,
                    backgroundY + backgroundHeight
                )
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)
                
                // Draw timer
                canvas.drawText(timerText, Windowwidth / 2f, timerY, timerPaint)
                
                // Draw score
                canvas.drawText(scoreText, Windowwidth / 2f, scoreY, scorePaint)
            }
        }
        else{
            Log.i("draw", "null canvas")
        }
    }

    override fun onDraw(canvas: Canvas) {

        canvas.drawColor(Color.WHITE)
        
        // Draw points
        points.forEachIndexed { index, point ->
            if (!point.collected) {
                canvas.drawCircle(point.x, point.y, GameGlobals.POINT_RADIUS, pointPaint)
            }
        }
        
        // Draw ball
        val ballPaint = Paint().apply {
            ballColorFilter?.let { colorFilter = it }
        }
        canvas.drawBitmap(icon, cx, cy, ballPaint)
        
        // Draw timer and score with background
        if (timerText.isNotEmpty()) {
            val timerY = 80f
            val scoreY = 130f
            val padding = 20f
            val cornerRadius = 15f
            
            // Calculate text widths for background
            val timerWidth = timerPaint.measureText(timerText)
            val scoreText = "Scor: $sharedScore"
            val scoreWidth = scorePaint.measureText(scoreText)
            val maxWidth = maxOf(timerWidth, scoreWidth) + padding * 2
            val backgroundHeight = 100f
            val backgroundX = (Windowwidth - maxWidth) / 2f
            val backgroundY = timerY - 50f
            
            // Draw rounded rectangle background
            val rect = android.graphics.RectF(
                backgroundX,
                backgroundY,
                backgroundX + maxWidth,
                backgroundY + backgroundHeight
            )
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)
            
            // Draw timer
            canvas.drawText(timerText, Windowwidth / 2f, timerY, timerPaint)
            
            // Draw score
            canvas.drawText(scoreText, Windowwidth / 2f, scoreY, scorePaint)
        }
    }
    
    @Synchronized fun updateTimer(timeText: String) {
        timerText = timeText
        invalidate()
    }
    
    @Synchronized fun updateScore(score: Int) {
        sharedScore = score
        invalidate()
    }

    @Synchronized fun updateMe(inx : Float , iny : Float){
        val scale=5
        val deltaX = scale*inx
        val deltaY = scale*iny
        cx += deltaX
        cy += deltaY

        // Allow ball to reach all edges - clamp to keep ball fully visible
        // cx and cy are top-left corner of ball bitmap
        if(cx > gameWidth){
            cx = gameWidth.toFloat()
        }
        else if(cx < 0){
            cx = 0F
        }

        if (cy > gameHeight){
            cy = gameHeight.toFloat()
        }
        else if(cy < 0){
            cy = 0F
        }
        
        // Check for point collisions
        checkPointCollisions()
        
        Log.i(TAG,"( cx,cy ) : " + "(" + "%5.2f".format(cx) + "," + "%5.2f".format(cy) + ")")

        invalidate()
    }
    
    private fun checkPointCollisions() {
        // Calculate ball center position (cx, cy is top-left corner of bitmap)
        val ballCenterX = cx + picWidth / 2f
        val ballCenterY = cy + picHeight / 2f
        // Use average dimension for ball radius - more accurate than max/min
        val ballRadius = (picWidth + picHeight) / 4f
        val pointRadius = GameGlobals.POINT_RADIUS
        
        points.forEachIndexed { index, point ->
            if (!point.collected) {
                // Calculate distance between ball center and point center
                val dx = ballCenterX - point.x
                val dy = ballCenterY - point.y
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                
                // Calculate the maximum distance for collision (sum of radii)
                val maxCollisionDistance = ballRadius + pointRadius
                
                // Only collect if there's clear overlap - require distance to be significantly less
                // This ensures the ball is actually covering the point, not just near it
                // Subtract 15f to require clear visual overlap
                if (distance < maxCollisionDistance - 15f) {
                    // Point collected - shared score
                    collectPoint(index)
                    onPointCollected?.invoke(index)
                    Log.i(TAG, "Point collected! Ball center: ($ballCenterX, $ballCenterY), Point: (${point.x}, ${point.y}), Distance: $distance, Max: $maxCollisionDistance")
                }
            }
        }
    }
}





