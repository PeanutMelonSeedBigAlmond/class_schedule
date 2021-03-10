package com.wp.csmu.classschedule.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object ViewUtils {
    fun getScreenPixels(context: Context): IntArray {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        return intArrayOf(width, height)
    }
}