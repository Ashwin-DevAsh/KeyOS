package tech.DevAsh.keyOS.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import tech.DevAsh.KeyOS.Helpers.AlertHelper

class KioskReceiver(private val kioskToggle: KioskToggle) : BroadcastReceiver() {
    companion object{
        const val START_KIOSK = "START_KIOSK"
        const val STOP_KIOSK = "STOP_KIOSK"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (START_KIOSK == intent!!.action) {
            kioskToggle.startKiosk(context)
        }else if(STOP_KIOSK == intent.action){
            AlertHelper.showToast(STOP_KIOSK, context!!)
            kioskToggle.stopKiosk(context)
        }
    }
}

interface KioskToggle{
    fun startKiosk(context: Context?)
    fun stopKiosk(context: Context?)
}