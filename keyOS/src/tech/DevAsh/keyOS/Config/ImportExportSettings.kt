package tech.DevAsh.keyOS.Config

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.launcher3.R
import com.google.gson.Gson
import kotlinx.android.synthetic.keyOS.activity_import_export_settings.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Api.IQRCodeService
import tech.DevAsh.keyOS.Config.Fragments.DisplayQr
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.KioskApp
import java.io.*
import javax.inject.Inject


class ImportExportSettings : AppCompatActivity() {

    @Inject
    lateinit var QRCodeService: IQRCodeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KioskApp.applicationComponents?.inject(this)
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
//            AlertHelper.showToast("Not supported in early access", this)
            DisplayQr(this).show(supportFragmentManager, "")
        }

        export.setOnClickListener {
            saveFile()
        }

        importFile.setOnClickListener {
            showFileChooser()
        }


    }

    private fun saveFile(){
        val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(packageManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                          packageName)
                == PackageManager.PERMISSION_GRANTED ){
            val jsonObject = (Gson().toJson(UserContext.user))
            generateNoteOnSD( "${System.currentTimeMillis()}.json", jsonObject.toString())
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
        }else if(requestCode==3){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                showFileChooser()
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
            AlertHelper.showToast("File exported to KeyOS/${fileName}", this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun showFileChooser() {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if(packageManager.checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                          packageName)
                == PackageManager.PERMISSION_GRANTED ){
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 0)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "Please install a File Manager.",
                               Toast.LENGTH_SHORT).show()
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, 3)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> if (resultCode == RESULT_OK) {
                val uri: Uri? = data?.data
                val backupData = (readTextFromUri(uri!!))
                try {
                    loadBackupData(backupData!!)
                }catch (e:Throwable){
                    e.printStackTrace()
                    AlertHelper.showToast("Invalid backup file", this)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadBackupData(backupData:String){
        val user:User = Gson().fromJson(backupData, User::class.java)
        UserContext.user = user
        RealmHelper.updateUser(user)
        AlertHelper.showToast("Imported Successfully", this)
    }


    private fun readTextFromUri(uri: Uri): String? {
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(
                inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        return stringBuilder.toString()
    }



}