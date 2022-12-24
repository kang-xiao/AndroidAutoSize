package com.kangxiao.autosize

import com.kangxiao.autosize.unit.Subunits
import com.kangxiao.autosize.utils.Preconditions

/**
 * 管理 AndroidAutoSize 支持的所有单位，AndroidAutoSize 支持五种单位(dp、sp、pt、in、mm)
 * 其中 dp、sp 这两个是比较常见的单位，作为 AndroidAutoSize 的主单位，默认被 AndroidAutoSize 支持
 * pt、in、mm 这三个是比较少见的单位，只可以选择其中一个，作为 AndroidAutoSize 的副单位，与dp、sp 一起被 AndroidAutoSize 支持
 * 副单位是用于规避修改 {@link DisplayMetrics#density} 所造成的对于其他使用 dp 布局的系统控件或三方库控件的不良影响
 * 您选择什么单位，就在 layout 文件中用什么单位布局
 *
 * 两个主单位和一个副单位，可以随时使用下面的方法关闭和重新开启对它们的支持
 * 如果您想完全避免修改 {@link DisplayMetrics#density} 所造成的对于其他使用 dp 布局的系统控件或三方库控件的不良影响
 * 那请调用 {@link #setSupportSubunits} 从三个冷门单位中选择一个作为副单位（三个单位的效果都是一样的，按自己的喜好选择）
 * 然后在 layout 文件中使用这个副单位进行布局，这样就可以完全规避修改 {@link DisplayMetrics#density} 所造成的问题
 * 因为 dp、sp 这两个单位在其他系统控件或三方控件中都是非常常见，但三个冷门单位却非常少见
 */
class UnitsManager {

    /**
     * 设计图上的总宽度，建议单位为 px，当使用者想将旧项目从主单位过渡到副单位，或从副单位过渡到主单位时使用
     * 因为在使用主单位时，建议在 AndroidManifest 中填写设计图的 dp 尺寸，比如 360 * 640
     * 而副单位有一个特性是可以直接在 AndroidManifest 中填写设计图的 px 尺寸，比如 1080 * 1920
     * 但在 AndroidManifest 中却只能填写一套设计图尺寸，并且已经填写了主单位的设计图尺寸
     * 所以当项目中同时存在副单位和主单位，并且副单位的设计图尺寸与主单位的设计图尺寸不同时，就需要在 {@link UnitsManager} 中保存副单位的设计图尺寸
     */
    private var mDesignWidth:Float? = null

    /**
     * 设计图上的总高度，建议单位为 px，当使用者想将旧项目从主单位过渡到副单位，或从副单位过渡到主单位时使用
     * 因为在使用主单位时，建议在 AndroidManifest 中填写设计图的 dp 尺寸，比如 360 * 640
     * 而副单位有一个特性是可以直接在 AndroidManifest 中填写设计图的 px 尺寸，比如 1080 * 1920
     * 但在 AndroidManifest 中却只能填写一套设计图尺寸，并且已经填写了主单位的设计图尺寸
     * 所以当项目中同时存在副单位和主单位，并且副单位的设计图尺寸与主单位的设计图尺寸不同时，就需要在 {@link UnitsManager} 中保存副单位的设计图尺寸
     */
    private var mDesignHeight:Float? = null

    /**
     * 是否支持 sp 单位，默认支持
     */
    private var isSupportSP = true

    /**
     * 是否支持 dp 单位，默认支持
     */
    private var isSupportDP = true

    /**
     * 是否支持副单位，以什么为副单位？默认不支持
     */
    private var mSupportSubunits:Subunits = Subunits.NONE

    /**
     * 是否支持 ScreenSizeDp 修改，默认不支持
     */
    private var isSupportScreenSizeDP = false

    /**
     * 设置设计图尺寸
     */
    fun setDesignSize(designWidth:Float,designHeight:Float):UnitsManager{

        return this
    }

    fun getDesignWidth() = mDesignWidth

    /**
     * 设置设计图上的总宽度，建议单位为 px
     */
    fun setDesignWidth(designWidth: Float):UnitsManager{
        Preconditions.checkArgument(designWidth>0,"designWidth must be > 0")
        mDesignWidth = designWidth
        return this
    }

    fun getDesignHeight() = mDesignHeight

    /**
     * 设置设计图上的总高度，建议单位为 px
     */
    fun setDesignHeight(designHeight: Float):UnitsManager{
        Preconditions.checkArgument(designHeight > 0,"designHeight must be > 0")
        mDesignHeight = designHeight
        return this
    }

    /**
     * 是否让 AndroidAutoSize 支持 dp 单位，默认支持，详情请看类文件的注释
     *  @return {@code true} 为支持，{@code false} 为不支持
     */
    fun setSupportDP(supportDP:Boolean):UnitsManager{
        this.isSupportDP = supportDP
        return this
    }

    /**
     * 是否支持 dp 单位，默认支持，详情请看类文件的注释
     *
     * @return {@code true} 为支持，{@code false} 为不支持
     */
    fun isSupportDP() = isSupportDP

    /**
     * 是否支持 sp 单位，默认支持
     * @return {@code true} 为支持，{@code false} 为不支持
     */
    fun isSupportSP() = isSupportSP

    /**
     * 是否让 AndroidAutoSize 支持 sp 单位，默认支持，详情请看类文件的注释
     * @return {@code true} 为支持，{@code false} 为不支持
     */
    fun setSupportSP(supportSP:Boolean):UnitsManager{
        isSupportSP = supportSP
        return this
    }

    /**
     * AndroidAutoSize 以什么单位为副单位，默认为 {@link Subunits#NONE}，即不支持副单位
     */
    fun getSupportSubunits():Subunits = mSupportSubunits

    /**
     * 是否支持 ScreenSizeDp 修改，默认不支持
     */
    fun isSupportScreenSizeDP() = isSupportScreenSizeDP

    /**
     * 是否让 AndroidAutoSize 支持 ScreenSizeDp 修改，默认不支持
     *
     * @param supportScreenSizeDP {@code true} 为支持，{@code false} 为不支持
     */
    fun setSupportScreenSizeDp(supportScreenSizeDP:Boolean):UnitsManager{
        isSupportScreenSizeDP = supportScreenSizeDP
        return this
    }

    /**
     * 让 AndroidAutoSize 以什么单位为副单位，在 pt、in、mm 这三个冷门单位中选择一个即可，三个效果都是一样的
     * 按自己的喜好选择
     * 默认为 {@link Subunits#NONE}，即不支持副单位
     */
    fun setSupportSubunits(supportSubunits:Subunits):UnitsManager{
        mSupportSubunits = Preconditions.checkNotNull(supportSubunits,"The supportSubunits can not be null,use Subunits.NONE instead.",null)
        return this
    }
}