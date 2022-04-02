package com.tientoan.autoclickshoppe

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.*

class AutoService : AccessibilityService(), View.OnClickListener {
    private lateinit var mWindowManager: WindowManager
    private lateinit var mViewMain: LinearLayout
    private lateinit var mParamsViewMain: WindowManager.LayoutParams
    private lateinit var mBtnPauseOrStart: ImageView
    private lateinit var mBtnStop: ImageView
    private var isStop = true
    private var isCancel = false
    private var job: Job? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onCreate() {
        super.onCreate()
        initView()
        showViewMain()
        Toast.makeText(this, "Auto đã sẵn sàng!", Toast.LENGTH_LONG).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.STOP -> {
                isCancel = true
            }
        }
        return START_STICKY
    }

    private fun initView() {
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mViewMain = LinearLayout(this)

        val mainLayout = LayoutInflater.from(this).inflate(R.layout.control_layout, mViewMain, true)

        mParamsViewMain = WindowManager.LayoutParams()
        mParamsViewMain.width = WindowManager.LayoutParams.WRAP_CONTENT
        mParamsViewMain.height = WindowManager.LayoutParams.WRAP_CONTENT
        mParamsViewMain.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        mParamsViewMain.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        mParamsViewMain.format = PixelFormat.TRANSLUCENT
        mParamsViewMain.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        mBtnPauseOrStart = mainLayout.findViewById(R.id.btnPauseOrPlay)
        mBtnPauseOrStart.setOnClickListener(this)

        mBtnStop = mainLayout.findViewById(R.id.btnStop)
        mBtnStop.setOnClickListener(this)
    }

    private fun showViewMain() {
        try {
            mWindowManager.removeView(mViewMain)
        } catch (e: Exception) {

        } finally {
            mWindowManager.addView(mViewMain, mParamsViewMain)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnPauseOrPlay -> {
                if (!isCancel) {
                    if (isStop) {
                        isStop = false
                        val scope = CoroutineScope(Job() + Dispatchers.Main)
                        job = scope.launch {
                            while (true) {
                                if (!isStop) {
                                    mBtnPauseOrStart.setImageResource(R.drawable.pause)
                                    delay(300)
                                    clickBuy()
                                } else {
                                    mBtnPauseOrStart.setImageResource(R.drawable.play)
                                    break
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this@AutoService, "Nút dừng phía dưới!!", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    Toast.makeText(this@AutoService, "Hết dùng thủ rồi ạ!!!", Toast.LENGTH_LONG)
                        .show()
                }
            }

            R.id.btnStop -> {
                isStop = true
            }
        }
    }

    private suspend fun clickBuy() {
        val buyNode = rootInActiveWindow.findAccessibilityNodeInfosByText("Đặt hàng")
        if (buyNode.isNotEmpty()) {
            buyNode[1].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            try {
                val isXacMinh =
                    rootInActiveWindow.getChild(0).getChild(0).getChild(0).getChild(0)
                        .getChild(0)
                        .getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).text
                if (isXacMinh.toString() == "Xác Minh") {
                    isStop = true
                    mBtnPauseOrStart.setImageResource(R.drawable.play)
                    Toast.makeText(this@AutoService, "Có capcha nè!!!", Toast.LENGTH_LONG)
                        .show()
                } else {
                    throw Exception()
                }
            } catch (e: Exception) {
//                    (M01): Tài khoản Shopee của bạn ghi nhận dấu hiệu bất thường. Vui lòng tham khảo và tuân thủ các Điều khoản Shopee.
//                    (M02): Rất tiếc, ưu đãi được chọn đã hết lượt sử dụng. Vui lòng chọn ưu đãi khác nhé!
                val m01AndM02Error =
                    rootInActiveWindow.getChild(0).getChild(0).getChild(0).getChild(1)
                        .getChild(0)
                if (m01AndM02Error != null) {
                    val dongy = rootInActiveWindow.getChild(0).getChild(0).getChild(0).getChild(3).getChild(0)
                    delay(200)
                    dongy.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }



}
