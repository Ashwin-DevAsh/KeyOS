package tech.DevAsh.keyOS.Config

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import com.android.launcher3.R
import com.google.gson.Gson
import kotlinx.android.synthetic.keyOS.activity_import_export_settings.*
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Config.Fragments.DisplayQr
import java.io.File
import java.io.FileWriter
import java.io.IOException

class ImportExportSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_export_settings)
        onClick()

    }



    fun onClick(){
        scan.setOnClickListener {
            val permissions = arrayOf(android.Manifest.permission.CAMERA)
            if(packageManager.checkPermission(
                            android.Manifest.permission.CAMERA,
                            packageName
                                             )== PackageManager.PERMISSION_GRANTED ){
                startActivity(
                        Intent(
                                this,
                                QrScanner::class.java
                              )
                             )
            }else{
                ActivityCompat.requestPermissions(this, permissions, 1)
            }
        }

        back.setOnClickListener {
            super.onBackPressed()
        }

        displayQr.setOnClickListener {
            DisplayQr().show(supportFragmentManager,"")
        }

        export.setOnClickListener {
            saveFile()
        }


    }

    private fun saveFile(){
        val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(packageManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, packageName)
                == PackageManager.PERMISSION_GRANTED ){
            val jsonObject = (Gson().toJson(UserContext.user))
            generateNoteOnSD("KeyOS.json", jsonObject.toString())
        }else{
            ActivityCompat.requestPermissions(this, permissions, 2)
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
                                           ) {

        if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(this, QrScanner::class.java))
            }
        }else if(requestCode==2){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                saveFile()
            }
        }
    }

    private fun generateNoteOnSD(fileName: String?, sBody: String?) {
        try {
            val root = File(Environment.getExternalStorageDirectory(), "KeyOS")
            if (!root.exists()) {
                root.mkdirs()
            }
            val file = File(root, fileName)
            val writer = FileWriter(file)
            writer.append(sBody)
            writer.flush()
            writer.close()
            AlertHelper.showSnackbar("File exported to KeyOS/KeyOS.json",this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}