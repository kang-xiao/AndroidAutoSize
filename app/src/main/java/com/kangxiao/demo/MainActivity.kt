package com.kangxiao.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kangxiao.autosize.AutoSizeConfig

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * 需要注意的是暂停 AutoSize 后，只是停止了对后续还没有启动的 {@link Activity} 进行适配工作
     * 但对已经启动且已经适配的 {@link Activity} 不会有任何影响
     */
    fun stop(view:View){
        Toast.makeText(applicationContext,"AutoSize stops working!",Toast.LENGTH_SHORT).show()
        AutoSizeConfig.getInstance().stop(this)
    }

    /**
     * 需要注意的是重新启动 AutoSize 后，只是重新开始了对后续还没有启动的 {@link Activity} 进行适配工作
     * 但对已经启动且在 stop 期间未适配的 {@link Activity} 不会有任何影响
     */
    fun restart(view:View){
        Toast.makeText(applicationContext,"AutoSize continues to work!",Toast.LENGTH_SHORT).show()
        AutoSizeConfig.getInstance().restart()
    }

    /**
     * 跳转到 {@link CustomAdaptActivity},展示项目内部的 {@link Activity} 自定义适配参数的用法
     */
    fun goCustomAdaptActivity(view: View){
        startActivity(Intent(applicationContext,CustomAdaptActivity::class.java))
    }
}