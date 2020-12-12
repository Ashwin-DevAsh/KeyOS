package tech.DevAsh.keyOS.Api.Request

import tech.DevAsh.keyOS.Database.User

class LaunchedInfo(var deviceInfo: DeviceInfo, var isLaunched:Boolean = true){
    var config:User? = if( isLaunched) User.user else null
}