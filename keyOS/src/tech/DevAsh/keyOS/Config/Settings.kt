package tech.DevAsh.KeyOS.Config

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import kotlinx.android.synthetic.dev.activity_settings.*
import tech.DevAsh.KeyOS.Config.AllowApps.Companion.Types
import tech.DevAsh.KeyOS.Config.Fragments.PermissionsBottomSheet
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.HelperLauncher
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.keyOS.Config.Adapters.UserAgreement
import tech.DevAsh.keyOS.Config.ImportExportSettings
import tech.DevAsh.keyOS.Config.ScreenSaver
import tech.DevAsh.keyOS.Config.WebFilter
import tech.DevAsh.keyOS.Database.BasicSettings


class Settings : AppCompatActivity() {
    

    var shouldLaunch = false
    private val permissionsBottomSheet=PermissionsBottomSheet(this)
    private var isFromLauncher:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmHelper.init(this)
        setContentView(R.layout.activity_settings)
        loadBottomButton()
        loadView()
        onClick()
        controlLaunchButton()
        checkUserAgreement()
        setUpDrawer()

    }

    private fun checkUserAgreement(){
        if(!UserContext.user!!.isEndUserLicenceAgreementDone){
            Handler().postDelayed({
                                      UserAgreement(this).show(supportFragmentManager, "")

                                  }, 1000)
        }
    }

    private fun setStatusBar(){
        val layout: LinearLayout = findViewById(R.id.statusBar)
        val params: ViewGroup.LayoutParams = layout.layoutParams
        params.height = getStatusBarHeight()
        params.width = 100
        layout.layoutParams = params

    }
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun setUpDrawer(){
        drawer_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.feedback -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "plain/text"
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("keyOS.DevAsh@gmail.com"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback & suggestions")
                    startActivity(Intent.createChooser(intent, "Email"))
                }
                R.id.bug -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "plain/text"
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("keyOS.DevAsh@gmail.com"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Bug report")
                    startActivity(Intent.createChooser(intent, "Email"))
                }
                R.id.rate -> {
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
                }
                R.id.share -> {
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND);
                        shareIntent.type = "text/plain";
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
                        var shareMessage = "\nLet me recommend you this application\n\n"
                        shareMessage =
                                shareMessage + "https://play.google.com/store/apps/details?id=" + "tech.DevAsh.keyOS" + "\n\n"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        startActivity(Intent.createChooser(shareIntent, "choose one"))
                    } catch (e: Exception) {

                    }
                }
                R.id.privacyPolicy -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(BuildConfig.PRIVACY_POLICY)
                    startActivity(intent)
                }

                R.id.appinfo -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }

                R.id.developerContact->{
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://www.devash.tech")
                    startActivity(intent)
                }

                else -> {

                }
            }

            return@setNavigationItemSelectedListener true
        }
    }


    private fun loadBottomButton(){
        isFromLauncher = intent.getBooleanExtra("isFromLauncher", false)
    }

    private fun controlLaunchButton(){
        val bottomDown: Animation = AnimationUtils.loadAnimation(
                this,
                R.anim.button_down
                                                                )
        val bottomUp: Animation = AnimationUtils.loadAnimation(
                this,
                R.anim.button_up
                                                              )

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
            val navDrawer = findViewById<DrawerLayout>(R.id.settingsLayout)
            // If the navigation drawer is not open then open it, if its already open then close it.
            // If the navigation drawer is not open then open it, if its already open then close it.
            navDrawer.openDrawer(GravityCompat.START)
        }

        importExport?.setOnClickListener {
            startActivity(Intent(this, ImportExportSettings::class.java))
        }

        password?.setOnClickListener {
            startActivity(Intent(this, Password::class.java))
        }

        webFilter.setOnClickListener {
            startActivity(Intent(this, WebFilter::class.java))

        }

        wifi?.setOnClickListener{
            optionsOnClick(wifiMode, wifi)
        }
        orientation?.setOnClickListener{
            optionsOnClick(orientationMode, orientation, BasicSettings.orientationOptions)
        }
        bluetooth?.setOnClickListener{
            optionsOnClick(bluetoothMode, bluetooth)
        }
        sound?.setOnClickListener {
            optionsOnClick(soundMode, sound, BasicSettings.soundOptions)
        }

        settings?.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }

        apps?.setOnClickListener {
            AllowApps.type=Types.ALLOWAPPS
            startActivity(Intent(this, AllowApps::class.java))
        }

        services.setOnClickListener {
            AllowApps.type=Types.ALLOWSERVICES
            startActivity(Intent(this, AllowApps::class.java))
        }

        singleApp.setOnClickListener{
            AllowApps.type=Types.SINGLEAPP
            startActivity(Intent(this, AllowApps::class.java))
        }

        screenSaver.setOnClickListener {
            startActivity(Intent(this, ScreenSaver::class.java))
        }

        phone.setOnClickListener {
            startActivity(Intent(this, PhoneCalls::class.java))
        }

        cameraSwitch.setOnCheckedChangeListener{ _, isChecked->
            UserContext.user?.basicSettings?.isDisableCamera = isChecked
            if (isChecked && !PermissionsHelper.isAdmin(this)) {
                    PermissionsHelper.getAdminPermission(this)
            }
        }

        exit.setOnClickListener {
            Kiosk.exitKiosk(this, UserContext.user?.password)
        }

        permissions.setOnClickListener {
            shouldLaunch = false
            permissionsBottomSheet.show(supportFragmentManager, "TAG")
        }

        notificationPanel.setOnCheckedChangeListener{ _, isChecked->
            UserContext.user!!.basicSettings.notificationPanel = isChecked
        }
    }



    private fun optionsOnClick(textView: TextView, parentView: View,
                               options: List<String> = BasicSettings.options){
        vibrate()
        val position = options.indexOf(textView.text)
        val nextOption = options[(position + 1) % 3]
        textView.text = nextOption
        animate(parentView)
        this.saveData()
    }


    fun animate(parentView: View){
        parentView.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100)
                .withEndAction {
                    parentView.animate().scaleX(1f).scaleY(1f).duration = 100
                }
    }

    private fun vibrate(){

        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 500 milliseconds
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
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
            permissionsBottomSheet.show(supportFragmentManager, "TAG")
        }
    }

    private fun loadView(){
        wifiMode?.text = UserContext.user?.basicSettings?.wifi
        orientationMode?.text = UserContext.user?.basicSettings?.orientation
        bluetoothMode?.text = UserContext.user?.basicSettings?.bluetooth
        soundMode?.text = UserContext.user?.basicSettings?.sound
        notificationPanel?.isChecked = UserContext.user?.basicSettings?.notificationPanel!!
        cameraSwitch?.isChecked = UserContext.user?.basicSettings?.isDisableCamera!! && PermissionsHelper.isAdmin(
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
            finish()
            Utilities.restartLauncher(this)
        }else{
            super.onBackPressed()
        }

    }


    private fun saveData(){
        val basicSettings = BasicSettings(
                wifiMode.text.toString(),
                orientationMode.text.toString(),
                bluetoothMode.text.toString(),
                soundMode.text.toString(),
                notificationPanel.isChecked,
                cameraSwitch.isChecked
                                         )
        UserContext.user!!.basicSettings = (basicSettings)
        RealmHelper.updateUser(UserContext.user!!)
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
        context.startActivity(selector)

        packageManager.setComponentEnabledSetting(helperLauncher,
                                                  PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                                                  PackageManager.DONT_KILL_APP)
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
                                          permissionsBottomSheet.show(supportFragmentManager, "TAG")
                                      }
                                  }, 250)
        }
    }


}