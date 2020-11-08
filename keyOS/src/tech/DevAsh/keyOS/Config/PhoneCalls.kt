package tech.DevAsh.KeyOS.Config

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.activity_phone_calls.*
import kotlinx.android.synthetic.keyOS.header_contact_listtile.view.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.Calls


class PhoneCalls : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_calls)
        onClick()
        loadView()
    }

    private fun loadView(){
        allowIncoming.isChecked = UserContext.user?.calls!!.allowIncoming
        allowOutgoing.isChecked = UserContext.user?.calls!!.allowOutgoing
        allowCalls.isChecked =  UserContext.user?.calls!!.allowCalls
        automaticWhitelist.isChecked = UserContext.user?.calls!!.automaticWhitelist
        if(!UserContext.user?.calls!!.allowCalls){
            options.alpha = 0.25f
        }

        if(BuildConfig.IS_PLAYSTORE_BUILD && Build.VERSION.SDK_INT > 25) {
            playstoreCover.visibility = View.VISIBLE
        }else{
            playstoreCover.visibility = View.GONE
        }
    }


    private fun onClick(){

        playstoreCover.setOnClickListener {
            AlertHelper.showToast("Not supported in playstore version", this)
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
                UserContext.user!!.calls.whitelistCalls=false
                UserContext.user!!.calls.blackListCalls=false
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
           UserContext.user!!.allowedServices.add(Apps("com.android.incallui"))
       } else{
           UserContext.user!!.allowedServices.removeAll(arrayListOf(Apps ("com.android.incallui")))
       }
        println( UserContext.user!!.allowedServices)
       UserContext.user!!.calls= Calls(
           allowCalls.isChecked,
           allowIncoming.isChecked,
           allowOutgoing.isChecked,
           UserContext.user!!.calls.whitelistCalls,
           UserContext.user!!.calls.blackListCalls,
           automaticWhitelist.isChecked,
           UserContext.user!!.calls.whiteListContacts,
           UserContext.user!!.calls.blacklistContacts
                                      )
        RealmHelper.updateUser(UserContext.user!!)
    }

    override fun onResume() {
        loadView()
        super.onResume()
    }

    override fun onBackPressed() {
        saveData()
        super.onBackPressed()
    }

}