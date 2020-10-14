package tech.DevAsh.KeyOS.Config

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.activity_edit_app.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
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
    }

    private fun saveData(){
        HelperVariables.selectedEditedApp?.hideShortcut = hideIcon.isChecked
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