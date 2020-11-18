package tech.DevAsh.keyOS.Config.Adapters

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
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.dev.fragment_user_agreement.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.User



class UserAgreement(val activity: Activity) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_agreement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMessageWithClickableLink(agreementText)
        onClick()
    }

    fun onClick(){
        acceptAndContinue.setOnClickListener {
            UserContext.user!!.isEndUserLicenceAgreementDone=true
            RealmHelper.updateUser(UserContext.user!!)
            dismiss()

        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(!UserContext.user!!.isEndUserLicenceAgreementDone){
            Handler().postDelayed({
                                      activity.finish()
                                  }, 500)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val touchOutsideView = dialog!!.window!!
                .decorView
                .findViewById<View>(R.id.touch_outside)
        touchOutsideView.setOnClickListener(null)
    }

    private fun setMessageWithClickableLink(textView: TextView) {
        //The text and the URL
        val content =
                      "To improve KeyOS and make sure all feature work properly," +
                      " some of your usage data will be collected anonymously." +
                      " You must read and agree to our Privacy Policy. before using KeyOS"

        val clickableSpan = object :ClickableSpan() {

            override fun onClick(textView: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(BuildConfig.PRIVACY_POLICY  )
                startActivity(intent)
            }
        }
        val startIndex = content.indexOf("Privacy Policy")
        val endIndex = startIndex + "Privacy Policy".length

        val spannableString = SpannableString(content)
        spannableString.setSpan(clickableSpan, startIndex, endIndex,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }


}