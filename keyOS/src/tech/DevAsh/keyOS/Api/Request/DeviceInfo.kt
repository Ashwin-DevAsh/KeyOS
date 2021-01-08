package tech.DevAsh.keyOS.Api.Request

import android.os.Build
import com.android.launcher3.BuildConfig
import tech.DevAsh.keyOS.Helpers.AlertDeveloper

class DeviceInfo(
        var sdk:String,
        var brand:String,
        var model:String,
        var deviceID:String,
        var versionName:String = BuildConfig.VERSION_NAME,
        var wifiMac:String? = AlertDeveloper.getMac())