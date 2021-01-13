package tech.DevAsh.keyOS

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.activity_error.*
import retrofit2.Call
import retrofit2.Response
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.keyOS.Api.Request.CrashInfo
import tech.DevAsh.keyOS.Api.Response.BasicResponse
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.Helpers.AlertDeveloper

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        reportCrash()
        onClick()
    }

    private fun reportCrash(){
        val error = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, intent)
        Toast.makeText(this,error,Toast.LENGTH_LONG).show()
        AlertDeveloper.sendCrashAlert(this,error)
    }

    fun onClick(){
        exit?.setOnClickListener {
            Kiosk.exitKiosk(this, User.user?.password)
        }
    }
}