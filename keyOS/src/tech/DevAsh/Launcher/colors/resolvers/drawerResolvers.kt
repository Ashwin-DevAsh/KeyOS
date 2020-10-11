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
import tech.DevAsh.Launcher.KioskPreferences
import tech.DevAsh.Launcher.colors.ColorEngine
import tech.DevAsh.Launcher.colors.ThemeAttributeColorResolver
import tech.DevAsh.Launcher.colors.WallpaperColorResolver
import tech.DevAsh.Launcher.KioskPrefs
import tech.DevAsh.Launcher.sensors.BrightnessManager
import tech.DevAsh.Launcher.theme.ThemeManager
import com.android.launcher3.R
import com.android.launcher3.util.Themes
import kotlin.math.max
import kotlin.math.min

@Keep
class DrawerQsbAutoResolver(config: Config) : ColorEngine.ColorResolver(config), KioskPreferences.OnPreferenceChangeListener, BrightnessManager.OnBrightnessChangeListener {

    override val themeAware = true
    private val isDark get() =  ThemeManager.getInstance(engine.context).isDark
    private val lightResolver = DrawerQsbLightResolver(Config("DrawerQsbAutoResolver@Light", engine, {
        _, _ -> if (!isDark) notifyChanged()
    }))
    private val darkResolver = DrawerQsbDarkResolver(Config("DrawerQsbAutoResolver@Dark", engine, {
        _, _ -> if (isDark) notifyChanged()
    }))
    private val prefs = context.KioskPrefs
    private var brightness = 1f

    override fun startListening() {
        super.startListening()
        if (prefs.brightnessTheme) {
            BrightnessManager.getInstance(context).addListener(this)
        }
    }

    override fun onBrightnessChanged(illuminance: Float) {
        brightness = min(max(illuminance - 2f, 0f), 35f) / 35
        notifyChanged()
    }

    override fun onValueChanged(key: String, prefs: KioskPreferences, force: Boolean) {
        notifyChanged()
    }

    override fun stopListening() {
        super.stopListening()
        BrightnessManager.getInstance(context).removeListener(this)
    }

    override fun resolveColor() = if (prefs.brightnessTheme) {
        ColorUtils.blendARGB(Color.BLACK, Color.WHITE, brightness)
    } else if (isDark) darkResolver.resolveColor() else lightResolver.resolveColor()

    override fun getDisplayName() = engine.context.resources.getString(R.string.theme_based)
}

@Keep
class DrawerQsbLightResolver(config: Config) : WallpaperColorResolver(config), KioskPreferences.OnPreferenceChangeListener {

    override val themeAware = true
    private val isDark get() = ThemeManager.getInstance(engine.context).isDark

    override fun onValueChanged(key: String, prefs: KioskPreferences, force: Boolean) {
        notifyChanged()
    }

    override fun resolveColor() = engine.context.resources.getColor(
            if (isDark)
                R.color.qsb_background_drawer_dark
            else
                R.color.qsb_background_drawer_default
    ).let {
        ColorUtils.compositeColors(ColorUtils
                .compositeColors(it, Themes.getAttrColor(themedContext, R.attr.allAppsScrimColor)),
                colorInfo.mainColor)
    }

    override fun getDisplayName() = engine.context.resources.getString(R.string.theme_light)
}

@Keep
class DrawerQsbDarkResolver(config: Config) : WallpaperColorResolver(config) {

    override val themeAware = true
    val color = engine.context.resources.getColor(R.color.qsb_background_drawer_dark_bar)

    override fun resolveColor() = ColorUtils.compositeColors(ColorUtils
            .compositeColors(color, Themes.getAttrColor(themedContext, R.attr.allAppsScrimColor)),
            colorInfo.mainColor)

    override fun getDisplayName() = engine.context.resources.getString(R.string.theme_dark)
}

@Keep
class ShelfBackgroundAutoResolver(config: Config) : ThemeAttributeColorResolver(config), BrightnessManager.OnBrightnessChangeListener {

    override val colorAttr = R.attr.allAppsScrimColor
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
            return ColorUtils.blendARGB(Color.BLACK, Color.WHITE, brightness)
        }
        return super.resolveColor()
    }

    override fun stopListening() {
        super.stopListening()
        BrightnessManager.getInstance(context).removeListener(this)
    }
}
