package tech.DevAsh.keyOS.Helpers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import tech.DevAsh.keyOS.KioskApp

object AnalyticsHelper {
    fun logEvent(context:Context,eventName:String,bundle: Bundle= Bundle()){
        Handler().post {
            context.KioskApp.firebaseAnalytics?.logEvent(eventName,bundle)
        }
    }
}