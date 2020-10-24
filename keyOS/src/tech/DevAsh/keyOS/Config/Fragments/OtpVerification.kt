package tech.DevAsh.keyOS.Config.Fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.keyOS.fragment_otp_verification.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.DevAsh.KeyOS.Config.Password
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Api.Request.EmailVerification
import tech.DevAsh.keyOS.Api.Response.BasicResponse
import tech.DevAsh.keyOS.KioskApp
import kotlin.random.Random.Default.nextInt


class OtpVerification(val password: Password, val email: String) : BottomSheetDialogFragment() {


    private var emailVerification:EmailVerification?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_otp_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getOtpVerification()
        onClick()
    }


    fun onClick(){
        done.setOnClickListener {
            checkOtp()
        }
    }


    private fun checkOtp(){
        if(emailVerification?.otp==otp.text.toString()){
            this.dismiss()
            Handler().postDelayed({
                                      password.save()
                    },500)
        }else if(otp.text.toString().isNotEmpty()){
            otpLayout.error = "Invalid Otp"
        }
    }


    private fun getOtpVerification(){
        emailVerification = EmailVerification()
        emailVerification?.email = email
        emailVerification?.otp = "${nextInt(1000,9999)}"
        requireActivity().KioskApp.mailService.emailVerification(emailVerification!!)?.enqueue(otpVerificationCallBack)
    }

    private var otpVerificationCallBack = object : Callback<BasicResponse>{
        override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
            println("response = " + response.body())
        }

        override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
            println(t.printStackTrace())
        }

    }




}