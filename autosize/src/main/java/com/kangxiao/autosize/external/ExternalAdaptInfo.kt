package com.kangxiao.autosize.external

import android.os.Parcel
import android.os.Parcelable

/**
 * 用来存储外部三方库的适配参数,因为 AutoSize 默认会对项目中的所有模块都进行适配
 * 三方库也不例外，但三方库的适配参数可能和自己项目中的适配参数不一致，导致三方库的适配效果和理想的效果差别很大
 * 所以需要向 AutoSize 提供三方库的适配参数，已完成对三方库的屏幕适配
 */
class ExternalAdaptInfo() : Parcelable {

    /**
     * 是否按照宽度进行等比例适配
     * {@code true} 为按照宽度适配，{@code false} 为按照高度适配
     */
    private var isBaseOnWidth: Boolean? = null

    /**
     * 设计图上的设计尺寸，单位dp
     */
    private var sizeInDp: Float? = null

    constructor(parcel: Parcel) : this() {
        isBaseOnWidth = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        sizeInDp = parcel.readValue(Float::class.java.classLoader) as? Float
    }

    constructor(isBaseOnWidth: Boolean) : this() {
        this.isBaseOnWidth = isBaseOnWidth
    }

    constructor(isBaseOnWidth: Boolean, sizeInDp: Float) : this() {
        this.isBaseOnWidth = isBaseOnWidth
        this.sizeInDp = sizeInDp
    }

    fun setBaseOnWidth(baseOnWidth: Boolean) {
        this.isBaseOnWidth = baseOnWidth
    }

    fun isBaseOnWidth() = isBaseOnWidth

    fun setSizeInDp(sizeInDp: Float) {
        this.sizeInDp = sizeInDp
    }

    fun getSizeInDp() = sizeInDp
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(isBaseOnWidth)
        parcel.writeValue(sizeInDp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExternalAdaptInfo> {
        override fun createFromParcel(parcel: Parcel): ExternalAdaptInfo {
            return ExternalAdaptInfo(parcel)
        }

        override fun newArray(size: Int): Array<ExternalAdaptInfo?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "ExternalAdaptInfo{" + "isBaseOnWidth=" + isBaseOnWidth + ", sizeInDp=" + sizeInDp + "}";
    }

}