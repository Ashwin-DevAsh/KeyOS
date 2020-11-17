package tech.DevAsh.KeyOS.Database;


import tech.DevAsh.keyOS.Database.Apps

object AppsContext {
    var allApps= mutableListOf<Apps>()

    var allService = mutableListOf<Apps>()

    var exceptions = arrayListOf(
            "com.android.settings.AllowBindAppWidgetActivity",
            "android.app.Dialog")
}