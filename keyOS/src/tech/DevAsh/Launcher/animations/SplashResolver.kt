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

package tech.DevAsh.Launcher.animations

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import tech.DevAsh.Launcher.getColorAttr
import tech.DevAsh.Launcher.getDrawableAttrNullable
import tech.DevAsh.Launcher.getIntAttr
import tech.DevAsh.Launcher.settings.ui.SettingsActivity
import tech.DevAsh.Launcher.theme.ThemeOverride
import tech.DevAsh.Launcher.util.LawnchairSingletonHolder
import com.android.launcher3.BuildConfig
import com.android.launcher3.Utilities

class SplashResolver(private val context: Context) {

    fun loadSplash(intent: Intent): SplashData {
        val activityInfo = intent.resolveActivityInfo(context.packageManager, 0)
        val themedContext: Context
        themedContext = if (activityInfo == null
                            || (activityInfo.packageName == BuildConfig.APPLICATION_ID
                            && activityInfo.name == SettingsActivity::class.java.name)) {
            ContextThemeWrapper(context, ThemeOverride.Settings().getTheme(context))
        } else {
            val theme = activityInfo.themeResource
            val packageContext = context.createPackageContext(activityInfo.packageName, 0)
            ContextThemeWrapper(packageContext, theme)
        }
        val layoutInDisplayCutoutMode = if (Utilities.ATLEAST_P)
            themedContext.getIntAttr(android.R.attr.windowLayoutInDisplayCutoutMode) else 0
        return SplashData(
                themedContext.getDrawableAttrNullable(android.R.attr.windowBackground),
                themedContext.getColorAttr(android.R.attr.statusBarColor),
                themedContext.getColorAttr(android.R.attr.navigationBarColor),
                layoutInDisplayCutoutMode)
    }

    data class SplashData(val background: Drawable?, val statusColor: Int, val navColor: Int,
                          val layoutInDisplayCutoutMode: Int) {




    }

    companion object : LawnchairSingletonHolder<SplashResolver>(::SplashResolver)
}
