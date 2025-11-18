package com.ballofknives.bluetoothmeatball

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.Display
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import kotlin.math.pow
import kotlin.math.sqrt

class GameSurface(context: Context) : SurfaceView(context), SurfaceHolder.Callback{
    private var cx : Float = 0.toFloat()
    var cy : Float = 0.toFloat()

    var picHeight: Int = 0
    var picWidth : Int = 0

    private var icon: Bitmap
    private var icons: List<Bitmap>

    private var Windowwidth : Int = 0
    private var Windowheight : Int = 0
    private var gameWidth: Int = 0
    private var gameHeight: Int = 0

    private var onBorderX = false
    private var onBorderY = false

    private var drawThread : DrawThread?= null


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
        gameWidth = Windowwidth - picWidth
        gameHeight = Windowheight - picHeight
    }

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
            canvas.drawBitmap(icon,cx,cy,null)
        }
        else{
            Log.i("draw", "null canvas")
        }
    }

    override fun onDraw(canvas: Canvas) {

        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(icon,cx,cy,null)
    }

    @Synchronized fun updateMe(inx : Float , iny : Float){
        val scale=5
        val deltaX = scale*inx
        val deltaY = scale*iny
        cx += deltaX
        cy += deltaY

        if(cx > gameWidth ){
            cx = gameWidth.toFloat()
            if (onBorderX){
                onBorderX = false
            }
        }
        else if(cx < (0)){
            cx = 0F
            if(onBorderX){
                onBorderX = false
            }
        }
        else{
            onBorderX = true
        }

        if (cy > (gameHeight)){
            cy = (gameHeight).toFloat()
            if (onBorderY){
                onBorderY = false
            }
        }

        else if(cy < (0)){
            cy = 0F
            if (onBorderY){
                onBorderY= false
            }
        }
        else{
            onBorderY = true
        }
        Log.i(TAG,"( cx,cy ) : " + "(" + "%5.2f".format(cx) + "," + "%5.2f".format(cy) + ")")

        invalidate()
    }
}
