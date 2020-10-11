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

package tech.DevAsh.Launcher

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.android.launcher3.AppFilter

open class KioskAppFilter(context: Context) : AppFilter() {

    private val hideList = HashSet<ComponentName>()

    init {
        hideList.add(ComponentName(context, KioskLauncher::class.java.name))
    }

    override fun shouldShowApp(componentName: ComponentName?, user: UserHandle?): Boolean {
        return !hideList.contains(componentName) && super.shouldShowApp(componentName, user)
    }
}