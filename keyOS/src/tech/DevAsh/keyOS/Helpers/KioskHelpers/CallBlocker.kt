package tech.DevAsh.KeyOS.Helpers.KioskHelpers

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.android.internal.telephony.ITelephony
import tech.DevAsh.KeyOS.Database.UserContext.user
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.KeyOS.Services.UsageAccessService.Companion.isAlive
import tech.DevAsh.keyOS.Database.Contact
import tech.DevAsh.keyOS.Database.User


object CallBlocker {

    private var lastState = TelephonyManager.CALL_STATE_IDLE
    var runnableCallBlocker:Runnable?=null
    var handlerCallBlocker:Handler?=null

    fun createCallBlockerLooper(context: Context ){
        handlerCallBlocker = Handler()
        runnableCallBlocker = Runnable {
            if(isAlive(context))runCallBlocker(context)
            handlerCallBlocker?.postDelayed(runnableCallBlocker!!, 250)
        }
        handlerCallBlocker?.postDelayed(runnableCallBlocker!!, 1000)
    }


    private fun runCallBlocker(context: Context){
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try{
            telephony.listen(object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, incomingNumber: String) {
                    if (isAlive(context)) {
                        onCall(state, incomingNumber, context.applicationContext, user)
                    }
                    super.onCallStateChanged(state, incomingNumber)
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }catch (e: Throwable){

        }

    }

    fun killCallBlockerLooper(){
        handlerCallBlocker!!.removeCallbacks(runnableCallBlocker!!)
        handlerCallBlocker=null
    }


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
                    AlertHelper.showToast("Call blocked $number", context)
                    rejectCall(context)
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> if (lastStateTemp != TelephonyManager.CALL_STATE_RINGING) {
                println("Outgoing")
                //outgoing call started
                if (!user!!.calls.allowCalls || !user!!.calls.allowOutgoing || !isValidNumber(number, user,context)) {
                    AlertHelper.showToast("Call blocked $number", context)
                    rejectCall(context)
                    Handler().postDelayed({ rejectCall(context) },8000)
                }
            } else {
                //incoming call answered
                println("Incoming ended")
                if (!user!!.calls.allowCalls || !user!!.calls.allowIncoming || !isValidNumber(number,user)) {
                    AlertHelper.showToast("Call blocked $number", context)
                    rejectCall(context)
                }
            }
        }
        lastState = state

    }

    private fun isValidNumber(number: String, user: User?,context: Context? = null):Boolean{

        if(number.isEmpty()){
            return true
        }

        val possible1 = Contact("", number)
        val possible2 = Contact("", "+91$number")
        val possible3 = Contact("", "+$number")


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





    private fun rejectCall(context: Context){
        println("Call rejected..")
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    println("Permission Not Given")
                }
               tm.endCall()
            }catch (e:Throwable){
                e.printStackTrace()
            }
        }
        else
            disconnectPhoneITelephony(context)
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
