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

package tech.DevAsh.Launcher.settings.ui.controllers

import android.content.Context
import androidx.annotation.Keep
import androidx.preference.Preference
import tech.DevAsh.Launcher.settings.ui.PreferenceController
import com.android.launcher3.R
import com.android.launcher3.states.RotationHelper

@Keep
class AllowRotationController(context: Context) : PreferenceController(context) {

    override val isVisible = !context.resources.getBoolean(R.bool.allow_rotation)

    override fun onPreferenceAdded(preference: Preference): Boolean {
        if (!super.onPreferenceAdded(preference)) return false
        preference.setDefaultValue(RotationHelper.getAllowRotationDefaultValue())
        return true
    }
}
