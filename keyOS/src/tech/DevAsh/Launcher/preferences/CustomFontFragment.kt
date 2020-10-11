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
import android.graphics.Typeface
import android.os.Bundle
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import tech.DevAsh.Launcher.KioskPrefs
import tech.DevAsh.Launcher.uiWorkerHandler
import com.android.launcher3.R
import com.android.launcher3.Utilities

@Keep
class CustomFontFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_font, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = view.context.KioskPrefs

        val fontName = view.findViewById<TextView>(R.id.font_name)
        val submitButton = view.findViewById<View>(R.id.button)

        fontName.text = prefs.customFontName
        submitButton.setOnClickListener {
            setFont(view.context, fontName.text.toString())
        }
    }

    private fun setFont(context: Context, fontName: String) {
        val request = FontRequest(
                "com.google.android.gms.fonts", // ProviderAuthority
                "com.google.android.gms",  // ProviderPackage
                "name=$fontName",  // Query
                R.array.com_google_android_gms_fonts_certs)

        // retrieve font in the background
        FontsContractCompat.requestFont(context, request, object : FontsContractCompat.FontRequestCallback() {
            override fun onTypefaceRetrieved(typeface: Typeface) {
                super.onTypefaceRetrieved(typeface)

                val prefs = context.KioskPrefs
                prefs.blockingEdit { customFontName = fontName }
                Utilities.restartLauncher(context)
            }

            override fun onTypefaceRequestFailed(reason: Int) {
                super.onTypefaceRequestFailed(reason)

                Toast.makeText(context, "Failed to load $fontName", Toast.LENGTH_LONG).show()
            }
        }, uiWorkerHandler)
    }
}
