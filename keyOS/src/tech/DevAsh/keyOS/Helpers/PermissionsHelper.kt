package tech.DevAsh.KeyOS.Helpers

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Process
import android.provider.Settings
import androidx.core.app.ActivityCompat
import tech.DevAsh.KeyOS.Receiver.SampleAdminReceiver
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


object PermissionsHelper {

    var openedForPermission = false
    var task: Task<LocationSettingsResponse>?=null

    fun isAccessServiceEnabled(context: Context, accessibilityServiceClass: Class<*>): Boolean {
        val prefString = Settings.Secure.getString(context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return prefString != null && prefString.contains(context.packageName + "/" + accessibilityServiceClass.name)
    }

    fun checkImportantPermissions(context: Context):Boolean{
        return isUsage(context) && isWrite(context) && isOverLay(context)
    }

    fun isUsage(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName)
        val granted: Boolean
        granted = if (mode == AppOpsManager.MODE_DEFAULT && Build.VERSION.SDK_INT >= 23) {
           context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
        return granted
    }

    fun isWrite(context: Context): Boolean {
         return  Settings.System.canWrite(context)
    }

    fun isUsb(context: Context): Boolean {
        return Settings.Secure.getInt(context.contentResolver, Settings.Secure.ADB_ENABLED, 0) != 1
    }

    fun isOverLay(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) Settings.canDrawOverlays(context) else true
    }

    fun isAdmin(context: Context):Boolean{
        val mDPM = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val mAdminName = ComponentName(context, SampleAdminReceiver::class.java)
        return mDPM.isAdminActive(mAdminName)
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

    fun getRuntimePermission(context: AppCompatActivity, permission: Array<String>, requestCode: Int){
        ActivityCompat.requestPermissions(context,
            permission,
            requestCode)
    }

     fun checkRuntimePermission(context: AppCompatActivity, permission: String): Boolean {
        val res: Int = context.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    private fun setupLocationServices(context: AppCompatActivity) {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10
        mLocationRequest.smallestDisplacement = 10f
        mLocationRequest.fastestInterval = 10
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        task = LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())
        locationSettingsResponseBuilder(context)
    }

    private fun locationSettingsResponseBuilder(context: AppCompatActivity) {
        task!!.addOnCompleteListener { task: Task<LocationSettingsResponse?> ->
            try {
                val response =
                    task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->             // Location settings are not satisfied. But could be fixed by showing the
                        try {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(
                              context,
                                101)
                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }


}