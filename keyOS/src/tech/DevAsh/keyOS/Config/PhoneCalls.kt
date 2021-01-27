package tech.DevAsh.KeyOS.Config

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.activity_phone_calls.*
import kotlinx.android.synthetic.dev.header_contact_listtile.view.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.Calls
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.Helpers.UpdateOriginalApk


class PhoneCalls : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_calls)
        onClick()
        loadView()
    }

    private fun loadView(){
        try{
            allowIncoming.isChecked = User.user?.calls!!.allowIncoming
            allowOutgoing.isChecked = User.user?.calls!!.allowOutgoing
            allowCalls.isChecked =  User.user?.calls!!.allowCalls
            automaticWhitelist.isChecked = User.user?.calls!!.automaticWhitelist
            if(!User.user?.calls!!.allowCalls){
                options.alpha = 0.25f
            }

            if(BuildConfig.IS_PLAYSTORE_BUILD && Build.VERSION.SDK_INT > 25) {
                playstoreCover.visibility = View.VISIBLE
            }else{
                playstoreCover.visibility = View.GONE
            }
        }catch (e:Throwable){}

    }


    private fun onClick(){

        playstoreCover.setOnClickListener {
            UpdateOriginalApk.showAlertDialog(this)
        }

        back?.setOnClickListener {
            onBackPressed()
        }

        cancel.setOnClickListener {
            onBackPressed()
        }

        done.setOnClickListener {
            saveData()
            finish()
        }

       subHeading.setOnClickListener {
            AllowApps.type= AllowApps.Companion.Types.ALLOWSERVICES
            startActivity(Intent(this, AllowApps::class.java))
        }


        allowCalls.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                options.alpha = 1f
            }else{
                options.alpha = 0.25f
            }
            saveData()
        }

        blacklist.setOnClickListener {
           openBlacklist()
        }

        whitelist.setOnClickListener {
           openWhitelist()
        }

        automaticWhitelist.setOnCheckedChangeListener{_,isChecked->
            if(isChecked){
                User.user!!.calls.whitelistCalls=false
                User.user!!.calls.blackListCalls=false
            }
            saveData()
        }

        allowIncoming.setOnCheckedChangeListener{ _, _ ->
            saveData()
        }

        allowOutgoing.setOnCheckedChangeListener { _, _ ->
            saveData()
        }

    }

    private fun openWhitelist(){
        ContactList.isBlackList = false
        startActivity(Intent(this,ContactList::class.java))
    }

    private fun openBlacklist(){
        ContactList.isBlackList = true
        startActivity(Intent(this,ContactList::class.java))
    }

    private fun saveData(){
       if(allowCalls.isChecked){
           User.user!!.allowedServices.add(Apps("com.android.incallui"))
       } else{
           User.user!!.allowedServices.removeAll(arrayListOf(Apps ("com.android.incallui")))
       }
        println( User.user!!.allowedServices)
        User.user!!.calls= Calls(
           allowCalls.isChecked,
           allowIncoming.isChecked,
           allowOutgoing.isChecked,
           User.user!!.calls.whitelistCalls,
           User.user!!.calls.blackListCalls,
           automaticWhitelist.isChecked,
           User.user!!.calls.whiteListContacts,
           User.user!!.calls.blacklistContacts
                                      )
        RealmHelper.updateUser(User.user!!)
    }

    override fun onResume() {
        loadView()
        super.onResume()
    }

    override fun onBackPressed() {
        saveData()
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
                                           ) {

        if(requestCode==4){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED ){
                UpdateOriginalApk.update(this)
            }
        }
    }

}