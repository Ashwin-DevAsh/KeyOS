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

package tech.DevAsh.Launcher.colors.overrides

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import androidx.preference.ListPreferenceDialogFragmentCompat
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat
import androidx.preference.PreferenceDialogFragmentCompat
import android.view.View
import android.widget.EditText
import tech.DevAsh.Launcher.applyAccent
import tech.DevAsh.Launcher.colors.ColorEngine

open class ThemedListPreferenceDialogFragment : ListPreferenceDialogFragmentCompat() {

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog?)?.applyAccent()
    }

    companion object {
        fun newInstance(key: String): ThemedListPreferenceDialogFragment {
            val fragment = ThemedListPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}

class ThemedEditTextPreferenceDialogFragmentCompat : EditTextPreferenceDialogFragmentCompat() {

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog?)?.applyAccent()
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        val color = ColorEngine.getInstance(context!!).accent
        val tintList = ColorStateList.valueOf(color)
        view?.findViewById<EditText>(android.R.id.edit)?.apply {
            highlightColor = color
            backgroundTintList = tintList
        }
    }

    companion object {
        fun newInstance(key: String): ThemedEditTextPreferenceDialogFragmentCompat {
            val fragment = ThemedEditTextPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}

class ThemedMultiSelectListPreferenceDialogFragmentCompat : MultiSelectListPreferenceDialogFragmentCompat() {

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog?)?.applyAccent()
    }

    companion object {
        fun newInstance(key: String): ThemedMultiSelectListPreferenceDialogFragmentCompat {
            val fragment = ThemedMultiSelectListPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}