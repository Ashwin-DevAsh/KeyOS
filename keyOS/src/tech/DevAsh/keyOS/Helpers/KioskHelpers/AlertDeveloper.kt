package tech.DevAsh.keyOS.Helpers.KioskHelpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Log
import retrofit2.Call
import retrofit2.Response
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Api.Request.DeviceInfo
import tech.DevAsh.keyOS.Api.Request.LaunchedInfo
import tech.DevAsh.keyOS.Api.Response.BasicResponse
import tech.DevAsh.keyOS.KioskApp

object AlertDeveloper {
    var TAG = this::class.java.simpleName
    fun sendNewInstallAlert(context: Context){
        Handler().post {
            try {
                Log.d(TAG, "sendNewInstallAlert: Sending email developer")
                context.KioskApp.mailService.newInstall(getInstallDetails(context))?.enqueue(callback)
                Log.d(TAG, "sendNewInstallAlert: Done")
            }catch (e:Throwable){
                e.printStackTrace()
            }
        }
    }

    fun sendUserLaunchedAlert(context: Context){
        Handler().post {
            try {
                Log.d(TAG, "sendNewInstallAlert: Sending email developer")
                context.KioskApp.mailService.userLaunched(
                        LaunchedInfo(getInstallDetails (context),UserContext.user!!))?.enqueue(callback)
                Log.d(TAG, "sendNewInstallAlert: Done")
            }catch (e:Throwable){
                e.printStackTrace()
            }
        }
    }

    var callback:retrofit2.Callback<BasicResponse> = object:retrofit2.Callback<BasicResponse>{
        override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
            Log.d(TAG, "onResponse: Success")
        }

        override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
            Log.d(TAG, "onResponse: Failed")
        }
    }

    @SuppressLint("HardwareIds")
    fun getInstallDetails(context: Context):DeviceInfo{
        val sdk = Build.VERSION.SDK_INT
        val brand = Build.BRAND
        val model = Build.MODEL
        val deviceID = Settings.Secure.getString(context.contentResolver,
                                                 Settings.Secure.ANDROID_ID);
       return DeviceInfo(sdk.toString(), brand, model, deviceID)
    }


}