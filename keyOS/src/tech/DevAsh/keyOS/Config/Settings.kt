package tech.DevAsh.KeyOS.Config

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.keyOS.activity_settings.*
import kotlinx.android.synthetic.keyOS.sheet_options.view.*
import tech.DevAsh.KeyOS.Config.Fragments.PermissionsBottomSheet
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.HelperLauncher
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.keyOS.Database.BasicSettings
import tech.DevAsh.KeyOS.Config.AllowApps.Companion.Types
import com.android.launcher3.views.Snackbar


class Settings : AppCompatActivity() {
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
                saveData()
                if(isFromLauncher){
                   onBackPressed()
                }else{
                    checkPermissionAndLaunch()
                }
            }
        }

        password?.setOnClickListener {
            startActivity(Intent(this, Password::class.java))
        }


        wifi?.setOnClickListener{
            peripheralOnClick(wifiMode,wifi)
        }
        orientation?.setOnClickListener{
            orientationOnClick(orientationMode,orientation)
        }
        bluetooth?.setOnClickListener{
            peripheralOnClick(bluetoothMode,bluetooth)
        }
        mobileData?.setOnClickListener {
            tech.DevAsh.KeyOS.Helpers.AlertHelper.showError("Not supported in your device",this)
            return@setOnClickListener
            peripheralOnClick(mobiledataMode,mobileData)
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

        phone.setOnClickListener {
            startActivity(Intent(this, PhoneCalls::class.java))
        }

        exit.setOnClickListener {
            Kiosk.exitKiosk(this, UserContext.user?.password)
        }
    }



    private fun peripheralOnClick(textView: TextView,parentView: View){
        vibrate()
        val position = BasicSettings.options.indexOf(textView.text)
        val nextOption = BasicSettings.options[(position + 1) % 3]
        textView.text = nextOption
        animate(textView,parentView)
    }

    private fun orientationOnClick(textView: TextView,parentView: View){
        vibrate()
        val position = BasicSettings.orientationOptions.indexOf(textView.text)
        val nextOption = BasicSettings.orientationOptions[(position + 1) % 3]
        textView.text = nextOption
        animate(textView,parentView)
    }


    fun animate(textView: TextView,parentView: View){
        parentView.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100)
                .withEndAction(object : java.lang.Runnable {
                    override fun run() {
                        parentView.animate().scaleX(1f).scaleY(1f).setDuration(100)
                    }
                })
    }

    private fun vibrate(){
        val v =  getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
    }





    private fun checkPermissionAndLaunch(){
        if(PermissionsHelper.checkImportantPermissions(this)){
            launch(this)
        }else{
            permissionsBottomSheet.show(supportFragmentManager, "TAG")
        }
    }

    private fun loadView(){
        wifiMode?.text = UserContext.user?.basicSettings?.wifi
        orientationMode?.text = UserContext.user?.basicSettings?.orientation
        bluetoothMode?.text = UserContext.user?.basicSettings?.bluetooth
        mobiledataMode?.text = UserContext.user?.basicSettings?.mobileData
        notificationPanel?.isChecked = UserContext.user?.basicSettings?.notificationPanel!!
    }


    override fun onBackPressed() {
        saveData()
        if(isFromLauncher){
            Utilities.restartLauncher(this)
//           PackageUpdatedTask(PackageUpdatedTask.OP_RELOAD, android.os.Process.myUserHandle())
//            val selector = Intent(Intent.ACTION_MAIN)
//            selector.addCategory(Intent.CATEGORY_HOME)
//            selector.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            selector.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//            startActivity(selector)
//            finish()
        }else{
            super.onBackPressed()
        }

    }

    private fun saveData(){
        val basicSettings = BasicSettings(
                wifiMode.text.toString(),
                orientationMode.text.toString(),
                bluetoothMode.text.toString(),
                mobiledataMode.text.toString(),
                notificationPanel.isChecked
                                         )
        UserContext.user!!.basicSettings = (basicSettings)
        RealmHelper.updateUser(UserContext.user!!)
    }



    private fun openBottomSheet(
            mode: TextView){

        val options = BottomSheetDialog(this)
        val sheetView: View = LayoutInflater.from(this).inflate(R.layout.sheet_options, null)

        fun onModeClick(
                clickView: View,
                option: String
                       ){
            clickView.setOnClickListener{
                mode.text = option
                options.cancel()
            }
        }

        onModeClick(sheetView.alwaysOn, BasicSettings.AlwaysON)
        onModeClick(sheetView.alwaysOff, BasicSettings.AlwaysOFF)
        onModeClick(sheetView.dontCare, BasicSettings.DontCare)

        options.setContentView(sheetView)
        options.show()
    }


    private fun launch(context: Context) {
        val packageManager = context.packageManager
        val helperLauncher = ComponentName(context, HelperLauncher::class.java)
        val kioskLauncher = ComponentName(context, KioskLauncher::class.java)

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

    override fun onRestart() {

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
        super.onRestart()
    }


}