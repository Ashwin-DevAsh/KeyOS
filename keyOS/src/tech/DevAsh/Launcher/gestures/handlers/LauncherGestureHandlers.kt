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

package tech.DevAsh.Launcher.gestures.handlers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.UserHandle
import androidx.annotation.Keep
import android.view.View
import android.widget.Toast
import tech.DevAsh.Launcher.animations.KioskAppTransitionManagerImpl
import tech.DevAsh.Launcher.gestures.GestureController
import tech.DevAsh.Launcher.gestures.GestureHandler
import tech.DevAsh.Launcher.gestures.ui.SelectAppActivity
import tech.DevAsh.Launcher.getIcon
import com.android.launcher3.LauncherState
import tech.DevAsh.Launcher.KioskPrefs
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.compat.LauncherAppsCompat
import com.android.launcher3.compat.UserManagerCompat
import com.android.launcher3.shortcuts.DeepShortcutManager
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.views.OptionsPopupView
import com.android.launcher3.widget.WidgetsFullSheet
import ninja.sesame.lib.bridge.v1.SesameFrontend
import org.json.JSONObject

@Keep
class OpenWidgetsGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_widgets)
    override val iconResource: Intent.ShortcutIconResource by lazy { Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_widget) }
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        WidgetsFullSheet.show(controller.launcher, true)
    }
}

@Keep
class OpenSettingsGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_settings)
    override val iconResource: Intent.ShortcutIconResource by lazy { Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_setting) }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.startActivity(Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
            `package` = controller.launcher.packageName
        })
    }
}

@Keep
class OpenOverviewGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_overview)
    override val iconResource: Intent.ShortcutIconResource by lazy { Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_setting) }
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        if (context.KioskPrefs.usePopupMenuView) {
            OptionsPopupView.showDefaultOptions(controller.launcher,
                    controller.touchDownPoint.x, controller.touchDownPoint.y)
        } else {
            controller.launcher.stateManager.goToState(LauncherState.OPTIONS)
        }
    }
}



@Keep
class StartAppSearchGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_app_search)
    override val iconResource: Intent.ShortcutIconResource by lazy { Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_search_shadow) }
    override val requiresForeground = Utilities.ATLEAST_P

    override fun onGestureTrigger(controller: GestureController, view: View?) {

    }
}

@Keep
class OpenOverlayGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_overlay)
//    override val iconResource: Intent.ShortcutIconResource by lazy { Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_super_g_color) }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        if (controller.launcher.googleNow != null) {
            controller.launcher.googleNow?.showOverlay(true)
        } else {
            controller.launcher.startActivity(Intent(Intent.ACTION_MAIN).setClassName(PACKAGE, "$PACKAGE.SearchActivity"))
        }
    }

    companion object {
        private const val PACKAGE = "com.google.android.googlequicksearchbox"
    }
}

@Keep
class StartAppGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val hasConfig = true
    override val configIntent = Intent(context, SelectAppActivity::class.java)
    override val displayName get() = if (target != null)
        String.format(displayNameWithTarget, appName) else displayNameWithoutTarget
    override val icon: Drawable
        get() = when {
            intent != null -> try {
                context.packageManager.getActivityIcon(intent!!)
            } catch (e: Exception) {
                context.getIcon()
            }
            target != null -> try {
                context.packageManager.getApplicationIcon(target?.componentName?.packageName!!)
            } catch (e: Exception) {
                context.getIcon()
            }
            else -> context.getIcon()
        }

    private val displayNameWithoutTarget: String = context.getString(R.string.action_open_app)
    private val displayNameWithTarget: String = context.getString(R.string.action_open_app_with_target)

    var type: String? = null
    var appName: String? = null
    var target: ComponentKey? = null
    var intent: Intent? = null
    var user: UserHandle? = null
    var packageName: String? = null
    var id: String? = null

    init {
        if (config?.has("appName") == true) {
            appName = config.getString("appName")
            type = if (config.has("type")) config.getString("type") else "app"
            if (type == "app") {
                target = ComponentKey(context, config.getString("target"))
            } else {
                intent = Intent.parseUri(config.getString("intent"), 0)
                user = UserManagerCompat.getInstance(context).getUserForSerialNumber(config.getLong("user"))
                packageName = config.getString("packageName")
                id = config.getString("id")
            }
        }
    }

    override fun saveConfig(config: JSONObject) {
        super.saveConfig(config)
        config.put("appName", appName)
        config.put("type", type)
        when (type) {
            "app" -> {
                config.put("target", target.toString())
            }
            "shortcut" -> {
                config.put("intent", intent!!.toUri(0))
                config.put("user", UserManagerCompat.getInstance(context).getSerialNumberForUser(user))
                config.put("packageName", packageName)
                config.put("id", id)
            }
        }
    }

    override fun onConfigResult(data: Intent?) {
        super.onConfigResult(data)
        if (data != null) {
            appName = data.getStringExtra("appName")
            type = data.getStringExtra("type")
            when (type) {
                "app" -> {
                    target = ComponentKey(context, data.getStringExtra("target"))
                }
                "shortcut" -> {
                    intent = Intent.parseUri(data.getStringExtra("intent"), 0)
                    user = data.getParcelableExtra("user")
                    packageName = data.getStringExtra("packageName")
                    id = data.getStringExtra("id")
                }
            }
        }
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        if (view == null) {
            val down = controller.touchDownPoint
            controller.launcher.prepareDummyView(down.x.toInt(), down.y.toInt()) {
                onGestureTrigger(controller, it)
            }
            return
        }
        val opts = view.let { controller.launcher.getActivityLaunchOptionsAsBundle(it) }
        when (type) {
            "app" -> {
                try {
                    LauncherAppsCompat.getInstance(context)
                            .startActivityForProfile(target!!.componentName, target!!.user, null, opts)
                } catch (e: NullPointerException){
                    // App is probably not installed anymore, show a Toast
                    Toast.makeText(context, R.string.failed, Toast.LENGTH_LONG).show()
                }
                val transitionManager = controller.launcher.launcherAppTransitionManager
                        as? KioskAppTransitionManagerImpl
                transitionManager?.playLaunchAnimation(controller.launcher, view,
                        Intent().setComponent(target!!.componentName))
            }
            "shortcut" -> {
                if (id?.startsWith("sesame_") == true) {
                    context.startActivity(SesameFrontend.addPackageAuth(context, intent!!), opts)
                } else {
                    DeepShortcutManager.getInstance(context)
                            .startShortcut(packageName, id, intent, opts, user)
                }
            }
        }
    }
}





interface VerticalSwipeGestureHandler {

    fun onDragStart(start: Boolean) { }

    fun onDrag(displacement: Float, velocity: Float) { }

    fun onDragEnd(velocity: Float, fling: Boolean) { }
}

interface StateChangeGestureHandler {

    fun getTargetState(): LauncherState
}
