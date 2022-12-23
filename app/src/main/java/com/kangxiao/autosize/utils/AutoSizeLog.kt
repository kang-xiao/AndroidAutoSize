package com.kangxiao.autosize.utils

import android.util.Log

/**
 * 用来打印日志
 */
object AutoSizeLog {

    private const val TAG = "AndroidAutoSize"

    private var debug: Boolean = false

    fun setDebug(debug: Boolean) {
        this.debug = debug
    }

    fun getDebug(): Boolean = debug

    fun d(message: String) {
        if (debug) {
            Log.d(TAG, message)
        }
    }

    fun w(message: String) {
        if (debug) {
            Log.w(TAG, message)
        }
    }

    fun e(message: String){
        if (debug){
            Log.e(TAG,message)
        }
    }

}