package tech.DevAsh.Launcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.android.launcher3.R
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.NotificationBlocker
import tech.DevAsh.keyOS.Database.User

class KioskSingleAppLauncher : AppCompatActivity() {

    lateinit var singleAppIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RealmHelper.init(this)
        User.getUsers()
        Kiosk.startKiosk(this)

        singleAppIntent = packageManager.getLaunchIntentForPackage(UserContext.user!!.singleApp.packageName)!!
        setContentView(R.layout.activity_kiosk_single_app_launcher)
    }

    override fun onResume() {
        Handler().post {
            startActivity(singleAppIntent)
        }
        super.onResume()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        NotificationBlocker.collapseNow(this)
        super.onWindowFocusChanged(hasFocus)
    }




}