package com.yujing.test

import android.app.Application
import com.yujing.utils.*
import com.yujing.ycrash.YCrash


class App : Application() {
    //标准单列
//    companion object {
//        val instance: App by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {App()}
//    }
    //单列
    companion object {
        private var instance: App? = null
            get() {
                if (field == null) field = App()
                return field
            }

        @Synchronized
        fun get(): App {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        YUtils.init(this)
        YCrash.getInstance().init(this)
        YCrash.getInstance().appName = "AppName"
        registerActivityLifecycleCallbacks(YActivityUtil.getActivityLifecycleCallbacks())
        //保存日志开
        YLog.saveOpen(YPath.getFilePath(this, "log"))
        YLog.setLogSaveListener { type, tag, msg -> return@setLogSaveListener type != YLog.DEBUG }
        //保存最近30天日志
        YLog.delDaysAgo(30)
    }
}