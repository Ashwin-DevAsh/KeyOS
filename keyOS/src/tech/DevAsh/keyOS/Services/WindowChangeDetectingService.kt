package tech.DevAsh.KeyOS.Services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.Plugins
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.Helpers.KioskHelpers.WebBlocker
import tech.DevAsh.keyOS.Receiver.KioskReceiver
import tech.DevAsh.keyOS.Receiver.KioskToggle
import java.util.*


class WindowChangeDetectingService : AccessibilityService() , KioskToggle {

    var kioskReceiver = KioskReceiver(this)
    var launcher:Intent?=null

    val TAG = this::class.simpleName


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    var prevActivities = arrayListOf<Intent>()

    private fun loadHomeScreen(){
        launcher = Intent(Intent.ACTION_MAIN)
        launcher?.addCategory(Intent.CATEGORY_HOME)
        launcher?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        prevActivities.add(launcher!!)
    }


    override fun onServiceConnected() {
        RealmHelper.init(this)
        startReceiver()
        loadHomeScreen()
        loadData()
        val info: AccessibilityServiceInfo = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
        info.notificationTimeout = 300
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info
    }

    private fun loadData(){
        try {
            User.getUsers(this)
            if(User.user!!.singleApp!=null){
                User.user?.allowedApps?.add(User.user?.singleApp)
            }
        }catch (e: Throwable){

        }
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

        if(PermissionsHelper.isMyLauncherCurrent(this) && Kiosk.isKisokEnabled){
            val appName: String? = event.packageName?.toString()
            val className :String? = event.className?.toString()
            Handler().post {
                checkActivity(this, appName,className)
            }
            WebBlocker.block(event, this)
        }
    }

    private fun showAppBlockAlertDialog(context: Context){
        KioskReceiver.sendBroadcast(context,KioskReceiver.SHOW_ALERT_DIALOG)
    }

    private fun checkActivity(context: Context,appName: String?,className: String?) {


        if(appName==null || className==null){
            return
        }

        if(blockRecentScreen(className)){
            return
        }

        if (appName == "com.android.settings" || appName == packageName || Apps.allApps.contains(Apps(appName))){
            if(isAllowedPackage(appName, className)){
                Handler().post{
                    if(prevActivities.last().component?.packageName!=appName){
                        if(appName==packageName){
                            prevActivities.add(launcher!!)
                        }else{
                            if(User.user?.allowedApps!!.contains(Apps(appName))){
                                val intent = packageManager.getLaunchIntentForPackage(appName)
                                if(intent!=null){
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    prevActivities.add(intent)
                                }
                            }

                        }
                    }
                }
            }else{
                Handler().post{
                    showAppBlockAlertDialog(context)
                }
                block(appName)
            }
        }
    }

    private fun blockRecentScreen(className: String?):Boolean{
        if(className.toString().contains("recent")){
            Handler().post {
                showAppBlockAlertDialog(this)
                startActivity(launcher)
            }
            Handler().postDelayed({
                                      KioskReceiver.sendBroadcast(this, KioskReceiver.REMOVE_ALERT_DIALOG)
                                  }, 2000)
            Log.d(TAG, "blockRecentScreen: $className")
            return true
        }
        return false
    }

    private fun isAllowedPackage(appName: String?, className: String?):Boolean{
        val app = Apps(appName)


        if(appName == packageName
           || Apps.exceptions.contains(appName)
           || Apps.exceptions.contains(className)
           || className.toString().contains("android.inputmethodservice")
           || try{ User.user.allowedPlugins?.contains(Plugins("","", className))!!}catch (e:Throwable){false}
           || try{ Class.forName(className.toString());true} catch (e: Throwable){false}
        ){
            return true
        }

        val serviceIndex = User.user?.allowedServices?.indexOf(app)
        val appIndex = User.user?.allowedApps?.indexOf(app)
        val editedAppIndex = User.user?.editedApps?.indexOf(app)

        if(serviceIndex!=-1){
            return true
        }

        if(appIndex==-1){
            return false
        }

        if(editedAppIndex==-1){
            return true
        }

        if (User.user!!.editedApps[editedAppIndex!!]!!.blockedActivities.contains(className)){
            return false
        }

        return true
    }

    private fun block(appName: String?) {
        try {
            startActivity(prevActivities.last())
        }catch (e: Throwable){
            startActivity(launcher)
        }finally {
            Handler().postDelayed({
                                      KioskReceiver.sendBroadcast(
                                              this,
                                              KioskReceiver.REMOVE_ALERT_DIALOG)
                                  }, 2000)
            Log.d(TAG, "block: $appName")
        }

    }

    override fun onInterrupt() {}

    override fun startKiosk(context: Context?) {
        Kiosk.isKisokEnabled=true
        User.getUsers(context)
        if(User.user!!.singleApp!=null){
            User.user?.allowedApps?.add(User.user?.singleApp)
        }
    }

    override fun stopKiosk(context: Context?) {
        Kiosk.isKisokEnabled=false
    }

    override fun onDestroy() {
        try{
            startActivity(launcher)
            unregisterReceiver(kioskReceiver)
            super.onDestroy()
        }catch (e:Throwable){}

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        unregisterReceiver(kioskReceiver)
        super.onTaskRemoved(rootIntent)
    }
}