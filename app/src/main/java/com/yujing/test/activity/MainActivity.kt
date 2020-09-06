package com.yujing.test.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.yujing.test.R
import com.yujing.test.base.BaseActivity
import com.yujing.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : BaseActivity() {
    private val yPicture: YPicture = YPicture()
    private var bitmap: Bitmap? = null
    private var uri: Uri? = null
    private var yInstallApk: YInstallApk? = null
    override val layoutId: Int
        get() = R.layout.activity_main

    override fun init() {
        //var a=findViewById<Button>(R.id.button1)
        button1.text = "拍照"
        button1.setOnClickListener { yPicture.gotoCamera(this) }
        button2.text = "相册"
        button2.setOnClickListener { yPicture.gotoAlbum(this) }
        button3.text = "剪切"
        button3.setOnClickListener { uri?.let { yPicture.gotoCrop(this, uri, 400, 400) } }
        button4.text = "Date测试"
        button4.setOnClickListener { openDate() }
        button5.text = "通知栏下载"
        button5.setOnClickListener { download() }
        button6.text = "App更新"
        button6.setOnClickListener { update() }
        button7.text = "测试"

        val yQueue=YQueue()

        button7.setOnClickListener {
            yQueue.run(1000) { text4.text ="你好" }
        }

        button8.text = "测试"
        button8.setOnClickListener {

        }

        yPicture.setPictureFromCameraListener { uri, file, Flag ->
            val bm = YConvert.uri2Bitmap(this, uri)
            this.bitmap = bitmap
            this.uri = uri
            YImageDialog.show(this, bitmap)
            YToast.show(this, "file:" + file.exists())
        }

        yPicture.setPictureFromCropListener { uri, file, Flag ->
            val bitmap = YConvert.uri2Bitmap(this, uri)
            this.bitmap = bitmap
            this.uri = uri
            YImageDialog.show(this, bitmap)
            YToast.show(this, "file:" + file.exists())
        }

        yPicture.setPictureFromAlbumListener { uri, file, Flag ->
            val bitmap = YConvert.uri2Bitmap(this, uri)
            this.bitmap = bitmap
            this.uri = uri
            YImageDialog.show(this, bitmap)
            YToast.show(this, "file:" + file.exists())
        }
        yInstallApk = YInstallApk(this)
        YPermissions.requestAll(this)
    }
    var i=0;
    fun loop() {
        text4.text="当前：${i++}"
    }
    private var yNoticeDownload: YNoticeDownload? = null
    private fun download() {
        val url = "https://down.qq.com/qqweb/QQ_1/android_apk/AndroidQQ_8.4.5.4745_537065283.apk"
        if (yNoticeDownload == null)
            yNoticeDownload = YNoticeDownload(this, url)
        yNoticeDownload?.setDownLoadFail { show("下载失败") }
        yNoticeDownload?.setDownLoadComplete { uri, file ->
            show("下载完成")
            yInstallApk?.install(file.path)
        }
        yNoticeDownload?.setDownLoadProgress { downloadSize, fileSize ->
            val progress = (10000.0 * downloadSize / fileSize).toInt() / 100.0 //下载进度，保留2位小数
            text1.text = "$downloadSize/$fileSize"
            text2.text = "进度：$progress%"
        }
        yNoticeDownload?.start()
    }

    private var yVersionUpdate: YVersionUpdate? = null
    private fun update() {
        val url = "https://down.qq.com/qqweb/QQ_1/android_apk/AndroidQQ_8.4.5.4745_537065283.apk"
        yVersionUpdate = YVersionUpdate(
            this,
            99,
            false,
            url,
            "1.9.99",
            "\n修复了bug1引起的问题\n新增功能：aaa"
        )
        yVersionUpdate?.useNotificationDownload = false
        yVersionUpdate?.update()
    }

    private fun openDate() {
        YDateDialog.setDefaultFullScreen(true)
        val yDateDialog = YDateDialog(this)
        yDateDialog.setFormat("yyyy年MM月dd日") // 设置日期格式（如："yyyy年MM月dd日HH:mm"）
        yDateDialog.initTime(Date()) //设置初始化日期，必须和设置格式相同（如："2016年07月01日15:19"）
        yDateDialog.isShowDay = true // 设置是否显示日滚轮,默认显示
        yDateDialog.isShowTime = false // 设置是否显示时间滚轮,默认显示
        yDateDialog.isShowMonth = true // 设置是否显示时间滚轮,默认显示
        yDateDialog.show { format: String?, calendar: Calendar?, date: Date?, yyyy: String?, MM: String?, dd: String?, HH: String?, mm: String? -> }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        yInstallApk?.onActivityResult(requestCode, resultCode, data)
        yPicture.onActivityResult(requestCode, resultCode, data)
        yVersionUpdate?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        yNoticeDownload?.onDestroy()
        yVersionUpdate?.onDestroy()
    }
}
