package tech.DevAsh.KeyOS.Config

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import io.realm.Realm
import kotlinx.android.synthetic.dev.activity_settings.*
import kotlinx.android.synthetic.dev.drawer_header.view.*
import tech.DevAsh.KeyOS.Config.AllowApps.Companion.Types
import tech.DevAsh.KeyOS.Config.Fragments.PermissionsBottomSheet
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.HelperLauncher
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.keyOS.Config.Fragments.UserAgreement
import tech.DevAsh.keyOS.Config.ImportExportSettings
import tech.DevAsh.keyOS.Config.ScreenSaver
import tech.DevAsh.keyOS.Config.WebFilter
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.Helpers.AlertDeveloper
import tech.DevAsh.keyOS.Helpers.AnalyticsHelper
import java.io.File
import java.util.*


class Settings : AppCompatActivity() {

    val TAG = this::class.simpleName

    var shouldLaunch = false
    private val permissionsBottomSheet=PermissionsBottomSheet(this)
    private var isFromLauncher:Boolean = false

    var resourseMapper: Map<String, Int> = mapOf(
            BasicSettings.AlwaysON to R.string.always_on,
            BasicSettings.AlwaysOFF to R.string.always_off,
            BasicSettings.DontCare to R.string.dont_care,
            BasicSettings.landscape to R.string.landscape,
            BasicSettings.portrait to R.string.portrait,
            BasicSettings.silent to R.string.silent,
            BasicSettings.normal to R.string.normal)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        RealmHelper.init(this)
        setContentView(R.layout.activity_settings)
        loadBottomButton()
        loadView()
        onClick()
        controlLaunchButton()
        checkUserAgreement()
        setUpDrawer()
        reviewPrompt()
    }



    private fun reviewPrompt() {
        val realm = Realm.getDefaultInstance()
        try {
            val reviewInfoDB = realm.copyFromRealm(realm.where(
                    tech.DevAsh.keyOS.Database.ReviewInfo::class.java).findFirst()!!)
            if(reviewInfoDB!!.launchedCount%5==0){
                val manager = ReviewManagerFactory.create(this)
                manager.requestReviewFlow()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val reviewInfo = task.result
                                manager.launchReviewFlow(this@Settings, reviewInfo)
                                        .addOnFailureListener {}
                                        .addOnCompleteListener {}
                            }
                        }.addOnFailureListener {}
            }
            tech.DevAsh.keyOS.Database.ReviewInfo.init(reviewInfoDB.launchedCount + 1, false)
        }catch (e: Throwable){
            tech.DevAsh.keyOS.Database.ReviewInfo.init(0, false)
        }
    }


    private fun checkUserAgreement(){
        if(!User.user!!.isEndUserLicenceAgreementDone || BuildConfig.IS_DEV_BUILD){
            Handler().postDelayed({

                                      UserAgreement(this).show(supportFragmentManager, TAG)
                                  }, 750)
        }
    }

    private fun setUpDrawer(){
        drawer_view.getHeaderView(0).androidID.text =
                AlertDeveloper.getInstallDetails(this).deviceID
        drawer_view.getHeaderView(0).setOnClickListener {
            val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
            startActivity(intent)
        }
        drawer_view.setNavigationItemSelectedListener {

            settingsLayout.closeDrawer(GravityCompat.START)

            when (it.itemId) {
                R.id.feedback -> {
                    AnalyticsHelper.logEvent(this, "sending_feedback")
                    feedBack()
                }
                R.id.bug -> {
                    AnalyticsHelper.logEvent(this, "report_bug")
                    bug()
                }
                R.id.rate -> {
                    AnalyticsHelper.logEvent(this, "rate_app")
                    rate()
                }
                R.id.share -> {
                    AnalyticsHelper.logEvent(this, "share_app")
                    share()
                }
                R.id.privacyPolicy -> {
                    AnalyticsHelper.logEvent(this, "Opened_privacyPolicy_website")
                    openWebsite(BuildConfig.PRIVACY_POLICY)
                }

                R.id.termsAndCondition -> {
                    AnalyticsHelper.logEvent(this, "Opened_termsAndCondition_website")
                    openWebsite(BuildConfig.TERMS_AND_CONDITIONS)
                }

                R.id.appinfo -> {
                    appInfo()
                }

                R.id.developerContact -> {
                    AnalyticsHelper.logEvent(this, "Opened_Developer_website")
                    openWebsite(color = "")
                }

                R.id.visit -> {
                    AnalyticsHelper.logEvent(this, "Opened_App_website")
                    openWebsite("https://www.keyos.in")
                }

                R.id.survey -> {
                    AnalyticsHelper.logEvent(this, "Opened_Survey")
                    openWebsite(
                            "https://docs.google.com/forms/d/e/1FAIpQLSee4_xynrFhk_BJqb5Arbt_ayS6eG_8WFN179J6dJi5Mt9FzQ/viewform?usp=pp_url")
                }

                R.id.update -> {

                    update()
                }

                else -> {

                }
            }

            return@setNavigationItemSelectedListener true
        }
    }

    private fun feedBack(){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("keyOS.DevAsh@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback & suggestions")
        startActivity(Intent.createChooser(intent, "Email"))
    }

    private fun appInfo(){
        Handler().postDelayed({

                                  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                  intent.data = Uri.parse("package:$packageName")
                                  startActivity(intent)
                              }, 500)
    }


    private fun share(){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND);
            shareIntent.type = "text/plain";
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
            val shareMessage = "Hey do you want to protect your kids from misusing there mobile phones. Or you want to manage your corporate mobiles from unauthorized usage . Or you want to create dedicated devices\n" +
                               "\n" +
                               "Check out this app it is freely available on play store\n" +
                               "\n" +
                               "Website link - https://www.keyos.in\n" +
                               "\n" +
                               "Playstore link - https://play.google.com/store/apps/details?id=tech.DevAsh.keyOS.dev"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {

        }
    }

    fun bug(){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("keyOS.DevAsh@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Bug report")
        try {
            val fileLocation  = File(Environment.getExternalStorageDirectory().absolutePath,
                                     "KeyOS/logs/log.txt")
            if(fileLocation.exists()){
                val path = Uri.fromFile(fileLocation)
                intent .putExtra(Intent.EXTRA_STREAM, path)
            }
        }catch (e: Throwable){}
        startActivity(Intent.createChooser(intent, "Email"))
    }

    fun rate(){
        Handler().postDelayed({
                                  val uri: Uri = Uri.parse("market://details?id=tech.DevAsh.keyOS")
                                  val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                                  goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                                                              Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                                              Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                  try {
                                      startActivity(goToMarket)
                                  } catch (e: ActivityNotFoundException) {
                                      startActivity(Intent(Intent.ACTION_VIEW,
                                                           Uri.parse(
                                                                   "http://play.google.com/store/apps/details?id=tech.DevAsh.keyOS")))
                                  }
                              }, 500)
    }

    private fun openWebsite(url: String = "https://www.devash.tech", color: String = "#ffffff"){
        Handler().postDelayed({
                                  val builder = CustomTabsIntent.Builder()
                                  try {
                                      val colorInt: Int = Color.parseColor(color)
                                      builder.setToolbarColor(colorInt)
                                  } catch (e: Throwable) {
                                  }

                                  val customTabsIntent = builder.build()
                                  customTabsIntent.launchUrl(this, Uri.parse(url))

                              }, 500)
    }

    private fun update(){
        val appUpdateManager: AppUpdateManager? = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        1)
            } else {
                rate()
            }
        }

    }


    private fun loadBottomButton(){
        isFromLauncher = intent.getBooleanExtra("isFromLauncher", false)
    }

    private fun controlLaunchButton(){
        val bottomDown: Animation = AnimationUtils.loadAnimation(
                this,
                R.anim.button_down)

        val bottomUp: Animation = AnimationUtils.loadAnimation(
                this,
                R.anim.button_up)

        launchContainer.cardElevation=10f


        if(Build.VERSION.SDK_INT>=23)
        scroller.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY >100) {
                if(launchContainer.visibility==View.VISIBLE){
                    launchContainer.startAnimation(bottomDown)
                    launchContainer.visibility=View.GONE
                }
                
            }
            if(scrollY < 100){
                if(launchContainer.visibility==View.GONE){
                    launchContainer.visibility=View.VISIBLE
                    launchContainer.startAnimation(bottomUp)
                }
            }
        }
    }


    private fun onClick(){
        launch?.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Tap_launch_button")
            if(launchContainer.visibility==View.VISIBLE){
                shouldLaunch = true
                saveData()
                if(isFromLauncher){
                   onBackPressed()
                }else{
                    checkPermissionAndLaunch()
                }
            }
        }

        drawer.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_Drawer")
            val navDrawer = findViewById<DrawerLayout>(R.id.settingsLayout)
            navDrawer.openDrawer(GravityCompat.START)
        }

        importExport?.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_Import_and_export")
            startActivity(Intent(this, ImportExportSettings::class.java))
        }

        password?.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_password")
            startActivity(Intent(this, Password::class.java))
        }

        webFilter.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_WebFilter")
            startActivity(Intent(this, WebFilter::class.java))

        }

        wifi?.setOnClickListener{
            AnalyticsHelper.logEvent(this, "Toggle_wifi")
            User.user.basicSettings.wifi= optionsOnClick(wifiMode, wifi,User.user.basicSettings.wifi)
            this.saveData()
        }
        orientation?.setOnClickListener{
            AnalyticsHelper.logEvent(this, "Toggle_orientation")
            User.user.basicSettings.orientation = optionsOnClick(orientationMode, orientation,User.user.basicSettings.orientation ,BasicSettings.orientationOptions)
            this.saveData()
        }
        bluetooth?.setOnClickListener{
            AnalyticsHelper.logEvent(this, "Toggle_bluetooth")
            User.user.basicSettings.bluetooth=optionsOnClick(bluetoothMode, bluetooth,User.user.basicSettings.bluetooth)
            this.saveData()
        }
        sound?.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Toggle_sound")
            User.user.basicSettings.sound = optionsOnClick(soundMode, sound, User.user.basicSettings.sound,BasicSettings.soundOptions)
            this.saveData()
        }

        settings?.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }

        apps?.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_AllowApps")
            AllowApps.type=Types.ALLOWAPPS
            startActivity(Intent(this, AllowApps::class.java))
        }

        services.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_Services")
            AllowApps.type=Types.ALLOWSERVICES
            startActivity(Intent(this, AllowApps::class.java))
        }

        singleApp.setOnClickListener{
            AnalyticsHelper.logEvent(this, "Opened_SingeApp")
            AllowApps.type=Types.SINGLEAPP
            startActivity(Intent(this, AllowApps::class.java))
        }

        screenSaver.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_ScreenSaver")
            startActivity(Intent(this, ScreenSaver::class.java))
        }

        phone.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_Call_blocker")
            startActivity(Intent(this, PhoneCalls::class.java))
        }

        cameraSwitch.setOnCheckedChangeListener{ _, isChecked->
            User.user?.basicSettings?.isDisableCamera = isChecked
            AnalyticsHelper.logEvent(this, "Switch_Camera")
            if (isChecked && !PermissionsHelper.isAdmin(this)) {
                    PermissionsHelper.getAdminPermission(this)
            }
        }

        exit.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Exit_App_from_Settings")
            Kiosk.exitKiosk(this, User.user?.password)
        }

        permissions.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Tap_permission_icon")
            shouldLaunch = false
            if(!permissionsBottomSheet.isAdded) {
                permissionsBottomSheet.show(supportFragmentManager, TAG)
            }
        }

        notificationPanel.setOnCheckedChangeListener{ _, isChecked->
            AnalyticsHelper.logEvent(this, "Switch_notification")
            User.user!!.basicSettings.notificationPanel = isChecked
        }
    }



    private fun optionsOnClick
            (textView: TextView, parentView: View, currentText:String,options: List<String> = BasicSettings.options):String
    {
        try{
            vibrate()
            val position = options.indexOf(currentText)
            val nextOption = options[(position + 1) % 3]
            textView.text = getString(resourseMapper[nextOption]!!)
            animate(parentView)
            return nextOption
        }catch (e:Throwable){}
        return "None"
    }


    fun animate(parentView: View){
        parentView.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100)
                .withEndAction {
                    parentView.animate().scaleX(1f).scaleY(1f).duration = 100
                }
    }

    private fun vibrate(){

        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(50)
        }
    }

    private fun checkPermissionAndLaunch(){
        if(PermissionsHelper.checkImportantPermissions(this)){
            if(shouldLaunch){
                shouldLaunch=false
                launch(this)
            } 
        }else{
            if(!permissionsBottomSheet.isAdded) {
                permissionsBottomSheet.show(supportFragmentManager, TAG)
            }
        }
    }

    private fun loadView(){
        wifiMode?.text = getString(resourseMapper[User.user?.basicSettings?.wifi]!!)
        orientationMode?.text =  getString(resourseMapper[User.user?.basicSettings?.orientation]!!)
        bluetoothMode?.text =  getString(resourseMapper[User.user?.basicSettings?.bluetooth]!!)
        soundMode?.text =  getString(resourseMapper[User.user?.basicSettings?.sound]!!)
        notificationPanel?.isChecked = User.user?.basicSettings?.notificationPanel!!
        cameraSwitch?.isChecked = User.user?.basicSettings?.isDisableCamera!! && PermissionsHelper.isAdmin(
                this)
        if(PermissionsHelper.checkImportantPermissions(this)){
            permissionsAlert.visibility = View.GONE
        }else{
            permissionsAlert.visibility = View.VISIBLE
        }
    }


    override fun onBackPressed() {
        if(settingsLayout.isDrawerOpen(drawer_view)){
            settingsLayout.closeDrawer(GravityCompat.START)
            return
        }

        saveData()
        if(isFromLauncher){
            finishAndRemoveTask()
            Utilities.restartLauncher(this)
        }else{
            super.onBackPressed()
        }

    }


    private fun saveData(){
        val basicSettings = BasicSettings(
                User.user.basicSettings.wifi,
                User.user.basicSettings.orientation,
                User.user.basicSettings.bluetooth,
                User.user.basicSettings.sound,
                notificationPanel.isChecked,
                cameraSwitch.isChecked)
        User.user!!.basicSettings = (basicSettings)
        RealmHelper.updateUser(User.user!!)
    }



    private fun launch(context: Context) {

        val packageManager = context.packageManager
        val helperLauncher = ComponentName(context, HelperLauncher::class.java)
        val kioskLauncher = ComponentName(context, KioskLauncher::class.java)

        if(!PermissionsHelper.isMyLauncherDefault(context))
        packageManager.setComponentEnabledSetting(helperLauncher,
                                                  PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                                  PackageManager.DONT_KILL_APP)
        packageManager.setComponentEnabledSetting(kioskLauncher,
                                                  PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                                  PackageManager.DONT_KILL_APP)

        val selector = Intent(Intent.ACTION_MAIN)
        selector.addCategory(Intent.CATEGORY_HOME)
        selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val homePage = Intent(this, KioskLauncher::class.java)

        context.startActivities(arrayOf(homePage, selector))

        packageManager.setComponentEnabledSetting(helperLauncher,
                                                  PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                                                  PackageManager.DONT_KILL_APP)

        finishAndRemoveTask()
    }



    override fun onResume() {
        restartPermissionSheet()
        super.onResume()
    }

    override fun onRestart() {
        loadView()
        super.onRestart()
    }

    override fun finish() {
        overridePendingTransition(0, R.anim.abc_fade_out)
        super.finish()
    }


    private fun restartPermissionSheet(){
        if(PermissionsHelper.openedForPermission){
            PermissionsHelper.openedForPermission=false
            Handler().postDelayed({
                                      if (PermissionsHelper.checkImportantPermissions(this)) {
                                          checkPermissionAndLaunch()
                                      } else {
                                          permissionsBottomSheet.show(supportFragmentManager, TAG)
                                      }
                                  }, 250)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {}
}