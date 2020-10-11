/*
 *     Copyright (C) 2019 Kiosk Team.
 *
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

package tech.DevAsh.Launcher.gestures.ui

import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.Launcher.gestures.GestureHandler
import com.android.launcher3.Launcher
import com.android.launcher3.states.InternalStateHandler

class GestureHandlerInitListener(private val handler: GestureHandler) : InternalStateHandler() {

    override fun init(launcher: Launcher, alreadyOnHome: Boolean): Boolean {
        handler.onGestureTrigger((launcher as KioskLauncher).gestureController)
        return true
    }
}
