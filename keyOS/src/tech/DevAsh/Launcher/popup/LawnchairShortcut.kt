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

package tech.DevAsh.Launcher.popup

import android.content.Context
import android.view.View
import tech.DevAsh.Launcher.lawnchairPrefs
import tech.DevAsh.Launcher.override.CustomInfoProvider
import tech.DevAsh.Launcher.util.LawnchairSingletonHolder
import com.android.launcher3.*
import com.android.launcher3.popup.SystemShortcut
import com.google.android.apps.nexuslauncher.CustomBottomSheet

class LawnchairShortcut(private val context: Context) {

    private val shortcuts = listOf(
            ShortcutEntry("edit", Edit(), true),
            ShortcutEntry("widgets", SystemShortcut.Widgets(), true),
            ShortcutEntry("install", SystemShortcut.Install(), true)
    )

    inner class ShortcutEntry(key: String, val shortcut: SystemShortcut<*>, enabled: Boolean) {

        val enabled by context.lawnchairPrefs.BooleanPref("pref_iconPopup_$key", enabled)
    }

    val enabledShortcuts get() = shortcuts.filter { it.enabled }.map { it.shortcut }



    class Edit : SystemShortcut<Launcher>(R.drawable.ic_edit_no_shadow, R.string.action_preferences) {

        override fun getOnClickListener(launcher: Launcher, itemInfo: ItemInfo): View.OnClickListener? {
            if (launcher.lawnchairPrefs.lockDesktop) return null
            if (!CustomInfoProvider.isEditable(itemInfo)) return null
            return View.OnClickListener {
                AbstractFloatingView.closeAllOpenViews(launcher)
                CustomBottomSheet.show(launcher, itemInfo)
            }
        }
    }

    companion object : LawnchairSingletonHolder<LawnchairShortcut>(::LawnchairShortcut)

}
