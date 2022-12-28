package com.kangxiao.autosize

import android.content.res.Resources
import android.util.SparseArray
import com.kangxiao.autosize.internal.CustomAdapt
import com.kangxiao.autosize.utils.Preconditions
import android.R.attr.screenSize
import android.content.res.Configuration
import android.util.DisplayMetrics
import com.kangxiao.autosize.unit.Subunits
import kotlin.math.roundToInt


/**
 * 当遇到本来正常的布局突然出现适配失效，适配等问题，重写当前 {@link Activity} 的 {@link Activity#getResources()}并调用
 * {@link AutoSizeCompat} 的对应方法即可解决问题
 */
object AutoSizeCompat {

    private val mCache:SparseArray<DisplayMetricsInfo> = SparseArray()
    private const val MODE_SHIFT = 30
    private const val MODE_MASK = 0x3 shl MODE_SHIFT
    private const val MODE_ON_WIDTH = 1 shl MODE_SHIFT
    private const val MODE_DEVICE_SIZE = 2 shl MODE_SHIFT

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配（AndroidManifest 的 Meta 属性）
     */
    fun autoConvertDensityOfGlobal(resources: Resources){
        if (AutoSizeConfig.getInstance().isBaseOnWidth()){
            autoConvertDensityBaseOnWidth(resources,AutoSizeConfig.getInstance().getDesignWidthInDp().toFloat())
        }else{
            autoConvertDensityBaseOnHeight(resources,AutoSizeConfig.getInstance().getDesignHeightInDp().toFloat())
        }
    }

    /**
     * 以宽度为基准进行适配
     *
     * @param resources       [Resources]
     * @param designWidthInDp 设计图的总宽度
     */
    fun autoConvertDensityBaseOnWidth(resources: Resources?, designWidthInDp: Float) {
        if (resources != null) {
            autoConvertDensity(resources, designWidthInDp, true)
        }
    }

    /**
     * 以高度为基准进行适配
     *
     * @param resources        [Resources]
     * @param designHeightInDp 设计图的总高度
     */
    fun autoConvertDensityBaseOnHeight(resources: Resources?, designHeightInDp: Float) {
        if (resources != null) {
            autoConvertDensity(resources, designHeightInDp, false)
        }
    }

    /**
     * 使用 {@link Activity}或 Fragment 的自定义参数进行适配
     */
    fun autoConvertDensityOfCustomAdapt(resources: Resources,customAdapt: CustomAdapt){
        Preconditions.checkNotNull(customAdapt,"customAdapt == null",null)
        var sizeInDp = customAdapt.getSizeInDp()

        //如果 CustomAdapt#getSizeInDp() 返回0，则使用在 AndroidManifest 上填写的设计图尺寸
        if (sizeInDp<=0){
            sizeInDp = if (customAdapt.isBaseOnWidth()){
                AutoSizeConfig.getInstance().getDesignWidthInDp().toFloat()
            }else{
                AutoSizeConfig.getInstance().getDesignHeightInDp().toFloat()
            }
        }
        autoConvertDensity(resources,sizeInDp,customAdapt.isBaseOnWidth())
    }

    /**
     * 这里是今日头条适配方案的核心代码，核心在于根据当前设备的实际情况做自动计算并转换 {@link DisplayMetrics#density}、
     * {@link DisplayMetrics#scaledDensity}、{@link DisplayMetrics#densityDpi} 这三个值，额外增加 {@link DisplayMetrics#xdpi}
     * 以支持单位 {@code pt}、{@code in}、{@code mm}
     */
    private fun autoConvertDensity(resources: Resources, sizeInDp: Float, baseOnWidth: Boolean) {
        Preconditions.checkNotNull(resources,"resources == null",null)
        Preconditions.checkMainThread()

        var subunitsDesignSize:Float = if (baseOnWidth){
            AutoSizeConfig.getInstance().getDesignWidthInDp().toFloat()
        }else{
            AutoSizeConfig.getInstance().getDesignHeightInDp().toFloat()
        }

        subunitsDesignSize = if (subunitsDesignSize>0){
            subunitsDesignSize
        }else{
            sizeInDp
        }

        var key = ((sizeInDp + subunitsDesignSize + screenSize) * AutoSizeConfig.getInstance().getInitScaledDensity()).roundToInt() and MODE_MASK.inv()

        key = if (baseOnWidth) key or MODE_ON_WIDTH else key and MODE_ON_WIDTH.inv()
        key = if (AutoSizeConfig.getInstance().isUseDeviceSize()) key or MODE_DEVICE_SIZE else key and MODE_DEVICE_SIZE.inv()

        val displayMetricsInfo = mCache[key]

        var targetDensity = 0f
        var targetDensityDpi = 0
        var targetScaledDensity = 0f
        var targetXdpi = 0f
        val targetScreenWidthDp: Int
        val targetScreenHeightDp: Int

        if (displayMetricsInfo == null) {
            targetDensity = if (baseOnWidth) {
                AutoSizeConfig.getInstance().getScreenWidth() * 1.0f / sizeInDp
            } else {
                AutoSizeConfig.getInstance().getScreenHeight() * 1.0f / sizeInDp
            }

            targetScaledDensity = if (AutoSizeConfig.getInstance().getPrivateFontScale() > 0) {
                targetDensity * AutoSizeConfig.getInstance().getPrivateFontScale()
            } else {
                val systemFontScale: Float = if (AutoSizeConfig.getInstance().isExcludeFontScale()) 1f else AutoSizeConfig.getInstance().getInitScaledDensity() * 1.0f / AutoSizeConfig.getInstance().getInitDensity()
                targetDensity * systemFontScale
            }
            targetDensityDpi = (targetDensity * 160).toInt()

            targetScreenWidthDp = (AutoSizeConfig.getInstance().getScreenWidth() / targetDensity).toInt()
            targetScreenHeightDp = (AutoSizeConfig.getInstance().getScreenHeight() / targetDensity).toInt()

            targetXdpi = if (baseOnWidth) {
                AutoSizeConfig.getInstance().getScreenWidth() * 1.0f / subunitsDesignSize
            } else {
                AutoSizeConfig.getInstance().getScreenHeight() * 1.0f / subunitsDesignSize
            }
            mCache.put(key, DisplayMetricsInfo(targetDensity, targetDensityDpi, targetScaledDensity, targetXdpi, targetScreenWidthDp, targetScreenHeightDp))
        } else {
            targetDensity = displayMetricsInfo.getDensity()
            targetDensityDpi = displayMetricsInfo.getDensityDpi()
            targetScaledDensity = displayMetricsInfo.getScaledDensity()
            targetXdpi = displayMetricsInfo.getXdpi()
            targetScreenWidthDp = displayMetricsInfo.getScreenWidthDp()
            targetScreenHeightDp = displayMetricsInfo.getScreenHeightDp()
        }

        setDensity(resources, targetDensity, targetDensityDpi, targetScaledDensity, targetXdpi)
        setScreenSizeDp(resources, targetScreenWidthDp, targetScreenHeightDp)
    }

    /**
     * 赋值
     */
    private fun setDensity(resources: Resources, density: Float, densityDpi: Int, scaledDensity: Float, xdpi: Float) {
        val activityDisplayMetrics = resources.displayMetrics
        setDensity(activityDisplayMetrics, density, densityDpi, scaledDensity, xdpi)
        val appDisplayMetrics: DisplayMetrics? =
            AutoSizeConfig.getInstance().getApplication()?.resources?.displayMetrics
        setDensity(appDisplayMetrics, density, densityDpi, scaledDensity, xdpi)

        //兼容 MIUI
        val activityDisplayMetricsOnMIUI: DisplayMetrics? = getMetricsOnMiui(resources)
        val appDisplayMetricsOnMIUI: DisplayMetrics? =
            getMetricsOnMiui(AutoSizeConfig.getInstance().getApplication()?.resources)

        setDensity(activityDisplayMetricsOnMIUI, density, densityDpi, scaledDensity, xdpi)
        if (appDisplayMetricsOnMIUI != null) {
            setDensity(appDisplayMetricsOnMIUI, density, densityDpi, scaledDensity, xdpi)
        }
    }

    /**
     * 解决 MIUI 更改框架导致的 MIUI7 + Android5.1.1 上出现的失效问题 (以及极少数基于这部分 MIUI 去掉 ART 然后置入 XPosed 的手机)
     */
    private fun getMetricsOnMiui(resources: Resources?): DisplayMetrics? {
        return if (AutoSizeConfig.getInstance().isMiui() && AutoSizeConfig.getInstance().getTmpMetricsField() != null) {
            try {
                (AutoSizeConfig.getInstance().getTmpMetricsField()!![resources] as DisplayMetrics)!!
            } catch (e: Exception) {
                null
            }
        } else null
    }

    private fun setDensity(displayMetrics: DisplayMetrics?, density: Float, densityDpi: Int, scaledDensity: Float, xdpi: Float) {
        if (AutoSizeConfig.getInstance().getUnitsManager().isSupportDP()) {
            displayMetrics?.density = density
            displayMetrics?.densityDpi = densityDpi
        }
        if (AutoSizeConfig.getInstance().getUnitsManager().isSupportSP()) {
            displayMetrics?.scaledDensity = scaledDensity
        }
        when (AutoSizeConfig.getInstance().getUnitsManager().getSupportSubunits()) {
            Subunits.NONE -> {
            }
            Subunits.PT -> displayMetrics?.xdpi = xdpi * 72f
            Subunits.IN -> displayMetrics?.xdpi = xdpi
            Subunits.MM -> displayMetrics?.xdpi = xdpi * 25.4f
            else -> {
            }
        }
    }

    private fun setScreenSizeDp(resources: Resources, screenWidthDp: Int, screenHeightDp: Int) {
        if (AutoSizeConfig.getInstance().getUnitsManager().isSupportDP() && AutoSizeConfig.getInstance().getUnitsManager().isSupportScreenSizeDP()) {
            val activityConfiguration: Configuration = resources.configuration
            setScreenSizeDp(activityConfiguration, screenWidthDp, screenHeightDp)
            val appConfiguration: Configuration = AutoSizeConfig.getInstance().getApplication()!!.resources.configuration
            setScreenSizeDp(appConfiguration, screenWidthDp, screenHeightDp)
        }
    }

    /**
     * Configuration赋值
     *
     * @param configuration  [Configuration]
     * @param screenWidthDp  [Configuration.screenWidthDp]
     * @param screenHeightDp [Configuration.screenHeightDp]
     */
    private fun setScreenSizeDp(configuration: Configuration, screenWidthDp: Int, screenHeightDp: Int) {
        configuration.screenWidthDp = screenWidthDp
        configuration.screenHeightDp = screenHeightDp
    }

}