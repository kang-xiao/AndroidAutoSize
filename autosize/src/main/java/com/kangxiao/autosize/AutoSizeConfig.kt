package com.kangxiao.autosize

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import com.kangxiao.autosize.external.ExternalAdaptManager
import com.kangxiao.autosize.unit.Subunits
import com.kangxiao.autosize.utils.AutoSizeLog
import com.kangxiao.autosize.utils.Preconditions
import com.kangxiao.autosize.utils.ScreenUtils
import java.lang.Exception
import java.lang.reflect.Field
import kotlin.concurrent.thread

class AutoSizeConfig {

    companion object {

        const val KEY_DESIGN_WIDTH_IN_DP = "design_width_in_dp"
        const val KEY_DESIGN_HEIGHT_IN_DP = "design_height_in_dp"

        private fun findClassByClassName(className: String): Boolean {
            return try {
                Class.forName(className)
                true
            } catch (e: Exception) {
                false
            }
        }

        val DEPENDENCY_ANDROIDX = findClassByClassName("androidx.fragment.app.FragmentActivity")
        val DEPENDENCY_SUPPORT = findClassByClassName("android.support.v4.app.FragmentActivity")

        @Volatile
        private var sInstance: AutoSizeConfig? = null

        fun getInstance(): AutoSizeConfig {
            if (sInstance == null) {
                synchronized(AutoSizeConfig::class.java) {
                    if (sInstance == null) {
                        sInstance = AutoSizeConfig()
                    }
                }
            }
            return sInstance!!
        }
    }

    private var mApplication: Application? = null

    /**
     * 管理外部三方库 {@link Activity} 的适配
     */
    private var mExternalAdaptManager: ExternalAdaptManager = ExternalAdaptManager()

    /**
     * 管理 AndroidAutoSize 支持的所有单位，AndroidAutoSize 支持五种单位 （dp、sp、pt、in、mm）
     */
    private var mUnitsManager: UnitsManager = UnitsManager()

    /**
     * 最初的 {@link DisplayMetrics#density}
     */
    private var mInitDensity: Float = -1f

    /**
     * 最初的 {@link DisplayMetrics#densityDpi}
     */
    private var mInitDensityDpi: Int = 0

    /**
     * 最初的 {@link DisplayMetrics#scaledDensity}
     */
    private var mInitScaledDensity: Float = 0f

    /**
     * 最初的 {@link DisplayMetrics#xdpi}
     */
    private var mInitXdpi: Float = 0f

    /**
     * 最初的 {@link Configuration#screenHeightDp}
     */
    private var mInitScreenHeightDp: Int = 0

    /**
     * 最初的 {@link Configuration#screenWidthDp}
     */
    private var mInitScreenWidthDp: Int = 0

    /**
     * 设计图上的总宽度，单位dp
     */
    private var mDesignWidthInDp: Int = 0

    /**
     * 设计图上的总高度，单位dp
     */
    private var mDesignHeightInDp: Int = 0

    /**
     * 设备的屏幕总宽度，单位px
     */
    private var mScreenWidth: Int = 0

    /**
     * 设备的屏幕总高度，单位px
     */
    private var mScreenHeight: Int = 0

    /**
     * 状态栏高度，当 {@link #isUseDeviceSize} 为 {@code false} 时，AndroidAutoSize 会将 {@link #mScreenHeight} 减去状态栏高度
     * AndroidAutoSize 默认使用 {@link ScreenUtils#getStatusBarHeight()} 方法获取状态栏高度
     * AndroidAutoSize 使用者可使用 {@link #setStatusBarHeight(int)} 自行设置状态栏高度
     */
    private var mStatusBarHeight: Int = 0

    /**
     * 为了保证在不同宽高比的屏幕上显示效果也能完全一致，所以本方案适配时是以设计图宽度与设备实际宽度的比例或设计图高度与设备实际高度的比例应用到
     * 每个 View 上 (只能在宽度和高度之中选一个作为基准)，从而使每个 View 的高和宽用同样的比例缩放，避免在与设计图高宽比不一致的设备上出现适配的 View 高或宽变形的问题
     * {@link #isBaseOnWidth} 为 {@code true} 时代表以宽度等比例缩放， {@code false} 代表以高度等比例缩放
     * {@link #isBaseOnWidth} 为全局配置，默认为 {@code true}，每个 {@link Activity} 也可以单独选择使用高或者宽做等比例缩放
     */
    private var isBaseOnWidth: Boolean = true

    /**
     * 此字段表示是否使用设备的实际尺寸做适配
     * {@link #isUseDeviceSize} 为 {@code true} 表示屏幕高度 {@link #mScreenHeight} 包含状态栏的高度
     * {@link #isUseDeviceSize} 为 {@code false} 表示 {@link mScreenHeight} 会减去状态栏的高度，默认为 {@code true}
     */
    private var isUseDeviceSize: Boolean = true

    /**
     * 用来替换在 BaseActivity 中加入适配代码的传统方式
     * 这种方案类似于 AOP,面向接口，侵入性低，方便统一管理，扩展性强，并且支持适配三方库
     */
    private var mActivityLifecycleCallbacksImpl:ActivityLifecycleCallbacksImpl? = null

    /**
     * 框架具有 热插拔 特性，支持在项目运行中动态停止和重新启动适配功能
     */
    private var isStop = false

    /**
     * 是否让框架支持自定义 Fragment 的适配参数，由于这个需求是比较少见的，所以需要使用者手动开启
     */
    private var isCustomFragment:Boolean = false

    /**
     * 屏幕方向，{@code true} 为纵向，{@code false} 为横向
     */
    private var isVertical = true

    /**
     * 是否屏蔽系统字体大小，对 AndroidAutoSize 的影响，如果为 {@code true}，App 内的字体大小将不会跟随系统设置中字体大小的改变
     * 如果为 {@code false}，则会跟谁系统字体大小的改变，默认为 {@code false}
     */
    private var isExcludeFontScale:Boolean = false

    /**
     * 区别于系统字体大小的放大比例，AndroidAutoSize 允许 APP 内部可以独立于系统字体大小之外，独自拥有全局调节 APP 字体大小的能力
     * 当然，在 APP 内您必须使用 sp 来作为字体的单位，否则此功能无效，将此值设为 0 则取消此功能
     */
    private var privateFontSize:Float = 0f

    /**
     * 是否是 Miui 系统
     */
    private var isMiui:Boolean = false

    /**
     * Miui 系统中的 mTmpMetrics 字段
     */
    private var mTmpMetricsField:Field? = null

    /**
     * 屏幕适配监听器，用来监听屏幕适配时的一些事件
     */
    private var mOnAdaptListener: onAdaptListener? = null

    fun init(application: Application):AutoSizeConfig{
        return init(application,true)
    }

    fun init(application: Application,isBaseOnWidth: Boolean):AutoSizeConfig{
        return init(application,isBaseOnWidth,null)
    }

    /**
     * 框架会在 APP 启动时自动调用此方法进行初始化，使用者无需手动初始化，初始化方法只需调用一次，否则报错
     */
    @SuppressLint("RestrictedApi")
    fun init(application: Application, isBaseOnWidth:Boolean, strategy: AutoAdaptStrategy?):AutoSizeConfig{

        Preconditions.checkArgument(mInitDensity == -1f,"AutoSizeConfig#init() can only be called once")
        Preconditions.checkNotNull(application,"application == null",null)
        this.mApplication = application
        this.isBaseOnWidth = isBaseOnWidth
        val displayMetrics = Resources.getSystem().displayMetrics
        val configuration = Resources.getSystem().configuration

        //设置一个默认值，避免在低配设备上因为获取 MetaData 过慢，导致适配时未能正常获取到设计图尺寸
        //建议使用者在低配设备上主动在 Application#onCreate 中调用 setDesignWidthInDp 代替已使用 AndroidManifest 配置设计图尺寸的方式
        if (AutoSizeConfig.getInstance().getUnitsManager().getSupportSubunits() == Subunits.NONE){
            mDesignHeightInDp = 640
            mDesignWidthInDp = 360
        }else{
            mDesignHeightInDp = 1920
            mDesignWidthInDp = 1080
        }
        mApplication?.let {
            getMetaData(it)
        }

        isVertical = application.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        val screenSize = ScreenUtils.getRawScreenSize(application)
        mScreenWidth = screenSize[0]
        mScreenHeight = screenSize[1]

        mStatusBarHeight = ScreenUtils.getStatusBarHeight()

        mInitDensity = displayMetrics.density
        mInitDensityDpi = displayMetrics.densityDpi
        mInitScaledDensity = displayMetrics.scaledDensity
        mInitXdpi = displayMetrics.xdpi
        mInitScreenWidthDp = configuration.screenWidthDp
        mInitScreenHeightDp = configuration.screenHeightDp

        application.registerComponentCallbacks(object : ComponentCallbacks{
            override fun onConfigurationChanged(newConfig: Configuration) {
                if (newConfig.fontScale > 0){
                    mInitScaledDensity = Resources.getSystem().displayMetrics.scaledDensity
                }
                isVertical = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                val configScreenSize = ScreenUtils.getScreenSize(application)
                mScreenWidth = configScreenSize[0]
                mScreenHeight = configScreenSize[1]
            }

            override fun onLowMemory() {
            }
        })

        mActivityLifecycleCallbacksImpl = ActivityLifecycleCallbacksImpl(WrapperAutoAdaptStrategy(strategy?:DefaultAutoAdaptStrategy()))
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacksImpl)

        if ("MiuiResources" == application.resources.javaClass.simpleName || "XResources" == application.resources.javaClass.simpleName){
            isMiui = true
            try {
                mTmpMetricsField = Resources::class.java.getDeclaredField("mTmpMetrics")
                mTmpMetricsField?.isAccessible = true
            }catch (e:Exception){
                mTmpMetricsField = null
            }
        }

        return this
    }

    /**
     * 重新开始框架的运行
     */
    fun restart(){
        Preconditions.checkNotNull(mActivityLifecycleCallbacksImpl,"Please call the AutoSizeConfig#init() first",null)
        synchronized(AutoSizeConfig::class.java){
            if (isStop){
                mApplication?.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacksImpl)
                isStop = false
            }
        }
    }

    /**
     * 停止框架的运行
     */
    fun stop(activity: Activity){
        Preconditions.checkNotNull(mActivityLifecycleCallbacksImpl,"Please call the AutoSizeConfig#init() first",null)
        synchronized(AutoSizeConfig::class.java){
            if (!isStop){
                mApplication?.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacksImpl)
                AutoSize.cancelAdapt(activity)
                isStop = true
            }
        }
    }

    /**
     * 设置屏幕适配逻辑策略类
     */
    fun setAutoAdaptStrategy(autoAdaptStrategy: AutoAdaptStrategy):AutoSizeConfig{
        Preconditions.checkNotNull(mActivityLifecycleCallbacksImpl,"Please call the AutoSizeConfig#init() first",null)
        mActivityLifecycleCallbacksImpl?.setAutoAdaptStrategy(WrapperAutoAdaptStrategy(autoAdaptStrategy))
        return this
    }

    /**
     * 设置屏幕适配监听器
     */
    fun setOnAdaptListener(onAdaptListener: onAdaptListener): AutoSizeConfig {
        this.mOnAdaptListener = onAdaptListener
        return this
    }

    /**
     * 是否全局按照宽度进行等比例适配
     */
    fun setBaseOnWidth(baseOnWidth:Boolean):AutoSizeConfig{
        isBaseOnWidth = baseOnWidth
        return this
    }

    /**
     * 是否使用设备的实际尺寸做适配
     */
    fun setUseDeviceSize(useDeviceSize:Boolean):AutoSizeConfig{
        isUseDeviceSize = useDeviceSize
        return this
    }

    /**
     * 是否打印Log
     */
    fun setLog(log:Boolean):AutoSizeConfig{
        AutoSizeLog.setDebug(log)
        return this
    }

    /**
     * 是否让框架支持自定义 Fragment 的适配参数，由于这个需求是比较少见的，所以需要使用者手动开启
     */
    fun setCustomFragment(customFragment:Boolean):AutoSizeConfig{
        isCustomFragment = customFragment
        return this
    }

    /**
     * 框架是否已经开启支持自定义 Fragment 的适配参数
     */
    fun isCustomFragment() = isCustomFragment

    /**
     * 框架是否已经停止运行
     */
    fun isStop() = isStop

    /**
     * 管理外部三方库 {@link Activity} 的适配
     */
    fun getExternalAdaptManager():ExternalAdaptManager = mExternalAdaptManager

    fun getOnAdaptListener() = mOnAdaptListener

    /**
     * 管理 AndroidAutoSize 支持的所有单位， AndroidAutoSize 支持五种单位(dp、sp、pt、in、mm)
     */
    fun getUnitsManager() = mUnitsManager

    fun isBaseOnWidth() = isBaseOnWidth

    fun isUseDeviceSize() = isUseDeviceSize

    fun getScreenWidth() = mScreenWidth

    fun getScreenHeight():Int{
       return if (isUseDeviceSize()){
            mScreenHeight
        }else{
            mScreenHeight - mStatusBarHeight
        }
    }

    fun getDesignWidthInDp():Int{
        Preconditions.checkArgument(mDesignWidthInDp>0,"you must set "+ KEY_DESIGN_WIDTH_IN_DP + " in your AndroidManifest file")
        return mDesignWidthInDp
    }

    fun getDesignHeightInDp():Int{
        Preconditions.checkArgument(mDesignHeightInDp>0,"you must set "+ KEY_DESIGN_HEIGHT_IN_DP + " in your AndroidManifest file")
        return mDesignHeightInDp
    }

    fun getInitDensity() = mInitDensity

    fun getInitDensityDpi() = mInitDensityDpi

    fun getInitScaledDensity() = mInitScaledDensity

    fun getInitXdpi() = mInitXdpi

    fun getInitScreenWidthDp() = mInitScreenWidthDp

    fun getInitScreenHeightDp() = mInitScreenHeightDp

    fun isVertical() = isVertical

    fun isMiui() = isMiui

    fun getTmpMetricsField() = mTmpMetricsField

    /**
     * 设置屏幕方向
     */
    fun setVertical(vertical:Boolean):AutoSizeConfig{
        isVertical = vertical
        return this
    }

    /**
     * 是否屏蔽系统字体大小 对 AndroidAutoSize 的影响，如果为 {@code true}，App 内的字体大小将不会跟随系统设置中字体大小的改变
     * 如果为 {@code false}，则会跟随系统设置中字体大小的改变，默认为{@code false}
     */
    fun setExcludeFontScale(excludeFontScale:Boolean):AutoSizeConfig{
        isExcludeFontScale = excludeFontScale
        return this
    }

    fun isExcludeFontScale() = isExcludeFontScale

    /**
     * 区别于系统大小的放大比例，AndroidAutoSize 允许 APP 内部可以独立于系统字体大小之外，独自拥有全局调节 APP 字体大小的能力
     * 当然，在 APP 内您必须使用 sp 来作为字体的单位，否则此功能无效
     *
     * @param fontSize 字体大小放大的比例，设为 0 则取消此功能
     */
    fun setPrivateFontScale(fontSize:Float):AutoSizeConfig{
        privateFontSize = fontSize
        return this
    }

    fun getPrivateFontScale() = privateFontSize

    /**
     * 设置屏幕宽度
     */
    fun setScreenWidth(screenWidth:Int):AutoSizeConfig{
        Preconditions.checkArgument(screenWidth>0,"screenWidth must be > 0")
        mScreenWidth = screenWidth
        return this
    }

    /**
     * 设置屏幕高度
     */
    fun setScreenHeight(screenHeight:Int):AutoSizeConfig{
        Preconditions.checkArgument(screenHeight>0,"screenHeight must be > 0")
        mScreenHeight = screenHeight
        return this
    }

    /**
     * 设置全局设计图宽度
     */
    fun setDesignWidthInDp(designWidthInDp:Int):AutoSizeConfig{
        Preconditions.checkArgument(designWidthInDp>0,"designWidthInDp must be > 0")
        mDesignWidthInDp = designWidthInDp
        return this
    }

    /**
     * 设置全局设计图高度
     */
    fun setDesignHeightInDp(designHeightInDp:Int):AutoSizeConfig{
        Preconditions.checkArgument(designHeightInDp>0,"designHeightInDp must be > 0")
        mDesignHeightInDp = designHeightInDp
        return this
    }

    /**
     * 设置状态栏高度
     */
    fun setStatusBarHeight(statusBarHeight:Int):AutoSizeConfig{
        Preconditions.checkArgument(statusBarHeight>0,"statusBarHeight must be > 0")
        mStatusBarHeight = statusBarHeight
        return this
    }

    fun getApplication() = mApplication

    /**
     * 获取使用者在 AndroidManifest 中填写的 Meta 信息
     */
    private fun getMetaData(context: Context){
        thread {
            val packageManager = context.packageManager
            var applicationInfo:ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(context.packageName,PackageManager.GET_META_DATA)
                applicationInfo.apply {
                    if (this.metaData!=null){
                        if (this.metaData.containsKey(KEY_DESIGN_WIDTH_IN_DP)){
                            mDesignWidthInDp = this.metaData.get(KEY_DESIGN_WIDTH_IN_DP) as Int
                        }
                        if (this.metaData.containsKey(KEY_DESIGN_HEIGHT_IN_DP)){
                            mDesignHeightInDp = this.metaData.get(KEY_DESIGN_HEIGHT_IN_DP) as Int
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }.start()
    }
}