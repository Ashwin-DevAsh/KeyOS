package tech.DevAsh.KeyOS.Helpers

import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MAIN
import android.content.Intent.CATEGORY_HOME
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.launcher3.BuildConfig
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import tech.DevAsh.KeyOS.Receiver.SampleAdminReceiver
import tech.DevAsh.KeyOS.Services.WindowChangeDetectingService
import java.util.*
import kotlin.collections.ArrayList


object PermissionsHelper {

    var runTimePermissions = arrayListOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.ACCESS_COARSE_LOCATION)

    var openedForPermission = false
    var task: Task<LocationSettingsResponse>?=null

    init {

        if(Build.VERSION.SDK_INT>=26){
            runTimePermissions.add(0, android.Manifest.permission.ANSWER_PHONE_CALLS)
        }

        if(!BuildConfig.IS_PLAYSTORE_BUILD){
            runTimePermissions.add(android.Manifest.permission.READ_CALL_LOG)
        }
    }

    fun isAccessServiceEnabled(context: Context, accessibilityServiceClass: Class<*>): Boolean {
        val prefString = Settings.Secure.getString(context.contentResolver,
                                                   Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return prefString != null && prefString.contains(
                context.packageName + "/" + accessibilityServiceClass.name)
    }

    fun checkImportantPermissions(context: Activity):Boolean{
        return isUsage(context) && isWrite(context) && isOverLay(context) && isRunTime(context) && isNotificationEnabled(
                context) && isAccessServiceEnabled(context,WindowChangeDetectingService::class.java)
    }

    fun isUsage(context: Context): Boolean {
        val packageManager = context.packageManager
        val applicationInfo: ApplicationInfo =
                packageManager.getApplicationInfo(context.packageName, 0)
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                                applicationInfo.uid, applicationInfo.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isWrite(context: Context): Boolean {
        return try{
            Settings.System.canWrite(context.applicationContext)
        }catch (e: Throwable){
            true
        }
    }

    fun isUsb(context: Context): Boolean {
        return Settings.Secure.getInt(context.contentResolver, Settings.Secure.ADB_ENABLED, 0) != 1
    }

    fun isOverLay(context: Context): Boolean {

        return try{
            Settings.canDrawOverlays(context.applicationContext)
        }catch (e: Throwable){
            true
        }
    }

    fun isAdmin(context: Context):Boolean{
        val mDPM = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val mAdminName = ComponentName(context, SampleAdminReceiver::class.java)
        return mDPM.isAdminActive(mAdminName)
    }

    fun isRunTime(context: Activity):Boolean{
        for ( i in runTimePermissions){
            if(!checkRuntimePermission(context, i)){
                return false
            }
        }
        return true
    }

    fun isNotificationEnabled(context: Context):Boolean{
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            notificationManager.isNotificationPolicyAccessGranted
        }else{
            true
        }
    }

    fun getAdminPermission(context: AppCompatActivity){
        val componentName = ComponentName(context, SampleAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        context.startActivityForResult(intent, 1)
    }



    fun getNotificationPermission(context: AppCompatActivity){
        openedForPermission=true
        val notificationManager: NotificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
             context.startActivityForResult(intent, 0)
        }
    }

    fun disableUSB(context: AppCompatActivity){
        openedForPermission=true
        val selector =
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        context.startActivityForResult(selector, 0)
    }

    fun getOverlayPermission(context: AppCompatActivity){
        openedForPermission=true
        val selector =
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                    "package:${context.packageName}"))
        context.startActivity(selector)
    }

    fun getAccessibilityService(context: AppCompatActivity){
        openedForPermission=true
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivityForResult(intent, 0)
    }

    fun getWriteSettingsPermission(context: AppCompatActivity){
        openedForPermission=true
        val selector =
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse(
                    "package:${context.packageName}"))
        context.startActivityForResult(selector, 0)
    }

    fun getUsagePermission(context: AppCompatActivity){
        openedForPermission=true
        val selector = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivityForResult(selector, 0)
    }

    fun allow(context: AppCompatActivity){
        openedForPermission=true
        Handler().post{
            disableUSB(context)
            getAdminPermission(context)
            getWriteSettingsPermission(context)
            getOverlayPermission(context)
            getUsagePermission(context)
            getAccessibilityService(context)
        }

    }

    fun getRuntimePermission(context: AppCompatActivity, permission: Array<String>,
                             requestCode: Int){
         openedForPermission = true
        if ("xiaomi" == Build.MANUFACTURER.toLowerCase(Locale.ROOT)) {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
            intent.setClassName("com.miui.securitycenter",
                                "com.miui.permcenter.permissions.PermissionsEditorActivity")
            intent.putExtra("extra_pkgname", context.packageName)
           context.startActivity(intent)
        }
        ActivityCompat.requestPermissions(context,
                                          permission,
                                          requestCode)
    }

     fun checkRuntimePermission(context: Activity, permission: String): Boolean {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
             return true
         }
         try {
             if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                 return false
             }
             return true
         }catch (e: Throwable){
             return true
         }

//        val res: Int = context.checkCallingOrSelfPermission(permission)
//        return res == PackageManager.PERMISSION_GRANTED
    }


    fun isMyLauncherDefault(context: Context): Boolean = ArrayList<ComponentName>().apply {
       context.packageManager.getPreferredActivities(
               arrayListOf(IntentFilter(ACTION_MAIN).apply { addCategory(CATEGORY_HOME) }),
               this,
               context.packageName)
    }.isNotEmpty()


    fun isMyLauncherCurrent(context: Context): Boolean {
        val intent = Intent(ACTION_MAIN)
        intent.addCategory(CATEGORY_HOME)
        val resolveInfo: ResolveInfo =
              context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return context.packageName == resolveInfo.activityInfo.packageName
    }



}