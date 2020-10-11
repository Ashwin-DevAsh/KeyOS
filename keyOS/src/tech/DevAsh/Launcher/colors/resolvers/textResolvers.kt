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

package tech.DevAsh.Launcher.colors.resolvers

import android.graphics.Color
import androidx.annotation.Keep
import androidx.core.graphics.ColorUtils
import tech.DevAsh.Launcher.colors.ThemeAttributeColorResolver
import tech.DevAsh.Launcher.KioskPrefs
import tech.DevAsh.Launcher.sensors.BrightnessManager
import com.android.launcher3.R
import com.android.launcher3.graphics.IconPalette
import kotlin.math.max
import kotlin.math.min

@Keep
class DrawerLabelAutoResolver(config: Config) : ThemeAttributeColorResolver(config), BrightnessManager.OnBrightnessChangeListener {

    override val colorAttr = android.R.attr.textColorSecondary
    private var brightness = 1f
    private val prefs = context.KioskPrefs

    override fun startListening() {
        super.startListening()
        if (prefs.brightnessTheme) {
            BrightnessManager.getInstance(context).addListener(this)
        }
    }

    override fun onBrightnessChanged(illuminance: Float) {
        brightness = min(max(illuminance - 4f, 0f), 35f) / 35
        notifyChanged()
    }

    override fun resolveColor(): Int {
        if (prefs.brightnessTheme) {
            val bg = ColorUtils.blendARGB(Color.BLACK, Color.WHITE, brightness)
            val fg = ColorUtils.blendARGB(Color.WHITE, Color.BLACK, brightness)
            return IconPalette.ensureTextContrast(fg, bg)
        }
        return super.resolveColor()
    }

    override fun stopListening() {
        super.stopListening()
        BrightnessManager.getInstance(context).removeListener(this)
    }
}

@Keep
class WorkspaceLabelAutoResolver(config: Config) : ThemeAttributeColorResolver(config) {

    override val colorAttr = R.attr.workspaceTextColor
}
