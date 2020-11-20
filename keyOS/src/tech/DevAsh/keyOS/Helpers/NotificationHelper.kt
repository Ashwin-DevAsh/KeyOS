package tech.DevAsh.keyOS.Helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.android.launcher3.R

object NotificationHelper {
    fun startAsForegroundNotification(service:Service){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                    service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "KeyOS Protection"
            val channelName: CharSequence = "Protection"
            val importance = NotificationManager.IMPORTANCE_MIN
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)
            val builder: Notification.Builder = Notification.Builder(service, channelId)
                    .setContentTitle("KeyOS Protection")
                    .setContentText("Your device completely protected by keyOS")
                    .setSmallIcon(R.drawable.ic_notification_key_ring)
                    .setAutoCancel(false)
            val notification: Notification = builder.build()
             service.startForeground(2, notification)
        } else {
            val builder = NotificationCompat.Builder(service)
                    .setContentTitle("KeyOS Protection")
                    .setContentTitle("Your device completely protected by keyOS")
                    .setSmallIcon(R.drawable.ic_notification_key_ring)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(false)
            val notification: Notification = builder.build()
            service.startForeground(2, notification)
        }
    }

}