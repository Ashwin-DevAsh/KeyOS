package tech.DevAsh.keyOS.Helpers.KioskHelpers

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Handler
import android.provider.Settings
import android.view.Surface

import tech.DevAsh.KeyOS.Services.UsageAccessService
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.keyOS.Database.User.user

object BasicSettingsHandler{
    var runnableCheckBasicSettings:Runnable? = null
    var handlerCheckBasicSettings: Handler?=null

    fun createBasicSettingsLooper(context: Context){
        handlerCheckBasicSettings = Handler()
        runnableCheckBasicSettings = Runnable {
            if(UsageAccessService.isAlive(context)) checkBasicSettings(context)
            handlerCheckBasicSettings?.postDelayed(runnableCheckBasicSettings!!, 250)
        }
        handlerCheckBasicSettings?.postDelayed(runnableCheckBasicSettings!!, 1000)
    }

    fun killBasicSettingsLooper(context: Context){
        handlerCheckBasicSettings!!.removeCallbacksAndMessages(runnableCheckBasicSettings!!)
        handlerCheckBasicSettings = null

    }

    private fun checkBasicSettings(context: Context) {
        checkWifi(context)
        checkBluetooth(context)
        checkOrientation(context)
        checkSound(context)
    }


    private fun checkWifi(context: Context){

        val wifiManager = context.applicationContext.getSystemService(
                Service.WIFI_SERVICE) as WifiManager

        if(user?.basicSettings?.wifi== BasicSettings.AlwaysON){
            if(!wifiManager.isWifiEnabled){
                println("Turning on wifi")
                wifiManager.isWifiEnabled = true
            }
        }
        else if(user?.basicSettings?.wifi== BasicSettings.AlwaysOFF){
            if(wifiManager.isWifiEnabled){
                println("Turning off wifi")
                wifiManager.isWifiEnabled = false
            }
        }
    }
    private fun checkSound(context: Context){
        try {
            if(user?.basicSettings?.sound != BasicSettings.DontCare){
                val audioManager: AudioManager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
                if(user?.basicSettings?.sound == BasicSettings.normal && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL ){
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }else if(user?.basicSettings?.sound == BasicSettings.silent && !(audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE || audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT)){
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }
            }
        }catch (e:Throwable){ }


    }
    private fun checkBluetooth(context: Context){
        try {
            val pm: PackageManager = context.packageManager
            val hasBluetooth: Boolean = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
            if(hasBluetooth && user?.basicSettings?.bluetooth != BasicSettings.DontCare){
                if(user?.basicSettings?.bluetooth== BasicSettings.AlwaysON){
                    turnOnBluetooth()
                }else if (user?.basicSettings?.bluetooth == BasicSettings.AlwaysOFF){
                    turnOffBluetooth()
                }
            }
        }catch (e: Throwable){

        }
    }
    private fun checkOrientation(context: Context ){
        if(user?.basicSettings?.orientation.equals(BasicSettings.DontCare)){
            return
        }

        try {
            if (user?.basicSettings?.orientation.equals(
                            BasicSettings.landscape) && Settings.System.getInt(
                            context.applicationContext.contentResolver,
                            Settings.System.ACCELEROMETER_ROTATION) == 1) {
                Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.USER_ROTATION,
                        Surface.ROTATION_90 //U
                        // se any of the Surface.ROTATION_ constants
                                      )
                Settings.System.putInt(context.applicationContext.contentResolver,
                                       Settings.System.ACCELEROMETER_ROTATION, 0)
            } else if (user?.basicSettings?.orientation.equals(
                            BasicSettings.portrait) && Settings.System.getInt(
                            context.applicationContext.contentResolver,
                            Settings.System.ACCELEROMETER_ROTATION) == 1) {
                Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.USER_ROTATION,
                        Surface.ROTATION_0 //U
                        // se any of the Surface.ROTATION_ constants
                                      )
                Settings.System.putInt(context.applicationContext.contentResolver,
                                       Settings.System.ACCELEROMETER_ROTATION, 0)
            }
        } catch (e: Throwable) {
        }
    }
    private fun turnOnBluetooth(){
        val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
    }
    private fun turnOffBluetooth(){
        val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.disable()
        }
    }


}