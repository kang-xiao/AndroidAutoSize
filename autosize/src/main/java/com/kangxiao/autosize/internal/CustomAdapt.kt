package com.kangxiao.autosize.internal

/**
 * 如果某些页面不想使用 AutoSize 初始化时设置的默认适配参数，请让该页面实现此接口
 * 实现此接口即可自定义用于适配的一些参数，从而影响最终的适配效果
 */
interface CustomAdapt {

    /**
     * 是否按照宽度进行等比例适配（为了保证在高宽比不同的屏幕上也能正常适配，所以只能在宽度和高度之中选一个作为基准进行适配）
     *
     * @return {@code true} 为按照宽度适配，{@code false} 为按照高度适配
     */
    fun isBaseOnWidth(): Boolean

    /**
     * 返回设计图上的设计尺寸，单位 dp
     *
     * {@link #getSizeInDp}须配合 {@link #inBaseOnWidth()} 使用，规则如下：
     * 如果 {@link #isBaseOnWidth()} 返回 {@code true}, {@link #getSizeInDp} 则应该返回设计图的总宽度
     * 如果 {@link #isBaseOnWidth()} 返回 {@code false}, {@link #getSizeInDp} 则应该返回设计图的总高度
     * 如果您不需要自定义设计图上的设计尺寸，想继续使用在 AndroidManifest 中填写的设计图尺寸，{@link #getSizeInDp} 则返回 {@code 0}
     *
     * @return 设计图上的设计尺寸，单位dp
     */
    fun getSizeInDp(): Float

}