package com.kangxiao.autosize

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * {@link ActivityLifecycleCallbacksImpl} 可用来替换在 BaseActivity 中加入适配代码的传统方式
 * {@link ActivityLifecycleCallbacksImpl} 这种方案类似于 AOP，面向接口，侵入性低，方便统一管理，扩展性强，并且也支持适配三方库的 {@link Activity}
 */
class ActivityLifecycleCallbacksImpl : Application.ActivityLifecycleCallbacks {

    private var mAutoAdaptStrategy: AutoAdaptStrategy? = null

    private var mFragmentLifecycleCallbacksImpl: FragmentLifecycleCallbacksImpl? = null
    private var mFragmentLifecycleCallbacksImplToAndroid: FragmentLifecycleCallbacksImplToAndroid? =
        null

    constructor(mAutoAdaptStrategy: AutoAdaptStrategy?) {
        if (AutoSizeConfig.DEPENDENCY_ANDROIDX) {
            mFragmentLifecycleCallbacksImplToAndroid =
                FragmentLifecycleCallbacksImplToAndroid(mAutoAdaptStrategy)
        } else if (AutoSizeConfig.DEPENDENCY_SUPPORT) {
            mFragmentLifecycleCallbacksImpl = FragmentLifecycleCallbacksImpl(mAutoAdaptStrategy)
        }
        this.mAutoAdaptStrategy = mAutoAdaptStrategy
    }

    fun setAutoAdaptStrategy(autoAdaptStrategy: AutoAdaptStrategy) {
        this.mAutoAdaptStrategy = autoAdaptStrategy
        mFragmentLifecycleCallbacksImpl?.setAutoAdaptStrategy(autoAdaptStrategy)
        mFragmentLifecycleCallbacksImplToAndroid?.setAutoAdaptStrategy(autoAdaptStrategy)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (AutoSizeConfig.getInstance().isCustomFragment()){
            if (mFragmentLifecycleCallbacksImplToAndroid != null && activity is androidx.fragment.app.FragmentActivity){
                mFragmentLifecycleCallbacksImplToAndroid?.let {
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(it,true)
                }
            }else if (mFragmentLifecycleCallbacksImpl != null && activity is android.support.v4.app.FragmentActivity){
                mFragmentLifecycleCallbacksImpl?.let {
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(it,true)
                }
            }
        }

        mAutoAdaptStrategy?.applyAdapt(activity,activity)
    }

    override fun onActivityStarted(activity: Activity) {
        mAutoAdaptStrategy?.applyAdapt(activity, activity)
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}