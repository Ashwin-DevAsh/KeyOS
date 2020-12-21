package tech.DevAsh.keyOS.Config.Fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.dev.fragment_user_agreement.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.keyOS.Database.User


class UserAgreement() : BottomSheetDialogFragment() {

    var activity:Activity?=null

    constructor(activity: Activity) : this() {
        this.activity=activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_agreement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMessageWithClickableLink(agreementText)
        onClick()
        loadView()
    }

    fun loadView(){
        agreementText1Checked.isChecked=true
        agreementText2Checked.isChecked=true
    }

    fun onClick(){
        acceptAndContinue.setOnClickListener {

            if(agreementText1Checked.isChecked && agreementText2Checked.isChecked){
                User.user!!.isEndUserLicenceAgreementDone=true
                RealmHelper.updateUser(User.user!!)
                dismiss()
            }


        }

        agreementText1.setOnClickListener {
            agreementText1Checked.setChecked(!agreementText1Checked.isChecked,true)
        }


        agreementText2.setOnClickListener {
            agreementText2Checked.setChecked(!agreementText2Checked.isChecked,true)
        }


        exit.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(!User.user!!.isEndUserLicenceAgreementDone){
            Handler().postDelayed({
                                      activity?.finish()
                                  }, 500)
        }
    }
    var content = SpannableString("To improve KeyOS and make sure all feature work properly," +
                  " some of your usage data will be collected anonymously." +
                  " You must read and agree to our Privacy Policy and Terms & Conditions. before using KeyOS")

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val touchOutsideView = dialog!!.window!!
                .decorView
                .findViewById<View>(R.id.touch_outside)
        touchOutsideView.setOnClickListener(null)
    }

    private fun setMessageWithClickableLink(textView: TextView) {
        //The text and the URL


        createSpannableText("Privacy Policy",BuildConfig.PRIVACY_POLICY ,content,textView)
        createSpannableText("Terms & Conditions",BuildConfig.TERMS_AND_CONDITIONS ,content,textView)



    }

    fun createSpannableText(text:String,url:String,content:SpannableString,textView: TextView){
        val clickableSpanPrivacyPolicy = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val builder = CustomTabsIntent.Builder()
                val colorInt: Int = Color.parseColor("#FFFFFF")
                builder.setToolbarColor(colorInt)
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context!!, Uri.parse(url))
            }
        }
        val startIndex = content.indexOf(text)
        val endIndex = startIndex + text.length

        val spannableString = SpannableString(content)
        spannableString.setSpan(clickableSpanPrivacyPolicy, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        this.content = spannableString
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }


}