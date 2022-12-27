package com.kangxiao.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kangxiao.autosize.internal.CustomAdapt

class CustomAdaptActivity : AppCompatActivity(),CustomAdapt {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_adapt)
    }

    override fun isBaseOnWidth(): Boolean = false

    /**
     * 这里使用 iPhone 的设计图, iPhone 的设计图尺寸为 750px * 1334px, 高换算成 dp 为 667 (1334px / 2 = 667dp)
     */
    override fun getSizeInDp(): Float = 667f

}