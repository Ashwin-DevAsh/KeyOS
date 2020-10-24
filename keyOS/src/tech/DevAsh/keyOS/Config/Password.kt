package tech.DevAsh.KeyOS.Config

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.android.launcher3.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.keyOS.activity_password.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.KeyOS.Helpers.UiHelper
import tech.DevAsh.keyOS.Api.IMailService
import tech.DevAsh.keyOS.Config.Fragments.OtpVerification
import tech.DevAsh.keyOS.KioskApp
import javax.inject.Inject


class Password : AppCompatActivity() {

    @Inject lateinit var mailService: IMailService

    companion object{
        var isOldPasswordExist:Boolean = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        KioskApp.applicationComponents?.inject(this)
        loadView()
        onClick()
        onTextChangeListener(password, passwordLayout, "password")
        onTextChangeListener(email, emailLayout, "email")
        onTextChangeListener(confirmPassword, confirmPasswordLayout, "confirmPassword")
        onTextChangeListener(oldPassword,oldPasswordLayout,"oldPassword")
    }

    private fun loadView(){
        val emailText = UserContext.user?.recoveryEmail
        email.setText(emailText)
        if(emailText != "" && emailText!=null){
            isOldPasswordExist = true
            heading.text = "Change Your"
            oldPasswordLayout.visibility = View.VISIBLE
        }
    }

    fun onClick(){
        done.setOnClickListener {
             if(validate()){
                 UiHelper.hideKeyboard(this)
                 if (!isOldPasswordExist || email.text.toString() != UserContext.user?.recoveryEmail){
                     Handler().postDelayed(
                             {
                                 sendOtp()
                             },500)
                 }else{
                     save()
                     Handler()
                             .postDelayed({
                                              finish()
                                           },500)
                 }

             }
        }

        back.setOnClickListener {
            super.onBackPressed()
        }

        cancel.setOnClickListener{
            super.onBackPressed()
        }

        password.setOnClickListener {
            passwordLayout.error=null
        }
    }

    private fun sendOtp(){
        OtpVerification(this,email.text.toString()).show(supportFragmentManager,"")
    }

    fun save(){
        UserContext.user?.recoveryEmail = email.text.toString()
        UserContext.user?.password =  password.text.toString()
        RealmHelper.updateUser(UserContext.user!!)
        UiHelper.hideKeyboard(this)

    }

    fun emailError(){
        if(!email.text!!.contains(".") || !email.text!!.contains("@") || email.text!!.length<5 || email.text!!.endsWith(".")){
            emailLayout.error = "Invalid email address"
        }else{
            emailLayout.error=null
        }
    }

    fun passwordError(){
        when {
            password.text!!.length<8 -> {
                passwordLayout.error = "Password must contain min 8 characters"
            }
            isOldPasswordExist && oldPassword.text.toString()==password.text.toString() -> {
                passwordLayout.error = "Old password must not be new password"
            }
            else -> {
                passwordLayout.error = null
            }
        }
    }

    fun oldPasswordError(){
        when {
            oldPassword.text!!.length<8 -> {
                oldPasswordLayout.error = "Password must contain min 8 characters"
            }
            else -> {
                oldPasswordLayout.error = null
            }
        }
    }

    fun confirmPasswordError(){
        if ((password.text.toString() != confirmPassword.text.toString())){
            confirmPasswordLayout.error = "Password not match"
        }else{
            confirmPasswordLayout.error = null
        }
    }

    private fun onTextChangeListener(editText: TextInputEditText, layout: TextInputLayout, type: String){
        editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                when(type){
                    "password"->this@Password.passwordError()
                    "email"->this@Password.emailError()
                    "confirmPassword"->this@Password.confirmPasswordError()
                    "oldPassword"->this@Password.oldPasswordError()
                }
            }
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus && editText.text.isNullOrEmpty()){
                layout.error=null
            }
        }
    }



    private fun validate():Boolean{
        if(isOldPasswordExist && oldPassword.text!!.isEmpty()){
            return false
        }
        else if(email.text!!.isEmpty() || password.text!!.isEmpty() || confirmPassword.text!!.isEmpty()){
            return false
        }else if(!email.text!!.contains(".") || !email.text!!.contains("@") || email.text!!.length<5){
            return false
        }else if(password.text!!.length<8){
            return false
        }else if(password.text.toString() != confirmPassword.text.toString()){
            return false
        }else if(isOldPasswordExist && oldPassword.text.toString()!=UserContext.user!!.password){
            if(!oldPassword.text.isNullOrEmpty()){
                oldPasswordLayout.error="Invalid old password"
            }
            return false
        }else if(isOldPasswordExist && oldPassword.text.toString()==password.text.toString()){
            if(!password.text.isNullOrEmpty()){
                passwordLayout.error="old password must not be new password"
            }
            return false
        }
        return true
    }
}