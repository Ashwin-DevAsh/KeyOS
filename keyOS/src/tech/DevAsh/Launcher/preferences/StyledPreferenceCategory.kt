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

package tech.DevAsh.Launcher.preferences

import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.widget.TextView
import tech.DevAsh.Launcher.colors.ColorEngine
import tech.DevAsh.Launcher.createDisabledColor
import tech.DevAsh.Launcher.settings.ui.ControlledPreference

class StyledPreferenceCategory(context: Context, attrs: AttributeSet?) :
        PreferenceCategory(context, attrs), ColorEngine.OnColorChangeListener,
        ControlledPreference by ControlledPreference.Delegate(context, attrs) {

    private var dependencyMet = true
    private var parentDependencyMet = true

    var title: TextView? = null

    private val enabled get() = dependencyMet && parentDependencyMet

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        title = holder.findViewById(android.R.id.title) as TextView
        title!!.isEnabled = enabled
        ColorEngine.getInstance(context).addColorChangeListeners(this, ColorEngine.Resolvers.ACCENT)
    }

    override fun onColorChange(resolveInfo: ColorEngine.ResolveInfo) {
        if (resolveInfo.key == ColorEngine.Resolvers.ACCENT) {
            title?.setTextColor(context.createDisabledColor(resolveInfo.color))
        }
    }

    override fun onDetached() {
        super.onDetached()
        ColorEngine.getInstance(context).removeColorChangeListeners(this, ColorEngine.Resolvers.ACCENT)
    }

    override fun onDependencyChanged(dependency: Preference?, disableDependent: Boolean) {
        dependencyMet = !disableDependent
        super.onDependencyChanged(dependency, disableDependent)
    }

    override fun onParentChanged(parent: Preference?, disableChild: Boolean) {
        parentDependencyMet = !disableChild
        super.onParentChanged(parent, disableChild)
    }
}
