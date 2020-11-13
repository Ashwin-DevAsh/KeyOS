package tech.DevAsh.KeyOS.Config

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import com.android.launcher3.Utilities
import kotlinx.android.synthetic.keyOS.activity_settings.*
import tech.DevAsh.KeyOS.Config.AllowApps.Companion.Types
import tech.DevAsh.KeyOS.Config.Fragments.PermissionsBottomSheet
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.HelperLauncher
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.keyOS.Config.ImportExportSettings
import tech.DevAsh.keyOS.Config.ScreenSaver
import tech.DevAsh.keyOS.Database.BasicSettings
import java.util.*


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

        importExport?.setOnClickListener {
            startActivity(Intent(this, ImportExportSettings::class.java))
        }

        password?.setOnClickListener {
            startActivity(Intent(this, Password::class.java))
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