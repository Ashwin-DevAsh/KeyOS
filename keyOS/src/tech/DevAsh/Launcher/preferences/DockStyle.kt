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

package tech.DevAsh.Launcher.preferences

import tech.DevAsh.Launcher.LawnchairPreferences
import kotlin.reflect.KMutableProperty0

abstract class DockStyle(protected val manager: StyleManager) {

    protected val hideProperty = manager::dockHidden

    abstract var hide: Boolean

    private class HiddenStyle(manager: StyleManager) : PredefinedStyle(manager, defaultHide = true)
   private class FlatStyle(manager: StyleManager) : PredefinedStyle(manager)
    private class CustomStyle(manager: StyleManager) : DockStyle(manager) {

        override var hide
            get() = false
            set(value) {}
    }

    private abstract class PredefinedStyle(manager: StyleManager,
                                           val defaultHide: Boolean = false) : DockStyle(manager) {

        override var hide
            get() = defaultHide
            set(value) { setProp(hideProperty, value, defaultHide) }

        fun <T> setProp(property: KMutableProperty0<T>, value: T, defaultValue: T) {
            if (value != defaultValue) {
                manager.prefs.blockingEdit {
                    bulkEdit {
                        manager.dockPreset = 0
                        property.set(value)
                    }
                }
            }
        }
    }

    class StyleManager(val prefs: LawnchairPreferences,
                       private val onPresetChange: () -> Unit,
                       private val onCustomizationChange: () -> Unit) {

        val onChangeListener = ::onValueChanged
        var dockPreset by prefs.StringIntPref("pref_dockPreset", 1, onChangeListener)
        var dockHidden by prefs.BooleanPref("pref_hideHotseat", false, onChangeListener)

        val styles = arrayListOf(CustomStyle(this),  FlatStyle(this), HiddenStyle(this))
        var currentStyle = styles[dockPreset]
        private var oldStyle = styles[dockPreset]

        private val listeners = HashSet<() -> Unit>()

        private fun onValueChanged() {
            currentStyle = styles[dockPreset]
            if (currentStyle.hide != oldStyle.hide) {
                onPresetChange()
            } else {
                onCustomizationChange()
            }
            oldStyle = styles[dockPreset]
            listeners.forEach { it() }
        }

        fun addListener(listener: () -> Unit) {
            listeners.add(listener)
        }

        fun removeListener(listener: () -> Unit) {
            listeners.remove(listener)
        }
    }

    companion object {

        val properties = hashMapOf(
                Pair("hide", DockStyle::hide))
    }
}
