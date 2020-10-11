/*
 *     This file is part of Kiosk Launcher.
 *
 *     Kiosk Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Kiosk Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Kiosk Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.DevAsh.Launcher.gestures.handlers

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.Keep
import android.view.View
import tech.DevAsh.Launcher.gestures.GestureController
import tech.DevAsh.Launcher.gestures.GestureHandler
import com.android.launcher3.R
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException

@Keep
class NotificationsOpenGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_notifications)

    @SuppressLint("PrivateApi", "WrongConstant")
    override fun onGestureTrigger(controller: GestureController, view: View?) {
        try {
            Class.forName("android.app.StatusBarManager")
                    .getMethod("expandNotificationsPanel")
                    .invoke(controller.launcher.getSystemService("statusbar"))
        } catch (ex: ClassNotFoundException) {
        } catch (ex: NoSuchMethodException) {
        } catch (ex: IllegalAccessException) {
        } catch (ex: InvocationTargetException) {
        }
    }
}

@Keep
class NotificationsCloseGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_close_notifications)

    @SuppressLint("PrivateApi", "WrongConstant")
    override fun onGestureTrigger(controller: GestureController, view: View?) {
        try {
            Class.forName("android.app.StatusBarManager")
                    .getMethod("collapsePanels")
                    .invoke(controller.launcher.getSystemService("statusbar"))
        } catch (ex: ClassNotFoundException) {
        } catch (ex: NoSuchMethodException) {
        } catch (ex: IllegalAccessException) {
        } catch (ex: InvocationTargetException) {
        }

    }
}