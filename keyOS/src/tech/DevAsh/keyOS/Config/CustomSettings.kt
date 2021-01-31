package tech.DevAsh.keyOS.Config

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.activity_allow_settings_plugins.*
import tech.DevAsh.keyOS.Config.Adapters.AllowPluginsAdapter
import tech.DevAsh.keyOS.Config.Adapters.PluginAdapter
import tech.DevAsh.keyOS.Database.Plugins
import tech.DevAsh.keyOS.Database.User

class CustomSettings : AppCompatActivity() {


    var pluginsAdapter : PluginAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_settings)
        loadAdapter()
        loadView()

    }

    fun loadView(){
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.BLACK

        val layoutManager = LinearLayoutManager(this)
        pluginsContainer.layoutManager = layoutManager
        pluginsContainer.adapter = pluginsAdapter
    }

    fun loadAdapter(){
        val plugins =  ArrayList<Plugins>()
        User.user.allowedPlugins.forEach{
            if(!plugins.contains(it)){
                plugins.add(it)
            }
        }

         pluginsAdapter = PluginAdapter(plugins,this)
    }
}