package tech.DevAsh.keyOS.Config

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.activity_web_filter.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext

class WebFilter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_filter)
        loadView()
        onClick()
    }

    fun onClick(){
        enableWebFilter.setOnCheckedChangeListener { _, isChecked ->
            UserContext.user!!.webFilter.isEnabled = isChecked
            if(isChecked){
                switchStatus.text = "ON"
                options.alpha = 1f
            }else{
                switchStatus.text = "OFF"
                options.alpha = 0.25f
            }
        }
        blockAdultWebsites.setOnCheckedChangeListener{_,isChecked->
            UserContext.user!!.webFilter.shouldBlockAdultSites = isChecked
        }

        whitelist.setOnClickListener {
            WebsiteList.websiteListType = WebsiteList.Companion.WebsiteListType.WHITELIST
            startActivity(Intent(this,WebsiteList::class.java))
        }

        blacklist.setOnClickListener {
            WebsiteList.websiteListType = WebsiteList.Companion.WebsiteListType.BLACKLIST
            startActivity(Intent(this,WebsiteList::class.java))
        }

        back.setOnClickListener {
            super.onBackPressed()
        }
    }


    fun loadView(){
        enableWebFilter.isChecked = UserContext.user!!.webFilter.isEnabled
        blockAdultWebsites.isChecked = UserContext.user!!.webFilter.shouldBlockAdultSites
        if(UserContext.user!!.webFilter.isEnabled){
            switchStatus.text = "ON"
            options.alpha = 1f
        }else{
            switchStatus.text = "OFF"
            options.alpha = 0.25f
        }
    }

    override fun onBackPressed() {
        RealmHelper.updateUser(UserContext.user!!)
        super.onBackPressed()
    }
}