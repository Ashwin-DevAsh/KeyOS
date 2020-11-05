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
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import tech.DevAsh.KeyOS.Config.Settings
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.Apps
import java.util.*


class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        RealmHelper.init(this)
        openActivity()
        loadView()

    }

    private fun openActivity(){
        Handler().postDelayed({
                                  startActivity(Intent(this, Settings::class.java))
                                  overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                  finish()
                              }, 1000)
    }

    fun loadView(){
        val textView = findViewById<View>(R.id.greetings) as TextView

        val paint: TextPaint = textView.paint
        val width: Float = paint.measureText(textView.text.toString())

        val textShader: Shader = LinearGradient(0f, 0f, width, textView.textSize, intArrayOf(
                Color.parseColor("#F97C3C"),
                Color.parseColor("#FDB54E"),
                Color.parseColor("#64B678"),
                Color.parseColor("#478AEA"),
                    Color.parseColor("#8446CC")), null, Shader.TileMode.CLAMP)
        textView.paint.shader = textShader
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