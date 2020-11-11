package tech.DevAsh.keyOS.Helpers

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


object UpdateOriginalApk {

    fun showAlertDialog(context: Activity){
        val builder =   MaterialDialog.Builder(context)
        builder.title("Update require")
                .content(
                        "This feature not available in playstore version. But still you can update it for free.")
                .onPositive{ _, _->
                    update(context)
                }
                .negativeText("Cancel")
                .positiveText("Update")
                .show()
    }

    fun update(context: Activity){

        val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(context.packageManager.checkPermission(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE, context.packageName)== PackageManager.PERMISSION_GRANTED ){
            DownloadNewVersion(context).execute()

        }else{
            ActivityCompat.requestPermissions(context, permissions, 4)
        }
    }
}


open class DownloadNewVersion(val activity: Activity) : AsyncTask<String?, Int?, Boolean?>() {

    val bar = ProgressDialog(activity, R.style.MyProgressDialog)
    var location : String?=null

    override fun onPreExecute() {
        super.onPreExecute()
        showNotification("Downloading...","100/0",100,0)
    }


    override  fun onPostExecute(result: Boolean?) {
        showNotification("KeyOS Update","Download Completed",0,0)
        OpenNewVersion(location!!, activity)
        super.onPostExecute(result)


    }

    override fun doInBackground(vararg arg0: String?): Boolean {
        var flag: Boolean
        try {
            val url = URL("https://www.keyos.digital/download")
            val c: HttpURLConnection = url.openConnection() as HttpURLConnection
            c.requestMethod = "GET"
            c.doOutput = false
            c.connect()
            location = Environment.getExternalStorageDirectory().toString() + "/Download/"
            val file = File(location)
            file.mkdirs()
            val outputFile = File(file, "KeyOS.apk")
            if (outputFile.exists()) {
                outputFile.delete()
            }
            val fos = FileOutputStream(outputFile)
            val `is`: InputStream = c.inputStream
            val total_size = 1000 //Integer.parseInt(response.getFirstHeader("Content-Length").getValue());
            val buffer = ByteArray(1024)
            var len1 = 0
            var per = 0
            var downloaded = 0
            while (`is`.read(buffer).also { len1 = it } != -1) {
                fos.write(buffer, 0, len1)
                downloaded += len1
                per = (downloaded * 100 / total_size)
                publishProgress(per)
            }

            fos.close()
            `is`.close()
            flag = true
        } catch (e: Exception) {
            e.printStackTrace()
            flag = false
        }
        return flag
    }

    override fun onProgressUpdate(vararg values: Int?) {
        showNotification("Downloading...","100/${values[0]!!}",100,values[0]!!)
        super.onProgressUpdate(*values)
    }






    private fun showNotification(title: String,subTitle:String,maxInt: Int,minInt: Int){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "Update"
            val channelName: CharSequence = "Update"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)

            val builder: Notification.Builder = Notification.Builder(activity, channelId)
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title)
                    .setContentText(subTitle)
                    .setOngoing(true)
                    .setProgress(maxInt,minInt,false)
                    .setSmallIcon(R.drawable.ic_key_ring)
                    .setAutoCancel(true)
            val notification: Notification = builder.build()
            notification.flags = Notification.FLAG_AUTO_CANCEL
            notificationManager.notify(1,notification)
        } else {
            val builder = NotificationCompat.Builder(activity)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title)
                    .setContentText(subTitle)
                    .setOngoing(true)
                    .setProgress(maxInt,minInt,false)
                    .setSmallIcon(R.drawable.ic_key_ring)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
            val notification: Notification = builder.build()
            notification.flags = Notification.FLAG_AUTO_CANCEL
            (activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1,notification)
        }
    }

    private fun OpenNewVersion(location: String, context: Activity) {
        val uri: Uri = FileProvider.getUriForFile(context,
                                                  BuildConfig.APPLICATION_ID + ".provider",
                                                  File(location+"KeyOS.apk"))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)






    }
}

