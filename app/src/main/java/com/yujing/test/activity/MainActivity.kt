package com.yujing.test.activity

import android.graphics.Color
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.yujing.base.contract.YLifeEvent
import com.yujing.bus.ThreadMode
import com.yujing.bus.YBus
import com.yujing.bus.YBusUtil
import com.yujing.socket.YSocketSync
import com.yujing.test.R
import com.yujing.test.activity.bluetooth.BleClientActivity
import com.yujing.test.activity.bluetooth.BleServerActivity
import com.yujing.test.base.KBaseActivity
import com.yujing.test.databinding.ActivityAllTestBinding
import com.yujing.utils.*
import com.yujing.view.YAlertDialogUtils
import com.yujing.view.YView
import com.yutils.view.utils.Create


class MainActivity : KBaseActivity<ActivityAllTestBinding>(null) {
    lateinit var textView1: TextView
    lateinit var textView2: TextView
    lateinit var editText1: EditText
    var yVersionUpdate = YVersionUpdate()

    override fun initBefore() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_test)
    }

    override fun init() {
        binding.wll.removeAllViews()
        binding.ll.removeAllViews()
        textView1 = Create.textView(binding.ll)
        textView2 = Create.textView(binding.ll)
        //生命周期监听
        setEventListener { event, obj ->
            when (event) {
                YLifeEvent.onDestroy -> {
                    YLog.d("MainActivity，onDestroy")
                }
                YLifeEvent.onStart -> {
                    YLog.d("MainActivity，onStart")
                }
                YLifeEvent.onCreate -> {
                    YLog.d("MainActivity，onCreate")
                }
                else -> {}
            }
        }

        Create.button(binding.wll, "退出APP") {
            finish()
        }

        Create.button(binding.wll, "更改间距") {
            binding.wll.vertical_Space = 20F
            binding.wll.horizontal_Space = 20F
            binding.wll.gravity = 2
            //binding.wll.isFull = true
            binding.wll.requestLayout()
            binding.wll.invalidate()
        }

        Create.button(binding.wll, "清除屏幕") {
            textView1.text = ""
        }

        //--------------------------------------------------------------------------------
        Create.space(binding.wll)//换行
        editText1 = Create.editText(binding.wll, "123456789")

        Create.button(binding.wll, "请求权限") {
//            val yPermissions = YPermissions(this)
//            yPermissions.setSuccessListener {
//                YLog.i("成功$it")
//            }.setFailListener {
//                YLog.i("失败$it")
//            }.setAllSuccessListener {
//                YLog.i("全部成功")
//            }.request(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA
//            )
            YPermissions.requestAll(this)
        }

        Create.button(binding.wll, "BLE_Server") {
            startActivity(BleServerActivity::class.java)
        }

        Create.button(binding.wll, "BLE_Client") {
            startActivity(BleClientActivity::class.java)
        }

        Create.button(binding.wll, "拍照") {
            YTake.take(this) {
                val bitmap = YConvert.uri2Bitmap(this, it)
                YImageDialog.show(bitmap)
            }
        }

        Create.button(binding.wll, "选择图片并剪切") {
            YTake.chosePictureAndCorp(this) {
                val bitmap = YConvert.uri2Bitmap(this, it)
                YImageDialog.show(bitmap)
            }
        }

        Create.button(binding.wll, "更新APP") {
            val url = "https://down.qq.com/qqweb/QQlite/qqlite_5.9.3.3468_Android_537067382.apk"
            yVersionUpdate.useNotificationDownload = false
            val description = "1.更新了xxxxxxx\n2.新增xxxxxxxxxxx\n3.修复xxxxx的bug\n4.版本迭代如果出现异常或者问题，请尽快联系开发者，进行修复和处理。谢谢大家的积极配合。"
            //yVersionUpdate.dialog.okButtonBackgroundColor= Color.parseColor("#21A9FA")
            yVersionUpdate.alertDialogListener = {
                val buttonOk = it.getButton(AlertDialog.BUTTON_POSITIVE)
                val buttonCancel = it.getButton(AlertDialog.BUTTON_NEGATIVE)
                YView.setButtonBackgroundTint(buttonOk, Color.parseColor("#6045D0A0"), Color.parseColor("#FF45D0A0"))
                YView.setButtonBackgroundTint(buttonCancel, Color.parseColor("#6045D0A0"), Color.parseColor("#FF45D0A0"))
            }
            yVersionUpdate.update(634, true, url, "6.3.4", description + description + description)
        }

        Create.button(binding.wll, "弹窗测试") {
            //多选
            val listName: MutableList<String> = ArrayList()
            listName.add("项目1")
            listName.add("项目2")
            listName.add("项目3")
            listName.add("项目4")
            listName.add("项目5")
            listName.add("项目6")
            val checked = BooleanArray(listName.size) { i -> false } //默认选中项，最终选中项
            YAlertDialogUtils().showMultiChoice("请选择", listName.toTypedArray(), checked, {
                //筛选选中项
                val newList: MutableList<String> = ArrayList()
                for (index in checked.indices) {
                    if (checked[index]) newList.add(listName[index])
                }
                textView1.text = "您选择了：${YJson.toJson(newList)}"
            })
        }

        TTS.filter = { it.replace("不正确", "不在范围内") }
        //语音过滤测试
        Create.button(binding.wll, "TTS过滤") {
            TTS.speak("电子秤皮重不正确")
        }

        //语音过滤测试
        Create.button(binding.wll, "TTS过滤") {
            TTS.speakToast("电子秤皮重不正确")
        }

        Create.button(binding.wll, "Ybus发送消息1") {
            Thread { YBusUtil.post("tag1", editText1.text.toString()) }.start()
        }

        Create.button(binding.wll, "弹窗测试") {
            val yDialog = YAlertDialogUtils()
            yDialog.titleTextSize = 20F
            yDialog.contentTextSize = 18F
            yDialog.buttonTextSize = 18F
            yDialog.width = (YScreenUtil.getScreenWidth() * 0.45).toInt()
            yDialog.contentPaddingTop = YScreenUtil.dp2px(40f)
            yDialog.contentPaddingBottom = YScreenUtil.dp2px(40f)
            yDialog.okButtonString = "确定"
            yDialog.cancelButtonString = "取消"
            val content = """
            |您正在执行一项操作
            |
            |执行后将无法修改，是否继续？
            """.trimMargin()
            yDialog.showMessageCancel("这是标题", content) {
                //确定事件
            }
        }

        Create.space(binding.wll)
        var ySocketSync: YSocketSync? = null

        Create.button(binding.wll, "连接") {
            ySocketSync?.exit()
            ySocketSync = YSocketSync("192.168.1.21", 8888)
            ySocketSync?.hearBytes = byteArrayOf(0)//心跳
            ySocketSync?.readTimeOut = 1000 * 5L//读取超时
            ySocketSync?.showLog = true
            ySocketSync?.showSendLog = true
            ySocketSync?.showReceiveLog = true
            ySocketSync?.clearInputStream = true //发送前清空输入流
            ySocketSync?.connectListeners?.add {
                YLog.i("连接状态", "连接${if (it) "成功" else "失败"}")
            }
            ySocketSync?.start()
        }

        Create.button(binding.wll, "发送（同步）") {
            textView1.text = ""
            Thread {
                val data = ySocketSync?.send(YConvert.hexStringToByte("02 52 44 53 01 ea 0d"))
                YThread.ui { textView1.text = "收到结果：" + YConvert.bytesToHexString(data) }
            }.start()
        }

        Create.button(binding.wll, "关闭") {
            ySocketSync?.exit()
        }

        Create.button(binding.wll, "读写配置文件") {
            val path = YPath.get() + "/config.txt"
            YPropertiesUtils.rootExplain = "这是一个配置文件"
            //提前设置key要匹配的注释内容
            YPropertiesUtils.explainMap["key1"] = "第一个普通的key"
            YPropertiesUtils.explainMap["中文key2"] = "中文key"
            YPropertiesUtils.explainMap["ip"] = "一个ip地址"
            YPropertiesUtils.explainMap["json"] = "这是一个对象"
            val map: HashMap<String, String> = HashMap()
            map["key1"] = "yujing"
            map["中文key2"] = "张三丰"
            map["ip"] = "192.168.1.1"
            // 创建或修改
            YPropertiesUtils.set(path, map)
            YPropertiesUtils.set(path, "中文key2", "张无忌")
            YPropertiesUtils.set(path, "json", Gson().toJson(map))
            // 取值
            println(YPropertiesUtils.get(path, "中文key2"))
            // 取值全部
            println(Gson().toJson(YPropertiesUtils.getAll(path)))

            //读
            YToast.show(Gson().toJson(YPropertiesUtils.getAll(path)))
        }
    }

    //通知栏下载需要调用onDestroy()
    override fun onDestroy() {
        super.onDestroy()
        yVersionUpdate.onDestroy()
    }

    @YBus("tag1", "tag2", threadMode = ThreadMode.MAIN)
    fun message1(message: String?) {
        YLog.i("收到：tag$message")
        textView1.text = textView1.text.toString() + "收到1:$message \n"
    }
}
