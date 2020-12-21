package tech.DevAsh.KeyOS.Config.Fragments

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.dev.sheet_permissions.*
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import tech.DevAsh.KeyOS.Services.WindowChangeDetectingService


class PermissionsBottomSheet : BottomSheetDialogFragment() {

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
        if(PermissionsHelper.isWrite(requireActivity())){
            writeSettings.isChecked=true
        }

        if(PermissionsHelper.isUsage(requireActivity())){
            usage.isChecked=true
        }

        if(PermissionsHelper.isAccessServiceEnabled(requireActivity(),
                                                    WindowChangeDetectingService::class.java)){
            accessablity.isChecked=true
        }

        if(PermissionsHelper.isOverLay(requireActivity())){
            overlay.isChecked=true
        }

        if(PermissionsHelper.isAdmin(requireActivity())){
           admin.isChecked=true
        }

        if (PermissionsHelper.isNotificationEnabled(requireActivity())){
            notificationAccess.isChecked = true
        }


        if(PermissionsHelper.isRunTime(requireActivity())){
            other.isChecked = true
        }

        if(PermissionsHelper.isUsb(requireActivity())){
            disableUsb.isChecked = true
        }

    }

    private fun onClick(){

        notificationAccess.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getNotificationPermission(requireActivity() as AppCompatActivity)
        }

        usage.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getUsagePermission(requireActivity() as AppCompatActivity)
        }

        writeSettings.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            if(Build.VERSION.SDK_INT>=23)
            PermissionsHelper.getWriteSettingsPermission(requireActivity() as AppCompatActivity)
        }

        accessablity.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getAccessibilityService(requireActivity() as AppCompatActivity)
        }

        overlay.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            if(Build.VERSION.SDK_INT>=23)
            PermissionsHelper.getOverlayPermission(requireActivity() as AppCompatActivity)
        }

        admin.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.getAdminPermission(requireActivity() as AppCompatActivity)
        }

        disableUsb.setOnCheckedChangeListener{ _, _->
            this.dismiss()
            PermissionsHelper.disableUSB(requireActivity() as AppCompatActivity)
        }

        other.setOnCheckedChangeListener { _, _ ->
            this.dismiss()
            val permission = Array(PermissionsHelper.runTimePermissions.size) { index -> PermissionsHelper.runTimePermissions[index] }
            PermissionsHelper.getRuntimePermission(requireActivity() as AppCompatActivity, permission, 0)
        }
    }


}