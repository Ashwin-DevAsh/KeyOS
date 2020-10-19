package tech.DevAsh.KeyOS.Config

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.TextView
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.activity_permissions.*
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.KeyOS.Helpers.UiHelper.handelAppBar
import tech.DevAsh.KeyOS.Services.WindowChangeDetectingService


class Permissions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        handelAppBar(scroller, appBar)
        resize()
        onClick()
        changeBadge()
    }

    private fun onClick(){

        allow.setOnClickListener {
           PermissionsHelper.allow(this)
        }

        usage.setOnClickListener{
            PermissionsHelper.getUsagePermission(this)
        }

        write.setOnClickListener{
              PermissionsHelper.getWriteSettingsPermission(this)
        }

        accessablity.setOnClickListener {
         PermissionsHelper.getAccessibilityService(this)
        }

        overlay.setOnClickListener {
              PermissionsHelper.getOverlayPermission(this)
        }

        admin.setOnClickListener {
           PermissionsHelper.getAdminPermission(this)
        }

        disableUsb.setOnClickListener {
                PermissionsHelper.disableUSB(this)
        }

        other.setOnClickListener {

        }

    }

    private fun resize(){
        val displayMetrics = DisplayMetrics()
        (this as AppCompatActivity).windowManager
            .defaultDisplay
            .getMetrics(displayMetrics)
        val width = (displayMetrics.widthPixels/2 )-85
        for(i in arrayOf(usage, write, accessablity, overlay, admin, disableUsb, other)){
            i.layoutParams.height = width
            i.layoutParams.width=width
        }
    }

    override fun onResume() {
        changeBadge()
        super.onResume()
    }

    private fun changeBadge(){

        fun updateUI(badge:TextView){
            badge.setBackgroundColor( resources.getColor(R.color.green))
            badge.text="Done"
        }

        if(PermissionsHelper.isWrite(this)){
             updateUI(writeBadge)
        }

        if(PermissionsHelper.isUsage(this)){
            updateUI(usageBadge)
        }

        if(PermissionsHelper.isAccessServiceEnabled(this,WindowChangeDetectingService::class.java)){
            updateUI(accessablityBadge)
        }

        if(PermissionsHelper.isOverLay(this)){
            updateUI(overlayBadge)
        }

        if(PermissionsHelper.isUsb(this)){
            updateUI(disableUsbBadge)
        }

        if(PermissionsHelper.isAdmin(this)){
           updateUI(adminBadge)
        }

    }

}