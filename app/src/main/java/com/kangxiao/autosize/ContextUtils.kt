package com.kangxiao.autosize

import android.annotation.SuppressLint
import android.app.Application
import java.lang.Exception
import java.lang.NullPointerException

/**
 * 有关于环境上下文的工具类
 */
object ContextUtils {

    /**
     * 通过反射ActivityThread获取当前Application
     */
    @SuppressLint("PrivateApi")
    fun getApplicationByReflect():Application{
        try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val currentActivityThread = activityThread.getMethod("currentActivityThread").invoke(null)
            val application = activityThread.getMethod("getApplication").invoke(currentActivityThread)
            application?.let {
                return@let it
            }?: kotlin.runCatching {
                throw NullPointerException("you should init first")
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        throw NullPointerException("you should init first")
    }

}