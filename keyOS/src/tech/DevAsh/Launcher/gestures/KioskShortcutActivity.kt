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

package tech.DevAsh.Launcher.gestures

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tech.DevAsh.Launcher.gestures.ui.HandlerListAdapter
import tech.DevAsh.Launcher.gestures.ui.RunHandlerActivity
import tech.DevAsh.Launcher.settings.ui.SettingsBaseActivity
import com.android.launcher3.R
import com.android.launcher3.graphics.LauncherIcons

class KioskShortcutActivity : SettingsBaseActivity() {
    private var selectedHandler: GestureHandler? = null
    private val launcherIcons by lazy { LauncherIcons.obtain(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action != Intent.ACTION_CREATE_SHORTCUT) {
            finish()
        }
        setContentView(R.layout.preference_insettable_recyclerview)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.list)
        recyclerView.adapter = HandlerListAdapter(this, false, "", ::onSelectHandler, false)
        recyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    private fun onSelectHandler(handler: GestureHandler) {
        selectedHandler = handler
        if (handler.configIntent != null) {
            startActivityForResult(handler.configIntent, REQUEST_CODE)
        } else {
            saveChanges()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedHandler?.onConfigResult(data)
            saveChanges()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveChanges() {
        val shortcutIntent = Intent(this, RunHandlerActivity::class.java).apply {
            action = START_ACTION
            `package` = packageName
            putExtra(EXTRA_HANDLER, selectedHandler.toString())
        }

        val icon = if (selectedHandler?.icon != null)
            launcherIcons.createScaledBitmapWithoutShadow(selectedHandler?.icon, Build.VERSION.SDK_INT)
        else null
        val intent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, selectedHandler?.displayName)
            if (icon != null)
                putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
            else
                putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, selectedHandler?.iconResource)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val START_ACTION = "ch.deletescape.keyOS.Launcher.START_ACTION"
        const val EXTRA_HANDLER = "ch.deletescape.keyOS.Launcher.EXTRA_HANDLER"
        const val REQUEST_CODE = 1337
    }
}