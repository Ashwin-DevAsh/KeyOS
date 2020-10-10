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

package tech.DevAsh.Launcher

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.Keep
import tech.DevAsh.Launcher.blur.BlurWallpaperProvider

import tech.DevAsh.Launcher.theme.ThemeManager
import tech.DevAsh.Launcher.util.extensions.d
import com.android.launcher3.BuildConfig
import com.android.launcher3.Utilities
import com.android.quickstep.RecentsActivity
import com.squareup.leakcanary.LeakCanary


class LawnchairApp : Application() {

    val activityHandler = ActivityHandler()
    val recentsEnabled by lazy { checkRecentsComponent() }
    var accessibilityService: LawnchairAccessibilityService? = null

    init {
        d("Hidden APIs allowed: ${Utilities.HIDDEN_APIS_ALLOWED}")
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.HAS_LEAKCANARY && lawnchairPrefs.initLeakCanary) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return
            }
            LeakCanary.install(this)
        }
    }

    fun onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler)

        ThemeManager.getInstance(this).registerColorListener()
        BlurWallpaperProvider.getInstance(this)
    }

    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            activityHandler.finishAll(recreateLauncher)
        } else {
            Utilities.restartLauncher(this)
        }
    }

    fun performGlobalAction(action: Int): Boolean {
        return if (accessibilityService != null) {
            accessibilityService!!.performGlobalAction(action)
        } else {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeManager.getInstance(this).updateNightMode(newConfig)
    }

    class ActivityHandler : ActivityLifecycleCallbacks {

        val activities = HashSet<Activity>()
        var foregroundActivity: Activity? = null

        fun finishAll(recreateLauncher: Boolean = true) {
            HashSet(activities).forEach { if (recreateLauncher && it is LawnchairLauncher) it.recreate() else it.finish() }
        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {
            foregroundActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity == foregroundActivity)
                foregroundActivity = null
            activities.remove(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
        }
    }

    @Keep
    fun checkRecentsComponent(): Boolean {
        if (!Utilities.ATLEAST_P) {
            d("API < P, disabling recents")
            return false
        }
        if (!Utilities.HIDDEN_APIS_ALLOWED) {
            d("Hidden APIs not allowed, disabling recents")
            return false
        }

        val resId = resources.getIdentifier("config_recentsComponentName", "string", "android")
        if (resId == 0) {
            d("config_recentsComponentName not found, disabling recents")
            return false
        }
        val recentsComponent = ComponentName.unflattenFromString(resources.getString(resId))
        if (recentsComponent == null) {
            d("config_recentsComponentName is empty, disabling recents")
            return false
        }
        val isRecentsComponent = recentsComponent.packageName == packageName
                && recentsComponent.className == RecentsActivity::class.java.name
        if (!isRecentsComponent) {
            d("config_recentsComponentName ($recentsComponent) is not Lawnchair, disabling recents")
            return false
        }
        return true
    }
}

val Context.lawnchairApp get() = applicationContext as LawnchairApp
