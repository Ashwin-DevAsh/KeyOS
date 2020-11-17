package tech.DevAsh.KeyOS.Services

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.Surface
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.android.launcher3.R
import io.realm.Realm
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.CallBlocker
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.keyOS.Database.User
import java.time.LocalDate
import java.time.ZoneId
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class UsageAccessService : Service() {

    private var prevActivities = arrayListOf("")



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

    }

    var isAlive = false
        get() {
            return field && PermissionsHelper.isMyLauncherCurrent(this)
        }

    private var timeExhaustApps = TimeExhaustApps()


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        return START_NOT_STICKY
    }


    private fun startAsForeground(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "KeyOS Protection"
            val channelName: CharSequence = "Protection"
            val importance = NotificationManager.IMPORTANCE_MIN
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)
            val builder: Notification.Builder = Notification.Builder(this, channelId)
                    .setContentTitle("KeyOS Protection")
                    .setContentText("Your device completely protected by keyOS")
                    .setSmallIcon(R.drawable.ic_key_ring)
                    .setAutoCancel(false)
            val notification: Notification = builder.build()
            startForeground(2, notification)
        } else {
            val builder = NotificationCompat.Builder(this)
                    .setContentTitle("KeyOS Protection")
                    .setContentTitle("Your device completely protected by keyOS")
                    .setSmallIcon(R.drawable.ic_key_ring)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(false)
            val notification: Notification = builder.build()
            startForeground(2, notification)
        }
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

            for(i in timeExhaustApps.blockedApps){
                mActivityManager?.killBackgroundProcesses(i)
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
            if(user!!.singleApp!=null){
                user?.allowedApps?.add(user?.singleApp)
            }
        }catch (e: Throwable){

        }
    }

    override fun onDestroy() {
        println("Destroied...")
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

        println("time = $appName $className")

        if(appName==null || className==null){
            return
        }

        if(isAllowedPackage(appName, className)){
            if(prevActivities.last()!=appName){
                prevActivities.add(appName!!)
            }
        }else{
            block(appName!!)
        }

    }

    private fun isAllowedPackage(appName: String?, className: String?):Boolean{
        val app = Apps(appName)


        if(appName==packageName || AppsContext.exceptions.contains(appName) || AppsContext.exceptions.contains(className)){
            return true
        }

        val serviceIndex = user?.allowedServices?.indexOf(app)
        val appIndex = user?.allowedApps?.indexOf(app)
        val editedAppIndex = user?.editedApps?.indexOf(app)

        if(serviceIndex!=-1){
            return true
        }

        if(appIndex==-1){
            showAppBlockAlertDialog(this,"")
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

            println("time : $appName $allowedTime $usageTime")

            if(usageTime!=null && !allowedTime.isGreaterThan(usageTime)){
                prevActivities.removeAll(arrayListOf(appName))
                timeExhaustApps.blockedApps.add(appName!!)
                showTimerAlertDialog(this, appName)
                return false
            }

        }

        if (user!!.editedApps[editedAppIndex!!]!!.blockedActivities.contains(className)){
           showAppBlockAlertDialog(this,"")
            return false
        }

        return true
    }

    var timeAlertdialog : AlertDialog?=null
    var blockAppAlertDialog : AlertDialog?=null

    private fun showAppBlockAlertDialog(context: Context, appName: String?){

//        if(blockAppAlertDialog!=null){
//            if(!blockAppAlertDialog!!.isShowing){
//                blockAppAlertDialog?.show()
//            }
//            return
//        }
//
//        val dialog = AlertDialog.Builder(context, R.style.MyProgressDialog)
//        dialog.setTitle("Access denied")
//        dialog.setMessage("You are not allowed to access this content")
//        dialog.setCancelable(false)
//        dialog.setPositiveButton("Ok") { dialogInterface: DialogInterface, _: Int ->
//            dialogInterface.dismiss()
//        }
//
//        blockAppAlertDialog = dialog.create()
//        blockAppAlertDialog?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//        blockAppAlertDialog?.show()
    }


    private fun showTimerAlertDialog(context: Context, appName: String?){

        if(timeAlertdialog!=null){
            if(!timeAlertdialog!!.isShowing){
                timeAlertdialog?.show()
            }
            return
        }

        val dialog = AlertDialog.Builder(context, R.style.MyProgressDialog)
        dialog.setTitle("App isn't available")
        dialog.setMessage("Application is paused as your app timer ran out")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Ok") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        timeAlertdialog = dialog.create()
        timeAlertdialog?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        timeAlertdialog?.show()
    }


    private fun getUsageStatistics(context: Context, appName: String?) :Time? {
        val mUsageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        var currentEvent: UsageEvents.Event?
        val allEvents : ArrayList<UsageEvents.Event> = ArrayList()
        val map : HashMap<String, AppUsageInfo> =  HashMap()
        val utc = ZoneId.of("UTC")
        val defaultZone = ZoneId.systemDefault()
        val date: LocalDate = LocalDate.now()
        val startDate = date.atStartOfDay(defaultZone).withZoneSameInstant(utc)
        val start = startDate.toInstant().toEpochMilli()
        val end = startDate.plusDays(1).toInstant().toEpochMilli()

        if(timeExhaustApps.startTime==start){
            println("time : old day")
            if(timeExhaustApps.blockedApps.contains(appName)){
                println("time : cache hit")

                val time = Time()
                time.hour = 24
                return time
            }
        }else{
            println("time : new day")
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

        return convertLongToTime(time + diff)
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

    private fun block(packageName: String) {

        val launcher = Intent(Intent.ACTION_MAIN)
        launcher.addCategory(Intent.CATEGORY_HOME)
        launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {

            if(prevActivities.last()==this.packageName){
                throw Exception()
            }

            val prev1 = packageManager.getLaunchIntentForPackage(
                    prevActivities[prevActivities.size - 1])
            val prev2 = packageManager.getLaunchIntentForPackage(
                    prevActivities[prevActivities.size - 2])
            when {

                prev1!=null -> {
                    if(prev1.`package`==this.packageName){
                        throw Exception()
                    }
                    prev1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    prev1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(prev1)
                }
                prev2!=null -> {
                    if(prev2.`package`==this.packageName){
                        throw Exception()
                    }
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
        }finally {
            appName=null
            className=null
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
        try{
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
        }catch (e:Throwable){

        }

    }

    private fun checkWifi(){
//        val pm: PackageManager = packageManager
//        val hasWifi: Boolean = pm.hasSystemFeature(WIFI_SERVICE)
//
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
        try {
            val pm: PackageManager = packageManager
            val hasBluetooth: Boolean = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
            if(hasBluetooth && user?.basicSettings?.bluetooth!=BasicSettings.DontCare){
                if(user?.basicSettings?.bluetooth==BasicSettings.AlwaysON){
                    turnOnBluetooth()
                }else if (user?.basicSettings?.bluetooth==BasicSettings.AlwaysOFF){
                    turnOffBluetooth()
                }
            }
        }catch (e:Throwable){

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
    var appName: String? = null
    var timeInForeground: Long = 0
}

class TimeExhaustApps{
    var startTime :Long=0
    var blockedApps = hashSetOf<String>()
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