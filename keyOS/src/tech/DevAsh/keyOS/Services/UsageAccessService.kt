package tech.DevAsh.KeyOS.Services

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import android.widget.Toast
import tech.DevAsh.KeyOS.Database.RealmHelper
import io.realm.Realm
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.User
import java.lang.reflect.Method


class UsageAccessService : Service() {
    var user: User?=null
    private var prevActivities = arrayListOf("com.DevAsh.demo")


    companion object {
        var runnableCheckActivity: Runnable? = null
        var runnableCheckBasicSettings:Runnable? = null
        var handlerCheckActivity: Handler? = null
        var handlerCheckBasicSettings:Handler?=null
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onCreate() {
        RealmHelper.init(applicationContext)
        loadData()
        createActivityLooper()
        createBasicSettingsLooper()
    }

    private fun createActivityLooper(){
        handlerCheckActivity = Handler()
        runnableCheckActivity = Runnable {
            checkActivity(applicationContext)
            handlerCheckActivity!!.postDelayed(runnableCheckActivity, 250)
        }
        handlerCheckActivity!!.postDelayed(runnableCheckActivity, 1000)
    }

    private fun createBasicSettingsLooper(){
        handlerCheckBasicSettings = Handler()
        runnableCheckBasicSettings = Runnable {
            checkBasicSettings(applicationContext)
            handlerCheckActivity!!.postDelayed(runnableCheckBasicSettings, 250)
        }
        handlerCheckBasicSettings!!.postDelayed(runnableCheckBasicSettings, 1000)
    }

    private fun loadData(){
        try {
            user = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance()
                .where(User::class.java).findFirst()!!)
        }catch (e: Throwable){}
    }

    override fun onDestroy() {
        handlerCheckActivity!!.removeCallbacks(runnableCheckActivity!!)
        handlerCheckBasicSettings!!.removeCallbacks(runnableCheckBasicSettings!!)
        super.onDestroy()
    }

    private fun checkActivity(context: Context) {
        val mUsageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 30,
            System.currentTimeMillis() + 10 * 1000)
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
        }
        if (!TextUtils.isEmpty(event.packageName) && event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            val appName = event.packageName
            val className = event.className
            if(
                isAllowedPackage(appName, className)
            ){
                if(prevActivities.last()!=appName){
                    prevActivities.add(appName)
                }
            }else{
                block(className)
            }
        }
    }

    private fun isAllowedPackage(appName:String, className:String):Boolean{

        if(appName==packageName){
            return true
        }

        val app = Apps(appName)

        if(UserContext.user!!.allowedServices.contains(app)){
            return true
        }

        val appIndex = UserContext.user!!.allowedApps.indexOf(app)
        val editedAppIndex = UserContext.user!!.editedApps.indexOf(app)

        if(appIndex==-1){
            return false
        }

        if(editedAppIndex==-1){
            return true
        }

        if (UserContext.user!!.editedApps[editedAppIndex]!!.blockedActivities.contains(className)){
            return false
        }

        return true
    }

    private fun block(className:String){
        val launcher = Intent(Intent.ACTION_MAIN)
        launcher.addCategory(Intent.CATEGORY_HOME)
        launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            val prev1 = packageManager.getLaunchIntentForPackage(prevActivities[prevActivities.size - 1])
            val prev2 = packageManager.getLaunchIntentForPackage(prevActivities[prevActivities.size - 2])
            when {
                prevActivities.last()==packageName->{
                    throw Exception()
                }
                prev1!=null -> {
                    prev1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    prev1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(prev1)
                }
                prev2!=null -> {
                    prev2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    prev2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(prev2)
                }
                else -> {
                    throw Exception()
                }
            }
        }catch (e: Throwable){
            startActivity(launcher)
        }
        Toast.makeText(applicationContext, "Access Denied : $className", Toast.LENGTH_SHORT).show()
    }


    private fun checkBasicSettings(context: Context){
        checkWifi()
        checkHotspot()

    }

    private fun checkWifi(){
        val wifiManager = this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if(user?.basicSettings?.wifi=="Always on"){
            if(!wifiManager.isWifiEnabled){
                wifiManager.isWifiEnabled = true
            }
        }
        else if(user?.basicSettings?.wifi=="Always off"){
            if(wifiManager.isWifiEnabled){
                wifiManager.isWifiEnabled = false
            }
        }

    }
    private fun checkHotspot(){
        if(user?.basicSettings?.wifi!="Always on"){
            val manager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val method: Method = manager.javaClass.getDeclaredMethod("getWifiApState")
            method.isAccessible = true
            val actualState = method.invoke(manager) as Int
            println(actualState)
            val DISABLED = 11
            val ENABLED = 13
            if(user?.basicSettings?.hotspot=="Always on"){
                if(actualState==DISABLED)
                    turnOnHotspot()
            }else if (user?.basicSettings?.hotspot=="Always off"){
                if(actualState==ENABLED)
                    turnOffHotspot()
            }
        }
    }

    private fun turnOnHotspot() {
    }

    private fun turnOffHotspot() {

    }

}