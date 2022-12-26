package com.kangxiao.autosize

import android.app.Activity

/**
 * {@link AutoAdaptStrategy}的装饰类，用于给 {@link AutoAdaptStrategy} 的实现类增加一些额外的职责
 */
class WrapperAutoAdaptStrategy(autoAdaptStrategy: AutoAdaptStrategy) : AutoAdaptStrategy {

    private var mAutoAdaptStrategy:AutoAdaptStrategy? = autoAdaptStrategy

    override fun applyAdapt(target: Any, activity: Activity) {
        val onAdaptListener = AutoSizeConfig.getInstance().getOnAdaptListener()
        onAdaptListener?.onAdaptBefore(target,activity)
        mAutoAdaptStrategy?.applyAdapt(target,activity)
        onAdaptListener?.onAdaptAfter(target, activity)
    }
}