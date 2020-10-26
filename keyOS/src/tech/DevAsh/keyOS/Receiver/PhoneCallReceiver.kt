package tech.DevAsh.KeyOS.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.CallBlocker


class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        println("Calling")
        val number = intent.extras!!
                .getString(TelephonyManager.EXTRA_INCOMING_NUMBER)

        println("Number...$number")
        val telephony = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                CallBlocker.onCall(state, incomingNumber, context.applicationContext,UserContext.user)
                super.onCallStateChanged(state, incomingNumber)
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }
}