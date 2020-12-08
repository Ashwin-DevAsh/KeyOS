package tech.DevAsh.KeyOS.Helpers.KioskHelpers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.activity_password_prompt.*
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Database.User


class PasswordPrompt : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_prompt)
        onClick()
    }

    fun onClick(){
        settings.setOnClickListener {
//            if(checkPassword())
//                Kiosk.openKioskSettings(this)
        }

        exit.setOnClickListener {
//            if(checkPassword())
//                Kiosk.exitKiosk(this)
        }
    }

    private fun checkPassword():Boolean{
        return if(User.user!!.password==password.text.toString()){
            true
        }else{
            AlertHelper.showError("Invalid Password", this)
            false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


}