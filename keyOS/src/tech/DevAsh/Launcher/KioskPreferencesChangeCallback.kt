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

import tech.DevAsh.Launcher.blur.BlurWallpaperProvider
import com.android.launcher3.compat.UserManagerCompat
import com.android.launcher3.pageindicators.WorkspacePageIndicator

class KioskPreferencesChangeCallback(val launcher: KioskLauncher) {

    fun recreate() {
        if (launcher.shouldRecreate()) launcher.scheduleRecreate()
    }

    fun reloadApps() {
        UserManagerCompat.getInstance(launcher).userProfiles.forEach { launcher.model.onPackagesReload(it) }
    }

    fun reloadAll() {
        launcher.model.forceReload()
    }

    fun restart() {
        launcher.scheduleRestart()
    }

    fun refreshGrid() {
        launcher.refreshGrid()
    }

    fun updateBlur() {
        BlurWallpaperProvider.getInstance(launcher).updateAsync()
    }

    fun resetAllApps() {
    }

    fun updatePageIndicator() {
        val indicator = launcher.workspace.pageIndicator
        if (indicator is WorkspacePageIndicator) {
            indicator.updateLineHeight()
        }
    }




}
