package tech.DevAsh.KeyOS.Services

import android.app.ActivityManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.icu.util.Calendar
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import io.realm.Realm
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.keyOS.Database.User
import java.lang.reflect.Method


class UsageAccessService : Service() {

    private var prevActivities = arrayListOf("com.DevAsh.demo")

    val event = UsageEvents.Event()
    var appName:String?=null
    var className:String?=null

    var packages: List<ApplicationInfo>? = null
    private var mActivityManager: ActivityManager? = null


    companion object {
        var user: User?= User()
        var runnableCheckActivity: Runnable? = null
        var runnableCheckBasicSettings:Runnable? = null
        var runnableKillApps:Runnable?=null

        var handlerCheckActivity: Handler? = null
        var handlerCheckBasicSettings:Handler?=null
        var handlerKillApps:Handler?=null

    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        RealmHelper.init(applicationContext)
        loadData()
        createActivityLooper()
        createBasicSettingsLooper()
        packages = packageManager.getInstalledApplications(0)
        mActivityManager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        createKillAppLooper()
    }

    private fun createActivityLooper(){
        handlerCheckActivity = Handler()
        runnableCheckActivity = Runnable {
            checkActivity(applicationContext)
            handlerCheckActivity?.postDelayed(runnableCheckActivity!!, 250)
        }
        handlerCheckActivity?.postDelayed(runnableCheckActivity!!, 1000)
    }

    private fun createKillAppLooper(){
        handlerKillApps = Handler()
        runnableKillApps = Runnable {
            killApp()
            handlerKillApps?.postDelayed(runnableKillApps!!, 250)
        }
        handlerKillApps?.postDelayed(runnableKillApps!!, 1000)
    }

    private fun createBasicSettingsLooper(){
        handlerCheckBasicSettings = Handler()
        runnableCheckBasicSettings = Runnable {
            checkBasicSettings(applicationContext)
            handlerCheckActivity?.postDelayed(runnableCheckBasicSettings!!, 250)
        }
        handlerCheckBasicSettings?.postDelayed(runnableCheckBasicSettings!!, 1000)
    }

    private fun killApp() {
        try{
            for (packageInfo in packages!!) {
                val app = Apps(packageInfo.packageName)
                if (packageInfo.packageName == packageName || user!!.allowedApps.contains(app) || user!!.allowedServices.contains(app)) {
                    continue
                }
                mActivityManager?.killBackgroundProcesses(packageInfo.packageName)
            }
        }catch (e:Throwable){}

    }


    private fun loadData(){
        try {

            user = Realm.getDefaultInstance()
                    .copyFromRealm(Realm
                                           .getDefaultInstance()
                                           .where(User::class.java)
                                           .findFirst()!!)
        }catch (e: Throwable){}
    }

    override fun onDestroy() {
        handlerCheckActivity!!.removeCallbacksAndMessages(runnableCheckActivity!!)
        handlerCheckBasicSettings!!.removeCallbacksAndMessages(runnableCheckBasicSettings!!)
        handlerCheckActivity = null
        handlerCheckBasicSettings = null
        super.onDestroy()
    }

    private fun checkActivity(context: Context) {
        val mUsageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 30,
                                                         System.currentTimeMillis() + 10 * 1000)
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
        }

        if(UsageEvents.Event.MOVE_TO_FOREGROUND==event.eventType){
             appName = event.packageName
             className = event.className
        }
        if(isAllowedPackage(appName, className)){
            if(appName!=null && prevActivities.last()!=appName){
                prevActivities.add(appName!!)
            }
        }else{
           if(appName!=null) block(appName)
        }

    }

    private fun isAllowedPackage(appName: String?, className: String?):Boolean{
        val app = Apps(appName)

        println("$appName $className")

        if(appName==packageName){
            return true
        }

        val serviceIndex = user?.allowedServices?.indexOf(app)
        val appIndex = user?.allowedApps?.indexOf(app)
        val editedAppIndex = user?.editedApps?.indexOf(app)

        if(serviceIndex!=-1){
            return true
        }

        if(appIndex==-1){
            return false
        }

        if(editedAppIndex==-1){
            return true
        }

        val allowedTime = Time.fromString(user!!.editedApps[editedAppIndex!!]!!.hourPerDay)
        val usageTime = getUsageStatistics(this, appName)

        println("$allowedTime $usageTime")

        if(!allowedTime.isGreaterThan(usageTime!!)){
            prevActivities = arrayListOf("com.DevAsh.demo")
            return false
        }

        if (user!!.editedApps[editedAppIndex]!!.blockedActivities.contains(className)){
            return false
        }



        return true
    }




    private fun getUsageStatistics(context: Context, packageName: String?) :Time? {

        val mUsageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -5)

        val stats: List<UsageStats> = mUsageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                cal.timeInMillis,
                System.currentTimeMillis())


        val statCount = stats.size
        for (i in 0 until statCount) {
            val pkgStats: UsageStats = stats[i]
            println(packageName)
            if (pkgStats.packageName==packageName){
                return (convertLongToTime(pkgStats.totalTimeInForeground))
            }

        }

        return null

    }

    private fun convertLongToTime(milliSeconds: Long):Time{
        val SECOND = 1000
        val MINUTE = 60 * SECOND
        val HOUR = 60 * MINUTE
        val DAY = 24 * HOUR
        val time = Time()
        val text = StringBuffer("")
        var ms = milliSeconds
        if (ms > DAY) {
            time.day = ms / DAY
            ms %= DAY.toLong()
        }
        if (ms > HOUR) {
            time.hour=ms / HOUR
            ms %= HOUR.toLong()
        }
        if (ms > MINUTE) {
            time.minute = ms / MINUTE
            ms %= MINUTE.toLong()
        }
        if (ms > SECOND) {
            time.seconds = ms / SECOND
            ms %= SECOND.toLong()
        }
        text.append("$ms ms")
        return time
    }




    private fun block(className: String?){
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

class Time{
    var day:Long?=0
    var hour:Long?=0
    var minute:Long?=0
    var seconds:Long?=0

    override fun toString(): String {
        return "Time(day=$day, hour=$hour, minute=$minute, seconds=$seconds)"
    }

    companion object {
        fun fromString(string: String):Time{
            val time = Time()
            time.seconds = 0
            time.hour = string.split(":")[0].toLong()
            time.minute = string.split(":")[1].toLong()
            time.seconds = 0
            return time
        }
    }


    fun isGreaterThan(time: Time):Boolean{


        if(this.hour!!>time.hour!!){
            print("$hour<${time.hour}}")
            return true
        }

        if(this.minute!!>time.minute!!){
            print("$minute<${time.minute}}")
            return true
        }

        return false
    }

}