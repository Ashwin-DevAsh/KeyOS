package tech.DevAsh.keyOS.Config

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.launcher3.R
import io.realm.RealmList
import kotlinx.android.synthetic.dev.activity_allow_settings_plugins.*
import kotlinx.android.synthetic.dev.activity_settings.*
import tech.DevAsh.KeyOS.Config.ToggleCallback
import tech.DevAsh.keyOS.Config.Adapters.AllowPluginsAdapter
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.Plugins
import tech.DevAsh.keyOS.Database.User

class AllowSettingsPlugins : AppCompatActivity(),ToggleCallback {

    var pluginsAdapter : AllowPluginsAdapter?=null
    var isEnabled = true
    var allowedPlugins = ArrayList<Plugins>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allow_settings_plugins)
        loadData()
        loadAdapter()
        loadView()
        onClick()
    }

    private fun onClick() {
        done.setOnClickListener{
            save()
        }

        back.setOnClickListener{
            super.onBackPressed()
        }

        cancel.setOnClickListener{
            super.onBackPressed()
        }
    }

    private fun loadData(){
        try{
             isEnabled = User.user?.shouldShowSettingsIcon!!
             allowedPlugins = ArrayList(User.user?.allowedPlugins!!)
        }catch(e:Throwable){
            isEnabled = true
            allowedPlugins = ArrayList()
        }
    }

    private fun loadAdapter(){
        val plugins =  ArrayList(Plugins.allPlugins)
        plugins.add(0,Plugins("",""))
        pluginsAdapter = AllowPluginsAdapter(
                plugins,
                allowedPlugins,
              this,
                "Selected plugins are available in settings option in KeyOS home screen" ,
                this)
    }

    fun loadView(){

        val layoutManager = LinearLayoutManager(this)
        pluginsContainer.layoutManager = layoutManager
        pluginsContainer.adapter = pluginsAdapter
    }

    fun save(){
        try{
            val allowedPlugins = getRealmList(pluginsAdapter?.allowedItems!!)
            User.user?.shouldShowSettingsIcon = getToggleState()
            User.user?.allowedPlugins = allowedPlugins
        }catch(e:Throwable){}
        super.onBackPressed()

    }

    private fun getRealmList(list: ArrayList<Plugins>): RealmList<Plugins> {
        val allowedItems = RealmList<Plugins>()
        for (i in list) {
            if (i.pluginName != "") {
                allowedItems.add(i)
            }
        }
        return allowedItems
    }


    override fun turnOn() {
        pluginsAdapter?.notifyDataSetChanged()
        isEnabled = true
    }

    override fun turnOff() {
        pluginsAdapter?.notifyDataSetChanged()
        isEnabled = false
    }

    override fun getToggleState(): Boolean {
      return isEnabled
    }
}