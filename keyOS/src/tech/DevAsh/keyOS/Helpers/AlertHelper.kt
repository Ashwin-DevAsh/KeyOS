package tech.DevAsh.KeyOS.Helpers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager


object AlertHelper {
    var timeAlertDialog : AlertDialog?=null


    fun showError(text: String, context: AppCompatActivity){
        SnackbarManager.show(
                Snackbar.with(context) // context
                        .text(text) // text to be displayed
                        .textTypeface(Typeface.DEFAULT_BOLD)
                        .duration(2000)
                        .textColor(Color.WHITE) // change the text color
                        .color(Color.parseColor("#b71c1c")) // change the background color
                , context
                            )
    }

    fun showToast(text: String, context: Context) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }


    fun showTimerAlertDialog(context: Context, appName: String?){
        if(timeAlertDialog!=null){
            if(!timeAlertDialog!!.isShowing){
                timeAlertDialog?.show()
            }
            return
        }

        val dialog = AlertDialog.Builder(context, R.style.MyProgressDialog)
        dialog.setTitle("App isn't available")
        dialog.setMessage("Application is paused as your app timer ran out")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Ok") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        timeAlertDialog = dialog.create()
        if(Build.VERSION.SDK_INT >= 26){
            timeAlertDialog?.window?.setType(
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else{
            timeAlertDialog?.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        timeAlertDialog?.show()
    }

}

