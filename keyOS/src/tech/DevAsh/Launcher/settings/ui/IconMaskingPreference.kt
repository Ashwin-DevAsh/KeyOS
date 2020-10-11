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

package tech.DevAsh.Launcher.settings.ui

import android.content.Context
import android.util.AttributeSet
import tech.DevAsh.Launcher.iconpack.IconPackManager
import tech.DevAsh.Launcher.preferences.StyledSwitchPreferenceCompat

class IconMaskingPreference(context: Context, attrs: AttributeSet?) :
        StyledSwitchPreferenceCompat(context, attrs) {

    private val manager = IconPackManager.getInstance(context)
    private val listener = {
        isVisible = manager.maskSupported()
    }

    override fun onAttached() {
        super.onAttached()
        manager.addListener(listener)
    }

    override fun onDetached() {
        super.onDetached()
        manager.removeListener(listener)
    }
}
