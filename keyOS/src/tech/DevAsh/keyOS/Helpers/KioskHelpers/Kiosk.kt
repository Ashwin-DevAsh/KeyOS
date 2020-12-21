package tech.DevAsh.KeyOS.Helpers.KioskHelpers

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import tech.DevAsh.KeyOS.Config.Settings
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.KeyOS.Receiver.SampleAdminReceiver
import tech.DevAsh.KeyOS.Services.UsageAccessService
import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.User.user
import tech.DevAsh.keyOS.Helpers.KioskHelpers.AlertDeveloper
import tech.DevAsh.keyOS.Receiver.KioskReceiver
import java.io.File


object Kiosk {
    private var usageIntent:Intent?=null
    var accessibilityService:Intent?=null
    var isKisokEnabled = false

    val TAG = this::class.simpleName


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
        if(!isMyServiceRunning(context, UsageAccessService::class.java)){
            isKisokEnabled = true
            writeLog()
            Log.d(TAG, "startKiosk: KioskStarted")
            NotificationBlocker.start()
            KioskReceiver.sendBroadcast(context, KioskReceiver.START_KIOSK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(getUsageAccessService(context))
            } else {
                context.startService(getUsageAccessService(context))
            }
            setCamera(context, user!!.basicSettings.isDisableCamera)
            AlertDeveloper.sendUserLaunchedAlert(context)
        }
    }




    fun stopKiosk(context: Context){
        KioskReceiver.sendBroadcast(context, KioskReceiver.STOP_KIOSK)
        isKisokEnabled = false
        NotificationBlocker.stop()
        context.stopService(getUsageAccessService(context))
        context.stopService(getAccessibilityService(context))
        setCamera(context, false)
        Log.d(TAG, "stopKiosk: KioskStopped")
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
            AlertDeveloper.sendUserLaunchedAlert(context, false)
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

    private fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager: ActivityManager =
                 context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun writeLog(){
        try {
            val path = File(Environment.getExternalStorageDirectory(), "KeyOS/logs")
            if (!path.exists()) {
                path.mkdir()
            }else{
                path.deleteOnExit()
                path.mkdir()
            }
            Runtime.getRuntime().exec(
                    arrayOf("logcat", "-f", path.absolutePath + "/log.txt"))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}