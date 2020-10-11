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
import androidx.annotation.Keep
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import tech.DevAsh.Launcher.applyColor
import tech.DevAsh.Launcher.getColorEngineAccent
import tech.DevAsh.Launcher.KioskPrefs
import tech.DevAsh.Launcher.settings.ui.search.SearchIndex
import com.android.launcher3.Utilities
import kotlin.reflect.KMutableProperty1

class DockSwitchPreference(context: Context, attrs: AttributeSet? = null) : StyledSwitchPreferenceCompat(context, attrs) {

    private val prefs = Utilities.getKioskPrefs(context)
    private val currentStyle get() = prefs.dockStyles.currentStyle
    private val inverted get() = key == "enableGradient"

    @Suppress("UNCHECKED_CAST")
    private val property get() = DockStyle.properties[key] as? KMutableProperty1<DockStyle, Boolean>

    private val onChangeListener = { isChecked = getPersistedBoolean(false) }

    init {
        isChecked = getPersistedBoolean(false)
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        isChecked = getPersistedBoolean(false)
    }

    override fun onAttached() {
        super.onAttached()
        prefs.dockStyles.addListener(onChangeListener)
    }

    override fun onDetached() {
        super.onDetached()
        prefs.dockStyles.removeListener(onChangeListener)
    }

    override fun getPersistedBoolean(defaultReturnValue: Boolean): Boolean {
        if (inverted) return property?.get(currentStyle) != true
        return property?.get(currentStyle) == true
    }

    override fun persistBoolean(value: Boolean): Boolean {
        property?.set(currentStyle, if (inverted) !value else value)
        return property != null
    }

    class DockSwitchSlice(context: Context, attrs: AttributeSet) : SwitchSlice(context, attrs) {

        override fun createSliceView(): View {
            return DockSwitchSliceView(context, key)
        }
    }

    class DockSwitchSliceView(
            context: Context,
            private val key: String)
        : Switch(context) {

        private val currentStyle get() = context.KioskPrefs.dockStyles.currentStyle
        private val inverted get() = key == "enableGradient"

        @Suppress("UNCHECKED_CAST")
        private val property get() = DockStyle.properties[key] as? KMutableProperty1<DockStyle, Boolean>
        private val listener = ::onChange

        init {
            applyColor(context.getColorEngineAccent())
            setOnCheckedChangeListener { _, isChecked ->
                persistBoolean(isChecked)
            }
            isChecked = getPersistedBoolean()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            context.KioskPrefs.dockStyles.addListener(listener)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            context.KioskPrefs.dockStyles.removeListener(listener)
        }

        private fun onChange() {
            isChecked = getPersistedBoolean()
        }

        private fun getPersistedBoolean(): Boolean {
            if (inverted) return property?.get(currentStyle) != true
            return property?.get(currentStyle) == true
        }

        private fun persistBoolean(value: Boolean): Boolean {
            property?.set(currentStyle, if (inverted) !value else value)
            return property != null
        }
    }

    companion object {

        @Keep
        @JvmStatic
        val sliceProvider = SearchIndex.SliceProvider.fromLambda(::DockSwitchSlice)
    }
}
