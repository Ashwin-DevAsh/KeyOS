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

package tech.DevAsh.Launcher.views

import android.content.Context
import android.util.AttributeSet
import tech.DevAsh.Launcher.settings.ui.DecorLayout

class PreferenceRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : InsettableRecyclerView(context, attrs, defStyleAttr) {

    private var elevationHelper: DecorLayout.ToolbarElevationHelper? = null
        set(value) {
            field?.destroy()
            field = value
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        elevationHelper = DecorLayout.ToolbarElevationHelper(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        elevationHelper = null
    }
}
