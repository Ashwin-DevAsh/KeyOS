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

package tech.DevAsh.Launcher.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import tech.DevAsh.Launcher.*
import tech.DevAsh.Launcher.colors.ColorEngine
import tech.DevAsh.Launcher.colors.ThemeAttributeColorResolver
import tech.DevAsh.Launcher.twilight.TwilightListener
import tech.DevAsh.Launcher.twilight.TwilightState
import tech.DevAsh.Launcher.util.SingletonHolder
import tech.DevAsh.Launcher.util.hasFlag
import tech.DevAsh.Launcher.util.removeFlag
import com.android.launcher3.R
import com.android.launcher3.uioverrides.WallpaperColorInfo
import tech.DevAsh.keyOS.KioskApp

/*
 * Copyright (C) 2018 paphonb@xda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class ThemeManager(val context: Context) : WallpaperColorInfo.OnChangeListener, TwilightListener,
        ColorEngine.OnColorChangeListener {

    private val app = context.KioskApp
    private val wallpaperColorInfo = WallpaperColorInfo.getInstance(context)!!
    private val listeners = HashSet<ThemeOverride>()
    private val prefs = context.KioskPrefs
    private var themeFlags = 0
    private var usingNightMode = context.resources.configuration.usingNightMode
        set(value) {
            if (field != value) {
                field = value
                updateTheme()
            }
        }

    val isDark get() = themeFlags and THEME_DARK != 0
    val supportsDarkText get() = themeFlags and THEME_DARK_TEXT != 0
    val displayName: String get() {
        val values = context.resources.getIntArray(R.array.themeValues)
        val strings = context.resources.getStringArray(R.array.themes)
        val index = values.indexOf(themeFlags)
        return strings.getOrNull(index) ?: context.resources.getString(R.string.theme_auto)
    }

    private val handler = Handler()
    private var listenToTwilight = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                } else {
                }
            }
        }
    private var isDuringNight = false
        set(value) {
            field = value
            if (!prefs.launcherTheme.hasFlag(THEME_FOLLOW_DAYLIGHT)) return
            if (themeFlags.hasFlag(THEME_DARK) != value) {
                updateTheme()
            }
        }

    private var colorEngineDarkText: Boolean? = null
        set(value) {
            if (field != value) {
                field = value
                updateTheme()
            }
        }

    init {
        updateTheme()
        wallpaperColorInfo.addOnChangeListener(this)
    }

    fun registerColorListener() {
        ColorEngine.getInstance(context).addColorChangeListeners(this, ColorEngine.Resolvers.WORKSPACE_ICON_LABEL)
    }

    override fun onColorChange(resolveInfo: ColorEngine.ResolveInfo) {
        if (!ThemeAttributeColorResolver::class.java.isAssignableFrom(resolveInfo.resolverClass)) {
            colorEngineDarkText = resolveInfo.isDark
        } else {
            colorEngineDarkText = null
        }
    }

    fun addOverride(themeOverride: ThemeOverride) {
        synchronized(listeners) {
            removeDeadListeners()
            listeners.add(themeOverride)
        }
        themeOverride.applyTheme(themeFlags)
    }

    fun removeOverride(themeOverride: ThemeOverride) {
        synchronized(listeners) {
            listeners.remove(themeOverride)
        }
    }

    fun getCurrentFlags() = themeFlags

    private fun removeDeadListeners() {
        val it = listeners.iterator()
        while (it.hasNext()) {
            if (!it.next().isAlive) {
                it.remove()
            }
        }
    }

    override fun onExtractedColorsChanged(ignore: WallpaperColorInfo?) {
        updateTheme()
    }

    fun updateTheme() {
        val theme = updateTwilightState(prefs.launcherTheme)
        val isBlack = isBlack(theme)

        val isDark = when {
            theme.hasFlag(THEME_FOLLOW_NIGHT_MODE) -> usingNightMode
            theme.hasFlag(THEME_FOLLOW_WALLPAPER) -> wallpaperColorInfo.isDark
            theme.hasFlag(THEME_FOLLOW_DAYLIGHT) -> isDuringNight
            else -> theme.hasFlag(THEME_DARK)
        }

        val supportsDarkText = when {
            colorEngineDarkText != null -> colorEngineDarkText == true
            theme.hasFlag(THEME_DARK_TEXT) -> true
            theme.hasFlag(THEME_FOLLOW_WALLPAPER) -> wallpaperColorInfo.supportsDarkText()
            else -> false
        }

        var newFlags = 0
        if (supportsDarkText) newFlags = newFlags or THEME_DARK_TEXT
        if (isDark) newFlags = newFlags or THEME_DARK
        if (isBlack) newFlags = newFlags or THEME_USE_BLACK
        if (newFlags == themeFlags) return
        themeFlags = newFlags
        reloadActivities()
        synchronized(listeners) {
            removeDeadListeners()
            listeners.forEach { it.onThemeChanged(themeFlags) }
        }
    }

    private fun updateTwilightState(theme: Int): Int {
        if (!theme.hasFlag(THEME_FOLLOW_DAYLIGHT)) {
            listenToTwilight = false
            return theme
        }


        BlankActivity.requestPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                KioskLauncher.REQUEST_PERMISSION_LOCATION_ACCESS) { granted ->
            if (granted) {
                listenToTwilight = true
            } else {
                prefs.launcherTheme = theme.removeFlag(THEME_DARK_MASK)
            }
        }

        return theme.removeFlag(THEME_DARK_MASK)
    }

    override fun onTwilightStateChanged(state: TwilightState?) {
        isDuringNight = state?.isNight == true
    }

    private fun reloadActivities() {
        HashSet(app.activityHandler.activities).forEach {
            if (it is ThemeableActivity) {
                it.onThemeChanged()
            } else {
                it.recreate()
            }
        }
    }

    fun updateNightMode(newConfig: Configuration) {
        usingNightMode = newConfig.usingNightMode
    }

    interface ThemeableActivity {

        fun onThemeChanged()
    }

    companion object : SingletonHolder<ThemeManager, Context>(ensureOnMainThread(useApplicationContext(::ThemeManager))) {

        const val THEME_FOLLOW_WALLPAPER = 1         // 000001 = 1
        const val THEME_DARK_TEXT = 1 shl 1          // 000010 = 2
        const val THEME_DARK = 1 shl 2               // 000100 = 4
        const val THEME_USE_BLACK = 1 shl 3          // 001000 = 8
        const val THEME_FOLLOW_NIGHT_MODE = 1 shl 4  // 010000 = 16
        const val THEME_FOLLOW_DAYLIGHT = 1 shl 5    // 100000 = 32

        const val THEME_AUTO_MASK = THEME_FOLLOW_WALLPAPER or THEME_FOLLOW_NIGHT_MODE or THEME_FOLLOW_DAYLIGHT
        const val THEME_DARK_MASK = THEME_DARK or THEME_AUTO_MASK

        fun isDarkText(flags: Int) = (flags and THEME_DARK_TEXT) != 0
        fun isDark(flags: Int) = (flags and THEME_DARK) != 0
        fun isBlack(flags: Int) = (flags and THEME_USE_BLACK) != 0
    }
}
