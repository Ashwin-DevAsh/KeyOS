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

package tech.DevAsh.Launcher.util

import android.content.Context
import androidx.preference.ListPreference

class EntriesBuilder(private val context: Context) {

    private val entries = ArrayList<String>()
    private val entryValues = ArrayList<String>()

    fun addEntry(entry: Int, value: String) {
        addEntry(context.getString(entry), value)
    }

    fun addEntry(entry: String, value: Int) {
        addEntry(entry, "$value")
    }

    fun addEntry(entry: Int, value: Int) {
        addEntry(context.getString(entry), "$value")
    }

    fun addEntry(entry: String, value: String) {
        entries.add(entry)
        entryValues.add(value)
    }

    fun build(): Pair<Array<String>, Array<String>> {
        return Pair(entries.toTypedArray(), entryValues.toTypedArray())
    }

    fun build(listPreference: ListPreference) {
        listPreference.entries = entries.toTypedArray()
        listPreference.entryValues = entryValues.toTypedArray()
    }
}

inline fun ListPreference.buildEntries(edit: EntriesBuilder.() -> Unit) {
    EntriesBuilder(context).apply(edit).build(this)
}
