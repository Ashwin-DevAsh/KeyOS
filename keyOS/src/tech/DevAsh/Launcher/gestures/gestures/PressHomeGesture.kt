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

package tech.DevAsh.Launcher.gestures.gestures

import tech.DevAsh.Launcher.gestures.Gesture
import tech.DevAsh.Launcher.gestures.GestureController

class PressHomeGesture(controller: GestureController) : Gesture(controller) {

    private val handler by controller.createHandlerPref("pref_gesture_press_home")
    override val isEnabled = true

    override fun onEvent(): Boolean {
        handler.onGestureTrigger(controller)
        return true
    }
}
