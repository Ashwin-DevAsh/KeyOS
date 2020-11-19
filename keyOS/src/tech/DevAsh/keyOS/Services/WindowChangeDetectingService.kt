package tech.DevAsh.KeyOS.Services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.Helpers.KioskHelpers.WebBlocker
import tech.DevAsh.keyOS.Receiver.KioskReceiver
import tech.DevAsh.keyOS.Receiver.KioskToggle
import java.util.*


class WindowChangeDetectingService : AccessibilityService() , KioskToggle {

    var kioskReceiver = KioskReceiver(this)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private var prevActivities = arrayListOf("")



    override fun onServiceConnected() {
        RealmHelper.init(this)
        startReceiver()
        val info: AccessibilityServiceInfo = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
        info.notificationTimeout = 300
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info
    }


    private fun startReceiver(){
        val filter = IntentFilter()
        filter.priority=1000
        filter.addAction(KioskReceiver.START_KIOSK)
        filter.addAction(KioskReceiver.STOP_KIOSK)
        filter.addAction(KioskReceiver.SHOW_ALERT_DIALOG)
        filter.addAction(KioskReceiver.REMOVE_ALERT_DIALOG)
        registerReceiver(kioskReceiver, filter)
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if(PermissionsHelper.isMyLauncherCurrent(this)){
            if(Kiosk.isKisokEnabled){
                checkActivity(this, event)
                WebBlocker.block(event, this)
            }
        }
    }

    private fun showAppBlockAlertDialog(context: Context){
        KioskReceiver.sendBroadcast(context,KioskReceiver.SHOW_ALERT_DIALOG)
    }

    private fun checkActivity(context: Context, event: AccessibilityEvent) {
        val appName: String? = event.packageName?.toString()
        val className :String? = event.className?.toString()

        if(appName==null || className==null){
            return
        }


        if ( appName == "com.android.settings" || appName == packageName
            || AppsContext.allApps.contains(Apps(appName))
            || event.className.toString().toLowerCase(Locale.ROOT).contains("recent")){

            if(isAllowedPackage(appName, className)){
                if(prevActivities.last()!=appName
                   && (UserContext.user!!.allowedApps.contains(Apps(appName)) || appName==packageName)){
                    prevActivities.add(appName)
                }
            }else{
                Handler().post{
                    showAppBlockAlertDialog(context)
                }
                block()

            }
        }


    }

    private fun isAllowedPackage(appName: String?, className: String?):Boolean{
        val app = Apps(appName)

        if(appName == packageName
           || AppsContext.exceptions.contains(appName)
           || AppsContext.exceptions.contains(className)
           || className.toString().contains("android.inputmethodservice")
           || try{ Class.forName(className.toString());true} catch (e: Throwable){false}
        ){
            return true
        }

        val serviceIndex = UserContext.user?.allowedServices?.indexOf(app)
        val appIndex = UserContext.user?.allowedApps?.indexOf(app)
        val editedAppIndex = UserContext.user?.editedApps?.indexOf(app)

        if(serviceIndex!=-1){
            return true
        }

        if(appIndex==-1){
            return false
        }

        if(editedAppIndex==-1){
            return true
        }

        if (UserContext.user!!.editedApps[editedAppIndex!!]!!.blockedActivities.contains(className)){
            return false
        }

        return true
    }

    private fun block() {
        val launcher = Intent(Intent.ACTION_MAIN)
        launcher.addCategory(Intent.CATEGORY_HOME)
        launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            println("Prev = $prevActivities")
            if(prevActivities.last()==this.packageName){
                throw Exception()
            }

            val prev1 = packageManager.getLaunchIntentForPackage(
                    prevActivities[prevActivities.size - 1])
            val prev2 = packageManager.getLaunchIntentForPackage(
                    prevActivities[prevActivities.size - 2])
            when {

                prev1!=null -> {
                    if(prev1.`package`==this.packageName){
                        throw Exception()
                    }
                    prev1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    prev1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(prev1)
                }
                prev2!=null -> {
                    if(prev2.`package`==this.packageName){
                        throw Exception()
                    }
                    prev2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    prev2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(prev2)
                }
                else -> {
                    throw Exception()
                }
            }

        }catch (e: Throwable){
            startActivity(launcher)
        }finally {
            Handler().postDelayed({
                                      KioskReceiver.sendBroadcast(this,KioskReceiver.REMOVE_ALERT_DIALOG)

                                  }, 2000)
        }

    }

    override fun onInterrupt() {}

    override fun startKiosk(context: Context?) {
        Kiosk.isKisokEnabled=true
        User.getUsers()
        if(UserContext.user!!.singleApp!=null){
            UserContext.user?.allowedApps?.add(UserContext.user?.singleApp)
        }
    }

    override fun stopKiosk(context: Context?) {
        Kiosk.isKisokEnabled=false
    }

    override fun onDestroy() {
        unregisterReceiver(kioskReceiver)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        unregisterReceiver(kioskReceiver)
        super.onTaskRemoved(rootIntent)
    }
}