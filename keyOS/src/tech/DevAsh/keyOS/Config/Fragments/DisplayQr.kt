package tech.DevAsh.keyOS.Config.Fragments

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.ContextCompat.getColor
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.keyOS.fragment_display_qr.*
import tech.DevAsh.KeyOS.Database.UserContext
import java.io.File
import java.io.FileWriter
import java.io.IOException


class DisplayQr : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_display_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadQr()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadQr(){
        val jsonObject =  (Gson().toJson(UserContext.user!!.basicSettings))
        val qrgEncoder = QRGEncoder(jsonObject.toString(), null,
                                    QRGContents.Type.TEXT, 1000)
        qrgEncoder.colorWhite = getColor(requireActivity(), R.color.colorPrimary)
        try {
            qrImage?.setImageBitmap(qrgEncoder.bitmap)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }



}