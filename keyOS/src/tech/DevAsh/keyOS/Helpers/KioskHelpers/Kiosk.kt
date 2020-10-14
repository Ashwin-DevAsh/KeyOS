package tech.DevAsh.KeyOS.Helpers.KioskHelpers

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.Launcher
import tech.DevAsh.KeyOS.Config.Settings
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Database.UserContext.user
import tech.DevAsh.KeyOS.Services.UsageAccessService
import tech.DevAsh.KeyOS.Services.WindowChangeDetectingService
import tech.DevAsh.Launcher.KioskApp
import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.keyOS.Database.Apps

object Kiosk {
    private var usageIntent:Intent?=null
    var accessibilityService:Intent?=null


    private fun getUsageAccessService(context: Context):Intent{
        if(usageIntent==null){
            usageIntent = Intent(context, UsageAccessService::class.java)
        }
        return usageIntent!!
    }
    private fun getAccessibilityService(context: Context):Intent{
        if(accessibilityService==null){
            accessibilityService = Intent(context, WindowChangeDetectingService::class.java)
        }
        return accessibilityService!!
    }
    fun startKiosk(context: Context){
        CallBlocker.start(context)
        NotificationBlocker.start()
        context.startService(getAccessibilityService(context))
        context.startService(getUsageAccessService(context))
    }

    fun stopKiosk(context: Context){
        CallBlocker.stop(context)
        NotificationBlocker.stop()
        context.stopService(getAccessibilityService(context))
        context.stopService(getUsageAccessService(context))
    }
    fun reStart(context: Context){
        startKiosk(context)
        startKiosk(context)
    }

    fun openKioskSettings(context: Activity,password:String){
        if(password == user?.password){
            val intent = Intent(context, Settings::class.java)
            intent.putExtra("isFromLauncher", true)
            context.startActivity(intent)
            stopKiosk(context)
            context.finishAffinity()
        }
    }

    fun isAllowedPackage(packageName: String):Boolean{
       return user!!.allowedApps.contains(
               Apps(packageName))
    }

    fun exitKiosk(context: Activity,password:String?){
        if(password == user?.password){
            stopKiosk(context.applicationContext)
            CallBlocker.stop(context.applicationContext)
            exitLauncher(context.applicationContext)
            context.finishAffinity()
        }
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