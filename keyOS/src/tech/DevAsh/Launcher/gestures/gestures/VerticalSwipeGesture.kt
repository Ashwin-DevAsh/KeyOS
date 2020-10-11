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
import tech.DevAsh.Launcher.gestures.handlers.*

class VerticalSwipeGesture(controller: GestureController) : Gesture(controller) {

    override val isEnabled = true

    private val swipeUpHandler by controller.createHandlerPref("pref_gesture_swipe_up",
                                                               StartAppSearchGestureHandler(controller.launcher, null))
    private val swipeDownHandler by controller.createHandlerPref("pref_gesture_swipe_down",
            NotificationsOpenGestureHandler(controller.launcher, null))

    val customSwipeUp get() = swipeUpHandler !is VerticalSwipeGestureHandler
    val customSwipeDown get() = swipeDownHandler !is NotificationsOpenGestureHandler

    fun onSwipeUp() {
        swipeUpHandler.onGestureTrigger(controller)
    }

    fun onSwipeDown() {
        swipeDownHandler.onGestureTrigger(controller)
    }

}
