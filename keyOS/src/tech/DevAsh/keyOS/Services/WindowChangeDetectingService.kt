package tech.DevAsh.KeyOS.Services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.android.launcher3.R
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Helpers.KioskHelpers.WebBlocker
import java.util.*


class WindowChangeDetectingService : AccessibilityService() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private var prevActivities = arrayListOf("")


    private fun startAsForeground(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "KeyOS Protection"
            val channelName: CharSequence = "Protection"
            val importance = NotificationManager.IMPORTANCE_MIN
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)
            val builder: Notification.Builder = Notification.Builder(this, channelId)
                    .setContentTitle("KeyOS Protection")
                    .setContentText("Your device completely protected by keyOS")
                    .setSmallIcon(R.drawable.ic_key_ring)
                    .setAutoCancel(false)
            val notification: Notification = builder.build()
            startForeground(3, notification)
        } else {
            val builder = NotificationCompat.Builder(this)
                    .setContentTitle("KeyOS Protection")
                    .setContentTitle("Your device completely protected by keyOS")
                    .setSmallIcon(R.drawable.ic_key_ring)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(false)
            val notification: Notification = builder.build()
            startForeground(3, notification)
        }
    }



    override fun onServiceConnected() {
        val info: AccessibilityServiceInfo = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
        info.notificationTimeout = 300
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if(PermissionsHelper.isMyLauncherCurrent(this)){
            if(Kiosk.isKisokEnabled){
                checkActivity(this, event)
                WebBlocker.block(event, this)
            }
        }
    }

    var blockAppAlertDialog : AlertDialog?=null

    private fun showAppBlockAlertDialog(context: Context){

        if(blockAppAlertDialog!=null){
            if(!blockAppAlertDialog!!.isShowing){
                blockAppAlertDialog?.show()
            }
            return
        }

        val dialog = AlertDialog.Builder(context,
                                         android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.sheet_access_denied, null)
        dialog.setView(view)

        blockAppAlertDialog = dialog.create()
        blockAppAlertDialog?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        blockAppAlertDialog?.show()

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
                block(context)

            }
        }


    }

    private fun isAllowedPackage(appName: String?, className: String?):Boolean{
        val app = Apps(appName)

        if(appName == packageName
           || AppsContext.exceptions.contains(appName)
           || AppsContext.exceptions.contains(className)
           || className.toString().contains("android.inputmethodservice")
           || try{ Class.forName( className.toString() );true} catch (e:Throwable){false}
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

    private fun block(context: Context) {
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
                                      blockAppAlertDialog?.dismiss()
                                  }, 2000)
        }

    }




    override fun onInterrupt() {

    }





}