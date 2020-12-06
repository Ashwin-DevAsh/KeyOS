package tech.DevAsh.keyOS.Api.Request

import com.android.launcher3.BuildConfig

class DeviceInfo(
        var sdk:String,
        var brand:String,
        var model:String,
        var deviceID:String,
        var versionName:String = BuildConfig.VERSION_NAME
                )