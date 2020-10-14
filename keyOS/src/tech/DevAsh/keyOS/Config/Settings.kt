package tech.DevAsh.KeyOS.Config

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.UserHandle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.PackageUpdatedTask
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
            openBottomSheet(wifiMode)
        }
        hotspot?.setOnClickListener{
            openBottomSheet(hotspotMode)
        }
        bluetooth?.setOnClickListener{
            openBottomSheet(bluetoothMode)
        }
        mobileData?.setOnClickListener {
            openBottomSheet(mobiledataMode, false)
        }


        settings?.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
        apps?.setOnClickListener {
            AllowApps.type="Allow Apps"
            startActivity(Intent(this, AllowApps::class.java))
        }

        services.setOnClickListener {
            AllowApps.type="Allow Services"
            startActivity(Intent(this, AllowApps::class.java))
        }

        phone.setOnClickListener {
            startActivity(Intent(this, PhoneCalls::class.java))
        }

        exit.setOnClickListener {
            Kiosk.exitKiosk(this,UserContext.user?.password)
        }
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
        hotspotMode?.text = UserContext.user?.basicSettings?.hotspot
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
                hotspotMode.text.toString(),
                bluetoothMode.text.toString(),
                mobiledataMode.text.toString(),
                notificationPanel.isChecked
                                         )
        UserContext.user!!.basicSettings = (basicSettings)
        RealmHelper.updateUser(UserContext.user!!)
    }



    private fun openBottomSheet(
            mode: TextView,
            advanceOptions: Boolean = true,
            additionalOption: Boolean = false
                               ){

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
        onModeClick(sheetView.allow, "Allow")
        onModeClick(sheetView.deny, "Deny")
        onModeClick(sheetView.alwaysOn, "Always on")
        onModeClick(sheetView.alwaysOff, "Always off")
        onModeClick(sheetView.denyIncoming, "Deny incoming")
        onModeClick(sheetView.denyOutgoing, "Deny outgoing")
        sheetView.whitelist.setOnClickListener {
            startActivity(Intent(this, PhoneBook::class.java))
            mode.text = "Whitelist Calls"
            options.cancel()

        }

        sheetView.blacklist.setOnClickListener {
            startActivity(Intent(this, PhoneBook::class.java))
            mode.text = "BlackList Calls"
            options.cancel()
        }

        if(advanceOptions){
            sheetView.advance.visibility=View.VISIBLE
        }else{
            sheetView.advance.visibility=View.GONE
        }
        if(additionalOption){
            sheetView.allow.text = "Allow calls"
            sheetView.deny.text = "Deny calls"
            onModeClick(sheetView.allow, "Allow calls")
            onModeClick(sheetView.deny, "Deny calls")
            sheetView.additional.visibility=View.VISIBLE
        }else{
            sheetView.additional.visibility=View.GONE
        }
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