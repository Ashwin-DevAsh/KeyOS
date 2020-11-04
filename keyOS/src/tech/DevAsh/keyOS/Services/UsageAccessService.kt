package tech.DevAsh.KeyOS.Services

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import com.android.launcher3.R
import io.realm.Realm
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.CallBlocker
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.keyOS.Database.User
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime


class UsageAccessService : Service() {

    private var prevActivities = arrayListOf("com.DevAsh.demo")



    var packages: List<ApplicationInfo>? = null
    private var mActivityManager: ActivityManager? = null


    companion object {
        var user: User?= User()
        var runnableCheckActivity: Runnable? = null
        var runnableCheckBasicSettings:Runnable? = null
        var runnableKillApps:Runnable?=null
        var runnableCallBlocker:Runnable?=null

        var handlerCheckActivity: Handler? = null
        var handlerCheckBasicSettings:Handler?=null
        var handlerKillApps:Handler?=null
        var handlerCallBlocker:Handler?=null

        var isAlive = false

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
        createCallBlockerLooper()
        resetRotation()
        isAlive=true

    }


    private fun resetRotation(){
        if(user?.basicSettings?.orientation!=BasicSettings.DontCare){
            Settings.System.putInt(applicationContext.contentResolver,
                                   Settings.System.ACCELEROMETER_ROTATION, 1)
        }
    }

    private fun createCallBlockerLooper(){
        handlerCallBlocker = Handler()
        runnableCallBlocker = Runnable {

            if(isAlive)runCallBlocker()
            handlerCallBlocker?.postDelayed(runnableCallBlocker!!, 250)
        }
        handlerCallBlocker?.postDelayed(runnableCallBlocker!!, 1000)
    }

    private fun createActivityLooper(){
        handlerCheckActivity = Handler()
        runnableCheckActivity = Runnable {
            if(isAlive) checkActivity(applicationContext)
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

    private fun createBasicSettingsLooper(){
        handlerCheckBasicSettings = Handler()
        runnableCheckBasicSettings = Runnable {
            if(isAlive) checkBasicSettings()
            handlerCheckActivity?.postDelayed(runnableCheckBasicSettings!!, 250)
        }
        handlerCheckBasicSettings?.postDelayed(runnableCheckBasicSettings!!, 1000)
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
        }catch (e: Throwable){}

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
        println("Usage Access destroyed")

        isAlive = false

        handlerCheckActivity!!.removeCallbacksAndMessages(runnableCheckActivity!!)
        handlerCheckBasicSettings!!.removeCallbacksAndMessages(runnableCheckBasicSettings!!)
        handlerCallBlocker!!.removeCallbacks(runnableCallBlocker!!)
        handlerKillApps!!.removeCallbacks(runnableKillApps!!)

        handlerKillApps= null
        handlerCallBlocker=null
        handlerCheckActivity = null
        handlerCheckBasicSettings = null

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

        val appName: String?
        val className: String?

        if(event.packageName!=null && event.className!=null){
             appName = event.packageName
             className = event.className
        }else{
            return
        }

        if(isAllowedPackage(appName, className)){
            if(appName!=null && prevActivities.last()!=appName){
                prevActivities.add(appName)
            }
        }else{
           if(appName!=null) block()
        }

    }

    private fun isAllowedPackage(appName: String?, className: String?):Boolean{
        val app = Apps(appName)


        if(appName==packageName || AppsContext.exceptions.contains(appName) || AppsContext.exceptions.contains(
                        className)){
            return true
        }

        val serviceIndex = user?.allowedServices?.indexOf(app)
        val appIndex = user?.allowedApps?.indexOf(app)
        val editedAppIndex = user?.editedApps?.indexOf(app)

        if(serviceIndex!=-1){
            return true
        }

        if(appIndex==-1){
            Toast.makeText(applicationContext, "Access Denied : $appName", Toast.LENGTH_SHORT).show()
            return false
        }

        if(editedAppIndex==-1){
            return true
        }

        val allowedTime = Time.fromString(user!!.editedApps[editedAppIndex!!]!!.hourPerDay)
        val usageTime = getUsageStatistics(this, appName)

        println("time : $appName $allowedTime $usageTime")

        if(usageTime!=null && !allowedTime.isGreaterThan(usageTime)){
            prevActivities.remove(appName)
            showAlertDialog(this, appName)
            return false
        }

        if (user!!.editedApps[editedAppIndex]!!.blockedActivities.contains(className)){
            Toast.makeText(applicationContext, "Access Denied : $className", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    var alert : AlertDialog?=null

    private fun showAlertDialog(context: Context, appName: String?){


        if(alert!=null){
            if(!alert!!.isShowing){
                alert?.show()
            }
            return
        }

        val dialog =  AlertDialog.Builder(context, R.style.MyProgressDialog);
        dialog.setTitle("App isn't available")
        dialog.setMessage("$appName is paused as your app timer ran out")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        alert = dialog.create()
        alert?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        alert?.show()
    }


    private fun getUsageStatistics(context: Context, appName: String?) :Time? {
        val mUsageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        var  currentEvent: UsageEvents.Event?
        val  allEvents : ArrayList<UsageEvents.Event> = ArrayList();
        val map : HashMap<String, AppUsageInfo> =  HashMap();
        val utc = ZoneId.of("UTC")
        val defaultZone = ZoneId.systemDefault()
        val date: LocalDate = LocalDate.now()
        val startDate = date.atStartOfDay(defaultZone).withZoneSameInstant(utc)
        val start = startDate.toInstant().toEpochMilli()
        val end = startDate.plusDays(1).toInstant().toEpochMilli()


        val usageEvents = mUsageStatsManager.queryEvents(start, end);


        while (usageEvents.hasNextEvent()) {
            currentEvent =  UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            if(currentEvent.packageName==appName)
            if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                currentEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND ) {
                allEvents.add(currentEvent)
                val key = currentEvent.packageName
                if (map[key] ==null)
                    map[key] =  AppUsageInfo (key);
            }
        }

        var lastEvent:UsageEvents.Event? = null;

        for ( i in 0 until allEvents.size-1){
           val E0= allEvents[i];
           val E1= allEvents[i + 1];
            if (E0.eventType == 1 && E1.eventType == 2){
                val diff = E1.timeStamp - E0.timeStamp;
                if(map[E0.packageName]==null){
                    map[E0.packageName]?.timeInForeground= diff;
                }else{
                    map[E0.packageName]!!.timeInForeground+= diff;
                }

            }
            lastEvent = E1

        }

        var diff:Long = 0
        println("time last event type ${lastEvent?.eventType }")
        if(lastEvent?.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND){
            diff = System.currentTimeMillis() - lastEvent.timeStamp
        }

        println("time : acutal time = ${convertLongToTime( map[appName]?.timeInForeground!!)}")
        println("time : calc time = ${convertLongToTime( map[appName]?.timeInForeground!!+diff)}")


        val time = map[appName]?.timeInForeground ?: return null

        return convertLongToTime(time+diff)



    }





    private fun convertLongToTime(milliSeconds: Long):Time{


        val SECOND = 1000
        val MINUTE = 60 * SECOND
        val HOUR = 60 * MINUTE
        val DAY = 24 * HOUR
        val time = Time()
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
        return time
    }

    private fun block() {
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
    }


    private fun checkBasicSettings() {
        checkWifi()
        checkBluetooth()
        checkOrientation()
        checkSound()
    }



    private fun runCallBlocker(){
        val telephony = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                if (isAlive) {
                    CallBlocker
                            .onCall(state, incomingNumber,
                                    this@UsageAccessService.applicationContext, user)
                }
                super.onCallStateChanged(state, incomingNumber)
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun checkWifi(){
        val wifiManager = this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        if(user?.basicSettings?.wifi==BasicSettings.AlwaysON){
            if(!wifiManager.isWifiEnabled){
                println("Turning on wifi")
                wifiManager.isWifiEnabled = true
            }
        }
        else if(user?.basicSettings?.wifi==BasicSettings.AlwaysOFF){
            if(wifiManager.isWifiEnabled){
                println("Turning off wifi")
                wifiManager.isWifiEnabled = false
            }
        }
    }

    private fun checkSound(){
        if(user?.basicSettings?.sound != BasicSettings.DontCare){
            val audioManager: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            if(user?.basicSettings?.sound == BasicSettings.normal && audioManager.ringerMode!=AudioManager.RINGER_MODE_NORMAL ){
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }else if(user?.basicSettings?.sound == BasicSettings.silent && audioManager.ringerMode!=AudioManager.RINGER_MODE_SILENT){
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
        }

    }

    private fun checkBluetooth(){
        if(user?.basicSettings?.bluetooth!=BasicSettings.DontCare){
            if(user?.basicSettings?.bluetooth==BasicSettings.AlwaysON){
                turnOnBluetooth()
            }else if (user?.basicSettings?.bluetooth==BasicSettings.AlwaysOFF){
                turnOffBluetooth()
            }
        }
    }

    private fun checkOrientation(){
        if(user?.basicSettings?.orientation.equals(BasicSettings.DontCare)){
            return
        }

        try {
            if (user?.basicSettings?.orientation.equals(BasicSettings.landscape) && Settings.System.getInt(
                            applicationContext.contentResolver,
                            Settings.System.ACCELEROMETER_ROTATION) == 1) {
                Settings.System.putInt(
                        contentResolver,
                        Settings.System.USER_ROTATION,
                        Surface.ROTATION_90 //U
                        // se any of the Surface.ROTATION_ constants
                                      )
                Settings.System.putInt(applicationContext.contentResolver,
                                       Settings.System.ACCELEROMETER_ROTATION, 0)
            } else if (user?.basicSettings?.orientation.equals(BasicSettings.portrait) && Settings.System.getInt(
                            applicationContext.contentResolver,
                            Settings.System.ACCELEROMETER_ROTATION) == 1) {
                Settings.System.putInt(
                        contentResolver,
                        Settings.System.USER_ROTATION,
                        Surface.ROTATION_0 //U
                        // se any of the Surface.ROTATION_ constants
                                      )
                Settings.System.putInt(applicationContext.contentResolver,
                                       Settings.System.ACCELEROMETER_ROTATION, 0)
            }
        } catch (e: Throwable) {
        }
    }

    private fun turnOnBluetooth(){
        val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
    }

    private fun turnOffBluetooth(){
        val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.disable()
        }
    }
}

class AppUsageInfo(var packageName: String) {
    var appIcon: Drawable? = null
    var appName: String? = null
    var timeInForeground: Long = 0
    var launchCount = 0
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
            return true
        }else if(this.hour!!<time.hour!!){
            return false
        }

        return this.minute!! > time.minute!!

    }

}