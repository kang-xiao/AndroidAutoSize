package com.kangxiao.autosize

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.kangxiao.autosize.utils.AutoSizeUtils

/**
 * 通过声明 {@link ContentProvider} 自动完成初始化
 */
class InitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val application : Application = (context?.applicationContext?:AutoSizeUtils.getApplicationByReflect()) as Application
        AutoSizeConfig.getInstance().setLog(true)
            .init(application)
            .setUseDeviceSize(false)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}