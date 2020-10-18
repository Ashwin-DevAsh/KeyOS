package tech.DevAsh.KeyOS.Helpers

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import tech.DevAsh.KeyOS.Receiver.SampleAdminReceiver


object PermissionsHelper {

    var runTimePermissions = arrayOf(
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                    )

    var openedForPermission = false
    var task: Task<LocationSettingsResponse>?=null

    fun isAccessServiceEnabled(context: Context, accessibilityServiceClass: Class<*>): Boolean {
        val prefString = Settings.Secure.getString(context.contentResolver,
                                                   Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return prefString != null && prefString.contains(
                context.packageName + "/" + accessibilityServiceClass.name)
    }

    fun checkImportantPermissions(context: Context):Boolean{
        return isUsage(context) && isWrite(context) && isOverLay(context) && isRunTime(context)
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
         return  Settings.System.canWrite(context.applicationContext)
    }

    fun isUsb(context: Context): Boolean {
        return Settings.Secure.getInt(context.contentResolver, Settings.Secure.ADB_ENABLED, 0) != 1
    }

    fun isOverLay(context: Context): Boolean {
        return Settings.canDrawOverlays(context.applicationContext)
    }

    fun isAdmin(context: Context):Boolean{
        val mDPM = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val mAdminName = ComponentName(context, SampleAdminReceiver::class.java)
        return mDPM.isAdminActive(mAdminName)
    }

    fun isRunTime(context: Context):Boolean{
        for ( i in runTimePermissions){
            if(!checkRuntimePermission(context, i)){
                return false
            }
        }
        return true
    }

    fun getAdminPermission(context: AppCompatActivity){
        openedForPermission=true
        val componentName = ComponentName(context, SampleAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        context.startActivityForResult(intent, 0)
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
        ActivityCompat.requestPermissions(context,
                                          permission,
                                          requestCode)
    }

     fun checkRuntimePermission(context: Context, permission: String): Boolean {
        val res: Int = context.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }






}