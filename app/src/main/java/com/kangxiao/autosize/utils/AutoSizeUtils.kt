package com.kangxiao.autosize.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.TypedValue
import java.lang.Exception
import java.lang.NullPointerException

/**
 * 常用工具类
 */
object AutoSizeUtils {

    fun dp2px(context: Context,value:Float):Int{
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,value,context.resources.displayMetrics)+ 0.5f).toInt()
    }

    fun sp2px(context: Context,value:Float):Int{
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,value,context.resources.displayMetrics)+ 0.5f).toInt()
    }

    fun pt2px(context: Context,value:Float):Int{
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT,value,context.resources.displayMetrics)+ 0.5f).toInt()
    }

    fun in2px(context: Context,value:Float):Int{
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN,value,context.resources.displayMetrics)+ 0.5f).toInt()
    }

    fun mm2px(context: Context,value:Float):Int{
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,value,context.resources.displayMetrics)+ 0.5f).toInt()
    }

    /**
     * 通过反射ActivityThread获取当前Application
     */
    @SuppressLint("PrivateApi")
    fun getApplicationByReflect(): Application {
        try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val currentActivityThread = activityThread.getMethod("currentActivityThread").invoke(null)
            val application = activityThread.getMethod("getApplication").invoke(currentActivityThread)
            application?.let {
                return@let it
            }?: kotlin.runCatching {
                throw NullPointerException("you should init first")
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        throw NullPointerException("you should init first")
    }

}