package tech.DevAsh.keyOS.Helpers

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import tech.DevAsh.keyOS.Api.ApiContext
import tech.DevAsh.keyOS.Helpers.KioskHelpers.AlertDeveloper
import tech.DevAsh.keyOS.KioskApp
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object UpdateOriginalApk {

    fun showAlertDialog(context: Activity){
        val builder =   MaterialDialog.Builder(context)
        builder.title("Download KeyOS")
                .content(
                        "This feature not available in playstore version." +
                        " But still you can download original version for free.")
                .onPositive{ _, _->
                    update(context)
                }
                .negativeText("Cancel")
                .positiveText("Download")
                .show()
    }

    fun update(context: Activity){
        AnalyticsHelper.logEvent(context, "UpdatePro Apk")
        val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(context.packageManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, context.packageName)== PackageManager.PERMISSION_GRANTED ){
          downloadApk(context)
        }else{
            ActivityCompat.requestPermissions(context, permissions, 4)
        }
    }

    private fun downloadApk(context: Activity){
        AlertDeveloper.sendProApkDownloadAlert(context)
        val url = "${ApiContext.basicServiceUrl}download/"
        val downloadManager: DownloadManager? =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val uri = Uri.parse(url)
        val request: DownloadManager.Request = DownloadManager.Request(uri)
        request.setVisibleInDownloadsUi(true)
        request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "KeyOS.apk")
        downloadManager?.enqueue(request)

    }
}

