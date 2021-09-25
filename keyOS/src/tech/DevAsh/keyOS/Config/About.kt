package tech.DevAsh.keyOS.Config

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.dev.activity_about.*
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Helpers.AnalyticsHelper
import tech.DevAsh.keyOS.Helpers.UpdateOriginalApk
import java.io.File

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        onClick()
        loadView()
    }

    fun loadView(){
        subHeading.text = "Running on version " + BuildConfig.VERSION_NAME + "\nstable"
    }


    private fun onClick(){

        back.setOnClickListener {
            onBackPressed()
        }

        update.setOnClickListener {
            AnalyticsHelper.logEvent(this, "manual_update")
            update()
        }

        feedback.setOnClickListener {
           AnalyticsHelper.logEvent(this, "sending_feedback")
           feedBack()
        }
        share.setOnClickListener {
            AnalyticsHelper.logEvent(this, "share_app")
            share()
        }
        bugReport.setOnClickListener {
            AnalyticsHelper.logEvent(this, "report_bug")
            bug()
        }

        appInfo.setOnClickListener {
            AnalyticsHelper.logEvent(this, "opening_app_info")
            appInfo()
        }

        quickSurvey.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_Survey")
            openWebsite(
                    "https://docs.google.com/forms/d/e/1FAIpQLSee4_xynrFhk_BJqb5Arbt_ayS6eG_8WFN179J6dJi5Mt9FzQ/viewform?usp=pp_url")
        }

        visit.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_App_website")
            openWebsite("https://www.keyos.in")
        }

        developerContact.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_Developer_website")
            openWebsite(color = "")
        }

        rate.setOnClickListener {
            AnalyticsHelper.logEvent(this, "rate_app")
            rate()
        }

        privacyPolicy.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_privacyPolicy_website")
            openWebsite(BuildConfig.PRIVACY_POLICY)
        }
        termsAndCondition.setOnClickListener {
            AnalyticsHelper.logEvent(this, "Opened_termsAndCondition_website")
            openWebsite(BuildConfig.TERMS_AND_CONDITIONS)
        }
//        donate.setOnClickListener {
//            AnalyticsHelper.logEvent(this, "Opened_Donation_page")
//            startActivity(Intent(this,Donate::class.java))
//        }
    }



    private fun feedBack(){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("keyOS.DevAsh@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback & suggestions")
        startActivity(Intent.createChooser(intent, "Email"))
    }

    private fun appInfo(){

                                  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                  intent.data = Uri.parse("package:$packageName")
                                  startActivity(intent)
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
                               "Playstore link - https://play.google.com/store/apps/details?id=tech.DevAsh.keyOS"
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

    private fun openWebsite(url: String = "https://www.devash.in", color: String = "#ffffff"){
        try {
            val builder = CustomTabsIntent.Builder()
            try {
                val colorInt: Int = Color.parseColor(color)
                builder.setToolbarColor(colorInt)
            } catch (e: Throwable) {
            }

            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, Uri.parse(url))
        } catch (e: Throwable) {
            try{
                val browserIntent =
                        Intent(Intent.ACTION_VIEW,
                               Uri.parse(url))
                startActivity(browserIntent)
            }catch (e:Throwable){
                AlertHelper.showToast(getString(R.string.failed), this)
            }
            }
    }

    private fun update(){
        if(BuildConfig.FLAVOR=="quickstepKeyOS"){
            val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(this)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
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
       }else{
           UpdateOriginalApk.update(this)
       }

    }


}