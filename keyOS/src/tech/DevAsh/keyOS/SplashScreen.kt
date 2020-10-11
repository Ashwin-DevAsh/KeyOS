package tech.DevAsh.KeyOS

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import tech.DevAsh.KeyOS.Config.Settings
import com.android.launcher3.R
import io.realm.Realm
import io.realm.RealmList
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.keyOS.Database.Calls
import tech.DevAsh.keyOS.Database.User


class SplashScreen : AppCompatActivity() {
    var loadAppsAndServices = LoadAppsAndServices(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        RealmHelper.init(this)
        Handler().post{
            getUsers()
            loadAppsAndServices.execute()
        }
        openActivity()
    }

    private fun openActivity(){
        Handler().postDelayed({
            startActivity(Intent(this, Settings::class.java))
            finish()
        }, 2000)
    }

    companion object{
     fun getUsers(){
        try {
            UserContext.user = Realm.getDefaultInstance()
                .copyFromRealm(Realm.getDefaultInstance().where(User::class.java).findFirst()!!)
        } catch (e: Throwable){
            Realm.getDefaultInstance().executeTransactionAsync{
                UserContext.user = User(
                        RealmList<Apps>(),
                        RealmList<Apps>(),
                        BasicSettings(),
                        Calls(),
                        "",
                        "1234")
                it.insertOrUpdate(UserContext.user!!)
                UserContext.user = Realm.getDefaultInstance()
                    .copyFromRealm(Realm.getDefaultInstance().where(User::class.java).findFirst()!!)
            }
        }
     }
    }
}

class LoadAppsAndServices(val context: Context) :AsyncTask<Any,Any,Any>(){
    override fun doInBackground(vararg params: Any?): Any {
        clearOldData()
        loadAppsAndServices(context)
        return true
    }

    private fun clearOldData(){
        AppsContext.allService.clear()
        AppsContext.allowedService.clear()
        AppsContext.allowedApps.clear()
        AppsContext.allApps.clear()
    }

    private fun loadAppsAndServices(context: Context){
        val packages = context.packageManager.getInstalledPackages( PackageManager.GET_ACTIVITIES)
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
        if(UserContext.user!!.allowedServices.contains(apps)){
            AppsContext.allowedService.add(apps)
        }
    }

    private fun addApp(_app: Apps){

        try {
            val index =  UserContext.user!!.editedApps.indexOf(_app)
            val editApp = UserContext.user!!.editedApps[index]!!
            _app.update(editApp)
        }catch (e:Throwable){}


        if(UserContext.user!!.allowedApps.contains(_app)){
            AppsContext.allowedApps.add(_app)
        }
        AppsContext.allApps.add(_app)

    }

}