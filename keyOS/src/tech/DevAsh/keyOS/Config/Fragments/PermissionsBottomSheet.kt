package tech.DevAsh.KeyOS.Config.Fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.launcher3.R
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
              updateUi()
              onClick()
    }

    private fun updateUi(){
        if(PermissionsHelper.isWrite(activity)){
            writeSettings.isChecked=true
        }

        if(PermissionsHelper.isUsage(activity)){
            usage.isChecked=true
        }

        if(PermissionsHelper.isAccessServiceEnabled(activity, WindowChangeDetectingService::class.java)){
            accessablity.isChecked=true
        }

        if(PermissionsHelper.isOverLay(activity)){
            overlay.isChecked=true
        }

        if(PermissionsHelper.isAdmin(activity)){
           admin.isChecked=true
        }
    }

    private fun onClick(){
        usage.setOnCheckedChangeListener{_,_->
            this.dismiss()
            PermissionsHelper.getUsagePermission(activity)
        }

        writeSettings.setOnCheckedChangeListener{_,_->
            this.dismiss()
            PermissionsHelper.getWriteSettingsPermission(activity)
        }

        accessablity.setOnCheckedChangeListener{_,_->
            this.dismiss()
            PermissionsHelper.getAccessibilityService(activity)
        }

        overlay.setOnCheckedChangeListener{_,_->
            this.dismiss()
            PermissionsHelper.getOverlayPermission(activity)
        }

        admin.setOnCheckedChangeListener{_,_->
            this.dismiss()
            PermissionsHelper.getAdminPermission(activity)
        }

        other.setOnCheckedChangeListener{_,_->
            PermissionsHelper.getRuntimePermission(activity, arrayOf(
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.MODIFY_PHONE_STATE,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_CALL_LOG,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.ANSWER_PHONE_CALLS,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),0)
        }
    }

}