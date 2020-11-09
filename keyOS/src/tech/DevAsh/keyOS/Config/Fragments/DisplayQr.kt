package tech.DevAsh.keyOS.Config.Fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getColor
import com.android.launcher3.BuildConfig
import com.android.launcher3.BuildConfig.QR_KEY
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotlinx.android.synthetic.keyOS.fragment_display_qr.*
import kotlinx.android.synthetic.keyOS.widget_listtile_apps.*
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


        onClick()



        importAndExport.QRCodeService
                .setPolicyData(SetPolicyData(uuid, UserContext.user!!))
                ?.enqueue(callBack)

        super.onViewCreated(view, savedInstanceState)
    }


    fun onClick(){
        share.setOnClickListener{
            val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if(importAndExport.packageManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,importAndExport.packageName)== PackageManager.PERMISSION_GRANTED ){
                Handler().postDelayed({
                                          share()
                                      },0)
            }else{
                ActivityCompat.requestPermissions(requireActivity(), permissions, 4)
            }
        }
    }

    fun share(){
        val view = shareContent
        val bitmap2 = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap2)
        view.draw(canvas)
        val bitmapPath = MediaStore.Images.Media.insertImage(importAndExport.contentResolver, bitmap2, "title", null)
        val bitmapUri = Uri.parse(bitmapPath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
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

            t.printStackTrace()
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