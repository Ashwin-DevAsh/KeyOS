package tech.DevAsh.keyOS.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import tech.DevAsh.KeyOS.Services.UsageAccessService

class AutoRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        context.startService(Intent(context, UsageAccessService::class.java)) // Restart your service here
    }
}