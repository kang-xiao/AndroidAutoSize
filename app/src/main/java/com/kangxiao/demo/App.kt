package com.kangxiao.demo

import android.app.Activity
import android.app.Application
import com.kangxiao.autosize.AutoSize
import com.kangxiao.autosize.AutoSizeConfig
import com.kangxiao.autosize.external.ExternalAdaptInfo
import com.kangxiao.autosize.onAdaptListener

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        //当 App 中出现多进程，并且您需要适配所有的进程，就需要在 App 初始化时调用 initCompatMultiProcess()
        AutoSize.initCompatMultiProcess(this)

        //如果在某些特殊情况下出现 InitProvider 未能正常实例化，导致 AndroidAutoSize 未能完成初始化
        //可以主动调用 AutoSize.checkAndInit(this) 方法，完成 AndroidAutoSize 的初始化后即可正常使用
//        AutoSize.checkAndInit(this)

        //以下是 AndroidAutoSize 可以自定义的参数，{@link AutoSizeConfig} 的每个方法的注释写的很详细，使用前跳进源码看下
        AutoSizeConfig.getInstance()
                //是否让框架支持自定义 Fragment 的适配参数，这个需求比较少见，所以需要使用者手动开启
                //如果没有这个需求建议不开启
                .setCustomFragment(true)

                //是否屏蔽系统字体大小对 AndroidAutoSize 的影响，默认为false,则会跟随系统设置中字体大小的改变
//                .setExcludeFontScale(true)

                //区别于系统字体大小的放大比例，AndroidAutoSize 允许 APP 内部可以独立于系统字体大小之外，独立拥有全局调节 APP 字体大小的能力
                //当然，在 APP 内您必须使用 sp 来作为字体的单位，否则此功能无效，不设置或将此值设为0 则取消此功能
//                .setPrivateFontScale(0.8f)

                //屏幕适配监听器
                .setOnAdaptListener(object : onAdaptListener{

                    override fun onAdaptBefore(target: Any, activity: Activity) {
                        //使用以下代码，可以解决横竖屏切换时的屏幕适配问题
                        //使用以下代码，可以支持 Android 的分屏或缩放模式，当前提是在分屏或缩放模式下当用户改变您 APP 的窗口大小时
                        //系统会重新绘制当前页面，某些情况下系统不会主动重新绘制当前页面，所以这时您需要自行重新绘制当前页面
//                        val screenSize = ScreenUtils.getScreenSize(activity) // 参数是Activity,不要传Application
//                        AutoSizeConfig.getInstance().setScreenWidth(screenSize[0])
//                        AutoSizeConfig.getInstance().setScreenWidth(screenSize[1])
                    }

                    override fun onAdaptAfter(target: Any, activity: Activity) {
                    }
                })
                //是否打印日志
                .setLog(true)

                //是否使用设备的实际尺寸做适配，默认为 false，如果设置为false，在以屏幕高度为基准进行适配时
                //AutoSize 会将屏幕总高度减去状态栏高度来做适配
                //设置为 true 则使用设备的实际屏幕高度，不会减去状态栏高度
                //在全面屏或刘海屏设备中，获取到屏幕高度可能不包含状态栏高度，所以在全面屏设备中不需要减去状态栏高度，所以可以 setUseDeviceSize(true)
//            .setUseDeviceSize(true)

                //是否全局按照宽度进行等比例适配，默认为 true
//            .setBaseOnWidth(false)
                //设置屏幕适配逻辑策略类，一般不用设置，使用框架默认的就好,默认框架：DefaultAutoAdaptStrategy()
//                .setAutoAdaptStrategy(DefaultAutoAdaptStrategy())

        customAdaptForExternal()
    }

    /**
     * 给外部的三方库 {@link Activity} 自定义适配参数，因为三方库的 {@link Activity} 并不能通过实现
     * {@link CustomAdapt} 接口的方式来提供自定义适配参数（远程依赖无法修改源码）
     * 所以使用 {@link ExternalAdaptManager} 来替代实现接口的方式，来提供自定义适配参数
     */
    private fun customAdaptForExternal(){
        /**
         * {@link ExternalAdaptManager} 是管理外部三方库的适配信息和状态的管理类
         */
        AutoSizeConfig.getInstance().getExternalAdaptManager()
            //加入的 Activity 将会放弃屏幕适配
            .addCancelAdaptOfActivity(CustomAdaptActivity::class.java)
            //为指定的 Activity 提供自定义适配参数，AndroidAutoSize 将会按照提供的适配参数进行适配
            .addExternalAdaptInfoOfActivity(MainActivity::class.java, ExternalAdaptInfo(true,400f))
    }

}