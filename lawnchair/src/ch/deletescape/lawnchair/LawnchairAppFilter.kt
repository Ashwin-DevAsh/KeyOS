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

package ch.deletescape.lawnchair

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.android.launcher3.AppFilter

open class LawnchairAppFilter(context: Context) : AppFilter() {
    // 用来排除应用本身

    private val hideList = HashSet<ComponentName>()

    init {
        hideList.add(ComponentName(context, LawnchairLauncher::class.java.name))
    }

    override fun shouldShowApp(componentName: ComponentName?, user: UserHandle?): Boolean {
        return !hideList.contains(componentName) && super.shouldShowApp(componentName, user)
    }
}