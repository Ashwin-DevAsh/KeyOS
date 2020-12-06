package tech.DevAsh.KeyOS

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.TextPaint
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import tech.DevAsh.KeyOS.Config.Settings
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.User
import java.util.*


class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmHelper.init(this)
        if(UserContext.user==null){User.getUsers(this)}
        setContentView(R.layout.activity_splash_screen)
        openActivity()
        loadView()
    }

    private fun openActivity(){

        println("Agree =" + UserContext.user!!.isEndUserLicenceAgreementDone)
        Handler().postDelayed({
                                  startActivity(Intent(this, Settings::class.java))
                                  overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                  finish()
                              }, 1000)
    }

    fun loadView(){


    }


}

class LoadAppsAndServices(val context: Context) :AsyncTask<Any, Any, Any>(){
    override fun doInBackground(vararg params: Any?): Any {
        clearOldData()
        loadAppsAndServices(context)
        sortAll()
        return true
    }

    fun sortAll(){
        sort(AppsContext.allService)
        sort(AppsContext.allApps)

    }

    private fun clearOldData(){
        AppsContext.allService.clear()
        AppsContext.allApps.clear()
    }

    private fun loadAppsAndServices(context: Context){
        val packages = context.packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        for (pkg in packages) {
            val intent =  context.packageManager.getLaunchIntentForPackage(pkg.packageName)
            val app = Apps(
                    pkg.packageName,
                    pkg.applicationInfo.loadIcon(context.packageManager),
                    pkg.applicationInfo.loadLabel(context.packageManager).toString(),
                    pkg
                          )
            if(intent==null){
                addService(app)
            }else{
                addApp(app)
            }
        }
    }

    private fun addService(apps: Apps){
        AppsContext.allService.add(apps)
    }

    private fun addApp(_app: Apps){

        if(_app.packageName=="com.android.settings" || _app.packageName==context.packageName){
            return
        }

        try {
            val index =  UserContext.user!!.editedApps.indexOf(_app)
            val editApp = UserContext.user!!.editedApps[index]!!
            _app.update(editApp)
        }catch (e: Throwable){}

        AppsContext.allApps.add(_app)



    }

    fun sort(appsList: List<Apps>){
        Collections.sort(appsList) { o1, o2 -> o1!!.appName.compareTo(o2!!.appName) }
    }

}