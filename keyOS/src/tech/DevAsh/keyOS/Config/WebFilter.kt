package tech.DevAsh.keyOS.Config

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import io.realm.RealmList
import kotlinx.android.synthetic.dev.activity_web_filter.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.Database.WebFilterDB

class WebFilter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_filter)
        try {
            loadView()
        }catch (e: Throwable){
            User.user!!.webFilter =
                  WebFilterDB(false, false, false, RealmList(), RealmList(), false)
            loadView()

        }
        onClick()
    }

    fun onClick(){
        enableWebFilter.setOnCheckedChangeListener { _, isChecked ->
            User.user!!.webFilter.isEnabled = isChecked
            if(isChecked){
                switchStatus.text = getString(R.string.on_caps)
                options.alpha = 1f
            }else{
                switchStatus.text = getString(R.string.off_caps)
                options.alpha = 0.25f
            }
        }
        blockAdultWebsites.setOnCheckedChangeListener{ _, isChecked->
            User.user!!.webFilter.shouldBlockAdultSites = isChecked
        }

        whitelist.setOnClickListener {
            WebsiteList.websiteListType = WebsiteList.Companion.WebsiteListType.WHITELIST
            startActivity(Intent(this, WebsiteList::class.java))
        }

        blacklist.setOnClickListener {
            WebsiteList.websiteListType = WebsiteList.Companion.WebsiteListType.BLACKLIST
            startActivity(Intent(this, WebsiteList::class.java))
        }

        back.setOnClickListener {
            super.onBackPressed()
        }
    }


    fun loadView(){
        enableWebFilter.isChecked = User.user!!.webFilter.isEnabled
        blockAdultWebsites.isChecked = User.user!!.webFilter.shouldBlockAdultSites
        if(User.user!!.webFilter.isEnabled){
            switchStatus.text = getString(R.string.on_caps)
            options.alpha = 1f
        }else{
            switchStatus.text = getString(R.string.off_caps)
            options.alpha = 0.25f
        }
    }

    override fun onBackPressed() {
        RealmHelper.updateUser(User.user!!)
        super.onBackPressed()
    }
}