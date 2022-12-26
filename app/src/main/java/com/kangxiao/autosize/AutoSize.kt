package com.kangxiao.autosize

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.util.SparseArray
import com.kangxiao.autosize.unit.Subunits
import com.kangxiao.autosize.utils.Preconditions
import android.util.DisplayMetrics
import android.net.Uri

import android.content.Context
import com.kangxiao.autosize.external.ExternalAdaptInfo

import com.kangxiao.autosize.external.ExternalAdaptManager
import com.kangxiao.autosize.internal.CustomAdapt



/**
 * 用于屏幕适配的核心方法都在这里,核心原理参考《今日头条官方适配方案》
 * 此方案只要应用到 {@link Activity} 上，这个 {@link Activity} 下的所有 Fragment、{@link Dialog}、
 * 自定义 {@link View} 都会达到适配的效果，如果某个页面不想使用适配请让该 {@link Activity} 实现 {@link CancelAdapt}
 */
object AutoSize {
    private val mCache: SparseArray<DisplayMetricsInfo> = SparseArray()
    private val MODE_SHIFT = 30
    private val MODE_MASK = 0x3 shl MODE_SHIFT
    private val MODE_ON_WIDTH = 1 shl MODE_SHIFT
    private val MODE_DEVICE_SIZE = 2 shl MODE_SHIFT

    /**
     * 检查 AndroidAutoSize 是否已经初始化
     */
    fun checkInit(): Boolean {
        return AutoSizeConfig.getInstance().getInitDensity() != -1f
    }

    /**
     * 由于 AndroidAutoSize 会通过 {@link InitProvider} 的实例化而自动完成初始化，并且 {@link AutoSizeConfig#init(Application)}
     * 只允许被调用一次，否则会报错，所以 {@link AutoSizeConfig#init(Application)} 的调用权限并没有设为 public，不允许外部使用者调用
     *
     * 可能会在某些特殊情况下出现 {@link InitProvider} 未能正常实例化的情况，导致 AndroidAutoSize 未能完成初始化
     * 所以提供此静态方法用于让外部使用者在异常情况下也可以初始化 AndroidAutoSize,在 {@link Application#onCreate()} 中调用即可
     */
    fun checkAndInit(application: Application) {
        if (!checkInit()) {
            AutoSizeConfig.getInstance()?.setLog(true)?.init(application)?.setUseDeviceSize(false)
        }
    }

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配（AndroidManifest 的 Meta 属性）
     */
    fun autoConvertDensityOfGlobal(activity: Activity) {
        if (AutoSizeConfig.getInstance().isBaseOnWidth()) {
            autoConvertDensityBaseOnWidth(activity, AutoSizeConfig.getInstance().getDesignWidthInDp().toFloat())
        } else {
            autoConvertDensityBaseOnHeight(activity, AutoSizeConfig.getInstance().getDesignHeightInDp().toFloat())
        }
    }

    /**
     * 使用外部三方库的 [Activity] 或 Fragment 的自定义适配参数进行适配
     *
     * @param activity          [Activity]
     * @param externalAdaptInfo 三方库的 [Activity] 或 Fragment 提供的适配参数, 需要配合 [ExternalAdaptManager.addExternalAdaptInfoOfActivity]
     */
    fun autoConvertDensityOfExternalAdaptInfo(
        activity: Activity?,
        externalAdaptInfo: ExternalAdaptInfo
    ) {
        Preconditions.checkNotNull(externalAdaptInfo, "externalAdaptInfo == null",null)
        var sizeInDp = externalAdaptInfo.getSizeInDp()!!

        //如果 ExternalAdaptInfo#getSizeInDp() 返回 0, 则使用在 AndroidManifest 上填写的设计图尺寸
        if (sizeInDp <= 0) {
            sizeInDp = if (externalAdaptInfo.isBaseOnWidth()!!) {
                AutoSizeConfig.getInstance().getDesignWidthInDp().toFloat()
            } else {
                AutoSizeConfig.getInstance().getDesignHeightInDp().toFloat()
            }
        }
        autoConvertDensity(activity!!, sizeInDp, externalAdaptInfo.isBaseOnWidth()!!)
    }


    /**
     * 使用 [Activity] 或 Fragment 的自定义参数进行适配
     *
     * @param activity    [Activity]
     * @param customAdapt [Activity] 或 Fragment 需实现 [CustomAdapt]
     */
    fun autoConvertDensityOfCustomAdapt(activity: Activity?, customAdapt: CustomAdapt) {
        Preconditions.checkNotNull(customAdapt, "customAdapt == null",null)
        var sizeInDp = customAdapt.getSizeInDp()

        //如果 CustomAdapt#getSizeInDp() 返回 0, 则使用在 AndroidManifest 上填写的设计图尺寸
        if (sizeInDp <= 0) {
            sizeInDp = if (customAdapt.isBaseOnWidth()) {
                AutoSizeConfig.getInstance().getDesignWidthInDp().toFloat()
            } else {
                AutoSizeConfig.getInstance().getDesignHeightInDp().toFloat()
            }
        }
        autoConvertDensity(activity!!, sizeInDp, customAdapt.isBaseOnWidth())
    }

    /**
     * 以宽度为基准进行适配
     *
     * @param activity        [Activity]
     * @param designWidthInDp 设计图的总宽度
     */
    fun autoConvertDensityBaseOnWidth(activity: Activity?, designWidthInDp: Float) {
        activity?.let {
            autoConvertDensity(it, designWidthInDp, true)
        }
    }

    /**
     * 以高度为基准进行适配
     *
     * @param activity         [Activity]
     * @param designHeightInDp 设计图的总高度
     */
    fun autoConvertDensityBaseOnHeight(activity: Activity?, designHeightInDp: Float) {
        activity?.let {
            autoConvertDensity(it, designHeightInDp, false)
        }
    }

    /**
     * 这是今日头条适配方案的核心代码，核心在于根据当前设备的实际情况做自动计算并转换 {@link DisplayMetrics#density}、
     * {@link DisplayMetrics#scaledDensity}、{@link DisplayMetrics#densityDpi} 这三个值，额外增加 {@link DisplayMetrics#xdpi}
     * 以支持单位 {@code pt}、{@code in}、{@code mm}
     *
     * @param activity {@link Activity}
     * @param sizeInDp 设计图上的设计尺寸，单位dp，如果 {@param isBaseOnWidth} 设置为 {@code true},
     *                 {@param sizeInDp} 则应该填写设计图的总宽度，如果 {@param isBaseOnWidth} 设置为 {@code false},
     *                 {@param sizeInDp} 则应该填写设计图的总高度
     * @param isBaseOnWidth 是否按照宽度进行等比例适配，{@code true} 为以宽度进行等比例适配，{@code false} 为以高度进行等比例适配
     */
    fun autoConvertDensity(activity: Activity, sizeInDp: Float, isBaseOnWidth: Boolean) {
        Preconditions.checkNotNull(activity, "activity == null", null)
        Preconditions.checkMainThread()

        var subunitsDesignSize = if (isBaseOnWidth) {
            AutoSizeConfig.getInstance().getUnitsManager().getDesignWidth() ?: 0f
        } else {
            AutoSizeConfig.getInstance().getUnitsManager().getDesignHeight() ?: 0f
        }

        subunitsDesignSize = if (subunitsDesignSize > 0f) {
            subunitsDesignSize
        } else {
            sizeInDp
        }

        val screenSize = if (isBaseOnWidth) {
            AutoSizeConfig.getInstance().getScreenWidth()
        } else {
            AutoSizeConfig.getInstance().getScreenHeight()
        }

        var key = Math.round(
            sizeInDp + subunitsDesignSize + screenSize * AutoSizeConfig.getInstance()
                ?.getInitScaledDensity()!!
        ) and (MODE_MASK.inv())
        key = if (isBaseOnWidth) {
            key or MODE_ON_WIDTH
        } else {
            key and MODE_ON_WIDTH.inv()
        }

        key = if (AutoSizeConfig.getInstance().isUseDeviceSize()) {
            key or MODE_DEVICE_SIZE
        } else {
            key and MODE_DEVICE_SIZE.inv()
        }

        val displayMetricsInfo = mCache.get(key)

        var targetDensity: Float = 0f
        var targetDensityDpi: Int = 0
        var targetScaledDensity: Float = 0f
        var targetXdpi: Float = 0f
        var targetScreenWidthDp: Int = 0
        var targetScreenHeightDp: Int = 0

        if (displayMetricsInfo == null) {
            if (isBaseOnWidth) {
                targetDensity =
                    (AutoSizeConfig.getInstance().getScreenWidth().toFloat().times(1f)) / sizeInDp
            } else {
                targetDensity =
                    (AutoSizeConfig.getInstance().getScreenHeight().toFloat().times(1f)) / sizeInDp
            }

            if (AutoSizeConfig.getInstance().getPrivateFontScale() > 0f) {
                targetScaledDensity =
                    AutoSizeConfig.getInstance().getPrivateFontScale().times(targetDensity)
            } else {
                val systemFontScale: Float =
                    if (AutoSizeConfig.getInstance().isExcludeFontScale()) {
                        1f
                    } else {
                        AutoSizeConfig.getInstance().getInitScaledDensity()
                            .times(1f).div(AutoSizeConfig.getInstance().getInitDensity())
                    }
                systemFontScale.let {
                    targetScaledDensity = targetDensity.times(it)
                }
            }
            targetScreenWidthDp =
                AutoSizeConfig.getInstance().getScreenWidth().div(targetDensity).toInt()
            targetScreenHeightDp =
                AutoSizeConfig.getInstance().getScreenHeight().div(targetDensity).toInt()

            targetXdpi = if (isBaseOnWidth) {
                AutoSizeConfig.getInstance().getScreenWidth().times(1f).div(subunitsDesignSize)
            } else {
                AutoSizeConfig.getInstance().getScreenHeight().times(1f).div(subunitsDesignSize)
            }

            mCache.put(
                key,
                DisplayMetricsInfo(
                    targetDensity,
                    targetDensityDpi,
                    targetScaledDensity,
                    targetXdpi,
                    targetScreenWidthDp,
                    targetScreenHeightDp
                )
            )
        } else {
            targetDensity = displayMetricsInfo.getDensity()
            targetDensityDpi = displayMetricsInfo.getDensityDpi()
            targetScaledDensity = displayMetricsInfo.getScaledDensity()
            targetXdpi = displayMetricsInfo.getXdpi()
            targetScreenWidthDp = displayMetricsInfo.getScreenWidthDp()
            targetScreenHeightDp = displayMetricsInfo.getScreenHeightDp()
        }
    }

    /**
     * 取消适配
     */
    fun cancelAdapt(activity: Activity) {
        Preconditions.checkMainThread()
        var initXdpi = AutoSizeConfig.getInstance().getInitXdpi()
        when (AutoSizeConfig.getInstance().getUnitsManager().getSupportSubunits()) {
            Subunits.PT -> {
                initXdpi = initXdpi.div(72f)
            }
            Subunits.MM -> {
                initXdpi = initXdpi.div(25.4f)
            }
            else -> {

            }
        }
        setDensity(
            activity,
            AutoSizeConfig.getInstance().getInitDensity(),
            AutoSizeConfig.getInstance().getInitDensityDpi(),
            AutoSizeConfig.getInstance().getInitScaledDensity(),
            initXdpi
        )
        setScreenSizeDp(
            activity,
            AutoSizeConfig.getInstance().getInitScreenWidthDp(),
            AutoSizeConfig.getInstance().getInitScreenHeightDp()
        )
    }

    /**
     * 赋值
     */
    private fun setDensity(
        activity: Activity,
        density: Float,
        densityDpi: Int,
        scaledDensity: Float,
        xdpi: Float
    ) {
        val activityDisplayMetrics = activity.resources.displayMetrics
        setDensity(activityDisplayMetrics, density, densityDpi, scaledDensity, xdpi)
        val appDisplayMetrics: DisplayMetrics? =
            AutoSizeConfig.getInstance().getApplication()?.resources?.displayMetrics
        setDensity(appDisplayMetrics, density, densityDpi, scaledDensity, xdpi)

        //兼容 MIUI
        val activityDisplayMetricsOnMIUI: DisplayMetrics? = getMetricsOnMiui(activity.resources)
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
        return if (AutoSizeConfig.getInstance().isMiui() && AutoSizeConfig.getInstance()
                .getTmpMetricsField() != null
        ) {
            try {
                (AutoSizeConfig.getInstance().getTmpMetricsField()!![resources] as DisplayMetrics)!!
            } catch (e: Exception) {
                null
            }
        } else null
    }

    private fun setDensity(
        displayMetrics: DisplayMetrics?,
        density: Float,
        densityDpi: Int,
        scaledDensity: Float,
        xdpi: Float
    ) {
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

    private fun setScreenSizeDp(
        activity: Activity,
        screenWidthDp: Int,
        screenHeightDp: Int
    ) {
        if (AutoSizeConfig.getInstance().getUnitsManager()
                .isSupportDP() && AutoSizeConfig.getInstance().getUnitsManager()
                .isSupportScreenSizeDP()
        ) {
            val activityConfiguration: Configuration = activity.resources.configuration
            setScreenSizeDp(activityConfiguration, screenWidthDp, screenHeightDp)
            val appConfiguration: Configuration =
                AutoSizeConfig.getInstance().getApplication()!!.resources.configuration
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
    private fun setScreenSizeDp(
        configuration: Configuration,
        screenWidthDp: Int,
        screenHeightDp: Int
    ) {
        configuration.screenWidthDp = screenWidthDp
        configuration.screenHeightDp = screenHeightDp
    }

    /**
     * 当 App 中出现多进程，并且您需要适配所有的进程，就需要在 App 初始化时调用 [.initCompatMultiProcess]
     * 建议实现自定义 [Application] 并在 [Application.onCreate] 中调用 [.initCompatMultiProcess]
     *
     * @param context [Context]
     */
    fun initCompatMultiProcess(context: Context) {
        context.contentResolver.query(
            Uri.parse("content://" + context.packageName + ".autosize-init-provider"),
            null,
            null,
            null,
            null
        )
    }

}