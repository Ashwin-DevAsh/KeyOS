package tech.DevAsh.keyOS.Config

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.android.launcher3.R
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.android.synthetic.dev.activity_donate.*
import kotlinx.android.synthetic.main.launcher.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Api.IDonationService
import tech.DevAsh.keyOS.Api.IMailService
import tech.DevAsh.keyOS.Api.Request.DonationInfo
import tech.DevAsh.keyOS.Api.Request.SendPassword
import tech.DevAsh.keyOS.Api.Response.BasicResponse
import tech.DevAsh.keyOS.Api.Response.DonationResponse
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.KioskApp

class Donate : AppCompatActivity(), PaymentResultListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        KioskApp.applicationComponents?.inject(this)
        initView()
        onClick()
    }

    private fun onClick(){
        done.setOnClickListener {
            try{
                if(amount.text.toString().isEmpty()){
                    donateLayout.error = "Please enter a valid amount"
                }
                val amount =  amount.text.toString().toDouble()
                if(amount<1){
                    donateLayout.error = "Please enter an amount of at least $1"
                }else{
                    donateLayout.error = ""
                    createOrder(KioskApp.donationService,amount,this)
                }
            }catch (e:Throwable){

                donateLayout.error = "Please enter a valid amount"
            }
        }
    }

    private fun initView(){
        amount.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateAmount()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }


    fun validateAmount(){
        try{
            if(amount.text.toString().isEmpty()){
                donateLayout.error = ""
                return;
            }
          val amount = (amount.text.toString()).toDouble()
          if(amount<1){
              donateLayout.error = "Please enter an amount of at least $1"
          }else{
              donateLayout.error = ""
          }
        }catch (e:Throwable){
            donateLayout.error = "Please enter a valid amount"
        }

    }

    @SuppressLint("HardwareIds")
    fun createOrder(donationService: IDonationService, amount: Double, context: Activity){

        loadingScreen.visibility = View.VISIBLE
        val callback = object: Callback<DonationResponse> {
            override fun onResponse(call: Call<DonationResponse>, response: Response<DonationResponse>) {
                val donationResponse = response.body();
                startPayment(amount,donationResponse?.orderID!!,donationResponse.keyID!!)
            }

            override fun onFailure(call: Call<DonationResponse>, t: Throwable) {}

        }

        val donationInfo= DonationInfo(amount, Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
        donationService.createOrder(donationInfo)?.enqueue(callback)
    }

    private fun startPayment(amount:Double,orderID:String,keyID:String) {

        val activity: Activity = this
        val co = Checkout()
        co.setKeyID(keyID);

        try {
            val options = JSONObject()
            options.put("name", "Donate KeyOS")
            options.put("currency", "INR")
            options.put(
                "amount",
                 amount*100
            )
            options.put("order_id", orderID);
            co.open(activity, options)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    override fun onPaymentSuccess(p0: String?) {

        Handler().postDelayed({
                                  startActivity(Intent(this,DonationResult::class.java))
                                  finish()
                              },1000)



    }

    override fun onPaymentError(p0: Int, p1: String?) {

        loadingScreen.visibility = View.GONE


    }
}