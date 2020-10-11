package tech.DevAsh.KeyOS.Services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.PasswordPrompt


class WindowChangeDetectingService : AccessibilityService() {


    override fun onKeyEvent(event: KeyEvent): Boolean {
//        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && HomeActivity._launcher!=null) {
//            val intent = Intent(HomeActivity._launcher, PasswordPrompt::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            HomeActivity._launcher.startActivity(intent)
//            return true
//        }
        return super.onKeyEvent(event)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event
                .eventType
        ) {

        }
    }


    override fun onInterrupt() {}
}