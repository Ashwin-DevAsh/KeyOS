package tech.DevAsh.KeyOS.Services

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.CallBlocker
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.keyOS.Database.Plugins
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.Database.User.user
import tech.DevAsh.keyOS.Helpers.KioskHelpers.BasicSettingsHandler
import tech.DevAsh.keyOS.Helpers.NotificationHelper
import tech.DevAsh.keyOS.Model.AppUsageInfo
import tech.DevAsh.keyOS.Model.Time
import tech.DevAsh.keyOS.Model.TimeExhaustApps
import tech.DevAsh.keyOS.Receiver.KioskReceiver
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class UsageAccessService : Service() {

    private val TAG = this::class.java.simpleName


    var prevActivities = arrayListOf<Intent>()
    var packages: List<ApplicationInfo>? = null
    var mActivityManager: ActivityManager? = null
    var launcher:Intent?=null
    var timeExhaustApps = TimeExhaustApps()



    companion object {
        var runnableCheckActivity: Runnable? = null
        var runnableKillApps:Runnable?=null
        var handlerCheckActivity: Handler? = null
        var handlerKillApps:Handler?=null
        private var isAlive = false
        fun isAlive(context: Context):Boolean{
            return isAlive && PermissionsHelper.isMyLauncherCurrent(context)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationHelper.startAsForegroundNotification(this)
        return START_REDELIVER_INTENT
    }


    override fun onCreate() {
        RealmHelper.init(applicationContext)
        loadData()
        loadHomeScreen()
        createActivityLooper()
        BasicSettingsHandler.createBasicSettingsLooper(this)
        packages = packageManager.getInstalledApplications(0)
        mActivityManager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        createKillAppLooper()
        CallBlocker.createCallBlockerLooper(this)
        resetRotation()
        isAlive = true
        super.onCreate()
    }

    private fun loadHomeScreen(){
        launcher = Intent(Intent.ACTION_MAIN)
        launcher?.addCategory(Intent.CATEGORY_HOME)
        launcher?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        prevActivities.add(launcher!!)
    }


    private fun resetRotation(){
        if(user?.basicSettings?.orientation!=BasicSettings.DontCare){
            Settings.System.putInt(applicationContext.contentResolver,
                                   Settings.System.ACCELEROMETER_ROTATION, 1)
        }
    }

    private fun createActivityLooper(){
        handlerCheckActivity = Handler()
        runnableCheckActivity = Runnable {
            if(isAlive(this)) checkActivity(applicationContext)
            handlerCheckActivity?.postDelayed(runnableCheckActivity!!, 250)
        }
        handlerCheckActivity?.postDelayed(runnableCheckActivity!!, 1000)
    }

    private fun createKillAppLooper(){
        handlerKillApps = Handler()
        runnableKillApps = Runnable {
            if(isAlive) killApp()
            handlerKillApps?.postDelayed(runnableKillApps!!, 250)
        }
        handlerKillApps?.postDelayed(runnableKillApps!!, 1000)
    }


    private fun killApp() {
        try{
            for (packageInfo in packages!!) {
                val app = Apps(packageInfo.packageName)
                if (packageInfo.packageName == packageName || user!!.allowedApps.contains(app) || user!!.allowedServices.contains(
                                app)) {
                    continue
                }
                mActivityManager?.killBackgroundProcesses(packageInfo.packageName)
            }

            for(i in timeExhaustApps.blockedApps){
                mActivityManager?.killBackgroundProcesses(i)
            }

        }catch (e: Throwable){}

    }


    private fun loadData(){
        try {
            User.getUsers(this)
            if(user!!.singleApp!=null){
                user?.allowedApps?.add(user?.singleApp)
            }
        }catch (e: Throwable){

        }
    }

    override fun onDestroy() {
        isAlive = false
        CallBlocker.killCallBlockerLooper()
        BasicSettingsHandler.killBasicSettingsLooper(this)
        handlerCheckActivity!!.removeCallbacksAndMessages(runnableCheckActivity!!)
        handlerKillApps!!.removeCallbacks(runnableKillApps!!)
        handlerKillApps= null
        handlerCheckActivity = null
        super.onDestroy()
    }

    var appName: String?=null
    var className: String?=null

    private fun checkActivity(context: Context) {
        val mUsageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 30,
                                                         System.currentTimeMillis() + 30 * 1000)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
        }

        if(event.packageName!=null && event.className!=null){
             appName = event.packageName
             className = event.className
        }

        Log.d(TAG, "checkActivity: $appName $className\"")

        if(appName==null || className==null){
            return
        }

        if(blockRecentScreen(className)){
            return
        }
        if(isAllowedPackage(appName, className)){
            Handler().post{
                if(prevActivities.last().component?.packageName!=appName){
                    if(appName==packageName){
                        prevActivities.add(launcher!!)
                    }else{
                        val intent = packageManager.getLaunchIntentForPackage(appName!!)
                        if(intent!=null){
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            prevActivities.add(intent)
                        }
                    }
                }
            }
        }else{
            block()
        }
    }

    private fun blockRecentScreen(className: String?):Boolean{
        if(className.toString().contains("recent")){
            Handler().post {
                showAppBlockAlertDialog()
                startActivity(launcher)
                appName=null
                this.className=null
            }
            Handler().postDelayed({
                                      KioskReceiver.sendBroadcast(this,
                                                                  KioskReceiver.REMOVE_ALERT_DIALOG)
                                  }, 2000)
            Log.d(TAG, "blockRecentScreen: $className")
            return true
        }
        return false
    }

    private fun isAllowedPackage(appName: String?, className: String?):Boolean{
        val app = Apps(appName)

        if(appName==packageName ||
           Apps.exceptions.contains(appName) ||
           Apps.exceptions.contains(className) ||
           try{user.allowedPlugins?.contains(Plugins("", "",className))!!}catch (e:Throwable){false}){

            return true
        }

        val serviceIndex = user?.allowedServices?.indexOf(app)
        val appIndex = user?.allowedApps?.indexOf(app)
        val editedAppIndex = user?.editedApps?.indexOf(app)

        if(serviceIndex!=-1){
            return true
        }

        if(appIndex==-1){
            showAppBlockAlertDialog()
            return false
        }

        if(editedAppIndex==-1){
            return true
        }

        val singleApp = user!!.singleApp ?: Apps("")

        if(singleApp.packageName!=appName && !user!!.editedApps[editedAppIndex!!]!!.hourPerDay.startsWith(
                        "24")){

            val allowedTime = Time.fromString(user!!.editedApps[editedAppIndex]!!.hourPerDay)
            val usageTime = getUsageStatistics(this, appName)

            if(usageTime!=null && !allowedTime.isGreaterThan(usageTime)){
                Handler().post {
                    prevActivities.filter {
                        it.component?.packageName!=appName!!
                    }
                    timeExhaustApps.blockedApps.add(appName!!)
                }
                AlertHelper.showTimerAlertDialog(this, appName)
                return false
            }
        }

        if (user!!.editedApps[editedAppIndex!!]!!.blockedActivities.contains(className)){
            showAppBlockAlertDialog()
            return false
        }
        return true
    }


    private fun showAppBlockAlertDialog() {
        KioskReceiver.sendBroadcast(this, KioskReceiver.SHOW_ALERT_DIALOG)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //create an intent that you want to start again.
        val intent = Intent(applicationContext,
                            UsageAccessService::class.java)
        val pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 500] =
                pendingIntent
        super.onTaskRemoved(rootIntent)
    }


    private fun getUsageStatistics(context: Context, appName: String?) : Time? {
        val mUsageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        var currentEvent: UsageEvents.Event?
        val allEvents : ArrayList<UsageEvents.Event> = ArrayList()
        val map : HashMap<String, AppUsageInfo> =  HashMap()
        val date: Calendar = GregorianCalendar()
        date.set(Calendar.HOUR_OF_DAY, 0)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        date.set(Calendar.MILLISECOND, 0)
        val start = date.timeInMillis
        val end = System.currentTimeMillis()
        if(timeExhaustApps.startTime==start){
            if(timeExhaustApps.blockedApps.contains(appName)){
                val time = Time()
                time.hour = 24
                return time
            }
        }else{
            timeExhaustApps.startTime=start
            timeExhaustApps.blockedApps.clear()
        }

        val usageEvents = mUsageStatsManager.queryEvents(start, end)


        while (usageEvents.hasNextEvent()) {
            currentEvent =  UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            if(currentEvent.packageName==appName)
            if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                currentEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND ) {
                allEvents.add(currentEvent)
                val key = currentEvent.packageName
                if (map[key] ==null)
                    map[key] =  AppUsageInfo(key)
            }
        }

        var lastEvent:UsageEvents.Event? = null;

        for ( i in 0 until allEvents.size-1){
           val E0= allEvents[i]
           val E1= allEvents[i + 1]
            if (E0.eventType == 1 && E1.eventType == 2){
                val diff = E1.timeStamp - E0.timeStamp
                if(map[E0.packageName]==null){
                    map[E0.packageName]?.timeInForeground= diff
                }else{
                    map[E0.packageName]!!.timeInForeground+= diff
                }
            }
            lastEvent = E1

        }
        var diff:Long = 0
        if(lastEvent?.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND){
            diff = System.currentTimeMillis() - lastEvent.timeStamp
        }
        val time = map[appName]?.timeInForeground ?: return null
        return Time.convertLongToTime(time + diff)
    }


    private fun block() {
        try {
            startActivity(prevActivities.last())
        }catch (e: Throwable){
            startActivity(launcher)
        }finally {
            Handler().postDelayed({
                                      KioskReceiver.sendBroadcast(
                                              this,
                                              KioskReceiver.REMOVE_ALERT_DIALOG)
                                  }, 2000)
            Log.d(TAG, "block: $appName")
            appName=null
            className=null
        }
    }
}

