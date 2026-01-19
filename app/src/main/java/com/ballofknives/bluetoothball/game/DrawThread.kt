package com.ballofknives.bluetoothball.game

import android.graphics.Canvas
import android.view.SurfaceHolder

class DrawThread (surfaceHolder: SurfaceHolder, panel : GameSurface) : Thread() {
    private var surfaceHolder : SurfaceHolder?= null
    private var panel : GameSurface ?= null
    private var run = false

    init {
        this.surfaceHolder = surfaceHolder
        this.panel = panel
    }

    fun setRunning(run : Boolean){
        this.run = run
    }

    override fun run() {
        var c: Canvas?= null
        while (run){
            c = null
            try {
                c = surfaceHolder!!.lockCanvas(null)
                synchronized(surfaceHolder!!){
                    panel!!.draw(c)
                }
                if (c!= null){
                    surfaceHolder!!.unlockCanvasAndPost(c)
                }
            }catch(e: Exception){
                return;
            }
        }
    }

}





