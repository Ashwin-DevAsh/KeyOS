package tech.DevAsh.KeyOS.Helpers.KioskHelpers

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import eu.chainfire.librootjava.RootJava.sendBroadcast
import tech.DevAsh.KeyOS.Config.Settings
import tech.DevAsh.KeyOS.Database.UserContext.user
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.KeyOS.Receiver.SampleAdminReceiver
import tech.DevAsh.KeyOS.Services.UsageAccessService
import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Receiver.KioskReceiver


object Kiosk {
    private var usageIntent:Intent?=null
    var accessibilityService:Intent?=null
    var isKisokEnabled = false;


    fun getUsageAccessService(context: Context):Intent{
        if(usageIntent==null){
            usageIntent = Intent(context, UsageAccessService::class.java)
        }
        return usageIntent!!
    }

    fun getAccessibilityService(context: Context):Intent{
        if(accessibilityService==null){
            accessibilityService = Intent(context, UsageAccessService::class.java)
        }
        return accessibilityService!!
    }

    fun startKiosk(context: Context){
        isKisokEnabled = true
        println("Start Kiosk...")
        NotificationBlocker.start()
        sendBroadcast(KioskReceiver.START_KIOSK)
        context.startService(getUsageAccessService(context))
        context.startService(getAccessibilityService(context))
        setCamera(context, user!!.basicSettings.isDisableCamera)
    }

    fun sendBroadcast(string: String){
        val intent = Intent(string)
        sendBroadcast(intent)
    }


    fun stopKiosk(context: Context){
        sendBroadcast(KioskReceiver.STOP_KIOSK)
        isKisokEnabled = false
        NotificationBlocker.stop()
        context.stopService(getUsageAccessService(context))
        context.stopService(getAccessibilityService(context))
        setCamera(context, false)
    }

    fun openKioskSettings(context: Activity, password: String){
        if(password == user?.password){
            val intent = Intent(context, Settings::class.java)
            intent.putExtra("isFromLauncher", true)
            context.startActivity(intent)
            stopKiosk(context)
            context.finishAffinity()
        }
    }

    fun canShowApp(packageName: String):Boolean{
        val app = Apps(packageName)
        val appIndex = user!!.allowedApps.indexOf(app)
        val editedAppIndex = user!!.editedApps.indexOf(app)

        if(appIndex==-1){
            return false
        }

        if(editedAppIndex==-1){
            return true
        }

        return !user!!.editedApps[editedAppIndex]!!.hideShortcut
    }

    fun exitKiosk(context: Activity, password: String?){
        if(password == user?.password){
            stopKiosk(context.applicationContext)
            exitLauncher(context.applicationContext)
            context.finishAndRemoveTask()
            context.finishAffinity()
            android.os.Handler().postDelayed({
                                                 android.os.Process.killProcess(
                                                         android.os.Process.myPid());
                                             }, 500)

        }
    }




    private fun setCamera(context: Context, boolean: Boolean){
        try {
            if(PermissionsHelper.isAdmin(context)){
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val deviceAdmin = ComponentName(context, SampleAdminReceiver::class.java)
                dpm.setCameraDisabled(deviceAdmin, boolean)
            }
        }catch (e: Throwable){}

    }


    private fun exitLauncher(context: Context) {
        val packageManager = context.packageManager
        val componentName = ComponentName(context, KioskLauncher::class.java)
        packageManager.setComponentEnabledSetting(componentName,
                                                  PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                                  PackageManager.DONT_KILL_APP)
        if (context.packageManager.getComponentEnabledSetting(componentName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            val selector = Intent(Intent.ACTION_MAIN)
            selector.addCategory(Intent.CATEGORY_HOME)
            selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val settings = Intent(android.provider.Settings.ACTION_SETTINGS)
            settings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivities(arrayOf(settings, selector))
        }
    }
}