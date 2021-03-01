@file:Suppress("MemberVisibilityCanBePrivate")

package com.yujing.base

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.yujing.base.activity.YActivity
import com.yujing.bus.YBus
import com.yujing.bus.YBusUtil
import com.yujing.bus.YMessage
import com.yujing.utils.YShow
import com.yujing.utils.YToast

/**
 * 基础activity
 *
 * @param <B> ViewDataBinding
 * @author yujing 2021年1月13日10:13:26
 */
/*
用法：
//kotlin
class AboutActivity : YBaseActivity<ActivityAboutBinding>(R.layout.activity_about) {
    override fun init() {
        binding.include.ivBack.setOnClickListener { finish() }
        binding.include.tvTitle.text = "关于我们"
    }
}
//java
public class OldActivity extends YBaseActivity<Activity1101Binding> {
    public OldActivity() {
        super(R.layout.activity_1101);
    }
    @Override
    protected void init() { }
}

RxBus用法
RxBus.getDefault().post(YMessage<Any?>(key,value))

YBus用法
//发送消息
YBusUtil.post("tag1","123456789")
//接收消息
@YBus("tag1")
fun message(message: Any) {
    YLog.i("收到：$message")
}
 */
abstract class YBaseActivity<B : ViewDataBinding>(var layout: Int?) : YActivity() {
    //open val binding: B by lazy { DataBindingUtil.setContentView(this, layout) }
    lateinit var binding: B
    var isActive = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (layout != null)//如果layout==null，请在initBefore里面给binding赋值
            binding = DataBindingUtil.setContentView(this, layout!!)
        initBefore()//初始化之前执行，这儿可以请求权限：YPermissions.requestAll(this)
        init()
        initAfter()
        YBusUtil.init(this)
    }

    /**
     * YBus总线消息
     *
     * @param yMessage 总线内容
     */
    @YBus()
    open fun onEvent(yMessage: YMessage<Any>) {
    }

    /**
     * 初始化数据
     */
    protected abstract fun init()
    open fun initBefore() {}
    open fun initAfter() {}

    /**
     * 跳转
     */
    open fun startActivity(classActivity: Class<*>?) {
        isActive = false
        val intent = Intent(this, classActivity!!)
        startActivity(intent)
    }

    /**
     * 跳转
     */
    open fun startActivity(classActivity: Class<*>?, resultCode: Int) {
        isActive = false
        startActivityForResult(classActivity, resultCode)
    }

    /**
     * 跳转
     */
    open fun startActivityForResult(classActivity: Class<*>?, resultCode: Int) {
        isActive = false
        val intent = Intent(this, classActivity!!)
        startActivityForResult(intent, resultCode)
    }

    /**
     * 显示toast
     */
    open fun show(str: String?) {
        if (str == null) return
        YToast.showLong(applicationContext, str)
    }

    override fun onStop() {
        isActive = false
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun finish() {
        super.finish()
        YShow.finish()
    }

    override fun onDestroy() {
        System.gc() // 系统自动回收
        YBusUtil.onDestroy(this)
        super.onDestroy()
    }
}