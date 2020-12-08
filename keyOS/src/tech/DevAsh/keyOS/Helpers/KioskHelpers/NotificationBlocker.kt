package tech.DevAsh.KeyOS.Helpers.KioskHelpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import tech.DevAsh.keyOS.Database.User


object NotificationBlocker {
     var pause = false
     var collapseNotificationHandler = Handler()
     fun collapseNow(context: Context) {
         collapseNotificationHandler.postDelayed(object : Runnable {
             @SuppressLint("WrongConstant")
             override fun run() {
                 if (!User.user!!.basicSettings.notificationPanel && !pause) {
                     val statusBarService = context.getSystemService("statusbar")
                     val statusBarManager = Class.forName("android.app.StatusBarManager")
                     val collapseStatusBar = statusBarManager.getMethod("collapsePanels")
                     collapseStatusBar.isAccessible = true
                     try {
                         collapseStatusBar.invoke(statusBarService)
                     } catch (e: Throwable) {
                     }
                     collapseNotificationHandler.postDelayed(this, 100L)
                 }
             }
         }, 300L)
     }

    fun stop(){pause=true}
    fun start(){pause = false}
}