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
import android.preference.SwitchPreference
//import android.support.v14.preference.SwitchPreference
import androidx.preference.AndroidResources
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import tech.DevAsh.Launcher.applyColor
import tech.DevAsh.Launcher.colors.ColorEngine


open class StyledSwitchPreference(context: Context, attrs: AttributeSet?) : SwitchPreference(context, attrs), ColorEngine.OnColorChangeListener {

//    private var checkableView: View? = null

    override fun onBindView(view: View?) {
        super.onBindView(view)
//        checkableView = view?.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET)
        ColorEngine.getInstance(context).addColorChangeListeners(this, ColorEngine.Resolvers.ACCENT)
    }

    override fun onColorChange(resolveInfo: ColorEngine.ResolveInfo) {
//        if (resolveInfo.key == ColorEngine.Resolvers.ACCENT && checkableView is Switch) {
//            (checkableView as Switch).applyColor(resolveInfo.color)
//        }
    }

    override fun onPrepareForRemoval() {
        super.onPrepareForRemoval()
        ColorEngine.getInstance(context).removeColorChangeListeners(this, ColorEngine.Resolvers.ACCENT)
    }
}
