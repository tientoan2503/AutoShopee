package com.tientoan.autoclickshoppe

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log


/**
 * Create by tientoan 2503 on 22/03/2022
 */
class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_OVERLAY_CODE = 123
        private const val REQUEST_ACCESSIBILITY_PERMISSION = 11
        private const val NAME_ACCESSIBILITY_SERVICE =
            "com.tientoan.autoclickshoppe/com.tientoan.autoclickshoppe.AutoService"
        const val SHARED_PRE = "shared_pre";
        const val LIMIT_USE = true
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (LIMIT_USE) {
            limitOpen()
        }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        Log.d("ToanNTe", "onCreate: height " + height)
        Log.d("ToanNTe", "onCreate: height " + width)

        if (!Settings.canDrawOverlays(this)) {
            openRequestOverlayPermission(this, REQUEST_OVERLAY_CODE)
        } else {
            if (!isAccessibilityEnabled()) {
                requestAccessibilityPermission()
            } else {
//                finish()
            }
        }
    }

    // giới hạn lần sử dụng
    private fun limitOpen() {
        var count = sharedPreferences.getInt(SHARED_PRE, 0);
        if (count > 3) {
            Toast.makeText(this, "Hết số lượt dùng thử!",  Toast.LENGTH_LONG).show()
            val intent = Intent(this, AutoService::class.java)
            intent.action = Action.STOP
            startService(intent)
            finish()
        }
        sharedPreferences.edit().putInt(SHARED_PRE, count + 1).apply()
    }

    private fun openRequestOverlayPermission(activity: Activity, requestOverlayCode: Int) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        activity.startActivityForResult(intent, requestOverlayCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_OVERLAY_CODE -> {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(
                        this,
                        "Cần cấp quyền để sử dụng ứng dụng này!",
                        Toast.LENGTH_LONG
                    ).show()
                    openRequestOverlayPermission(this, REQUEST_OVERLAY_CODE)
                } else {
                    if (!isAccessibilityEnabled()) {
                        requestAccessibilityPermission()
                    }
                }
            }

            REQUEST_ACCESSIBILITY_PERMISSION -> {
                if (isAccessibilityEnabled()) {
                    val intent = Intent(this, AutoService::class.java)
                    startService(intent)
//                    finish()
                }
            }
        }
    }

    private fun requestAccessibilityPermission() {
        if (!isAccessibilityEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, REQUEST_ACCESSIBILITY_PERMISSION)
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        var accessibilityEnabled = 0
        val accessibilityFound = false
        try {
            accessibilityEnabled =
                Settings.Secure.getInt(this.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(
                            NAME_ACCESSIBILITY_SERVICE,
                            ignoreCase = true
                        )
                    ) {
                        return true
                    }
                }
            }
        }
        return accessibilityFound
    }
}