package tech.DevAsh.KeyOS.Helpers.KioskHelpers

import android.Manifest
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat

import tech.DevAsh.KeyOS.Receiver.PhoneCallReceiver
import com.android.internal.telephony.ITelephony
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.Contact
import tech.DevAsh.keyOS.Database.User


object CallBlocker {

    private var lastState = TelephonyManager.CALL_STATE_IDLE
    val broadcastReceiver =  PhoneCallReceiver()


    fun onCall(state: Int, number: String, context: Context,user: User?) {
        val lastStateTemp: Int = lastState
        lastState = state
        println("state = $state number = $number \n")
        if (lastStateTemp == state) {
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                //onIncoming call ringing
                println("Incoming")
                if (!user!!.calls.allowCalls || !user!!.calls.allowIncoming || !isValidNumber(
                                number, user, context)) {
                    rejectCall(context)
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> if (lastStateTemp != TelephonyManager.CALL_STATE_RINGING) {
                println("Outgoing")
                //outgoing call started
                if (!user!!.calls.allowCalls || !user!!.calls.allowOutgoing || !isValidNumber(number, user,context)) {
                    rejectCall(context)
                }
            } else {
                //incoming call answered
                println("Incoming ended")
                if (!user!!.calls.allowCalls || !user!!.calls.allowIncoming || !isValidNumber(number,user)) {
                    rejectCall(context)
                }
            }
        }
        lastState = state

    }

    private fun isValidNumber(number: String, user: User?,context: Context? = null):Boolean{
        val possible1 = Contact("", number)
        val possible2 = Contact("", "+91$number")
        val possible3 = Contact("", "+$number")

        println("Callblocker number = $number\n")

        when {
            user!!.calls.blackListCalls -> {
                return !(user!!.calls.blacklistContacts.contains(possible1)
                         || user!!.calls.blacklistContacts.contains(possible2)
                         || user!!.calls.blacklistContacts.contains(possible3))
            }
            user!!.calls.whitelistCalls -> {
                println(user!!.calls.whiteListContacts)
                return (user!!.calls.whiteListContacts.contains(possible1)
                        || user!!.calls.whiteListContacts.contains(possible2)
                        || user!!.calls.whiteListContacts.contains(possible3))
            }
            user!!.calls.automaticWhitelist ->{
                return if(number.startsWith("+")){
                    contactExists(context!!, number)
                }else if (number.startsWith("91") && number.length==12){
                    contactExists(context!!, "+$number")
                }else{
                    contactExists(context!!, "+91$number")

                }
            }
            else -> {
                return true
            }
        }
    }


    private fun contactExists(context: Context, number: String?): Boolean {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cr: ContentResolver = context.contentResolver
        cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")?.use { cursor ->
            val numberIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
               if(number == reformatNumber(cursor.getString(numberIndex))){
                       return true
               }
            }
        }
        return false
    }

    private fun reformatNumber(number: String):String{
        val reformatNumber:String = number.replace(" ","").replace("-","")
        return if(reformatNumber.startsWith("+")){
            reformatNumber
        }else{
            "+91$reformatNumber"
        }
    }



    fun start(context: Context){
        val pm: PackageManager = context.packageManager
        val componentName = ComponentName(context, PhoneCallReceiver::class.java)
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP)
        registerReceivers(context)
    }
    fun stop(context: Context){
        val pm: PackageManager = context.packageManager
        val componentName = ComponentName(context, PhoneCallReceiver::class.java)
        pm.setComponentEnabledSetting(componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP)
        try{
            context.unregisterReceiver(broadcastReceiver)
        }catch (e: Throwable){
            e.printStackTrace()
        }
    }
    private fun rejectCall(context: Context){
        println("Call rejected..")
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            tm.endCall()
        else
            disconnectPhoneITelephony(context)
    }

    private fun registerReceivers(context: Context){
        val broadcast = "PACKAGE_NAME.android.action.broadcast"
        val intentFilter = IntentFilter(broadcast)
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun disconnectPhoneITelephony(context: Context) {
        val telephonyService: ITelephony
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            val c = Class.forName(telephony.javaClass.name)
            val m = c.getDeclaredMethod("getITelephony")
            m.isAccessible = true
            telephonyService = m.invoke(telephony) as ITelephony
            telephonyService.endCall()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
