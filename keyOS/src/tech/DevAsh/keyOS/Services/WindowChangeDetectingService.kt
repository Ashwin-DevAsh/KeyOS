package tech.DevAsh.KeyOS.Services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Browser
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.android.launcher3.R
import io.realm.Realm
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.Helpers.KioskHelpers.WebBlocker
import java.util.*


class WindowChangeDetectingService : AccessibilityService() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }



    override fun onServiceConnected() {
        val info: AccessibilityServiceInfo = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
        info.notificationTimeout = 300
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if(PermissionsHelper.isMyLauncherCurrent(this) && Kiosk.isKisokEnabled){
            println("source = "+event.source?.viewIdResourceName)
            if(PermissionsHelper.isMyLauncherCurrent(this)){
                WebBlocker.block(event,this)
            }
        }
    }


    override fun onInterrupt() {

    }





}