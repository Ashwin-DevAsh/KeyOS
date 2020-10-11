package tech.DevAsh.KeyOS.Helpers.KioskHelpers

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.*

class StatusBarLocker(private val context: Context) {
    private var view: CustomViewGroup? = null
    fun lock() {
        val manager = windowManager
        val localLayoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY // fix
        } else {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        localLayoutParams.gravity = Gravity.CENTER
        localLayoutParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
//        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
//        val result: Int
//        result = if (resId > 0) {
//            context.resources.getDimensionPixelSize(resId)
//        } else {
//            // Use Fallback size:
//            60 // 60px Fallback
//        }
        localLayoutParams.height =  WindowManager.LayoutParams.MATCH_PARENT
        localLayoutParams.format = PixelFormat.RGB_565
        view = CustomViewGroup(context)
        manager.addView(view, localLayoutParams)
    }

    private val windowManager: WindowManager
         get() = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun release() {
        if (view != null) {
            windowManager.removeView(view)
        }
    }

    private class CustomViewGroup(context: Context?) : ViewGroup(context) {
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            return true
        }
    }

    companion object {
        fun askPermission(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    context.startActivity(myIntent)
                }
            }
        }
    }
}