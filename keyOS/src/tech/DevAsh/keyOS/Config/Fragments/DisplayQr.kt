package tech.DevAsh.keyOS.Config.Fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.ContextCompat.getColor
import com.android.launcher3.BuildConfig
import com.android.launcher3.BuildConfig.QR_KEY
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotlinx.android.synthetic.keyOS.fragment_display_qr.*
import retrofit2.Call
import retrofit2.Response
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Api.Request.SetPolicyData
import tech.DevAsh.keyOS.Api.Response.BasicResponse
import tech.DevAsh.keyOS.Config.ImportExportSettings
import java.util.*


class DisplayQr(var importAndExport: ImportExportSettings) : BottomSheetDialogFragment() {

    val uuid = UUID.randomUUID().toString()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_display_qr, container, false)
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
        importAndExport.QRCodeService
                .setPolicyData(SetPolicyData(uuid, UserContext.user!!))
                ?.enqueue(callBack)
        super.onViewCreated(view, savedInstanceState)
    }


    var callBack = object :retrofit2.Callback<BasicResponse>{
        override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
            if(response.body()?.result=="success"){

                Handler().postDelayed({
                                          loadQr(uuid)
                                      }, 500)
            }else{
            onFailed()
            }
        }

        override fun onFailure(call: Call<BasicResponse>, t: Throwable) {

            onFailed()

        }

    }

    private fun loadQr(uuid: String){

        try {
            val jwt = Jwts.builder().claim("id", uuid)
                    .signWith(SignatureAlgorithm.HS256, QR_KEY)
                    .compact()

            val qrgEncoder = QRGEncoder(jwt.toString(), null,
                                        QRGContents.Type.TEXT, 1000)
            qrgEncoder.colorWhite = getColor(requireActivity(), R.color.colorPrimary)
            qrImage?.setImageBitmap(qrgEncoder.bitmap)
            loadingScreen.visibility=View.GONE
        } catch (e: Throwable) {
            e.printStackTrace()

            onFailed()
        }
    }

    fun onFailed(){

        Handler().postDelayed({
            try {
                dismiss()
                AlertHelper.showToast("Failed!", importAndExport)
            }catch (e:Throwable){}
                              }, 800)

    }



}