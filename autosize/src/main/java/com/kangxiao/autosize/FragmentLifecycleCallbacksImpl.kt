package com.kangxiao.autosize

import android.os.Bundle
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager

/**
 * {@link FragmentLifecycleCallbacksImpl} 可用来替换在 BaseFragment 中加入适配代码的传统方式
 * {@link FragmentLifecycleCallbacksImpl} 这种方案类似于 AOP,面向接口，侵入性低，方便统一管理，扩展性强，并且也支持适配三方库的 {@link Fragment}
 */
class FragmentLifecycleCallbacksImpl : FragmentManager.FragmentLifecycleCallbacks {

    private var mAutoAdaptStrategy:AutoAdaptStrategy? = null

    constructor(mAutoAdaptStrategy: AutoAdaptStrategy?) : super() {
        this.mAutoAdaptStrategy = mAutoAdaptStrategy
    }

    fun setAutoAdaptStrategy(autoAdaptStrategy: AutoAdaptStrategy){
        this.mAutoAdaptStrategy = autoAdaptStrategy
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        f.activity?.let { mAutoAdaptStrategy?.applyAdapt(f, it) }
    }
}