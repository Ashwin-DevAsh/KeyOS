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

package tech.DevAsh.Launcher.iconpack

import android.content.Context
import com.android.launcher3.FastBitmapDrawable
import com.android.launcher3.ItemInfoWithIcon
import com.android.launcher3.ShortcutInfo
import com.google.android.apps.nexuslauncher.DynamicDrawableFactory
import com.google.android.apps.nexuslauncher.clock.CustomClock

class KioskDrawableFactory(context: Context) : DynamicDrawableFactory(context) {

    private val iconPackManager = IconPackManager.getInstance(context)
    val customClockDrawer by lazy { CustomClock(context) }

    override fun newIcon(info: ItemInfoWithIcon): FastBitmapDrawable {
        return iconPackManager.newIcon((info as? ShortcutInfo)?.customIcon ?: info.iconBitmap,
                info, this).also { it.setIsDisabled(info.isDisabled) }
    }
}
