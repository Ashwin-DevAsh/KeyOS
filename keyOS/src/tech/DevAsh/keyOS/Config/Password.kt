package tech.DevAsh.KeyOS.Config

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.activity_password.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.KeyOS.Helpers.UiHelper


class Password : AppCompatActivity() {
    companion object{
        var isOldPasswordExist:Boolean = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        loadView()
        onClick()
    }

    private fun loadView(){
        val emailText = UserContext.user?.recoveryEmail
        email.setText(emailText)
        if(emailText!=""){
            isOldPasswordExist = true
            heading.text = "Change Your"
            oldPasswordContainer.visibility = View.VISIBLE
        }
    }

    fun onClick(){
        done.setOnClickListener {
             if(validate()){
                 save()
             }
        }

        back.setOnClickListener {
            super.onBackPressed()
        }

        cancel.setOnClickListener{
            super.onBackPressed()
        }
    }

    private fun save(){
        UserContext.user?.recoveryEmail = email.text.toString()
        UserContext.user?.password =  password.text.toString()
        RealmHelper.updateUser(UserContext.user!!)
        UiHelper.hideKeyboard(this)
        Handler().postDelayed({
            finish()
        },500)
    }

    private fun validate():Boolean{
        if(isOldPasswordExist && oldPassword.text.isEmpty()){
            AlertHelper.showError("Invalid credentials", this)
            return false
        }
        else if(email.text.isEmpty() || password.text.isEmpty() || confirmPassword.text.isEmpty()){
            AlertHelper.showError("Invalid credentials",this)
            return false
        }else if(!email.text.contains(".") || !email.text.contains("@") || email.text.length<5){
            AlertHelper.showError("Invalid email address",this)
            return false
        }else if(password.text.length<8){
            AlertHelper.showError("Password should contains at least 8 characters",this)
            return false
        }else if(password.text.toString() != confirmPassword.text.toString()){
            AlertHelper.showError("Password not match",this)
            return false
        }else if(isOldPasswordExist && oldPassword.text.toString()!=UserContext.user!!.password){
            AlertHelper.showError("Invalid Old Password",this)
            return false
        }else if(isOldPasswordExist && oldPassword.text.toString()==password.text.toString()){
            AlertHelper.showError("Old password must not be new password",this)
            return false
        }
        return true
    }
}