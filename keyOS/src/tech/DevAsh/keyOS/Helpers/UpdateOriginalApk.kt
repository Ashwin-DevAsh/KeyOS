package tech.DevAsh.keyOS.Helpers

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import tech.DevAsh.keyOS.KioskApp
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object UpdateOriginalApk {

    fun showAlertDialog(context: Activity){
        val builder =   MaterialDialog.Builder(context)
        builder.title("Update require")
                .content(
                        "This feature not available in playstore version. But still you can download it for free.")
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


    var location : String?=null


    private fun cancelNotification(activity: Activity){
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

    private fun showNotification(activity: Activity,title: String,subTitle:String,maxInt: Int,minInt: Int){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "Update"
            val channelName: CharSequence = "Update"
            val importance = NotificationManager.IMPORTANCE_MIN
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
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
            val notification: Notification = builder.build()
            notification.flags = Notification.FLAG_AUTO_CANCEL
            (activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1,notification)
        }
    }

    private fun openNewVersion(location: String, context: Activity) {
        val uri: Uri = FileProvider.getUriForFile(context,
                                                  BuildConfig.APPLICATION_ID + ".provider",
                                                  File(location+"KeyOS.apk"))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)

    }

    internal class DownloadNewVersion(val activity: Activity): AsyncTask<Any, Any, Any>() {

        override fun doInBackground(vararg params: Any?): Any {
            showNotification(activity,"Downloading...","0%",100,0)
            val response =  activity.KioskApp.updateService.update()?.execute()
            location = Environment.getExternalStorageDirectory().toString() + "/Download/"
            val file = File(location)
            file.mkdirs()
            val outputFile = File(file, "KeyOS.apk")
            if (outputFile.exists()) {
                outputFile.delete()
            }

            val fos = FileOutputStream(outputFile)
            val `is`: InputStream = response?.body()!!.byteStream()
            val total_size = response.body()!!.contentLength().toInt() //Integer.parseInt(response.getFirstHeader("Content-Length").getValue());
            val buffer = ByteArray(1024)
            var len1 = 0
            var per = 0
            var downloaded = 0
            while (`is`.read(buffer).also { len1 = it } != -1) {
                fos.write(buffer, 0, len1)
                downloaded += len1
                per = (downloaded * 100 / total_size)
                showNotification(activity,"Downloading...","${per}%",100,per!!)
            }
            fos.close()
            cancelNotification(activity)
            openNewVersion(location!!, activity)
            return true
        }

    }

}

