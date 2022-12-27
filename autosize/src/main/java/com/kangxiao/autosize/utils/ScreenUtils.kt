package com.kangxiao.autosize.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import java.lang.Exception

/**
 * 屏幕和状态栏相关工具类
 */
object ScreenUtils {

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(): Int {
        var result = 0
        try {
            val resourceId =
                Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = Resources.getSystem().getDimensionPixelSize(resourceId)
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 获取当前的屏幕尺寸
     */
    fun getScreenSize(context: Context): ArrayList<Int> {
        val size = arrayListOf<Int>(2)

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)

        size[0] = metrics.widthPixels
        size[1] = metrics.heightPixels
        return size
    }

    /**
     * 获取原始的屏幕尺寸
     */
    fun getRawScreenSize(context: Context):ArrayList<Int>{
        val size = arrayListOf<Int>(2)

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)

        var widthPixels = metrics.widthPixels
        var heightPixels = metrics.heightPixels

        if (Build.VERSION.SDK_INT in 14..16){
            try {
                widthPixels = Display::class.java.getMethod("getRawWidth").invoke(display) as Int
                heightPixels = Display::class.java.getMethod("getRawHeight").invoke(display) as Int
            }catch (e:Exception){
            }
        }
        if (Build.VERSION.SDK_INT >= 17){
            try {
                val realSize = Point()
                Display::class.java.getMethod("getRealSize",Point::class.java).invoke(display,realSize)
                widthPixels = realSize.x
                heightPixels = realSize.y
            }catch (e : Exception){
            }
        }
        size[0] = widthPixels
        size[1] = heightPixels
        return size
    }

    /**
     * 导航栏的高度
     */
    fun getHeightOfNavigationBar(context: Context):Int{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            if (Settings.Global.getInt(context.contentResolver,"force_fsg_nav_bar",0)!=0){
                return 0
            }
        }
        val realHeight = getRawScreenSize(context)[1]
        val displayHeight = getScreenSize(context)[1]
        return realHeight - displayHeight
    }

}