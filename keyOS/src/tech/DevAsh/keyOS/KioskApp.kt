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

package tech.DevAsh.keyOS

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import tech.DevAsh.Launcher.blur.BlurWallpaperProvider

import tech.DevAsh.Launcher.theme.ThemeManager
import com.android.launcher3.Utilities
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.LoadAppsAndServices
import tech.DevAsh.Launcher.KioskLauncher
import tech.DevAsh.keyOS.Api.IMailService
import tech.DevAsh.keyOS.Api.IUpdateService
import tech.DevAsh.keyOS.Database.User
import javax.inject.Inject


class KioskApp : Application() {



    var applicationComponents:ApplicationComponents?=null
    val activityHandler = ActivityHandler()
    var loadAppsAndServices = LoadAppsAndServices(this)
    @Inject lateinit var mailService: IMailService
    @Inject lateinit var updateService:IUpdateService


    override fun onCreate() {
        RealmHelper.init(this)
        User.getUsers()
        loadAppsAndServices.execute()
        applicationComponents = DaggerApplicationComponents.create()
        applicationComponents!!.inject(this)


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

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
        }
    }

}

val Context.KioskApp get() = applicationContext as KioskApp
