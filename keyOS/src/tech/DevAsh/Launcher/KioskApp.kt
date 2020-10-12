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

package tech.DevAsh.Launcher

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import androidx.annotation.Keep
import tech.DevAsh.Launcher.blur.BlurWallpaperProvider

import tech.DevAsh.Launcher.theme.ThemeManager
import com.android.launcher3.BuildConfig
import com.android.launcher3.Utilities
import com.android.quickstep.RecentsActivity
import io.realm.Realm
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.LoadAppsAndServices
import tech.DevAsh.KeyOS.SplashScreen
import tech.DevAsh.keyOS.Database.User


class KioskApp : Application() {

    val activityHandler = ActivityHandler()
    var loadAppsAndServices = LoadAppsAndServices(this)


    override fun onCreate() {
        RealmHelper.init(this)
        Handler().post{
            User.getUsers()
            loadAppsAndServices.execute()
        }
        super.onCreate()
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



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeManager.getInstance(this).updateNightMode(newConfig)
    }

    class ActivityHandler : ActivityLifecycleCallbacks {

        val activities = HashSet<Activity>()
        var foregroundActivity: Activity? = null

        fun finishAll(recreateLauncher: Boolean = true) {
            HashSet(activities).forEach { if (recreateLauncher && it is KioskLauncher) it.recreate() else it.finish() }
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

}

val Context.KioskApp get() = applicationContext as KioskApp
