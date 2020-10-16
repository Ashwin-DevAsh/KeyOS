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
import io.realm.Realm
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.keyOS.Database.User
import java.lang.reflect.Method


class UsageAccessService : Service() {

    private var prevActivities = arrayListOf("com.DevAsh.demo")


    companion object {
        var user: User?=null
        var runnableCheckActivity: Runnable? = null
        var runnableCheckBasicSettings:Runnable? = null
        var handlerCheckActivity: Handler? = null
        var handlerCheckBasicSettings:Handler?=null
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        println("Usage access Service Created")
        RealmHelper.init(applicationContext)
        loadData()
        createActivityLooper()
        createBasicSettingsLooper()
    }

    private fun createActivityLooper(){
        handlerCheckActivity = Handler()
        runnableCheckActivity = Runnable {
            checkActivity(applicationContext)
            handlerCheckActivity?.postDelayed(runnableCheckActivity, 250)
        }
        handlerCheckActivity?.postDelayed(runnableCheckActivity, 1000)
    }

    private fun createBasicSettingsLooper(){
        handlerCheckBasicSettings = Handler()
        runnableCheckBasicSettings = Runnable {
            checkBasicSettings(applicationContext)
            handlerCheckActivity?.postDelayed(runnableCheckBasicSettings, 250)
        }
        handlerCheckBasicSettings?.postDelayed(runnableCheckBasicSettings, 1000)
    }

    private fun loadData(){
        try {

            user = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance()
                                                                    .where(User::class.java)
                                                                    .findFirst()!!)
            println("get User")
        }catch (e: Throwable){}
    }

    override fun onDestroy() {
        println("Usage access Service Destroyed")
        handlerCheckActivity!!.removeCallbacksAndMessages(runnableCheckActivity!!)
        handlerCheckBasicSettings!!.removeCallbacksAndMessages(runnableCheckBasicSettings!!)
        handlerCheckActivity = null
        handlerCheckBasicSettings = null
        user=null
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
                block(packageName)
            }
        }
    }

    private fun isAllowedPackage(appName: String, className: String):Boolean{

        checkAppUsage(appName)
        if(appName==packageName){
            return true
        }

        val app = Apps(appName)

        if(user!!.allowedServices.contains(app)){
            return true
        }

        val appIndex = user!!.allowedApps.indexOf(app)
        val editedAppIndex = user!!.editedApps.indexOf(app)

        if(appIndex==-1){
            return false
        }

        if(editedAppIndex==-1){
            return true
        }

        if (user!!.editedApps[editedAppIndex]!!.blockedActivities.contains(className)){
            return false
        }

        return true
    }



    private fun checkAppUsage(packageName: String){
        val end_time = System.currentTimeMillis()
        val start_time = 0.toLong() //LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()
        getUsageStatistics(start_time, end_time, packageName)
    }

    private fun getUsageStatistics(start_time: Long, end_time: Long, packageName: String) {
        var currentEvent: UsageEvents.Event
        val map: HashMap<String, AppUsageInfo?> = HashMap()
        val sameEvents: HashMap<String, MutableList<UsageEvents.Event>> = HashMap()
        val mUsageStatsManager = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val usageEvents = mUsageStatsManager.queryEvents(start_time, end_time)

        while (usageEvents.hasNextEvent()) {
            currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                val key = currentEvent.packageName
                if (map[key] == null) {
                    map[key] = AppUsageInfo(key)
                    sameEvents[key] = ArrayList()
                }
                sameEvents[key]!!.add(currentEvent)
            }
        }

        for ((_, value) in sameEvents.entries) {
            val totalEvents = value.size
            if (totalEvents > 1) {
                for (i in 0 until totalEvents - 1) {
                    val E0 = value[i]
                    val E1 = value[i + 1]
                    if (E1.eventType == 1 || E0.eventType == 1) {
                        map[E1.packageName]!!.launchCount++
                    }
                    if (E0.eventType == 1 && E1.eventType == 2) {
                        val diff = E1.timeStamp - E0.timeStamp
                        map[E0.packageName]!!.timeInForeground += diff
                    }
                }
            }

            if (value[0].eventType == 2) {
                val diff = value[0].timeStamp - start_time
                map[value[0].packageName]!!.timeInForeground += diff
            }

            if (value[totalEvents - 1].eventType == 1) {
                val diff = end_time - value[totalEvents - 1].timeStamp
                map[value[totalEvents - 1].packageName]!!.timeInForeground += diff
            }
        }
        val timeInMilli = map[packageName]?.timeInForeground
        if(timeInMilli!=null) println("$packageName => ${milliToHour(timeInMilli)}")

    }


    private fun milliToHour(milliseconds: Long):String{
        println(milliseconds)
        val minutes = (milliseconds / (1000 * 60) % 60)
        val hours = (milliseconds / (1000 * 60 * 60) % 24)
        return ("$hours : $minutes")
    }




    private fun block(className: String){
        val launcher = Intent(Intent.ACTION_MAIN)
        launcher.addCategory(Intent.CATEGORY_HOME)
        launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            val prev1 = packageManager.getLaunchIntentForPackage(
                    prevActivities[prevActivities.size - 1])
            val prev2 = packageManager.getLaunchIntentForPackage(
                    prevActivities[prevActivities.size - 2])
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
        if(user?.basicSettings?.wifi==BasicSettings.AlwaysON){
            if(!wifiManager.isWifiEnabled){
                wifiManager.isWifiEnabled = true
            }
        }
        else if(user?.basicSettings?.wifi==BasicSettings.AlwaysON){
            if(wifiManager.isWifiEnabled){
                wifiManager.isWifiEnabled = false
            }
        }
    }

    private fun checkHotspot(){
        if(user?.basicSettings?.wifi!=BasicSettings.AlwaysON){
            val manager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val method: Method = manager.javaClass.getDeclaredMethod("getWifiApState")
            method.isAccessible = true
            val actualState = method.invoke(manager) as Int
            println(actualState)
            val DISABLED = 11
            val ENABLED = 13
            if(user?.basicSettings?.hotspot==BasicSettings.AlwaysON){
                if(actualState==DISABLED)
                    turnOnHotspot()
            }else if (user?.basicSettings?.hotspot==BasicSettings.AlwaysOFF){
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

internal class AppUsageInfo(var packageName: String) {
    var timeInForeground: Long = 0
    var launchCount = 0
}
