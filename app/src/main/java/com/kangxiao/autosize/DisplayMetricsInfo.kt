package com.kangxiao.autosize

import android.os.Parcel
import android.os.Parcelable

class DisplayMetricsInfo() : Parcelable{

    private var density:Float = 0f
    private var densityDpi:Int = 0
    private var scaledDensity:Float = 9f
    private var xdpi:Float = 0f
    private var screenWidthDp:Int = 0
    private var screenHeightDp:Int = 0

    fun getDensity() = density
    fun getDensityDpi() = densityDpi
    fun getScaledDensity() = scaledDensity
    fun getXdpi() = xdpi
    fun getScreenWidthDp() = screenWidthDp
    fun getScreenHeightDp() = screenHeightDp

    constructor(parcel: Parcel) : this() {
        density = parcel.readValue(Float::class.java.classLoader) as Float
        densityDpi = parcel.readValue(Int::class.java.classLoader) as Int
        scaledDensity = parcel.readValue(Float::class.java.classLoader) as Float
        xdpi = parcel.readValue(Float::class.java.classLoader) as Float
        screenWidthDp = parcel.readValue(Int::class.java.classLoader) as Int
        screenHeightDp = parcel.readValue(Int::class.java.classLoader) as Int
    }

    constructor(
        density: Float,
        densityDpi: Int,
        scaledDensity: Float,
        xdpi: Float,
        screenWidthDp: Int,
        screenHeightDp: Int
    ) : this() {
        this.density = density
        this.densityDpi = densityDpi
        this.scaledDensity = scaledDensity
        this.xdpi = xdpi
        this.screenWidthDp = screenWidthDp
        this.screenHeightDp = screenHeightDp
    }

    constructor(density: Float, densityDpi: Int, scaledDensity: Float, xdpi: Float) : this() {
        this.density = density
        this.densityDpi = densityDpi
        this.scaledDensity = scaledDensity
        this.xdpi = xdpi
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(density)
        parcel.writeValue(densityDpi)
        parcel.writeValue(scaledDensity)
        parcel.writeValue(xdpi)
        parcel.writeValue(screenWidthDp)
        parcel.writeValue(screenHeightDp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DisplayMetricsInfo> {
        override fun createFromParcel(parcel: Parcel): DisplayMetricsInfo {
            return DisplayMetricsInfo(parcel)
        }

        override fun newArray(size: Int): Array<DisplayMetricsInfo?> {
            return arrayOfNulls(size)
        }
    }


}