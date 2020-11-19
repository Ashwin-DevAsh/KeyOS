package tech.DevAsh.keyOS.Receiver

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import com.android.launcher3.R
import eu.chainfire.librootjava.RootJava

class KioskReceiver(private val kioskToggle: KioskToggle) : BroadcastReceiver() {
    companion object{
        const val START_KIOSK = "START_KIOSK"
        const val STOP_KIOSK = "STOP_KIOSK"
        const val SHOW_ALERT_DIALOG = "SHOW_ALERT_DIALOG"
        const val REMOVE_ALERT_DIALOG = "REMOVE_ALERT_DIALOG"
        var blockAppAlertDialog : AlertDialog?=null

        fun sendBroadcast(context: Context?, string: String){
            val intent = Intent(string)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            context?.sendBroadcast(intent)
        }

    }
    override fun onReceive(context: Context?, intent: Intent?) {
        when {
            START_KIOSK == intent!!.action -> {
                kioskToggle.startKiosk(context)
            }
            STOP_KIOSK == intent.action -> {
                kioskToggle.stopKiosk(context)
            }
            SHOW_ALERT_DIALOG == intent.action -> {
                showAlertDialog(context)
            }
            REMOVE_ALERT_DIALOG == intent.action -> {
                removeAlertDialog()
            }
        }
    }

    private fun showAlertDialog(context: Context?){
        if(blockAppAlertDialog!=null){
            if(!blockAppAlertDialog!!.isShowing){
                blockAppAlertDialog?.show()
            }
            return
        }

        val dialog = AlertDialog.Builder(context,
                                         android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.sheet_access_denied, null)
        dialog.setView(view)

        blockAppAlertDialog = dialog.create()
        if(Build.VERSION.SDK_INT >= 26){
            blockAppAlertDialog?.window?.setType(
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else{
            blockAppAlertDialog?.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        blockAppAlertDialog?.show()

    }

    fun removeAlertDialog(){
        blockAppAlertDialog?.dismiss()
    }
}

interface KioskToggle{
    fun startKiosk(context: Context?)
    fun stopKiosk(context: Context?)
}