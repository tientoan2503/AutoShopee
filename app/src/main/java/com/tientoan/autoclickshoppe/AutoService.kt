package com.tientoan.autoclickshoppe

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*

class AutoService : AccessibilityService(), View.OnClickListener {
    private var isClick: Boolean = false
    private lateinit var mWindowManager: WindowManager
    private lateinit var mViewMain: LinearLayout
    private lateinit var mParamsViewMain: WindowManager.LayoutParams
    private lateinit var mBtnPauseOrStart: ImageView
    private var isStop = false
    private var isCancel = false

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
                    isStop = false
                    isClick = true
                    val scope = CoroutineScope(Job() + Dispatchers.Main)
                    GlobalScope.launch(Dispatchers.Main) {
                        while (true) {
                            if (!isStop)
                                clickBuy()
                        }
                    }
                } else {
                    Toast.makeText(this@AutoService, "Hết dùng thủ rồi ạ!!!", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    var isClicked = false
    private fun clickBuy() {
        val buyNode = rootInActiveWindow.findAccessibilityNodeInfosByText("Đặt hàng")
        if (buyNode.isNotEmpty()) {
            mBtnPauseOrStart.setImageResource(R.drawable.pause)
            buyNode[1].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            isClicked = true
        } else {
            if (isClicked) {
                isClicked = false
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
                        clickLocation(500, 50)
                        Toast.makeText(this@AutoService, "Có lỗi!!!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    clickLocation(500, 50)
                    Toast.makeText(this@AutoService, "Có lỗi!!!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Click theo x y location
    @RequiresApi(Build.VERSION_CODES.N)
    private fun clickLocation(x: Int, y: Int) {
        val clickPath = Path()
        clickPath.moveTo(x.toFloat(), y.toFloat())
        val builder = GestureDescription.Builder()
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(clickPath, 0, 10))
            .build()
        dispatchGesture(gestureDescription, null, null)
    }

}