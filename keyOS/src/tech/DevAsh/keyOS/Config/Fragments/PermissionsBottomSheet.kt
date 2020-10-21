package tech.DevAsh.KeyOS.Config.Fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.keyOS.sheet_permissions.*
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.KeyOS.Services.WindowChangeDetectingService


class PermissionsBottomSheet(private val activity: AppCompatActivity) : BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View? {




        return inflater.inflate(R.layout.sheet_permissions,
                                container,
                                false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.behavior.peekHeight = sheet.height
                sheet.parent.parent.requestLayout()
            }
        }
        updateUi()
        onClick()
    }





    override fun onResume() {
        updateUi()
        super.onResume()
    }

    private fun updateUi(){
        if(PermissionsHelper.isWrite(activity)){
            writeSettings.isChecked=true
        }

        if(PermissionsHelper.isUsage(activity)){
            usage.isChecked=true
        }

        if(PermissionsHelper.isAccessServiceEnabled(activity,
                                                    WindowChangeDetectingService::class.java)){
            accessablity.isChecked=true
        }

        if(PermissionsHelper.isOverLay(activity)){
            overlay.isChecked=true
        }

        if(PermissionsHelper.isAdmin(activity)){
           admin.isChecked=true
        }

        if (PermissionsHelper.isNotificationEnabled(activity)){
            notificationAccess.isChecked = true
        }


        if(PermissionsHelper.isRunTime(activity)){
            other.isChecked = true
        }

        if(PermissionsHelper.isUsb(activity)){
            disableUsb.isChecked = true
        }

    }

    private fun onClick(){

        notificationAccess.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getNotificationPermission(activity)
        }

        usage.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getUsagePermission(activity)
        }

        writeSettings.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getWriteSettingsPermission(activity)
        }

        accessablity.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getAccessibilityService(activity)
        }

        overlay.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getOverlayPermission(activity)
        }

        admin.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getAdminPermission(activity)
        }

        disableUsb.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.disableUSB(activity)
        }

        other.setOnCheckedChangeListener { _, _ ->
            this.dismiss()
            PermissionsHelper.getRuntimePermission(activity, PermissionsHelper.runTimePermissions,
                                                   0)
        }
    }


}