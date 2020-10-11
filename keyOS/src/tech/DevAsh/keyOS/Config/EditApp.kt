package tech.DevAsh.KeyOS.Config

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.activity_edit_app.*
import tech.DevAsh.KeyOS.Helpers.HelperVariables

class EditApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_app)
        loadView()
        onClick()
    }

    private fun loadView(){
        appIcon.setImageDrawable(HelperVariables.selectedApp?.icon)
        appName.text = HelperVariables.selectedApp?.appName
        appPackageName.text = HelperVariables.selectedApp?.packageName
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

    fun saveData(){
        super.onBackPressed()
    }

    private fun openAppInfo(){
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:${HelperVariables.selectedApp?.packageName}")
        startActivity(i)
    }
}