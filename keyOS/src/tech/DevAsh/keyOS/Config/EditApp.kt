package tech.DevAsh.KeyOS.Config

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.activity_edit_app.*
import kotlinx.android.synthetic.main.pref_dialog_grid_size.view.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.KeyOS.Helpers.HelperVariables

class EditApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_app)
        loadView()
        onClick()
    }

    private fun loadView(){
        hideIcon.isChecked = HelperVariables.selectedEditedApp?.hideShortcut!!
        appIcon.setImageDrawable(HelperVariables.selectedEditedApp?.icon)
        appName.text = HelperVariables.selectedEditedApp?.appName
        appPackageName.text = HelperVariables.selectedEditedApp?.packageName
        time.text = HelperVariables.selectedEditedApp?.hourPerDay
    }

    fun onClick(){
        blockActivities.setOnClickListener {
            startActivity(Intent(this, ActivityList::class.java))
        }

        appInfo.setOnClickListener {
            openAppInfo()
        }

        done.setOnClickListener {
            saveData()
        }

        timer.setOnClickListener{
            AlertHelper.showSnackbar("Not supported in early access", this)

            //            setTimer()
        }

        back.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun setTimer(){
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder (this)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.pref_dialog_grid_size, null)

        loadDialogView(view = dialogView)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.setOnDismissListener {
            time.text = getTime(dialogView)
        }
        alertDialog.show()
    }

    private fun getTime(view: View):String{
        val numRowsPicker = view.findViewById<NumberPicker>(R.id.rowsPicker)
        val numColumnsPicker = view.findViewById<NumberPicker>(R.id.columnsPicker)

        var hour = "${numRowsPicker.value}"
        var min = "${numColumnsPicker.value}"

        if(hour.length==1) hour = "0${hour}"
        if(min.length==1) min = "0${min}"

        if(hour=="24") min = "00"

        return "$hour:$min"

    }

    private fun loadDialogView(view: View){

       val numRowsPicker = view.findViewById<NumberPicker>(R.id.rowsPicker)
       val numColumnsPicker = view.findViewById<NumberPicker>(R.id.columnsPicker)

        view.textView5.text = ":"
        view.firstHeading.text = "Hours"
        view.secondHeading.text = "Minutes"



        numRowsPicker.minValue = 0
        numRowsPicker.maxValue = 24
        numColumnsPicker.minValue = 0
        numColumnsPicker.maxValue = 59

        numRowsPicker.value = HelperVariables.selectedEditedApp?.hourPerDay!!.split(":")[0].toInt()
        numColumnsPicker.value = HelperVariables.selectedEditedApp?.hourPerDay!!.split(":")[1].toInt()

    }


    private fun saveData(){
        HelperVariables.selectedEditedApp?.hideShortcut = hideIcon.isChecked
        HelperVariables.selectedEditedApp?.hourPerDay = time.text.toString()
        UserContext.user!!.editedApps.remove(HelperVariables.selectedEditedApp)
        UserContext.user!!.editedApps.add(HelperVariables.selectedEditedApp)
        RealmHelper.updateUser(UserContext.user!!)
        super.onBackPressed()
        super.onBackPressed()
    }

    private fun openAppInfo(){
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:${HelperVariables.selectedEditedApp?.packageName}")
        startActivity(i)
    }
}