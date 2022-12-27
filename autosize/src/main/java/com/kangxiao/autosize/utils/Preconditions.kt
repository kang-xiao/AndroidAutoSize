package com.kangxiao.autosize.utils

import android.os.Looper
import java.lang.StringBuilder

/**
 * 常用判断类
 */
object Preconditions {

    fun checkArgument(expression: Boolean) {
        if (!expression) {
            throw IllegalArgumentException()
        }
    }

    fun checkArgument(expression: Boolean, errorMessage: String) {
        if (!expression) {
            throw IllegalArgumentException(errorMessage)
        }
    }

    fun checkState(expression: Boolean) {
        if (!expression) {
            throw IllegalStateException()
        }
    }

    fun checkState(expression: Boolean, errorMessage: String) {
        if (!expression) {
            throw IllegalStateException(errorMessage)
        }
    }

    fun checkArgument(expression: Boolean, errorMessageTemplate: String, errorMessageArgs: Array<Any>) {
        if (!expression) {
            throw IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs))
        }
    }

    fun <T> checkNotNull(reference: T): T {
        if (reference == null) {
            throw NullPointerException()
        } else {
            return reference
        }
    }

    fun <T> checkNotNull(reference: T, errorMessageTemplate: String, errorMessageArgs: Array<Any>?): T {
        if (reference == null) {
            throw NullPointerException(errorMessageArgs?.let { format(errorMessageTemplate, it) })
        } else {
            return reference
        }
    }

    fun checkElementIndex(index: Int, size: Int, desc: String): Int {
        if (index in 0 until size) {
            return index
        } else {
            throw IndexOutOfBoundsException(baseElementIndex(index, size, desc))
        }
    }

    fun checkMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw IllegalStateException("Not in applications main thread")
        }
    }

    private fun baseElementIndex(index: Int, size: Int, desc: String): String? {
        if (index < 0) {
            return format("%s (%s) must not be negative", arrayOf(desc, Integer.valueOf(index)))
        } else if (size < 0) {
            throw IllegalArgumentException(
                StringBuilder(26).append("negative size: ").append(size).toString()
            )
        } else {
            return format(
                "%s (%s) must be less than size (%s)",
                arrayOf(desc, Integer.valueOf(index), Integer.valueOf(size))
            )
        }
    }

    fun checkPositionIndex(index: Int, size: Int): Int {
        return checkPositionIndex(index, size, "index")
    }

    fun checkPositionIndex(index: Int, size: Int, desc: String): Int {
        if (index in 0..size) {
            return index
        } else {
            throw IndexOutOfBoundsException(basePositionIndex(index, size, desc))
        }
    }

    private fun basePositionIndex(index: Int, size: Int, desc: String): String? {
        return if (index < 0) {
            format("%s (%s) must not be negative", arrayOf(desc, Integer.valueOf(index)))
        } else if (size < 0) {
            throw IllegalArgumentException(
                StringBuilder(26).append("negative size: ").append(size).toString()
            )
        } else {
            format(
                "%s (%s) must be less than size (%s)",
                arrayOf(desc, Integer.valueOf(index), Integer.valueOf(size))
            )
        }
    }

    fun checkPositionIndexes(start: Int, end: Int, size: Int) {
        if (start < 0 || end < start || end > size) {
            throw IndexOutOfBoundsException(basePositionIndexes(start, end, size))
        }
    }

    private fun basePositionIndexes(start: Int, end: Int, size: Int): String? {
        return if (start in 0..size){
            if (end in 0..size){
                format("end index (%s) must not be less than start index (%s)",arrayOf(Integer.valueOf(end),Integer.valueOf(start)))
            }else{
                basePositionIndex(end,size,"end index")
            }
        }else{
            basePositionIndex(start,size,"start index")
        }
    }


    fun format(template: String, args: Array<Any>): String {
        val builder = StringBuilder(template.length + 16 * args.size)
        var templateStart = 0

        var index = 0
        var placeholderStart = 0

        for (i in args.indices) {
            placeholderStart = template.indexOf("%s", templateStart)
            index = i
            if (placeholderStart == -1) {
                break
            }
            builder.append(template.substring(templateStart, placeholderStart))
            builder.append(args[i])
            templateStart = placeholderStart + 2
        }

        builder.append(template.substring(templateStart))
        if (index < args.size) {
            builder.append(" [")
            builder.append(args[index])
            index++

            while (index < args.size) {
                builder.append(", ")
                builder.append(args[index])
                index++
            }

            builder.append("]")
        }
        return builder.toString()
    }
}