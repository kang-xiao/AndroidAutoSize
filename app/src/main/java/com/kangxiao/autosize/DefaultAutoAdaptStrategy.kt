package com.kangxiao.autosize

import android.app.Activity
import com.kangxiao.autosize.AutoSize.autoConvertDensityOfGlobal
import com.kangxiao.autosize.utils.AutoSizeLog.d
import com.kangxiao.autosize.external.ExternalAdaptInfo
import com.kangxiao.autosize.internal.CancelAdapt

import java.util.Locale

import com.kangxiao.autosize.internal.CustomAdapt
import com.kangxiao.autosize.utils.AutoSizeLog

/**
 * 屏幕适配逻辑策略默认实现类，可通过 {@link AutoSizeConfig#init(Application,boolean,AutoAdaptStrategy)}
 * 和 {@link AutoSizeConfig#setAutoAdaptStrategy(AutoAdaptStrategy)} 切换策略
 */
class DefaultAutoAdaptStrategy : AutoAdaptStrategy {

    /**
     * 检查是否开启了外部三方库的适配模式，只要不主动调用 ExternalAdaptManager 的方法，下面的代码就不会执行
     */
    override fun applyAdapt(target: Any, activity: Activity) {
        if (AutoSizeConfig.getInstance().getExternalAdaptManager().isRun()){
            AutoSizeLog.w(String.format(Locale.ENGLISH,"%s canceled the adaptation!",target::class.java.name))

            if (AutoSizeConfig.getInstance().getExternalAdaptManager().isCancelAdapt(target::javaClass.get())) {
                AutoSizeLog.w(String.format(Locale.ENGLISH, "%s canceled the adaptation!", target::class.java.name));
                AutoSize.cancelAdapt(activity);
                return
            }else{
                val info = AutoSizeConfig.getInstance().getExternalAdaptManager().getExternalAdaptInfoOfActivity(target::javaClass.get())
                if (info != null){
                    d(java.lang.String.format(Locale.ENGLISH, "%s used %s for adaptation!", target::class.java.name, ExternalAdaptInfo::class.java.name))
                    AutoSize.autoConvertDensityOfExternalAdaptInfo(activity, info)
                    return
                }
            }
        }

        //如果 target 实现 CancelAdapt 接口表示放弃适配，所有的适配效果都将失效
        if (target is CancelAdapt){
            AutoSizeLog.w(String.format(Locale.ENGLISH, "%s canceled the adaptation!", target::class.java.name));
            AutoSize.cancelAdapt(activity);
            return
        }

        //如果 target 实现 CustomAdapt 接口表示该 target 想自定义一些用于适配的参数, 从而改变最终的适配效果
        if (target is CustomAdapt) {
            d(java.lang.String.format(Locale.ENGLISH, "%s implemented by %s!", target::class.java.name, CustomAdapt::class.java.name))
            AutoSize.autoConvertDensityOfCustomAdapt(activity, target)
        } else {
            d(java.lang.String.format(Locale.ENGLISH, "%s used the global configuration.", target::class.java.name))
            autoConvertDensityOfGlobal(activity)
        }
    }
}