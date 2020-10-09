/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.DevAsh.Launcher.gestures.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import tech.DevAsh.Launcher.LawnchairLauncher
import tech.DevAsh.Launcher.gestures.BlankGestureHandler
import tech.DevAsh.Launcher.gestures.GestureController
import tech.DevAsh.Launcher.gestures.GestureHandler
import tech.DevAsh.Launcher.gestures.LawnchairShortcutActivity
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R

class RunHandlerActivity : Activity() {
    private val fallback by lazy { BlankGestureHandler(this, null) }
    private val launcher
        get() = (LauncherAppState.getInstance(this).launcher as? LawnchairLauncher)
    private val controller
        get() = launcher?.gestureController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == LawnchairShortcutActivity.START_ACTION) {
            val handlerString = intent.getStringExtra(LawnchairShortcutActivity.EXTRA_HANDLER)
            if (handlerString != null) {
                val handler = GestureController.createGestureHandler(this.applicationContext, handlerString, fallback)
                if (handler.requiresForeground) {
                    val listener = GestureHandlerInitListener(handler)
                    val homeIntent = listener.addToIntent(
                            Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_HOME)
                                    .setPackage(packageName)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

                    startActivity(homeIntent)
                } else {
                    triggerGesture(handler)
                }
            }
        }
        finish()
    }

    private fun triggerGesture(handler: GestureHandler) = if (controller != null) {
        handler.onGestureTrigger(controller!!)
    } else {
        Toast.makeText(this.applicationContext, R.string.failed, Toast.LENGTH_LONG).show()
    }
}