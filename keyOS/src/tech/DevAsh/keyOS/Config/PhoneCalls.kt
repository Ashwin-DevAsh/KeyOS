package tech.DevAsh.KeyOS.Config

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.activity_phone_calls.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
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
    }


    private fun onClick(){

        back?.setOnClickListener {
            onBackPressed()
        }

        cancel.setOnClickListener {
            onBackPressed()
        }

        done.setOnClickListener {
            saveData()
        }


        allowCalls.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                options.alpha = 1f
            }else{
                options.alpha = 0.25f
            }
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
        finish()
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